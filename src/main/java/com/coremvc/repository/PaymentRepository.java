package com.coremvc.repository;

import com.coremvc.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaypalOrderId(String paypalOrderId);

    Page<Payment> findByUserId(Long userId, Pageable pageable);

    Page<Payment> findByStatus(Payment.PaymentStatus status, Pageable pageable);

    Page<Payment> findByUserIdAndStatus(Long userId, Payment.PaymentStatus status, Pageable pageable);
}
