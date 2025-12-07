package com.coremvc.service;

import com.coremvc.dto.paypal.CreateOrderRequest;
import com.coremvc.dto.paypal.PayPalOrderResponse;
import com.coremvc.dto.paypal.PaymentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PayPalService {

    PayPalOrderResponse createOrder(CreateOrderRequest request, Long userId);

    PayPalOrderResponse captureOrder(String paypalOrderId);

    PaymentDto getPaymentByPaypalOrderId(String paypalOrderId);

    PaymentDto getPaymentById(Long id);

    Page<PaymentDto> getPaymentsByUserId(Long userId, Pageable pageable);

    Page<PaymentDto> getAllPayments(Pageable pageable);

    PayPalOrderResponse cancelOrder(String paypalOrderId);
}
