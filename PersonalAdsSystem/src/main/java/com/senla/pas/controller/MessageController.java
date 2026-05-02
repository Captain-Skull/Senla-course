package com.senla.pas.controller;

import com.senla.pas.dto.request.MessageRequest;
import com.senla.pas.dto.response.MessageResponse;
import com.senla.pas.service.MessageService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats/{chatId}/messages")
public class MessageController {

    private final MessageService messageService;
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public ResponseEntity<List<MessageResponse>> getMessages(@PathVariable Long chatId) {
        logger.info("Запрос на получение сообщений из чата: {}", chatId);
        return ResponseEntity.ok(messageService.getMessagesByChat(chatId));
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<MessageResponse> getMessageById(@PathVariable Long chatId, @PathVariable Long messageId) {
        logger.info("Запрос на получение сообщения {} из чата {}", messageId, chatId);
        return ResponseEntity.ok(messageService.getMessageById(messageId));
    }

    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(@PathVariable Long chatId, @Valid @RequestBody MessageRequest request) {
        logger.info("Запрос на отправку сообщения в чат {}", chatId);
        MessageResponse response = messageService.sendMessage(chatId, request);
        logger.info("Сообщение {} отправлено в чат {}", response.getId(), chatId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<MessageResponse> updateMessage(@PathVariable Long chatId, @PathVariable Long messageId, @Valid @RequestBody MessageRequest request) {
        logger.info("Запрос на обновление сообщения {} в чате {}", chatId, messageId);
        return ResponseEntity.ok(messageService.updateMessage(messageId, request));
    }

    @PatchMapping("/{messageId}/read")
    public ResponseEntity<MessageResponse> readMessage(@PathVariable Long messageId) {
        logger.info("Запрос отметки сообщения {} как прочитанного", messageId);
        return ResponseEntity.ok(messageService.readMessage(messageId));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<MessageResponse> deleteMessage(@PathVariable Long chatId, @PathVariable Long messageId) {
        logger.info("Запрос на удаление сообещения {} в чате {}", messageId, chatId);
        return ResponseEntity.ok(messageService.deleteMessage(messageId));
    }
}
