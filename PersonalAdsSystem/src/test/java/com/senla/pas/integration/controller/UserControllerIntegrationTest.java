package com.senla.pas.integration.controller;

import com.senla.pas.dto.request.RegisterRequest;
import com.senla.pas.dto.request.UpdateUserRequest;
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

class UserControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("GET /api/users — positive for admin")
    void getAllUsers_shouldReturnListForAdmin() throws Exception {
        mockMvc.perform(get("/api/users").with(authUser(3L, "admin", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("GET /api/users — negative forbidden for non-admin")
    void getAllUsers_shouldReturnForbiddenForUser() throws Exception {
        mockMvc.perform(get("/api/users").with(authUser(1L, "owner", "USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/users/{id} — positive")
    void getUserById_shouldReturnUser() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 1).with(authUser(2L, "other", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("owner")));
    }

    @Test
    @DisplayName("GET /api/users/{id} — negative not found")
    void getUserById_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 99999).with(authUser(2L, "other", "USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/users/me — positive")
    void getMyProfile_shouldReturnCurrentUser() throws Exception {
        mockMvc.perform(get("/api/users/me").with(authUser(1L, "owner", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @DisplayName("GET /api/users/me — negative unauthorized")
    void getMyProfile_shouldReturnUnauthorizedWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/users/filter — positive")
    void getUsersFiltered_shouldReturnFilteredUsers() throws Exception {
        mockMvc.perform(get("/api/users/filter")
                        .param("direction", "DESC")
                        .param("minRating", "1")
                        .param("maxRating", "5")
                        .with(authUser(1L, "owner", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("GET /api/users/filter — negative validation")
    void getUsersFiltered_shouldReturnBadRequestOnInvalidRange() throws Exception {
        mockMvc.perform(get("/api/users/filter")
                        .param("minRating", "oops")
                        .with(authUser(1L, "owner", "USER")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/users/me — positive")
    void updateMyProfile_shouldReturnUpdatedUser() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest(null, null, null, "Updated from integration test");

        mockMvc.perform(put("/api/users/me")
                        .with(authUser(1L, "owner", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @DisplayName("PUT /api/users/me — negative conflict")
    void updateMyProfile_shouldReturnConflictWhenEmailExists() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest(null, "other@test.com", null, null);

        mockMvc.perform(put("/api/users/me")
                        .with(authUser(1L, "owner", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - positive for owner")
    void deleteUser_shouldReturnOkForOwner() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        String username = "toDelete" + suffix;
        RegisterRequest registerRequest = new RegisterRequest(username, username + "@mail.com", "Password123");
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long createdUserId = responseId(result);

        mockMvc.perform(delete("/api/users/{id}", createdUserId).with(authUser(createdUserId, username, "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(createdUserId.intValue())));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} — positive for admin")
    void deleteUser_shouldReturnOkForAdmin() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        RegisterRequest registerRequest = new RegisterRequest("toDelete" + suffix, "toDelete" + suffix + "@mail.com", "Password123");
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long createdUserId = responseId(result);

        mockMvc.perform(delete("/api/users/{id}", createdUserId).with(authUser(3L, "admin", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(createdUserId.intValue())));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} — negative forbidden")
    void deleteUser_shouldReturnForbiddenForAnotherRegularUser() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 1).with(authUser(2L, "other", "USER")))
                .andExpect(status().isForbidden());
    }
}
