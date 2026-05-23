package com.cgv.spring_boot.domain.payment.service;

import com.cgv.spring_boot.domain.payment.dto.request.PaymentCreateRequest;
import com.cgv.spring_boot.domain.payment.dto.PaymentCancelResult;
import com.cgv.spring_boot.domain.payment.dto.PaymentReadyResult;
import com.cgv.spring_boot.domain.payment.dto.response.PaymentResponse;
import com.cgv.spring_boot.domain.payment.entity.Payment;
import com.cgv.spring_boot.domain.payment.entity.PaymentStatus;
import com.cgv.spring_boot.domain.payment.exception.PaymentErrorCode;
import com.cgv.spring_boot.domain.payment.repository.PaymentRepository;
import com.cgv.spring_boot.domain.reservation.entity.Reservation;
import com.cgv.spring_boot.global.error.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String DEFAULT_CURRENCY = "KRW";

    private final PaymentRepository paymentRepository;
    private final PortOnePaymentClient portOnePaymentClient;
    private final PaymentIdGenerator paymentIdGenerator;

    @Transactional
    public PaymentReadyResult createReadyPayment(Reservation reservation, int totalAmount, String orderName, String customData) {
        if (paymentRepository.existsByReservationId(reservation.getId())) {
            log.warn("payment rejected. reservationId={}, reason=payment_already_exists", reservation.getId());
            throw new BusinessException(PaymentErrorCode.PAYMENT_ALREADY_EXISTS);
        }

        String paymentId = paymentIdGenerator.generate();
        log.info("payment requested. reservationId={}, paymentId={}, totalAmount={}",
                reservation.getId(), paymentId, totalAmount);
        Payment payment = paymentRepository.save(
                Payment.createReady(reservation, paymentId, orderName, totalAmount, DEFAULT_CURRENCY, customData)
        );

        return new PaymentReadyResult(
                payment.getId(),
                payment.getReservation().getId(),
                payment.getPaymentId(),
                payment.getOrderName(),
                payment.getTotalAmount(),
                payment.getCurrency(),
                payment.getCustomData()
        );
    }

    public PaymentResponse requestPayment(PaymentReadyResult payment) {
        return portOnePaymentClient.instantPay(payment.paymentId(), new PaymentCreateRequest(
                payment.orderName(),
                payment.totalAmount(),
                payment.currency(),
                payment.customData()
        ));
    }

    @Transactional
    public void markPaymentPaid(Long paymentPk, PaymentResponse response) {
        Payment payment = paymentRepository.findById(paymentPk)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        payment.markPaid(response.pgProvider(), response.paidAt());
        log.info("AUDIT payment succeeded. reservationId={}, paymentId={}, provider={}",
                payment.getReservation().getId(), response.paymentId(), response.pgProvider());
    }

    @Transactional
    public void markPaymentFailed(Long paymentPk, BusinessException e) {
        Payment payment = paymentRepository.findById(paymentPk)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        payment.markFailed();
        log.warn("AUDIT payment failed. reservationId={}, paymentId={}, reason={}",
                payment.getReservation().getId(), payment.getPaymentId(), e.getErrorCode().getMessage());
    }

    @Transactional
    public PaymentCancelResult getPaidPaymentForCancel(Reservation reservation) {
        Payment payment = paymentRepository.findByReservationId(reservation.getId())
                .orElse(null);

        if (payment == null || payment.getStatus() != PaymentStatus.PAID) {
            return null;
        }

        return new PaymentCancelResult(payment.getId(), payment.getReservation().getId(), payment.getPaymentId());
    }

    public void requestPaymentCancel(PaymentCancelResult payment) {
        portOnePaymentClient.cancel(payment.paymentId());
    }

    @Transactional
    public void markPaymentCancelled(Long paymentPk) {
        Payment payment = paymentRepository.findById(paymentPk)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        payment.cancel();
        log.info("AUDIT payment cancelled. reservationId={}, paymentId={}",
                payment.getReservation().getId(), payment.getPaymentId());
    }

}
