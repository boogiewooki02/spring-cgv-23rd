package com.cgv.spring_boot.domain.reservation.service;

import com.cgv.spring_boot.domain.reservation.dto.ReservationRequest;
import com.cgv.spring_boot.domain.reservation.entity.Reservation;
import com.cgv.spring_boot.domain.reservation.repository.ReservationRepository;
import com.cgv.spring_boot.domain.reservation.repository.ReservedSeatRepository;
import com.cgv.spring_boot.domain.schedule.entity.Schedule;
import com.cgv.spring_boot.domain.schedule.repository.ScheduleRepository;
import com.cgv.spring_boot.domain.theater.entity.Hall;
import com.cgv.spring_boot.domain.theater.entity.HallType;
import com.cgv.spring_boot.domain.user.entity.User;
import com.cgv.spring_boot.domain.user.repository.UserRepository;
import com.cgv.spring_boot.domain.reservation.exception.ReservationErrorCode;
import com.cgv.spring_boot.global.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;

    @Mock private ReservationRepository reservationRepository;
    @Mock private ScheduleRepository scheduleRepository;
    @Mock private UserRepository userRepository;
    @Mock private ReservedSeatRepository reservedSeatRepository;

    @Test
    @DisplayName("예매 성공 테스트")
    void reserve_success() {
        // given
        Long userId = 1L;
        ReservationRequest.SeatRequest seatRequest = new ReservationRequest.SeatRequest("A", 1);
        ReservationRequest request = new ReservationRequest(10L, List.of(seatRequest));

        User user = mock(User.class);
        Schedule schedule = mock(Schedule.class);
        Hall hall = mock(Hall.class);
        HallType hallType = HallType.builder()
                .typeName("일반관")
                .rowCount(10)
                .colCount(10)
                .build();
        Reservation reservation = Reservation.builder().build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(scheduleRepository.findById(request.scheduleId())).willReturn(Optional.of(schedule));
        given(schedule.getId()).willReturn(request.scheduleId());
        given(schedule.getHall()).willReturn(hall);
        given(hall.getHallType()).willReturn(hallType);
        given(reservedSeatRepository.existsByScheduleIdAndSeatRowAndSeatCol(anyLong(), anyString(), anyInt()))
                .willReturn(false);
        given(reservationRepository.save(any(Reservation.class))).willReturn(reservation);

        // when
        Long resultId = reservationService.reserve(userId, request);

        // then
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(reservedSeatRepository, times(1)).saveAllAndFlush(anyList());
    }

    @Test
    @DisplayName("실패 케이스: 이미 예약된 좌석을 선택하면 예외가 발생한다")
    void reserve_fail_already_reserved() {
        // given
        Long userId = 1L;
        ReservationRequest request = new ReservationRequest(10L, List.of(new ReservationRequest.SeatRequest("A", 1)));
        Schedule schedule = mock(Schedule.class);
        Hall hall = mock(Hall.class);
        HallType hallType = HallType.builder()
                .typeName("일반관")
                .rowCount(10)
                .colCount(10)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mock(User.class)));
        given(scheduleRepository.findById(anyLong())).willReturn(Optional.of(schedule));
        given(schedule.getId()).willReturn(request.scheduleId());
        given(schedule.getHall()).willReturn(hall);
        given(hall.getHallType()).willReturn(hallType);
        given(reservedSeatRepository.existsByScheduleIdAndSeatRowAndSeatCol(anyLong(), anyString(), anyInt()))
                .willReturn(true);

        // when(then)
        assertThatThrownBy(() -> reservationService.reserve(userId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ReservationErrorCode.ALREADY_RESERVED_SEAT.getMessage());

        verify(reservationRepository, never()).save(any());
    }
}
