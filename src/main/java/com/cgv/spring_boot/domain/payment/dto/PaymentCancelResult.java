package com.cgv.spring_boot.domain.payment.dto;

public record PaymentCancelResult(
        Long paymentPk,
        Long reservationId,
        String paymentId
) {
}
