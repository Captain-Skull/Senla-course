package com.senla.pas.controller;

import com.senla.pas.dto.request.ChatRequest;
import com.senla.pas.dto.response.ChatResponse;
import com.senla.pas.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@Tag(name = "Управление чатами", description = "Создание и просмотр чатов")
public class ChatController {

    private final ChatService chatService;
    private final static Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    @Operation(summary = "Получить список моих чатов")
    public ResponseEntity<List<ChatResponse>> getMyChats() {
        logger.info("Запрос всех чатов текущего пользователя");
        return ResponseEntity.ok(chatService.getMyChats());
    }

    @GetMapping("/{chatId}")
    @Operation(summary = "Получить чат по ID")
    public ResponseEntity<ChatResponse> getChatById(@PathVariable Long chatId) {
        logger.info("Запрос чата по ID: {}", chatId);
        return ResponseEntity.ok(chatService.getChatById(chatId));
    }

    @PostMapping
    @Operation(summary = "Создать или открыть чат")
    public ResponseEntity<ChatResponse> getOrCreateChat(@Valid @RequestBody ChatRequest request) {
        logger.info("Получение или создания чата");
        return ResponseEntity.ok(chatService.getOrCreateChat(request));
    }
}
