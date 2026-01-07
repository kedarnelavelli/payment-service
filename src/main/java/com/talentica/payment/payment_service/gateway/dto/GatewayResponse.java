package com.talentica.payment.payment_service.gateway.dto;

public record GatewayResponse(
        boolean success,
        String transactionId,
        String errorMessage
) {
}
