package com.coremvc.service.impl;

import com.coremvc.dto.RestPage;
import com.coremvc.dto.paypal.CreateOrderRequest;
import com.coremvc.dto.paypal.PayPalOrderResponse;
import com.coremvc.dto.paypal.PaymentDto;
import com.coremvc.exception.BadRequestException;
import com.coremvc.exception.ResourceNotFoundException;
import com.coremvc.model.Payment;
import com.coremvc.repository.PaymentRepository;
import com.coremvc.service.PayPalService;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PayPalServiceImpl implements PayPalService {

    private final PayPalHttpClient payPalHttpClient;
    private final PaymentRepository paymentRepository;

    @Override
    public PayPalOrderResponse createOrder(CreateOrderRequest request, Long userId) {
        log.info("Creating PayPal order for user: {}, amount: {} {}", userId, request.getAmount(), request.getCurrency());

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.checkoutPaymentIntent("CAPTURE");

        AmountWithBreakdown amountWithBreakdown = new AmountWithBreakdown()
                .currencyCode(request.getCurrency())
                .value(request.getAmount().toString());

        PurchaseUnitRequest purchaseUnitRequest = new PurchaseUnitRequest()
                .amountWithBreakdown(amountWithBreakdown);

        if (request.getDescription() != null) {
            purchaseUnitRequest.description(request.getDescription());
        }

        List<PurchaseUnitRequest> purchaseUnits = new ArrayList<>();
        purchaseUnits.add(purchaseUnitRequest);
        orderRequest.purchaseUnits(purchaseUnits);

        ApplicationContext applicationContext = new ApplicationContext()
                .brandName("MVC Core")
                .landingPage("BILLING")
                .userAction("PAY_NOW");

        if (request.getReturnUrl() != null) {
            applicationContext.returnUrl(request.getReturnUrl());
        }
        if (request.getCancelUrl() != null) {
            applicationContext.cancelUrl(request.getCancelUrl());
        }

        orderRequest.applicationContext(applicationContext);

        OrdersCreateRequest ordersCreateRequest = new OrdersCreateRequest();
        ordersCreateRequest.requestBody(orderRequest);

        try {
            HttpResponse<Order> response = payPalHttpClient.execute(ordersCreateRequest);
            Order order = response.result();

            Payment payment = Payment.builder()
                    .paypalOrderId(order.id())
                    .userId(userId)
                    .status(Payment.PaymentStatus.CREATED)
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .description(request.getDescription())
                    .build();

            paymentRepository.save(payment);

            log.info("PayPal order created successfully: {}", order.id());

            return buildOrderResponse(order, payment);

        } catch (IOException e) {
            log.error("Error creating PayPal order", e);
            throw new BadRequestException("Failed to create PayPal order: " + e.getMessage());
        }
    }

    @Override
    public PayPalOrderResponse captureOrder(String paypalOrderId) {
        log.info("Capturing PayPal order: {}", paypalOrderId);

        Payment payment = paymentRepository.findByPaypalOrderId(paypalOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with PayPal order ID: " + paypalOrderId));

        if (payment.getStatus() == Payment.PaymentStatus.COMPLETED) {
            throw new BadRequestException("Order already captured");
        }

        OrdersCaptureRequest ordersCaptureRequest = new OrdersCaptureRequest(paypalOrderId);

        try {
            HttpResponse<Order> response = payPalHttpClient.execute(ordersCaptureRequest);
            Order order = response.result();

            payment.setStatus(Payment.PaymentStatus.COMPLETED);

            if (order.payer() != null) {
                if (order.payer().email() != null) {
                    payment.setPayerEmail(order.payer().email());
                }
                if (order.payer().payerId() != null) {
                    payment.setPayerId(order.payer().payerId());
                }
            }

            if (order.purchaseUnits() != null && !order.purchaseUnits().isEmpty()) {
                PurchaseUnit purchaseUnit = order.purchaseUnits().get(0);
                if (purchaseUnit.payments() != null && purchaseUnit.payments().captures() != null
                        && !purchaseUnit.payments().captures().isEmpty()) {
                    payment.setPaypalCaptureId(purchaseUnit.payments().captures().get(0).id());
                }
            }

            paymentRepository.save(payment);

            log.info("PayPal order captured successfully: {}", paypalOrderId);

            return buildOrderResponse(order, payment);

        } catch (IOException e) {
            log.error("Error capturing PayPal order: {}", paypalOrderId, e);
            payment.setStatus(Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new BadRequestException("Failed to capture PayPal order: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDto getPaymentByPaypalOrderId(String paypalOrderId) {
        Payment payment = paymentRepository.findByPaypalOrderId(paypalOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with PayPal order ID: " + paypalOrderId));
        return toPaymentDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDto getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + id));
        return toPaymentDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDto> getPaymentsByUserId(Long userId, Pageable pageable) {
        Page<PaymentDto> page = paymentRepository.findByUserId(userId, pageable)
                .map(this::toPaymentDto);
        return new RestPage<>(page.getContent(), pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDto> getAllPayments(Pageable pageable) {
        Page<PaymentDto> page = paymentRepository.findAll(pageable)
                .map(this::toPaymentDto);
        return new RestPage<>(page.getContent(), pageable, page.getTotalElements());
    }

    @Override
    public PayPalOrderResponse cancelOrder(String paypalOrderId) {
        log.info("Cancelling PayPal order: {}", paypalOrderId);

        Payment payment = paymentRepository.findByPaypalOrderId(paypalOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with PayPal order ID: " + paypalOrderId));

        if (payment.getStatus() == Payment.PaymentStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel a completed order");
        }

        payment.setStatus(Payment.PaymentStatus.CANCELLED);
        paymentRepository.save(payment);

        log.info("PayPal order cancelled: {}", paypalOrderId);

        return PayPalOrderResponse.builder()
                .orderId(payment.getId().toString())
                .paypalOrderId(payment.getPaypalOrderId())
                .status(payment.getStatus().name())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    private PayPalOrderResponse buildOrderResponse(Order order, Payment payment) {
        List<PayPalOrderResponse.LinkDescription> links = null;
        String approvalUrl = null;

        if (order.links() != null) {
            links = order.links().stream()
                    .map(link -> PayPalOrderResponse.LinkDescription.builder()
                            .href(link.href())
                            .rel(link.rel())
                            .method(link.method())
                            .build())
                    .collect(Collectors.toList());

            approvalUrl = order.links().stream()
                    .filter(link -> "approve".equals(link.rel()))
                    .findFirst()
                    .map(LinkDescription::href)
                    .orElse(null);
        }

        return PayPalOrderResponse.builder()
                .orderId(payment.getId().toString())
                .paypalOrderId(order.id())
                .status(order.status())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .approvalUrl(approvalUrl)
                .links(links)
                .createdAt(payment.getCreatedAt())
                .build();
    }

    private PaymentDto toPaymentDto(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .paypalOrderId(payment.getPaypalOrderId())
                .paypalCaptureId(payment.getPaypalCaptureId())
                .status(payment.getStatus().name())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .payerEmail(payment.getPayerEmail())
                .payerId(payment.getPayerId())
                .description(payment.getDescription())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
