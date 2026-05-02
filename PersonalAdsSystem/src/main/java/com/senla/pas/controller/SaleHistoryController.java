package com.senla.pas.controller;

import com.senla.pas.dto.request.SaleHistoryRequest;
import com.senla.pas.dto.response.SaleHistoryResponse;
import com.senla.pas.service.SaleHistoryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SaleHistoryController {

    private final SaleHistoryService saleHistoryService;
    private static final Logger logger = LoggerFactory.getLogger(SaleHistoryController.class);

    @Autowired
    public SaleHistoryController(SaleHistoryService saleHistoryService) {
        this.saleHistoryService = saleHistoryService;
    }

    @GetMapping("/my-sales")
    public ResponseEntity<List<SaleHistoryResponse>> getMySales() {
        logger.info("Получение продаж текущего пользователя");
        return ResponseEntity.ok(saleHistoryService.getMySales());
    }

    @GetMapping("/my-purchases")
    public ResponseEntity<List<SaleHistoryResponse>> getMyPurchases() {
        logger.info("Получение покупок текущего пользователя");
        return ResponseEntity.ok(saleHistoryService.getMyPurchases());
    }

    @GetMapping("/{saleId}")
    public ResponseEntity<SaleHistoryResponse> getSaleById(@PathVariable Long saleId) {
        logger.info("Получить историю покупки по ID: {}", saleId);
        return ResponseEntity.ok(saleHistoryService.getSaleById(saleId));
    }

    @PostMapping("/ad/{adId}/buy")
    public ResponseEntity<SaleHistoryResponse> buyDirectly(@PathVariable Long adId) {
        logger.info("Запрос на покупку напрямую из объявления {}", adId);
        SaleHistoryResponse response = saleHistoryService.buyDirectly(adId);
        logger.info("Запрос на покупку {} успешно отработал. ID: {}", adId, response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/chat/{chatId}/buy")
    public ResponseEntity<SaleHistoryResponse> buyViaChat(@PathVariable Long chatId, @Valid @RequestBody SaleHistoryRequest request) {
        logger.info("Запрос на покупку из чата {}", chatId);
        SaleHistoryResponse response = saleHistoryService.buyViaChat(chatId, request);
        logger.info("Запрос на покупку из чата {} успешно отработал. ID: {}", chatId, response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
