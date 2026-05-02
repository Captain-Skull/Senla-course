package com.senla.pas.controller;

import com.senla.pas.dto.request.CommentRequest;
import com.senla.pas.dto.response.CommentResponse;
import com.senla.pas.entity.Comment;
import com.senla.pas.service.CommentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ad/{adId}/comments")
public class CommentController {

    private final CommentService commentService;
    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getCommentsByAd(@PathVariable Long adId) {
        logger.info("Запрос на получение комментариев поста {}", adId);
        return ResponseEntity.ok(commentService.getCommentsByAd(adId));
    }

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long adId, @RequestBody @Valid CommentRequest request) {
        logger.info("Запрос на отправку комментария к посту {}", adId);
        CommentResponse response = commentService.addCommentToAd(adId, request);
        logger.info("Комментарий {} к посту {} успешно отправлен", response.getId(), adId);
        return ResponseEntity.ok(commentService.addCommentToAd(adId, request));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(@PathVariable Long adId, @PathVariable Long commentId, @RequestBody @Valid CommentRequest request) {
        logger.info("Запрос на изменение комментария {} к посту {}", commentId, adId);
        return ResponseEntity.ok(commentService.updateComment(commentId, request));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<CommentResponse> deleteComment(@PathVariable Long adId, @PathVariable Long commentId) {
        logger.info("Запрос на удаление комментария {} в объявлении {}", adId, commentId);
        return ResponseEntity.ok(commentService.deleteComment(commentId));
    }
}
