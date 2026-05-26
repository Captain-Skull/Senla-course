package com.senla.pas.controller;

import com.senla.pas.dto.request.PaymentRequest;
import com.senla.pas.dto.response.PaymentResponse;
import com.senla.pas.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Управление платежами", description = "Создание и просмотр платежей")
public class PaymentController {

    private final PaymentService paymentService;
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('PAYMENT_READ')")
    @Operation(summary = "Получить список моих платежей")
    public ResponseEntity<List<PaymentResponse>> getMyPayments() {
        logger.info("Получение платежей текущего пользователя");
        return ResponseEntity.ok(paymentService.getMyPayments());
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAuthority('PAYMENT_READ')")
    @Operation(summary = "Получить платеж по ID")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long paymentId) {
        logger.info("Получение платежа по ID: {}", paymentId);
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
    }

    @GetMapping("/ad/{adId}")
    @PreAuthorize("hasAuthority('PAYMENT_READ')")
    @Operation(summary = "Получить список платежей объявления по ID")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByAd(@PathVariable Long adId) {
        logger.info("Запрос платежей объявления: {}", adId);
        return ResponseEntity.ok(paymentService.getPaymentsByAd(adId));
    }

    @GetMapping("/ad/{adId}/active")
    @PreAuthorize("hasAuthority('PAYMENT_READ')")
    @Operation(summary = "Получить активный платеж объявления по ID")
    public ResponseEntity<PaymentResponse> getActivePayment(@PathVariable Long adId) {
        logger.info("Запрос активного продвижения объявления: {}", adId);
        return ResponseEntity.ok(paymentService.getActivePaymentByAd(adId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PAYMENT_CREATE')")
    @Operation(summary = "Создать платеж")
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentRequest request
    ) {
        logger.info("Запрос продвижения объявления: {}", request.getAdId());
        PaymentResponse response = paymentService.createPayment(request);
        logger.info("Продвижение создано для объявления: {}", request.getAdId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
