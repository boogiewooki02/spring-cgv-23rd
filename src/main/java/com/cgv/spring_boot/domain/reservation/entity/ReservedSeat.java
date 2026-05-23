package com.cgv.spring_boot.domain.reservation.entity;

import com.cgv.spring_boot.domain.schedule.entity.Schedule;
import com.cgv.spring_boot.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Table(
        indexes = {
                @Index(name = "idx_reserved_seat_reservation_id", columnList = "res_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_reserved_seat_per_schedule", columnNames = {"schedule_id", "seat_row", "seat_col"})
        }
)
public class ReservedSeat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seat_row", nullable = false)
    private String seatRow;

    @Column(name = "seat_col", nullable = false)
    private int seatCol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "res_id")
    private Reservation reservation;

    @Builder
    public ReservedSeat(String seatRow, int seatCol, Schedule schedule, Reservation reservation) {
        this.seatRow = seatRow;
        this.seatCol = seatCol;
        this.schedule = schedule;
        this.reservation = reservation;
    }

    public static ReservedSeat create(Reservation reservation, Schedule schedule, SeatPosition seatPosition) {
        return ReservedSeat.builder()
                .seatRow(seatPosition.seatRow())
                .seatCol(seatPosition.seatCol())
                .schedule(schedule)
                .reservation(reservation)
                .build();
    }
}
