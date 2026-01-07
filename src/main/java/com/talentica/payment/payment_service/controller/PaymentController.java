package com.talentica.payment.payment_service.controller;

import com.talentica.payment.payment_service.domain.entity.PaymentOrder;
import com.talentica.payment.payment_service.dto.request.*;
import com.talentica.payment.payment_service.dto.response.PaymentResponse;
import com.talentica.payment.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/purchase")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse purchase(@Valid @RequestBody PurchaseRequest request) {

        PaymentOrder order = paymentService.purchase(
                request.amount(),
                request.currency()
        );

        return toResponse(order);
    }

    @PostMapping("/authorize")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse authorize(@Valid @RequestBody AuthorizeRequest request) {

        PaymentOrder order = paymentService.authorize(
                request.amount(),
                request.currency()
        );

        return toResponse(order);
    }

    @PostMapping("/{orderId}/capture")
    public PaymentResponse capture(@PathVariable UUID orderId) {

        PaymentOrder order = paymentService.capture(orderId);
        return toResponse(order);
    }

    @PostMapping("/{orderId}/cancel")
    public PaymentResponse cancel(@PathVariable UUID orderId) {

        PaymentOrder order = paymentService.cancel(orderId);
        return toResponse(order);
    }

    @PostMapping("/{orderId}/refund")
    public PaymentResponse refund(
            @PathVariable UUID orderId,
            @Valid @RequestBody RefundRequest request) {

        PaymentOrder order = paymentService.refund(
                orderId,
                request.amount()
        );

        return toResponse(order);
    }

    private PaymentResponse toResponse(PaymentOrder order) {
        return new PaymentResponse(
                order.getId(),
                order.getStatus(),
                order.getAmount(),
                order.getCurrency()
        );
    }
}
