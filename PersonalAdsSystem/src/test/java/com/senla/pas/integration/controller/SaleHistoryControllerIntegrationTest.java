package com.senla.pas.integration.controller;

import com.senla.pas.dto.request.CreateAdRequest;
import com.senla.pas.dto.request.RegisterRequest;
import com.senla.pas.dto.request.SaleHistoryRequest;
import com.senla.pas.enums.AdCategory;
import com.senla.pas.integration.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SaleHistoryControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("GET /api/sales/my-sales — positive")
    void getMySales_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/sales/my-sales").with(authUser(2L, "other", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("GET /api/sales/my-sales — negative unauthorized")
    void getMySales_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/sales/my-sales"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/sales/my-purchases — positive")
    void getMyPurchases_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/sales/my-purchases").with(authUser(1L, "owner", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("GET /api/sales/my-purchases — negative unauthorized")
    void getMyPurchases_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/sales/my-purchases"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/sales/{id} — positive")
    void getSaleById_shouldReturnSale() throws Exception {
        mockMvc.perform(get("/api/sales/{id}", 1).with(authUser(1L, "owner", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @DisplayName("GET /api/sales/{id} — negative forbidden")
    void getSaleById_shouldReturnForbiddenForUnrelatedUser() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        RegisterRequest registerRequest = new RegisterRequest("outsider" + suffix, "outsider" + suffix + "@mail.com", "Password123");
        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        Long outsiderId = responseId(registerResult);

        mockMvc.perform(get("/api/sales/{id}", 1).with(authUser(outsiderId, "outsider" + suffix, "USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/sales/ad/{adId}/buy — positive")
    void buyDirectly_shouldReturnCreated() throws Exception {
        Long adId = createAdByOtherUser();

        mockMvc.perform(post("/api/sales/ad/{adId}/buy", adId).with(authUser(1L, "owner", "USER")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.adId", is(adId.intValue())));
    }

    @Test
    @DisplayName("POST /api/sales/ad/{adId}/buy — negative bad request for own ad")
    void buyDirectly_shouldReturnBadRequestForOwnAd() throws Exception {
        mockMvc.perform(post("/api/sales/ad/{adId}/buy", 1).with(authUser(1L, "owner", "USER")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/sales/chat/{chatId}/buy — positive")
    void buyViaChat_shouldReturnCreated() throws Exception {
        Long adId = createAdByOtherUser();
        Long chatId = createChatByOwnerForAd(adId);
        SaleHistoryRequest request = new SaleHistoryRequest(1000);

        mockMvc.perform(post("/api/sales/chat/{chatId}/buy", chatId)
                        .with(authUser(1L, "owner", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.adId", is(adId.intValue())));
    }

    @Test
    @DisplayName("POST /api/sales/chat/{chatId}/buy — negative forbidden")
    void buyViaChat_shouldReturnForbiddenForNonBuyer() throws Exception {
        Long adId = createAdByOtherUser();
        Long chatId = createChatByOwnerForAd(adId);
        SaleHistoryRequest request = new SaleHistoryRequest(1000);

        mockMvc.perform(post("/api/sales/chat/{chatId}/buy", chatId)
                        .with(authUser(2L, "other", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isForbidden());
    }

    private Long createAdByOtherUser() throws Exception {
        CreateAdRequest create = new CreateAdRequest("Sale ad " + System.nanoTime(), "For sale history flow", AdCategory.ELECTRONICS, 2000);
        MvcResult result = mockMvc.perform(post("/api/ads")
                        .with(authUser(2L, "other", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(create)))
                .andExpect(status().isCreated())
                .andReturn();
        return responseId(result);
    }

    private Long createChatByOwnerForAd(Long adId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/chats/ad/{adId}", adId)
                        .with(authUser(1L, "owner", "USER")))
                .andExpect(status().isOk())
                .andReturn();
        return responseId(result);
    }
}
