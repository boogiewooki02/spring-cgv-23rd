package com.cgv.spring_boot.domain.reservation.entity;

import com.cgv.spring_boot.domain.payment.entity.Payment;
import com.cgv.spring_boot.domain.schedule.entity.Schedule;
import com.cgv.spring_boot.domain.user.entity.User;
import com.cgv.spring_boot.global.common.entity.BaseEntity;
import com.cgv.spring_boot.domain.reservation.exception.ReservationErrorCode;
import com.cgv.spring_boot.global.error.exception.BusinessException;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Table(indexes = {
        @Index(name = "idx_reservation_status_expires_at", columnList = "status, expires_at")
})
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "res_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status; // BOOKED, CANCELED

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @OneToOne(mappedBy = "reservation", fetch = FetchType.LAZY)
    private Payment payment;

    @Builder
    public Reservation(ReservationStatus status, User user, Schedule schedule, LocalDateTime expiresAt) {
        this.status = status;
        this.user = user;
        this.schedule = schedule;
        this.expiresAt = expiresAt;
    }

    public static Reservation createPending(User user, Schedule schedule, LocalDateTime expiresAt) {
        return Reservation.builder()
                .status(ReservationStatus.PENDING_PAYMENT)
                .user(user)
                .schedule(schedule)
                .expiresAt(expiresAt)
                .build();
    }

    /**
     * 예약 상태 변경 관련 메서드
     */
    public void cancel() {
        if (this.status == ReservationStatus.CANCELLED) {
            throw new BusinessException(ReservationErrorCode.ALREADY_CANCELLED);
        }
        if (this.status == ReservationStatus.EXPIRED) {
            throw new BusinessException(ReservationErrorCode.INVALID_RESERVATION_STATUS);
        }
        this.status = ReservationStatus.CANCELLED;
    }

    public void confirm() {
        if (this.status != ReservationStatus.PENDING_PAYMENT) {
            throw new BusinessException(ReservationErrorCode.INVALID_RESERVATION_STATUS);
        }
        this.status = ReservationStatus.RESERVED;
    }

    public void expire() {
        if (this.status != ReservationStatus.PENDING_PAYMENT) {
            throw new BusinessException(ReservationErrorCode.INVALID_RESERVATION_STATUS);
        }
        this.status = ReservationStatus.EXPIRED;
    }
}
