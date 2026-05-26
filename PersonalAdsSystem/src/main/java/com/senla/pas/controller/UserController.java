package com.senla.pas.controller;

import com.senla.pas.dto.request.UpdateUserRequest;
import com.senla.pas.dto.response.UserResponse;
import com.senla.pas.enums.SortDirection;
import com.senla.pas.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Validated
@Tag(name = "Управление пользователями", description = "Создание, изменение, удаление пользователей")
public class UserController {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить список всех пользователей админом")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        logger.info("Запрос всех пользователей");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('USERS_READ')")
    @Operation(summary = "Получить пользователя по ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        logger.info("Запрос пользователя по ID: {}", userId);
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('USERS_READ')")
    @Operation(summary = "Получить текущего пользователя")
    public ResponseEntity<UserResponse> getMyProfile() {
        logger.info("Запрос профиля текущего пользователя");
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAuthority('USERS_READ')")
    @Operation(summary = "Получить список отфильтрованных пользователей")
    public ResponseEntity<List<UserResponse>> getUsersFilteredByRating(
            @RequestParam(defaultValue = "DESC") SortDirection direction,
            @Min(0) @Max(5) @RequestParam(required = false) Double minRating,
            @Min(0) @Max(5) @RequestParam(required = false) Double maxRating
            ) {
        logger.info("Запрос пользователей с фильтрацией по рейтингу");
        return ResponseEntity.ok(userService.getUsersFilteredByRating(direction, minRating, maxRating));
    }

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('USERS_UPDATE')")
    @Operation(summary = "Обновить мой профиль")
    public ResponseEntity<UserResponse> updateMyProfile(@Valid @RequestBody UpdateUserRequest request) {
        logger.info("Запрос на обновление текущего пользователя");
        return ResponseEntity.ok(userService.updateUser(request));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('USERS_DELETE')")
    @Operation(summary = "Удалить профиль")
    public ResponseEntity<UserResponse> deleteUser(@PathVariable Long userId) {
        logger.info("Запрос на удаление пользователя: {}", userId);
        return ResponseEntity.ok(userService.deleteUser(userId));
    }
}
