package com.talentica.payment.payment_service.gateway;

import com.talentica.payment.payment_service.domain.entity.PaymentOrder;
import com.talentica.payment.payment_service.gateway.dto.GatewayResponse;

import java.math.BigDecimal;

public interface AuthorizeNetGateway {

    GatewayResponse purchase(PaymentOrder order);

    GatewayResponse authorize(PaymentOrder order);

    GatewayResponse capture(PaymentOrder order, String refTransactionId);

    GatewayResponse cancel(PaymentOrder order, String refTransactionId);

    GatewayResponse refund(PaymentOrder order,
                           String refTransactionId,
                           BigDecimal refundAmount);
}

