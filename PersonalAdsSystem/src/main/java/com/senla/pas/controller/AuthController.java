package com.senla.pas.controller;

import com.senla.pas.dto.request.LoginRequest;
import com.senla.pas.dto.request.RegisterRequest;
import com.senla.pas.dto.response.AuthResponse;
import com.senla.pas.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "авторизация", description = "Регистрация и вход пользователей")
public class AuthController {

    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Регистрация пользователя")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Запрос на регистрацию пользователя: {}", request.getUsername());
        AuthResponse response = authService.registerUser(request);
        logger.info("Пользователь успешно зарегистрирован: {}", request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Вход пользователя")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Запрос на вход пользователя: {}", request.getUsernameOrEmail());
        AuthResponse response = authService.login(request);
        logger.info("Пользователь успешно вошел: {}", request.getUsernameOrEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/admin")
    @PreAuthorize("hasAuthority('REGISTER_ADMIN')")
    @Operation(summary = "Регистрация администратора")
    public ResponseEntity<AuthResponse> registerAdmin(@Valid @RequestBody RegisterRequest request) {
        logger.info("Запрос на регистрацию администратора: {}", request.getUsername());
        AuthResponse response = authService.registerAdmin(request);
        logger.info("Администратор успешно зарегистрирован: {}", request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
