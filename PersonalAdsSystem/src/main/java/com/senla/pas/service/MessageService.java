package com.senla.pas.service;

import com.senla.pas.dao.ChatDao;
import com.senla.pas.dao.MessageDao;
import com.senla.pas.dao.UserDao;
import com.senla.pas.dto.request.MessageRequest;
import com.senla.pas.dto.response.MessageResponse;
import com.senla.pas.entity.Chat;
import com.senla.pas.entity.Message;
import com.senla.pas.entity.User;
import com.senla.pas.exception.ForbiddenException;
import com.senla.pas.exception.ResourceNotFoundException;
import com.senla.pas.mapper.MessageMapper;
import com.senla.pas.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MessageService {

    private final MessageDao messageDao;
    private final ChatDao chatDao;
    private final UserDao userDao;
    private final MessageMapper messageMapper;
    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    @Autowired
    public MessageService(MessageDao messageDao, ChatDao chatDao, UserDao userDao, MessageMapper messageMapper) {
        this.messageDao = messageDao;
        this.chatDao = chatDao;
        this.userDao = userDao;
        this.messageMapper = messageMapper;
    }

    public MessageResponse getMessageById(Long messageId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Message message = messageDao.findById(messageId).orElseThrow(() -> new ResourceNotFoundException("Сообщение не найдено: " + messageId));
        Chat chat = message.getChat();

        if (!chatDao.isChatAvailableForUser(chat.getId(), userId)) {
            throw new ForbiddenException("Недостаточно прав для просмотра сообщения");
        }

        logger.info("Получение сообщения по id: {}", messageId);
        return messageMapper.toResponse(message);
    }

    public List<MessageResponse> getMessagesByChat(Long chatId) {
        Long userId = SecurityUtils.getCurrentUserId();

        chatDao.findById(chatId).orElseThrow(() -> new ResourceNotFoundException("Чат не найден: " + chatId));

        if (!chatDao.isChatAvailableForUser(chatId, userId)) {
            throw new ForbiddenException("Недостаточно прав для получения сообщений чата: " + chatId);
        }

        logger.info("Получение сообщений из чата {}", chatId);
        return messageMapper.toResponseList(messageDao.findByChatId(chatId));
    }

    @Transactional
    public MessageResponse sendMessage(Long chatId, MessageRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        Chat chat = chatDao.findById(chatId).orElseThrow(() -> new ResourceNotFoundException("Чат не найден: " + chatId));

        if (!chatDao.isChatAvailableForUser(chatId, userId)) {
            throw new ForbiddenException("Недостаточно прав для отправки сообщений в чате: " + chatId);
        }

        User user = userDao.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден: " + userId));

        Message message = messageMapper.toEntity(request);
        message.setChat(chat);
        message.setSender(user);
        messageDao.save(message);

        logger.info("Отправка сообщения {} в чат {} пользователем {}", message.getId(), chatId, userId);
        return messageMapper.toResponse(message);
    }

    @Transactional
    public MessageResponse updateMessage(Long messageId, MessageRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        Message message = messageDao.findById(messageId).orElseThrow(() -> new ResourceNotFoundException("Сообщение не найдено: " + messageId));

        if (!userId.equals(message.getSender().getId())) {
            throw new ForbiddenException("Сообщение может изменять только отправитель");
        }

        message.setContent(request.getContent());

        messageDao.update(message);

        logger.info("Обновление сообщения {} в чате {} пользователем {}", messageId, message.getChat().getId(), userId);
        return messageMapper.toResponse(message);
    }

    @Transactional
    public MessageResponse readMessage(Long messageId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Message message = messageDao.findById(messageId).orElseThrow(() -> new ResourceNotFoundException("Сообщение не найдено: " + messageId));

        if (userId.equals(message.getSender().getId())) {
            throw new ForbiddenException(
                    "Нельзя отметить своё сообщение как прочитанное"
            );
        }

        if (!chatDao.isChatAvailableForUser(message.getChat().getId(), userId)) {
            throw new ForbiddenException(
                    "Отметить сообщение прочитанным может только получатель"
            );
        }

        message.setRead(true);
        messageDao.update(message);

        logger.info("Сообщение {} в чате {} прочитано пользователем {}", messageId, message.getChat().getId(), userId);
        return messageMapper.toResponse(message);
    }

    @Transactional
    public MessageResponse deleteMessage(Long messageId) {
        Message message = messageDao.findById(messageId).orElseThrow(() -> new ResourceNotFoundException("Сообщение не найдено: " + messageId));
        Long userId = SecurityUtils.getCurrentUserId();

        if (!userId.equals(message.getSender().getId())) {
            throw new ForbiddenException("Удалить сообщение может только отправитель");
        }

        messageDao.delete(messageId);
        logger.info("Сообщение {} в чате {} удалено пользователем {}", messageId, message.getChat().getId(), userId);
        return messageMapper.toResponse(message);
    }
}
