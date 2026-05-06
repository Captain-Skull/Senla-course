package com.senla.pas.controller;

import com.senla.pas.dto.request.MessageRequest;
import com.senla.pas.dto.response.MessageResponse;
import com.senla.pas.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Управление сообщениями", description = "Создание, изменение, удаление и просмотр сообщений")
public class MessageController {

    private final MessageService messageService;
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    @Operation(summary = "Получить список сообщений из чата по ID")
    public ResponseEntity<List<MessageResponse>> getMessages(@PathVariable Long chatId) {
        logger.info("Запрос на получение сообщений из чата: {}", chatId);
        return ResponseEntity.ok(messageService.getMessagesByChat(chatId));
    }

    @GetMapping("/{messageId}")
    @Operation(summary = "Получить сообщение")
    public ResponseEntity<MessageResponse> getMessageById(@PathVariable Long chatId, @PathVariable Long messageId) {
        logger.info("Запрос на получение сообщения {} из чата {}", messageId, chatId);
        return ResponseEntity.ok(messageService.getMessageById(messageId));
    }

    @PostMapping
    @Operation(summary = "Отправить сообщение")
    public ResponseEntity<MessageResponse> sendMessage(@PathVariable Long chatId, @Valid @RequestBody MessageRequest request) {
        logger.info("Запрос на отправку сообщения в чат {}", chatId);
        MessageResponse response = messageService.sendMessage(chatId, request);
        logger.info("Сообщение {} отправлено в чат {}", response.getId(), chatId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{messageId}")
    @Operation(summary = "Обновить сообщение")
    public ResponseEntity<MessageResponse> updateMessage(@PathVariable Long chatId, @PathVariable Long messageId, @Valid @RequestBody MessageRequest request) {
        logger.info("Запрос на обновление сообщения {} в чате {}", chatId, messageId);
        return ResponseEntity.ok(messageService.updateMessage(messageId, request));
    }

    @PatchMapping("/{messageId}/read")
    @Operation(summary = "Прочитать сообщение")
    public ResponseEntity<MessageResponse> readMessage(@PathVariable Long chatId, @PathVariable Long messageId) {
        logger.info("Запрос отметки сообщения {} как прочитанного в чате {}", messageId, chatId);
        return ResponseEntity.ok(messageService.readMessage(messageId));
    }

    @DeleteMapping("/{messageId}")
    @Operation(summary = "Удалить сообщение")
    public ResponseEntity<MessageResponse> deleteMessage(@PathVariable Long chatId, @PathVariable Long messageId) {
        logger.info("Запрос на удаление сообещения {} в чате {}", messageId, chatId);
        return ResponseEntity.ok(messageService.deleteMessage(messageId));
    }
}
