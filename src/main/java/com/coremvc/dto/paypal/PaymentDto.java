package com.coremvc.dto.paypal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {

    private Long id;
    private String paypalOrderId;
    private String paypalCaptureId;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String payerEmail;
    private String payerId;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
