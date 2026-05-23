package com.cgv.spring_boot.domain.reservation.service;

import com.cgv.spring_boot.domain.payment.dto.response.PaymentResponse;
import com.cgv.spring_boot.domain.payment.dto.PaymentCancelResult;
import com.cgv.spring_boot.domain.payment.dto.PaymentReadyResult;
import com.cgv.spring_boot.domain.payment.service.PaymentService;
import com.cgv.spring_boot.domain.reservation.dto.ReservationRequest;
import com.cgv.spring_boot.domain.reservation.exception.ReservationErrorCode;
import com.cgv.spring_boot.domain.reservation.entity.Reservation;
import com.cgv.spring_boot.domain.reservation.entity.ReservationStatus;
import com.cgv.spring_boot.domain.reservation.entity.ReservedSeat;
import com.cgv.spring_boot.domain.reservation.entity.SeatPosition;
import com.cgv.spring_boot.domain.reservation.repository.ReservationRepository;
import com.cgv.spring_boot.domain.reservation.repository.ReservedSeatRepository;
import com.cgv.spring_boot.domain.schedule.exception.ScheduleErrorCode;
import com.cgv.spring_boot.domain.schedule.entity.Schedule;
import com.cgv.spring_boot.domain.schedule.repository.ScheduleRepository;
import com.cgv.spring_boot.domain.user.entity.User;
import com.cgv.spring_boot.domain.user.exception.UserErrorCode;
import com.cgv.spring_boot.domain.user.repository.UserRepository;
import com.cgv.spring_boot.global.error.code.GlobalErrorCode;
import com.cgv.spring_boot.global.error.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final ReservedSeatRepository reservedSeatRepository;
    private final PaymentService paymentService;
    private final TransactionTemplate transactionTemplate;

    /**
     * 예매 좌석 선점
     */
    @Transactional
    public Long reserve(Long userId, ReservationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        Schedule schedule = scheduleRepository.findById(request.scheduleId())
                .orElseThrow(() -> new BusinessException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        List<SeatPosition> seatPositions = request.seats().stream()
                .map(seatRequest -> new SeatPosition(seatRequest.seatRow(), seatRequest.seatCol()))
                .toList();

        // 상영관 범위를 벗어난 좌석인지 확인
        validateSeatRange(schedule, seatPositions);
        // 같은 요청 안에서 중복 좌석을 선택했는지 확인
        validateDuplicateSeatsInRequest(seatPositions);
        // 이미 다른 예매가 선점한 좌석인지 확인
        validateAlreadyReservedSeats(schedule, seatPositions);

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);
        Reservation reservation = Reservation.createPending(user, schedule, expiresAt);

        Reservation savedReservation = reservationRepository.save(reservation);

        List<ReservedSeat> reservedSeats = seatPositions.stream()
                .map(seatPosition -> ReservedSeat.create(savedReservation, schedule, seatPosition))
                .toList();

        // DB 유니크 제약조건 검증
        try {
            reservedSeatRepository.saveAllAndFlush(reservedSeats);
        } catch (DataIntegrityViolationException e) {
            log.warn("reservation rejected. userId={}, scheduleId={}, reason=seat_conflict_on_flush",
                    userId, schedule.getId());
            throw new BusinessException(ReservationErrorCode.ALREADY_RESERVED_SEAT);
        }

        log.info("AUDIT reservation created. userId={}, reservationId={}, scheduleId={}, seatCount={}",
                userId, savedReservation.getId(), schedule.getId(), seatPositions.size());
        return savedReservation.getId();
    }

    /** 좌석 범위 검증 */
    private void validateSeatRange(Schedule schedule, List<SeatPosition> seatPositions) {
        seatPositions.forEach(seatPosition -> seatPosition.validateAgainst(schedule.getHall().getHallType()));
    }

    /** 요청 내 중복 좌석 검증 */
    private void validateDuplicateSeatsInRequest(List<SeatPosition> seatPositions) {
        Set<SeatPosition> uniqueSeats = Set.copyOf(seatPositions);
        if (uniqueSeats.size() != seatPositions.size()) {
            log.warn("reservation rejected. reason=duplicated_seats_in_request, seatCount={}", seatPositions.size());
            throw new BusinessException(ReservationErrorCode.ALREADY_RESERVED_SEAT);
        }
    }

    /** 기예약 좌석 검증 */
    private void validateAlreadyReservedSeats(Schedule schedule, List<SeatPosition> seatPositions) {
        boolean alreadyReserved = seatPositions.stream()
                .anyMatch(seatPosition -> reservedSeatRepository.existsByScheduleIdAndSeatRowAndSeatCol(
                        schedule.getId(),
                        seatPosition.seatRow(),
                        seatPosition.seatCol())
                );

        if (alreadyReserved) {
            log.warn("reservation rejected. scheduleId={}, reason=already_reserved_seat", schedule.getId());
            throw new BusinessException(ReservationErrorCode.ALREADY_RESERVED_SEAT);
        }
    }

    /**
     * 예매 결제 및 확정
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public PaymentResponse pay(Long userId, Long reservationId) {
        PaymentReadyResult payment = transactionTemplate.execute(status -> preparePayment(userId, reservationId));

        try {
            PaymentResponse response = paymentService.requestPayment(payment);
            transactionTemplate.executeWithoutResult(status -> completePayment(userId, payment, response));
            return response;
        } catch (BusinessException e) {
            transactionTemplate.executeWithoutResult(status -> paymentService.markPaymentFailed(payment.paymentPk(), e));
            throw e;
        }
    }

    private PaymentReadyResult preparePayment(Long userId, Long reservationId) {
        Reservation reservation = getOwnedReservation(userId, reservationId);
        validateReservationPayable(reservation);

        long seatCount = reservedSeatRepository.countByReservationId(reservationId);
        Schedule schedule = reservation.getSchedule();
        int totalAmount = Math.toIntExact(seatCount * schedule.getTicketPrice());
        String orderName = schedule.getMovie().getTitle() + " 예매";
        String customData = "{\"reservationId\":" + reservationId + ",\"seatCount\":" + seatCount + "}";

        return paymentService.createReadyPayment(reservation, totalAmount, orderName, customData);
    }

    private void completePayment(Long userId, PaymentReadyResult payment, PaymentResponse response) {
        Reservation reservation = reservationRepository.findById(payment.reservationId())
                .orElseThrow(() -> new BusinessException(ReservationErrorCode.RESERVATION_NOT_FOUND));
        paymentService.markPaymentPaid(payment.paymentPk(), response);
        reservation.confirm();
        log.info("AUDIT reservation paid. userId={}, reservationId={}, paymentId={}, totalAmount={}",
                userId, payment.reservationId(), response.paymentId(), payment.totalAmount());
    }

    /** 결제 가능 예약 검증 */
    private void validateReservationPayable(Reservation reservation) {
        if (reservation.getExpiresAt().isBefore(LocalDateTime.now())) {
            reservation.expire();
            reservedSeatRepository.deleteByReservation(reservation);
            log.warn("reservation payment rejected. reservationId={}, reason=expired", reservation.getId());
            throw new BusinessException(ReservationErrorCode.RESERVATION_EXPIRED);
        }

        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT) {
            log.warn("reservation payment rejected. reservationId={}, reason=invalid_status, status={}",
                    reservation.getId(), reservation.getStatus());
            throw new BusinessException(ReservationErrorCode.INVALID_RESERVATION_STATUS);
        }
    }

    /**
     * 예매 취소
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void cancel(Long userId, Long reservationId) {
        PaymentCancelResult payment = transactionTemplate.execute(status -> prepareCancel(userId, reservationId));

        if (payment != null) {
            paymentService.requestPaymentCancel(payment);
            transactionTemplate.executeWithoutResult(status -> completeCancel(userId, reservationId, payment));
        }
    }

    private PaymentCancelResult prepareCancel(Long userId, Long reservationId) {
        Reservation reservation = getOwnedReservation(userId, reservationId);
        PaymentCancelResult payment = paymentService.getPaidPaymentForCancel(reservation);

        if (payment == null) {
            cancelReservation(userId, reservation);
        }

        return payment;
    }

    private void completeCancel(Long userId, Long reservationId, PaymentCancelResult payment) {
        Reservation reservation = getOwnedReservation(userId, reservationId);
        paymentService.markPaymentCancelled(payment.paymentPk());
        cancelReservation(userId, reservation);
    }

    private void cancelReservation(Long userId, Reservation reservation) {
        reservation.cancel();
        reservedSeatRepository.deleteByReservation(reservation);
        log.info("AUDIT reservation cancelled. userId={}, reservationId={}", userId, reservation.getId());
    }

    /** 본인 예약 조회 */
    private Reservation getOwnedReservation(Long userId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ReservationErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getUser().getId().equals(userId)) {
            log.warn("AUDIT reservation access denied. userId={}, reservationId={}", userId, reservationId);
            throw new BusinessException(GlobalErrorCode.FORBIDDEN_ACCESS);
        }

        return reservation;
    }
}
