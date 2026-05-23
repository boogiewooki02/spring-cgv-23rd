package com.cgv.spring_boot.domain.payment.dto;

public record PaymentReadyResult(
        Long paymentPk,
        Long reservationId,
        String paymentId,
        String orderName,
        int totalAmount,
        String currency,
        String customData
) {
}
