package com.talentica.payment.payment_service.repository;

import com.talentica.payment.payment_service.domain.entity.PaymentOrder;
import com.talentica.payment.payment_service.domain.entity.PaymentTransaction;
import com.talentica.payment.payment_service.domain.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {
    Optional<PaymentTransaction>
    findTopByPaymentOrderAndStatusOrderByCreatedAtDesc(
            PaymentOrder paymentOrder,
            TransactionStatus status
    );
}

