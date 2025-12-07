package com.coremvc.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payment_paypal_order_id", columnList = "paypal_order_id"),
    @Index(name = "idx_payment_status", columnList = "status"),
    @Index(name = "idx_payment_user_id", columnList = "user_id"),
    @Index(name = "idx_payment_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Payment extends BaseEntity {

    @Column(name = "paypal_order_id", unique = true)
    private String paypalOrderId;

    @Column(name = "paypal_capture_id")
    private String paypalCaptureId;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "payer_email")
    private String payerEmail;

    @Column(name = "payer_id")
    private String payerId;

    @Column(length = 500)
    private String description;

    public enum PaymentStatus {
        CREATED,
        APPROVED,
        COMPLETED,
        CANCELLED,
        FAILED,
        REFUNDED
    }
}
