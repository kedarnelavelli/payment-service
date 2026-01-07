package com.talentica.payment.payment_service.service;

import com.talentica.payment.payment_service.domain.entity.PaymentOrder;
import com.talentica.payment.payment_service.domain.entity.PaymentTransaction;
import com.talentica.payment.payment_service.domain.enums.*;
import com.talentica.payment.payment_service.exception.ResourceNotFoundException;
import com.talentica.payment.payment_service.gateway.AuthorizeNetGateway;
import com.talentica.payment.payment_service.repository.PaymentOrderRepository;
import com.talentica.payment.payment_service.repository.PaymentTransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentOrderRepository orderRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final AuthorizeNetGateway gateway;
    private final PaymentStateValidator stateValidator;

    @Transactional
    public PaymentOrder purchase(BigDecimal amount, String currency) {

        PaymentOrder order = createOrder(amount, currency);

        stateValidator.validate(order.getStatus(), PaymentAction.PURCHASE);

        var gatewayResponse = gateway.purchase(order);

        saveTransaction(order, TransactionType.PURCHASE,
                amount,
                gatewayResponse.transactionId(),
                gatewayResponse.success());

        if (gatewayResponse.success()) {
            order.setStatus(PaymentStatus.CAPTURED);
        } else {
            order.setStatus(PaymentStatus.FAILED);
        }

        return orderRepository.save(order);
    }

    @Transactional
    public PaymentOrder authorize(BigDecimal amount, String currency) {

        PaymentOrder order = createOrder(amount, currency);

        stateValidator.validate(order.getStatus(), PaymentAction.AUTHORIZE);

        var response = gateway.authorize(order);

        saveTransaction(order, TransactionType.AUTHORIZE,
                amount,
                response.transactionId(),
                response.success());

        order.setStatus(response.success()
                ? PaymentStatus.AUTHORIZED
                : PaymentStatus.FAILED);

        return orderRepository.save(order);
    }


    @Transactional
    public PaymentOrder capture(UUID orderId) {

        PaymentOrder order = getOrder(orderId);

        stateValidator.validate(order.getStatus(), PaymentAction.CAPTURE);

        String refTxnId = getLastSuccessfulGatewayTxnId(order);

        var response = gateway.capture(order, refTxnId);

        saveTransaction(order, TransactionType.CAPTURE,
                order.getAmount(),
                response.transactionId(),
                response.success());

        order.setStatus(response.success()
                ? PaymentStatus.CAPTURED
                : PaymentStatus.FAILED);

        return orderRepository.save(order);
    }

    @Transactional
    public PaymentOrder cancel(UUID orderId) {

        PaymentOrder order = getOrder(orderId);

        stateValidator.validate(order.getStatus(), PaymentAction.CANCEL);

        String refTxnId = getLastSuccessfulGatewayTxnId(order);

        var response = gateway.cancel(order, refTxnId);

        saveTransaction(order, TransactionType.CANCEL,
                order.getAmount(),
                response.transactionId(),
                response.success());

        order.setStatus(response.success()
                ? PaymentStatus.CANCELLED
                : PaymentStatus.FAILED);

        return orderRepository.save(order);
    }


    @Transactional
    public PaymentOrder refund(UUID orderId, BigDecimal refundAmount) {

        PaymentOrder order = getOrder(orderId);

        stateValidator.validate(order.getStatus(), PaymentAction.REFUND);

        String refTxnId = getLastSuccessfulGatewayTxnId(order);

        var response = gateway.refund(order, refTxnId, refundAmount);

        saveTransaction(order, TransactionType.REFUND,
                refundAmount,
                response.transactionId(),
                response.success());

        if (response.success()) {
            order.setStatus(
                    refundAmount.compareTo(order.getAmount()) < 0
                            ? PaymentStatus.PARTIALLY_REFUNDED
                            : PaymentStatus.REFUNDED
            );
        }

        return orderRepository.save(order);
    }



    private PaymentOrder createOrder(BigDecimal amount, String currency) {
        return orderRepository.save(
                PaymentOrder.builder()
                        .amount(amount)
                        .currency(currency)
                        .status(PaymentStatus.CREATED)
                        .build()
        );
    }

    private PaymentOrder getOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Payment order not found"));
    }

    private void saveTransaction(PaymentOrder order,
                                 TransactionType type,
                                 BigDecimal amount,
                                 String gatewayTxnId,
                                 boolean success) {

        PaymentTransaction txn = PaymentTransaction.builder()
                .paymentOrder(order)
                .type(type)
                .amount(amount)
                .status(success
                        ? TransactionStatus.SUCCESS
                        : TransactionStatus.FAILED)
                .gatewayTransactionId(gatewayTxnId)
                .build();

        transactionRepository.save(txn);
    }

    private String getLastSuccessfulGatewayTxnId(PaymentOrder order) {

        return transactionRepository
                .findTopByPaymentOrderAndStatusOrderByCreatedAtDesc(
                        order, TransactionStatus.SUCCESS
                )
                .map(PaymentTransaction::getGatewayTransactionId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No successful transaction found for this order"
                        ));
    }

}

