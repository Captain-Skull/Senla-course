package com.senla.pas.controller;

import com.senla.pas.dto.request.CreateAdRequest;
import com.senla.pas.dto.request.UpdateAdRequest;
import com.senla.pas.dto.response.AdResponse;
import com.senla.pas.enums.AdCategory;
import com.senla.pas.enums.AdSort;
import com.senla.pas.enums.SortDirection;
import com.senla.pas.service.AdService;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ads")
public class AdController {

    private final AdService adService;
    private static final Logger logger = LoggerFactory.getLogger(AdController.class);

    @Autowired
    public AdController(AdService adService) {
        this.adService = adService;
    }

    @GetMapping
    public ResponseEntity<List<AdResponse>> getAds(
            @RequestParam(required = false) AdCategory category,
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "DATE") AdSort sortBy,
            @RequestParam(defaultValue = "DESC") SortDirection sortDirection,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        logger.info("Запрос объявлений: category={}, search={}, page={}, size={}", category, searchText, page, size);
        return ResponseEntity.ok(
                adService.getAdsWithFilters(category, searchText, minPrice, maxPrice, isActive, sortBy, sortDirection, page, size)
        );
    }

    @GetMapping("/{adId}")
    public ResponseEntity<AdResponse> getAdById(@PathVariable Long adId) {
        logger.info("Запрос объявления: {}", adId);
        return ResponseEntity.ok(adService.getAdById(adId));
    }

    @GetMapping("my")
    public ResponseEntity<List<AdResponse>> getMyAds() {
        logger.info("Запрос объявлений текущего пользователя");
        return ResponseEntity.ok(adService.getMyAds());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AdResponse>> getUserAds(@PathVariable Long userId) {
        logger.info("Запрос объявлений пользователя {}", userId);
        return ResponseEntity.ok(adService.getAdsByUser(userId));
    }

    @PostMapping
    public ResponseEntity<AdResponse> createAd(@RequestBody CreateAdRequest request) {
        logger.info("Создание нового объявления");
        return ResponseEntity.ok(adService.createAd(request));
    }

    @PutMapping("/{adId}")
    public ResponseEntity<AdResponse> updateAd(@PathVariable Long adId, @RequestBody UpdateAdRequest request) {
        logger.info("Запрос обновления объявления {} ,", adId);
        return ResponseEntity.ok(adService.updateAd(adId, request));
    }

    @DeleteMapping("/{adId}")
    public ResponseEntity<AdResponse> deleteAd(@PathVariable Long adId) {
        logger.info("Запрос удаления объявления, {}", adId);
        return ResponseEntity.ok(adService.deleteAd(adId));
    }
}
