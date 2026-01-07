package com.talentica.payment.payment_service.dto.response;

import java.time.Instant;

public record ErrorResponse(
        String error,
        String message,
        Instant timestamp
) {
}
