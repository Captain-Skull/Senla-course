package com.senla.pas.integration.controller;

import com.senla.pas.dto.request.LoginRequest;
import com.senla.pas.dto.request.RegisterRequest;
import com.senla.pas.integration.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("POST /api/auth/register — positive")
    void registerUser_shouldReturnCreated() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        RegisterRequest request = new RegisterRequest("user" + suffix, "user" + suffix + "@mail.com", "Password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.user.username", is("user" + suffix)));
    }

    @Test
    @DisplayName("POST /api/auth/register — negative validation")
    void registerUser_shouldReturnBadRequestOnValidation() throws Exception {
        RegisterRequest request = new RegisterRequest("", "not-mail", "1");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register/admin - positive")
    void registerAdmin_shouldReturnCreated() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        RegisterRequest request = new RegisterRequest("admin" + suffix, "admin" + suffix + "@mail.com", "admin123");

        mockMvc.perform(post("/api/auth/register/admin")
                        .with(authUser(3L, "admin", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.user.username", is("admin" + suffix)))
                .andExpect(jsonPath("$.user.roles", hasItem("ROLE_ADMIN")));;
    }

    @Test
    @DisplayName("POST /api/auth/register/admin — negative validation")
    void registerAdmin_shouldReturnBadRequestOnValidation() throws Exception {
        RegisterRequest request = new RegisterRequest("", "not-mail", "1");

        mockMvc.perform(post("/api/auth/register/admin")
                        .with(authUser(3L, "admin", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register/admin — negative unauthorized")
    void registerAdmin_shouldReturnUnauthorizedWithoutAuth() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        RegisterRequest request = new RegisterRequest("admin" + suffix, "admin" + suffix + "@mail.com", "admin123");

        mockMvc.perform(post("/api/auth/register/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/register/admin — negative unauthorized")
    void registerAdmin_shouldReturnForbiddenForNotAdmin() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        RegisterRequest request = new RegisterRequest("admin" + suffix, "admin" + suffix + "@mail.com", "admin123");

        mockMvc.perform(post("/api/auth/register/admin")
                        .with(authUser(2L, "other", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/auth/login — positive")
    void login_shouldReturnToken() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        RegisterRequest register = new RegisterRequest("login" + suffix, "login" + suffix + "@mail.com", "Password123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(register))).andExpect(status().isCreated());

        LoginRequest login = new LoginRequest("login" + suffix, "Password123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(json(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.user.username", is("login" + suffix)));
    }

    @Test
    @DisplayName("POST /api/auth/login — negative bad credentials")
    void login_shouldReturnUnauthorizedOnBadCredentials() throws Exception {
        LoginRequest login = new LoginRequest("missing-user", "wrong-pass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(login)))
                .andExpect(status().isUnauthorized());
    }

}
