package com.senla.pas.controller;

import com.senla.pas.dto.request.RatingRequest;
import com.senla.pas.dto.response.RatingResponse;
import com.senla.pas.service.RatingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/{userId}/ratings")
public class RatingController {

    private final RatingService ratingService;
    private static final Logger logger = LoggerFactory.getLogger(RatingController.class);

    @Autowired
    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @GetMapping
    public ResponseEntity<List<RatingResponse>> getRatingsByUser(@PathVariable Long userId) {
        logger.info("Запрос на получение отзывов пользователя {}", userId);
        return ResponseEntity.ok(ratingService.getUserRatings(userId));
    }

    @PostMapping
    public ResponseEntity<RatingResponse> addRating(@PathVariable Long userId, @Valid @RequestBody RatingRequest request) {
        logger.info("Запрос на добавление рейтинга пользователю {}", userId);
        RatingResponse response = ratingService.addRatingToUser(userId, request);
        logger.info("Отзыв {} успешно добавлен пользователю {}", response.getId(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
