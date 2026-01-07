package com.talentica.payment.payment_service.service;

import com.talentica.payment.payment_service.domain.entity.PaymentOrder;
import com.talentica.payment.payment_service.domain.entity.PaymentTransaction;
import com.talentica.payment.payment_service.domain.enums.PaymentStatus;
import com.talentica.payment.payment_service.domain.enums.TransactionStatus;
import com.talentica.payment.payment_service.exception.ResourceNotFoundException;
import com.talentica.payment.payment_service.gateway.AuthorizeNetGateway;
import com.talentica.payment.payment_service.gateway.dto.GatewayResponse;
import com.talentica.payment.payment_service.repository.PaymentOrderRepository;
import com.talentica.payment.payment_service.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentOrderRepository orderRepository;

    @Mock
    private PaymentTransactionRepository transactionRepository;

    @Mock
    private AuthorizeNetGateway gateway;

    @Mock
    private PaymentStateValidator stateValidator;

    @InjectMocks
    private PaymentService paymentService;

    /* ===================== HELPERS ===================== */

    private PaymentOrder createOrder(PaymentStatus status) {
        return PaymentOrder.builder()
                .id(UUID.randomUUID())
                .amount(BigDecimal.valueOf(200))
                .currency("USD")
                .status(status)
                .build();
    }

    private PaymentTransaction successfulTxn(String gatewayTxnId) {
        return PaymentTransaction.builder()
                .gatewayTransactionId(gatewayTxnId)
                .status(TransactionStatus.SUCCESS)
                .build();
    }

    private GatewayResponse successResponse(String txnId) {
        return new GatewayResponse(true, txnId, null);
    }

    private GatewayResponse failureResponse() {
        return new GatewayResponse(false, null, "gateway error");
    }

    /* ===================== PURCHASE ===================== */

    @Test
    void purchase_shouldCreateCapturedOrder_whenGatewaySucceeds() {

        when(orderRepository.save(any(PaymentOrder.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        when(gateway.purchase(any()))
                .thenReturn(successResponse("txn123"));

        PaymentOrder result =
                paymentService.purchase(BigDecimal.valueOf(200), "USD");

        assertEquals(PaymentStatus.CAPTURED, result.getStatus());

        verify(gateway).purchase(any());
        verify(transactionRepository).save(any());
    }

    @Test
    void purchase_shouldFailOrder_whenGatewayFails() {

        when(orderRepository.save(any(PaymentOrder.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        when(gateway.purchase(any()))
                .thenReturn(failureResponse());

        PaymentOrder result =
                paymentService.purchase(BigDecimal.valueOf(200), "USD");

        assertEquals(PaymentStatus.FAILED, result.getStatus());
    }

    /* ===================== AUTHORIZE ===================== */

    @Test
    void authorize_shouldSetStatusAuthorized_whenSuccessful() {

        when(orderRepository.save(any(PaymentOrder.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        when(gateway.authorize(any()))
                .thenReturn(successResponse("authTxn"));

        PaymentOrder result =
                paymentService.authorize(BigDecimal.valueOf(200), "USD");

        assertEquals(PaymentStatus.AUTHORIZED, result.getStatus());
    }

    /* ===================== CAPTURE ===================== */

    @Test
    void capture_shouldSetStatusCaptured_whenSuccessful() {

        PaymentOrder order = createOrder(PaymentStatus.AUTHORIZED);

        when(orderRepository.findById(order.getId()))
                .thenReturn(Optional.of(order));

        when(transactionRepository
                .findTopByPaymentOrderAndStatusOrderByCreatedAtDesc(
                        order, TransactionStatus.SUCCESS))
                .thenReturn(Optional.of(successfulTxn("authTxn")));

        when(gateway.capture(order, "authTxn"))
                .thenReturn(successResponse("capTxn"));

        when(orderRepository.save(any(PaymentOrder.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        PaymentOrder result = paymentService.capture(order.getId());

        assertEquals(PaymentStatus.CAPTURED, result.getStatus());
    }

    @Test
    void capture_shouldThrow_whenOrderNotFound() {

        UUID orderId = UUID.randomUUID();

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.capture(orderId));
    }

    /* ===================== CANCEL ===================== */

    @Test
    void cancel_shouldSetStatusCancelled_whenSuccessful() {

        PaymentOrder order = createOrder(PaymentStatus.CAPTURED);

        when(orderRepository.findById(order.getId()))
                .thenReturn(Optional.of(order));

        when(transactionRepository
                .findTopByPaymentOrderAndStatusOrderByCreatedAtDesc(
                        order, TransactionStatus.SUCCESS))
                .thenReturn(Optional.of(successfulTxn("capTxn")));

        when(gateway.cancel(order, "capTxn"))
                .thenReturn(successResponse("voidTxn"));

        when(orderRepository.save(any(PaymentOrder.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        PaymentOrder result = paymentService.cancel(order.getId());

        assertEquals(PaymentStatus.CANCELLED, result.getStatus());
    }

    /* ===================== REFUND ===================== */

    @Test
    void refund_shouldSetRefunded_whenFullRefundSucceeds() {

        PaymentOrder order = createOrder(PaymentStatus.CAPTURED);

        when(orderRepository.findById(order.getId()))
                .thenReturn(Optional.of(order));

        when(transactionRepository
                .findTopByPaymentOrderAndStatusOrderByCreatedAtDesc(
                        order, TransactionStatus.SUCCESS))
                .thenReturn(Optional.of(successfulTxn("capTxn")));

        when(gateway.refund(order, "capTxn", BigDecimal.valueOf(200)))
                .thenReturn(successResponse("refundTxn"));

        when(orderRepository.save(any(PaymentOrder.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        PaymentOrder result =
                paymentService.refund(order.getId(), BigDecimal.valueOf(200));

        assertEquals(PaymentStatus.REFUNDED, result.getStatus());
    }


    @Test
    void refund_shouldSetPartiallyRefunded_whenPartialRefundSucceeds() {

        PaymentOrder order = createOrder(PaymentStatus.CAPTURED);

        when(orderRepository.findById(order.getId()))
                .thenReturn(Optional.of(order));

        when(transactionRepository
                .findTopByPaymentOrderAndStatusOrderByCreatedAtDesc(
                        order, TransactionStatus.SUCCESS))
                .thenReturn(Optional.of(successfulTxn("capTxn")));

        when(gateway.refund(order, "capTxn", BigDecimal.valueOf(100)))
                .thenReturn(successResponse("refundTxn"));

        when(orderRepository.save(any(PaymentOrder.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        PaymentOrder result =
                paymentService.refund(order.getId(), BigDecimal.valueOf(100));

        assertEquals(PaymentStatus.PARTIALLY_REFUNDED, result.getStatus());
    }

    @Test
    void refund_shouldNotChangeStatus_whenGatewayFails() {

        PaymentOrder order = createOrder(PaymentStatus.CAPTURED);

        when(orderRepository.findById(order.getId()))
                .thenReturn(Optional.of(order));

        when(transactionRepository
                .findTopByPaymentOrderAndStatusOrderByCreatedAtDesc(
                        order, TransactionStatus.SUCCESS))
                .thenReturn(Optional.of(successfulTxn("capTxn")));

        when(gateway.refund(any(), any(), any()))
                .thenReturn(failureResponse());

        when(orderRepository.save(any(PaymentOrder.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        PaymentOrder result =
                paymentService.refund(order.getId(), BigDecimal.valueOf(100));

        assertEquals(PaymentStatus.CAPTURED, result.getStatus());
    }
}
