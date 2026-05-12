package com.senla.pas.integration.controller;

import com.senla.pas.integration.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("GET /api/chats — positive")
    void getMyChats_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/chats").with(authUser(1L, "owner", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("GET /api/chats — negative unauthorized")
    void getMyChats_shouldReturnUnauthorizedWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/chats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/chats/{id} — positive")
    void getChatById_shouldReturnChat() throws Exception {
        mockMvc.perform(get("/api/chats/{id}", 1).with(authUser(1L, "owner", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @DisplayName("GET /api/chats/{id} — negative forbidden")
    void getChatById_shouldReturnForbiddenForUnrelatedUser() throws Exception {
        mockMvc.perform(get("/api/chats/{id}", 1).with(authUser(3L, "admin", "ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/chats/ad/{adId} — positive")
    void getOrCreateChat_shouldReturnChat() throws Exception {
        mockMvc.perform(post("/api/chats/ad/{adId}", 3).with(authUser(1L, "owner", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    @DisplayName("POST /api/chats/ad/{adId} — negative forbidden for own ad")
    void getOrCreateChat_shouldReturnForbiddenForOwnAd() throws Exception {
        mockMvc.perform(post("/api/chats/ad/{adId}", 1).with(authUser(1L, "owner", "USER")))
                .andExpect(status().isForbidden());
    }
}
