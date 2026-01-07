package com.talentica.payment.payment_service.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RefundRequest(
        @NotNull @Positive BigDecimal amount
) {}
