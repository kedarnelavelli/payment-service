package com.talentica.payment.payment_service.repository;

import com.talentica.payment.payment_service.domain.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, UUID> {
}

