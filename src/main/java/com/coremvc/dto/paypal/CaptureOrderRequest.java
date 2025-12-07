package com.coremvc.dto.paypal;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptureOrderRequest {

    @NotBlank(message = "PayPal order ID is required")
    private String paypalOrderId;
}
