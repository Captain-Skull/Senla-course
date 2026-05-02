package com.senla.pas.controller;

import com.senla.pas.dto.request.ChatRequest;
import com.senla.pas.dto.response.ChatResponse;
import com.senla.pas.service.ChatService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService chatService;
    private final static Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public ResponseEntity<List<ChatResponse>> getMyChats() {
        logger.info("Запрос всех чатов текущего пользователя");
        return ResponseEntity.ok(chatService.getMyChats());
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<ChatResponse> getChatById(@PathVariable Long chatId) {
        logger.info("Запрос чата по ID: {}", chatId);
        return ResponseEntity.ok(chatService.getChatById(chatId));
    }

    @PostMapping
    public ResponseEntity<ChatResponse> getOrCreateChat(@Valid @RequestBody ChatRequest request) {
        logger.info("Получение или создания чата");
        return ResponseEntity.ok(chatService.getOrCreateChat(request));
    }
}
