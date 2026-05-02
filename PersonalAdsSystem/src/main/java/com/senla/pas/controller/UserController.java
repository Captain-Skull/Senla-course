package com.senla.pas.controller;

import com.senla.pas.dto.request.UpdateUserRequest;
import com.senla.pas.dto.response.UserResponse;
import com.senla.pas.enums.SortDirection;
import com.senla.pas.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        logger.info("Запрос всех пользователей");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        logger.info("Запрос пользователя по ID: {}", userId);
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile() {
        logger.info("Запрос профиля текущего пользователя");
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @GetMapping("/filter")
    public ResponseEntity<List<UserResponse>> getUsersFilteredByRating(
            @RequestParam(defaultValue = "DESC") SortDirection direction,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxRating
            ) {
        logger.info("Запрос пользователей с фильтрацией по рейтингу");
        return ResponseEntity.ok(userService.getUsersFilteredByRating(direction, minRating, maxRating));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(@RequestBody UpdateUserRequest request) {
        logger.info("Запрос на обновление текущего пользователя");
        return ResponseEntity.ok(userService.updateUser(request));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<UserResponse> deleteUser(@PathVariable Long userId) {
        logger.info("Запрос на удаление пользователя: {}", userId);
        return ResponseEntity.ok(userService.deleteUser(userId));
    }
}
