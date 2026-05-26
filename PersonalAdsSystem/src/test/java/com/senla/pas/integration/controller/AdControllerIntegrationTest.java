package com.senla.pas.integration.controller;

import com.senla.pas.dto.request.CreateAdRequest;
import com.senla.pas.dto.request.UpdateAdRequest;
import com.senla.pas.enums.AdCategory;
import com.senla.pas.integration.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("GET /api/ads — positive")
    void getAds_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/ads").with(authUser(1L, "owner", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("GET /api/ads — negative unauthorized")
    void getAds_shouldReturnUnauthorizedWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/ads"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/ads/{id} — positive")
    void getAdById_shouldReturnAd() throws Exception {
        mockMvc.perform(get("/api/ads/{id}", 1).with(authUser(1L, "owner", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("iPhone 13")));
    }

    @Test
    @DisplayName("GET /api/ads/{id} — negative not found")
    void getAdById_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/ads/{id}", 999999).with(authUser(1L, "owner", "USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/ads/my — positive")
    void getMyAds_shouldReturnOnlyOwnerAds() throws Exception {
        mockMvc.perform(get("/api/ads/my").with(authUser(1L, "owner", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("GET /api/ads/my — negative unauthorized")
    void getMyAds_shouldReturnUnauthorizedWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/ads/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/ads/user/{id} — positive")
    void getUserAds_shouldReturnAdsByUser() throws Exception {
        mockMvc.perform(get("/api/ads/user/{id}", 2).with(authUser(1L, "owner", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("GET /api/ads/user/{id} — negative unauthorized")
    void getUserAds_shouldReturnUnauthorizedWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/ads/user/{id}", 2))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/ads — positive")
    void createAd_shouldReturnCreated() throws Exception {
        CreateAdRequest request = new CreateAdRequest("Nintendo Switch", "Состояние отличное", AdCategory.ELECTRONICS, 25000);

        mockMvc.perform(post("/api/ads")
                        .with(authUser(1L, "owner", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title", is("Nintendo Switch")));
    }

    @Test
    @DisplayName("POST /api/ads — negative validation")
    void createAd_shouldReturnBadRequestOnValidation() throws Exception {
        CreateAdRequest request = new CreateAdRequest("", "", null, -1);

        mockMvc.perform(post("/api/ads")
                        .with(authUser(1L, "owner", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/ads/{id} — positive")
    void updateAd_shouldReturnUpdated() throws Exception {
        UpdateAdRequest request = new UpdateAdRequest("iPhone 13 Pro", "Обновленное описание", AdCategory.ELECTRONICS, 65000, true);

        mockMvc.perform(put("/api/ads/{id}", 1)
                        .with(authUser(1L, "owner", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("iPhone 13 Pro")));
    }

    @Test
    @DisplayName("PUT /api/ads/{id} — negative forbidden for non-owner")
    void updateAd_shouldReturnForbiddenForNonOwner() throws Exception {
        UpdateAdRequest request = new UpdateAdRequest("Hacked", "Nope", AdCategory.ELECTRONICS, 1, true);

        mockMvc.perform(put("/api/ads/{id}", 1)
                        .with(authUser(2L, "other", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/ads/{id} — positive for admin")
    void deleteAd_shouldReturnOkForAdmin() throws Exception {
        mockMvc.perform(delete("/api/ads/{id}", 3).with(authUser(3L, "admin", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)));
    }

    @Test
    @DisplayName("DELETE /api/ads/{id} — negative unauthorized")
    void deleteAd_shouldReturnUnauthorizedWithoutAuth() throws Exception {
        mockMvc.perform(delete("/api/ads/{id}", 1))
                .andExpect(status().isUnauthorized());
    }
}
