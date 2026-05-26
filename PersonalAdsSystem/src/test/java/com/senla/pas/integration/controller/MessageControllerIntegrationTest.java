package com.senla.pas.integration.controller;

import com.senla.pas.dto.request.MessageRequest;
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

class MessageControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("GET /api/chats/{chatId}/messages — positive")
    void getMessages_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/chats/{chatId}/messages", 1).with(authUser(1L, "owner", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("GET /api/chats/{chatId}/messages — negative unauthorized")
    void getMessages_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/chats/{chatId}/messages", 1))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/chats/{chatId}/messages/{messageId} — positive")
    void getMessageById_shouldReturnMessage() throws Exception {
        mockMvc.perform(get("/api/chats/{chatId}/messages/{messageId}", 1, 1).with(authUser(1L, "owner", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @DisplayName("GET /api/chats/{chatId}/messages/{messageId} — negative not found")
    void getMessageById_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/chats/{chatId}/messages/{messageId}", 1, 99999).with(authUser(1L, "owner", "USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/chats/{chatId}/messages — positive")
    void sendMessage_shouldReturnCreated() throws Exception {
        MessageRequest request = new MessageRequest("Новое сообщение");

        mockMvc.perform(post("/api/chats/{chatId}/messages", 1)
                        .with(authUser(1L, "owner", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    @DisplayName("POST /api/chats/{chatId}/messages — negative validation")
    void sendMessage_shouldReturnBadRequestOnValidation() throws Exception {
        MessageRequest request = new MessageRequest("");

        mockMvc.perform(post("/api/chats/{chatId}/messages", 1)
                        .with(authUser(1L, "owner", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/chats/{chatId}/messages/{messageId} — positive")
    void updateMessage_shouldReturnUpdated() throws Exception {
        Long messageId = createMessageAsOther("Редактируемое");
        MessageRequest update = new MessageRequest("Отредактированное сообщение");

        mockMvc.perform(put("/api/chats/{chatId}/messages/{messageId}", 1, messageId)
                        .with(authUser(2L, "other", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(messageId.intValue())))
                .andExpect(jsonPath("$.content", is("Отредактированное сообщение")));
    }

    @Test
    @DisplayName("PUT /api/chats/{chatId}/messages/{messageId} — negative forbidden")
    void updateMessage_shouldReturnForbiddenForNonSender() throws Exception {
        MessageRequest update = new MessageRequest("Чужой апдейт");

        mockMvc.perform(put("/api/chats/{chatId}/messages/{messageId}", 1, 1)
                        .with(authUser(1L, "owner", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(update)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/chats/{chatId}/messages/{messageId}/read — positive")
    void readMessage_shouldReturnOk() throws Exception {
        mockMvc.perform(patch("/api/chats/{chatId}/messages/{messageId}/read", 1, 1)
                        .with(authUser(1L, "owner", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @DisplayName("PATCH /api/chats/{chatId}/messages/{messageId}/read — negative forbidden for sender")
    void readMessage_shouldReturnForbiddenForSender() throws Exception {
        mockMvc.perform(patch("/api/chats/{chatId}/messages/{messageId}/read", 1, 1)
                        .with(authUser(2L, "other", "USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/chats/{chatId}/messages/{messageId} — positive")
    void deleteMessage_shouldReturnOk() throws Exception {
        Long messageId = createMessageAsOther("Удаляемое сообщение");

        mockMvc.perform(delete("/api/chats/{chatId}/messages/{messageId}", 1, messageId)
                        .with(authUser(2L, "other", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(messageId.intValue())));
    }

    @Test
    @DisplayName("DELETE /api/chats/{chatId}/messages/{messageId} — negative unauthorized")
    void deleteMessage_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/chats/{chatId}/messages/{messageId}", 1, 1))
                .andExpect(status().isUnauthorized());
    }

    private Long createMessageAsOther(String content) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/chats/{chatId}/messages", 1)
                        .with(authUser(2L, "other", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new MessageRequest(content))))
                .andExpect(status().isCreated())
                .andReturn();
        return responseId(result);
    }
}
