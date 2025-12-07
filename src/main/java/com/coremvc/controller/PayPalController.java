package com.coremvc.controller;

import com.coremvc.dto.ApiResponse;
import com.coremvc.dto.paypal.CaptureOrderRequest;
import com.coremvc.dto.paypal.CreateOrderRequest;
import com.coremvc.dto.paypal.PayPalOrderResponse;
import com.coremvc.dto.paypal.PaymentDto;
import com.coremvc.service.PayPalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/paypal")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class PayPalController {

    private final PayPalService payPalService;

    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<PayPalOrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        Long userId = getCurrentUserId();
        PayPalOrderResponse response = payPalService.createOrder(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>("PayPal order created successfully", response, true));
    }

    @PostMapping("/orders/capture")
    public ResponseEntity<ApiResponse<PayPalOrderResponse>> captureOrder(
            @Valid @RequestBody CaptureOrderRequest request) {

        PayPalOrderResponse response = payPalService.captureOrder(request.getPaypalOrderId());

        return ResponseEntity.ok(
                new ApiResponse<>("PayPal order captured successfully", response, true));
    }

    @GetMapping("/orders/{paypalOrderId}")
    public ResponseEntity<ApiResponse<PaymentDto>> getOrderByPaypalOrderId(
            @PathVariable String paypalOrderId) {

        PaymentDto payment = payPalService.getPaymentByPaypalOrderId(paypalOrderId);

        return ResponseEntity.ok(
                new ApiResponse<>("Payment retrieved successfully", payment, true));
    }

    @PostMapping("/orders/{paypalOrderId}/cancel")
    public ResponseEntity<ApiResponse<PayPalOrderResponse>> cancelOrder(
            @PathVariable String paypalOrderId) {

        PayPalOrderResponse response = payPalService.cancelOrder(paypalOrderId);

        return ResponseEntity.ok(
                new ApiResponse<>("PayPal order cancelled successfully", response, true));
    }

    @GetMapping("/payments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PaymentDto>>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<PaymentDto> payments = payPalService.getAllPayments(pageable);

        return ResponseEntity.ok(
                new ApiResponse<>("Payments retrieved successfully", payments, true));
    }

    @GetMapping("/payments/{id}")
    public ResponseEntity<ApiResponse<PaymentDto>> getPaymentById(@PathVariable Long id) {
        PaymentDto payment = payPalService.getPaymentById(id);

        return ResponseEntity.ok(
                new ApiResponse<>("Payment retrieved successfully", payment, true));
    }

    @GetMapping("/payments/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<Page<PaymentDto>>> getPaymentsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<PaymentDto> payments = payPalService.getPaymentsByUserId(userId, pageable);

        return ResponseEntity.ok(
                new ApiResponse<>("Payments retrieved successfully", payments, true));
    }

    @GetMapping("/payments/my")
    public ResponseEntity<ApiResponse<Page<PaymentDto>>> getMyPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Long userId = getCurrentUserId();

        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<PaymentDto> payments = payPalService.getPaymentsByUserId(userId, pageable);

        return ResponseEntity.ok(
                new ApiResponse<>("Payments retrieved successfully", payments, true));
    }

    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<String>> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "PAYPAL-TRANSMISSION-ID", required = false) String transmissionId,
            @RequestHeader(value = "PAYPAL-TRANSMISSION-TIME", required = false) String transmissionTime,
            @RequestHeader(value = "PAYPAL-TRANSMISSION-SIG", required = false) String transmissionSig,
            @RequestHeader(value = "PAYPAL-CERT-URL", required = false) String certUrl,
            @RequestHeader(value = "PAYPAL-AUTH-ALGO", required = false) String authAlgo) {

        log.info("Received PayPal webhook: transmissionId={}", transmissionId);
        log.debug("Webhook payload: {}", payload);

        return ResponseEntity.ok(
                new ApiResponse<>("Webhook received successfully", "OK", true));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.User) {
                return null;
            }
        }
        return null;
    }
}
