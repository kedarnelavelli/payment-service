package com.talentica.payment.payment_service.dto.response;

import com.talentica.payment.payment_service.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResponse(
        UUID orderId,
        PaymentStatus status,
        BigDecimal amount,
        String currency
) {}

