package com.talentica.payment.payment_service.validation;

import com.talentica.payment.payment_service.domain.enums.PaymentStatus;
import com.talentica.payment.payment_service.domain.enums.PaymentAction;
import com.talentica.payment.payment_service.exception.InvalidPaymentStateException;
import com.talentica.payment.payment_service.service.PaymentStateValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentStateValidatorTest {

    private final PaymentStateValidator validator =
            new PaymentStateValidator();

    @Test
    void purchase_shouldBeAllowed_fromCreated() {
        assertDoesNotThrow(() ->
                validator.validate(PaymentStatus.CREATED, PaymentAction.PURCHASE)
        );
    }

    @Test
    void authorize_shouldBeAllowed_fromCreated() {
        assertDoesNotThrow(() ->
                validator.validate(PaymentStatus.CREATED, PaymentAction.AUTHORIZE)
        );
    }

    @Test
    void capture_shouldBeAllowed_fromAuthorized() {
        assertDoesNotThrow(() ->
                validator.validate(PaymentStatus.AUTHORIZED, PaymentAction.CAPTURE)
        );
    }

    @Test
    void cancel_shouldBeAllowed_fromAuthorized() {
        assertDoesNotThrow(() ->
                validator.validate(PaymentStatus.AUTHORIZED, PaymentAction.CANCEL)
        );
    }

    @Test
    void refund_shouldBeAllowed_fromCaptured() {
        assertDoesNotThrow(() ->
                validator.validate(PaymentStatus.CAPTURED, PaymentAction.REFUND)
        );
    }

    /* ===================== INVALID ===================== */

    @Test
    void capture_shouldFail_whenOrderNotAuthorized() {
        assertThrows(InvalidPaymentStateException.class, () ->
                validator.validate(PaymentStatus.CREATED, PaymentAction.CAPTURE)
        );
    }

    @Test
    void refund_shouldFail_whenOrderNotCaptured() {
        assertThrows(InvalidPaymentStateException.class, () ->
                validator.validate(PaymentStatus.CREATED, PaymentAction.REFUND)
        );
    }

    @Test
    void purchase_shouldFail_whenOrderAlreadyCaptured() {
        assertThrows(InvalidPaymentStateException.class, () ->
                validator.validate(PaymentStatus.CAPTURED, PaymentAction.PURCHASE)
        );
    }
}

