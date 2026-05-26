package com.senla.pas.integration.controller;

import com.senla.pas.integration.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiDocControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("GET /api/docs/openapi.json — positive")
    void openApi_shouldReturnJson() throws Exception {
        mockMvc.perform(get("/api/docs/openapi.json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.openapi").exists());
    }

    @Test
    @DisplayName("POST /api/docs/openapi.json — negative method not allowed")
    void openApi_shouldReturnMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/api/docs/openapi.json"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("GET /api/docs/swagger-ui.html — positive")
    void swaggerUi_shouldReturnHtml() throws Exception {
        mockMvc.perform(get("/api/docs/swagger-ui.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }

    @Test
    @DisplayName("POST /api/docs/swagger-ui.html — negative method not allowed")
    void swaggerUi_shouldReturnMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/api/docs/swagger-ui.html"))
                .andExpect(status().isMethodNotAllowed());
    }
}
