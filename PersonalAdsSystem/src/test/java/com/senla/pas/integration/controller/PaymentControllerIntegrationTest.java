package com.senla.pas.integration.controller;

import com.senla.pas.dto.request.CreateAdRequest;
import com.senla.pas.dto.request.PaymentRequest;
import com.senla.pas.enums.AdCategory;
import com.senla.pas.enums.PromotionPlan;
import com.senla.pas.integration.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("GET /api/payments/my — positive")
    void getMyPayments_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/payments/my").with(authUser(1L, "owner", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("GET /api/payments/my — negative unauthorized")
    void getMyPayments_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/payments/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/payments/{id} — positive")
    void getPaymentById_shouldReturnPayment() throws Exception {
        mockMvc.perform(get("/api/payments/{id}", 1).with(authUser(1L, "owner", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @DisplayName("GET /api/payments/{id} — negative forbidden")
    void getPaymentById_shouldReturnForbiddenForAnotherUser() throws Exception {
        mockMvc.perform(get("/api/payments/{id}", 1).with(authUser(2L, "other", "USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/payments/ad/{adId} — positive")
    void getPaymentsByAd_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/payments/ad/{adId}", 2).with(authUser(1L, "owner", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("GET /api/payments/ad/{adId} — negative forbidden")
    void getPaymentsByAd_shouldReturnForbiddenForNonOwner() throws Exception {
        mockMvc.perform(get("/api/payments/ad/{adId}", 2).with(authUser(2L, "other", "USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/payments/ad/{adId}/active — positive")
    void getActivePayment_shouldReturnPayment() throws Exception {
        mockMvc.perform(get("/api/payments/ad/{adId}/active", 2).with(authUser(1L, "owner", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.adId", is(2)));
    }

    @Test
    @DisplayName("GET /api/payments/ad/{adId}/active — negative not found")
    void getActivePayment_shouldReturnNotFoundForNoActivePromotion() throws Exception {
        Long adId = createOwnedAd();

        mockMvc.perform(get("/api/payments/ad/{adId}/active", adId).with(authUser(1L, "owner", "USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/payments — positive")
    void createPayment_shouldReturnCreated() throws Exception {
        Long adId = createOwnedAd();
        PaymentRequest request = new PaymentRequest(adId, PromotionPlan.DAY);

        mockMvc.perform(post("/api/payments")
                        .with(authUser(1L, "owner", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.adId", is(adId.intValue())));
    }

    @Test
    @DisplayName("POST /api/payments — negative bad request for already premium ad")
    void createPayment_shouldReturnBadRequestForAlreadyPremiumAd() throws Exception {
        PaymentRequest request = new PaymentRequest(2L, PromotionPlan.DAY);

        mockMvc.perform(post("/api/payments")
                        .with(authUser(1L, "owner", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest());
    }

    private Long createOwnedAd() throws Exception {
        CreateAdRequest create = new CreateAdRequest("Ad for payment " + System.nanoTime(), "Payment test ad", AdCategory.ELECTRONICS, 1000);
        MvcResult result = mockMvc.perform(post("/api/ads")
                        .with(authUser(1L, "owner", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(create)))
                .andExpect(status().isCreated())
                .andReturn();
        return responseId(result);
    }
}
