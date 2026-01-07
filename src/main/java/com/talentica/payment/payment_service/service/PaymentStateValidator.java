package com.talentica.payment.payment_service.service;

import com.talentica.payment.payment_service.domain.enums.PaymentAction;
import com.talentica.payment.payment_service.domain.enums.PaymentStatus;
import com.talentica.payment.payment_service.exception.InvalidPaymentStateException;
import org.springframework.stereotype.Component;

@Component
public class PaymentStateValidator {

    public void validate(PaymentStatus currentStatus, PaymentAction action) {
        switch (action) {
            case PURCHASE -> {
                if (currentStatus != PaymentStatus.CREATED) {
                    throw new InvalidPaymentStateException(
                            "Purchase allowed only in CREATED state"
                    );
                }
            }
            case AUTHORIZE -> {
                if (currentStatus != PaymentStatus.CREATED) {
                    throw new InvalidPaymentStateException(
                            "Authorize allowed only in CREATED state"
                    );
                }
            }
            case CAPTURE -> {
                if (currentStatus != PaymentStatus.AUTHORIZED) {
                    throw new InvalidPaymentStateException(
                            "Capture allowed only in AUTHORIZED state"
                    );
                }
            }
            case CANCEL -> {
                if (currentStatus != PaymentStatus.AUTHORIZED) {
                    throw new InvalidPaymentStateException(
                            "Cancel allowed only in AUTHORIZED state"
                    );
                }
            }
            case REFUND -> {
                if (currentStatus != PaymentStatus.CAPTURED
                        && currentStatus != PaymentStatus.PARTIALLY_REFUNDED) {
                    throw new InvalidPaymentStateException(
                            "Refund allowed only after CAPTURE"
                    );
                }
            }
        }
    }
}

