package com.talentica.payment.payment_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PurchaseRequest(
        @NotNull @Positive BigDecimal amount,
        @NotBlank String currency
) {}

