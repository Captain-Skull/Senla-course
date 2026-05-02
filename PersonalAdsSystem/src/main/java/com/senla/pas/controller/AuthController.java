package com.senla.pas.controller;

import com.senla.pas.dto.request.LoginRequest;
import com.senla.pas.dto.request.RegisterRequest;
import com.senla.pas.dto.response.AuthResponse;
import com.senla.pas.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        logger.info("Запрос на регистрацию пользователя: {}", request.getUsername());
        AuthResponse response = authService.register(request);
        logger.info("Пользователь успешно зарегистрирован: {}", request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        logger.info("Запрос на вход пользователя: {}", request.getUsernameOrEmail());
        AuthResponse response = authService.login(request);
        logger.info("Пользователь успешно вошел: {}", request.getUsernameOrEmail());
        return ResponseEntity.ok(response);
    }
}
