package com.senla.pas.service;

import com.senla.pas.dao.AdDao;
import com.senla.pas.dao.ChatDao;
import com.senla.pas.dao.UserDao;
import com.senla.pas.dto.request.ChatRequest;
import com.senla.pas.dto.response.ChatResponse;
import com.senla.pas.entity.Ad;
import com.senla.pas.entity.Chat;
import com.senla.pas.entity.User;
import com.senla.pas.exception.BadRequestException;
import com.senla.pas.exception.ForbiddenException;
import com.senla.pas.exception.ResourceNotFoundException;
import com.senla.pas.mapper.ChatMapper;
import com.senla.pas.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final ChatDao chatDao;
    private final AdDao adDao;
    private final UserDao userDao;
    private final ChatMapper chatMapper;

    @Autowired
    public ChatService(ChatDao chatDao, AdDao adDao,
                       UserDao userDao, ChatMapper chatMapper) {
        this.chatDao = chatDao;
        this.adDao = adDao;
        this.userDao = userDao;
        this.chatMapper = chatMapper;
    }

    public ChatResponse getChatById(Long chatId) {
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("Получение чата {} пользователем {}", chatId, userId);

        Chat chat = chatDao.findById(chatId).orElseThrow(() -> new ResourceNotFoundException("Чат не найден: " + chatId));

        if (!chatDao.isChatAvailableForUser(chatId, userId)) {
            throw new ForbiddenException("Нет доступа к этому чату");
        }

        return chatMapper.toResponse(chat);
    }

    public List<ChatResponse> getMyChats() {
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("Получение чатов пользователя: {}", userId);
        return chatMapper.toResponseList(chatDao.findByUserId(userId));
    }

    @Transactional
    public ChatResponse getOrCreateChat(ChatRequest request) {
        Long buyerId = SecurityUtils.getCurrentUserId();
        logger.info("Получение или создание чата для объявления {} покупателем {}", request.getAdId(), buyerId);

        Optional<Chat> existingChat = chatDao.findByAdAndBuyer(
                request.getAdId(), buyerId
        );
        if (existingChat.isPresent()) {
            logger.info("Найден существующий чат: {}", existingChat.get().getId());
            return chatMapper.toResponse(existingChat.get());
        }

        Ad ad = adDao.findById(request.getAdId()).orElseThrow(() -> new ResourceNotFoundException("Объявление не найдено: " + request.getAdId()));

        if (buyerId.equals(ad.getUser().getId())) {
            throw new ForbiddenException("Нельзя создать чат по своему объявлению");
        }

        if (!ad.getIsActive()) {
            throw new BadRequestException("Нельзя создать чат по неактивному объявлению");
        }

        User buyer = userDao.findById(buyerId).orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден: " + buyerId));

        Chat chat = new Chat();
        chat.setAd(ad);
        chat.setBuyer(buyer);
        chat.setSeller(ad.getUser());

        chatDao.save(chat);

        logger.info("Создан чат {} для объявления {} между покупателем {} и продавцом {}", chat.getId(), request.getAdId(), buyerId, ad.getUser().getId());

        return chatMapper.toResponse(chat);
    }
}