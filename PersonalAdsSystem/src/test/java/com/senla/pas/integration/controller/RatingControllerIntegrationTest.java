package com.senla.pas.integration.controller;

import com.senla.pas.dto.request.RatingRequest;
import com.senla.pas.integration.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RatingControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("GET /api/users/{userId}/ratings — positive")
    void getRatingsByUser_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/ratings", 1).with(authUser(1L, "owner", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("GET /api/users/{userId}/ratings — negative unauthorized")
    void getRatingsByUser_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/ratings", 1))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/users/{userId}/ratings — positive")
    void addRating_shouldReturnCreated() throws Exception {
        RatingRequest request = new RatingRequest((short) 5);

        mockMvc.perform(post("/api/users/{userId}/ratings", 1)
                        .with(authUser(2L, "other", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recipient.id", is(1)))
                .andExpect(jsonPath("$.reviewer.id", is(2)));
    }

    @Test
    @DisplayName("POST /api/users/{userId}/ratings — negative bad request")
    void addRating_shouldReturnBadRequestForSelfRating() throws Exception {
        RatingRequest request = new RatingRequest((short) 5);

        mockMvc.perform(post("/api/users/{userId}/ratings", 1)
                        .with(authUser(1L, "owner", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest());
    }
}
