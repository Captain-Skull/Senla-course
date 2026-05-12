package com.senla.pas.integration.controller;

import com.senla.pas.dto.request.CommentRequest;
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

class CommentControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("GET /api/ads/{adId}/comments — positive")
    void getComments_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/ads/{adId}/comments", 1).with(authUser(1L, "owner", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("GET /api/ads/{adId}/comments — negative unauthorized")
    void getComments_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/ads/{adId}/comments", 1))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/ads/{adId}/comments — positive")
    void addComment_shouldReturnCreated() throws Exception {
        CommentRequest request = new CommentRequest("Новый комментарий");

        mockMvc.perform(post("/api/ads/{adId}/comments", 1)
                        .with(authUser(1L, "owner", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.content", is("Новый комментарий")));
    }

    @Test
    @DisplayName("POST /api/ads/{adId}/comments — negative validation")
    void addComment_shouldReturnBadRequestOnValidation() throws Exception {
        CommentRequest request = new CommentRequest("");

        mockMvc.perform(post("/api/ads/{adId}/comments", 1)
                        .with(authUser(1L, "owner", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/ads/{adId}/comments/{commentId} — positive")
    void updateComment_shouldReturnUpdated() throws Exception {
        CommentRequest request = new CommentRequest("Обновленный текст");

        mockMvc.perform(put("/api/ads/{adId}/comments/{commentId}", 1, 1)
                        .with(authUser(2L, "other", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.content", is("Обновленный текст")));
    }

    @Test
    @DisplayName("PUT /api/ads/{adId}/comments/{commentId} — negative forbidden")
    void updateComment_shouldReturnForbiddenForNonAuthor() throws Exception {
        CommentRequest request = new CommentRequest("Чужой апдейт");

        mockMvc.perform(put("/api/ads/{adId}/comments/{commentId}", 1, 1)
                        .with(authUser(1L, "owner", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/ads/{adId}/comments/{commentId} — positive")
    void deleteComment_shouldReturnOk() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/ads/{adId}/comments", 1)
                        .with(authUser(1L, "owner", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CommentRequest("Удаляемый комментарий"))))
                .andExpect(status().isCreated())
                .andReturn();

        Long commentId = responseId(createResult);

        mockMvc.perform(delete("/api/ads/{adId}/comments/{commentId}", 1, commentId)
                        .with(authUser(1L, "owner", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentId.intValue())));
    }

    @Test
    @DisplayName("DELETE /api/ads/{adId}/comments/{commentId} — negative unauthorized")
    void deleteComment_shouldReturnUnauthorizedWithoutAuth() throws Exception {
        mockMvc.perform(delete("/api/ads/{adId}/comments/{commentId}", 1, 1))
                .andExpect(status().isUnauthorized());
    }
}
