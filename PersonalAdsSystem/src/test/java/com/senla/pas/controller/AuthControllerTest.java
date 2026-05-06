package com.senla.pas.controller;

import com.senla.pas.dto.request.LoginRequest;
import com.senla.pas.dto.request.RegisterRequest;
import com.senla.pas.dto.response.AuthResponse;
import com.senla.pas.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;
    @InjectMocks
    private AuthController authController;

    @Test
    void register_positive() {
        RegisterRequest request = new RegisterRequest();
        AuthResponse response = new AuthResponse();
        when(authService.register(request)).thenReturn(response);

        ResponseEntity<AuthResponse> result = authController.register(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(authService).register(request);
    }

    @Test
    void register_negative_serviceThrows() {
        RegisterRequest request = new RegisterRequest();
        when(authService.register(request)).thenThrow(new IllegalStateException("fail"));

        assertThrows(IllegalStateException.class, () -> authController.register(request));
    }

    @Test
    void register_npeSafety_nullFields() {
        RegisterRequest request = new RegisterRequest();
        when(authService.register(request)).thenReturn(new AuthResponse());

        assertDoesNotThrow(() -> authController.register(request));
    }

    @Test
    void login_positive() {
        LoginRequest request = new LoginRequest();
        AuthResponse response = new AuthResponse();
        when(authService.login(request)).thenReturn(response);

        ResponseEntity<AuthResponse> result = authController.login(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(authService).login(request);
    }

    @Test
    void login_negative_serviceThrows() {
        LoginRequest request = new LoginRequest();
        when(authService.login(request)).thenThrow(new IllegalArgumentException("bad creds"));

        assertThrows(IllegalArgumentException.class, () -> authController.login(request));
    }

    @Test
    void login_npeSafety_nullUsernameOrEmail() {
        LoginRequest request = new LoginRequest();
        when(authService.login(request)).thenReturn(new AuthResponse());

        assertDoesNotThrow(() -> authController.login(request));
    }
}
