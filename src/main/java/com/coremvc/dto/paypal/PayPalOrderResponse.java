package com.coremvc.dto.paypal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayPalOrderResponse {

    private String orderId;
    private String paypalOrderId;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String approvalUrl;
    private List<LinkDescription> links;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinkDescription {
        private String href;
        private String rel;
        private String method;
    }
}
