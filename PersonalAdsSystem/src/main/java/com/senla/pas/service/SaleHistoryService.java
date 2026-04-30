package com.senla.pas.service;

import com.senla.pas.dao.AdDao;
import com.senla.pas.dao.ChatDao;
import com.senla.pas.dao.SaleHistoryDao;
import com.senla.pas.dao.UserDao;
import com.senla.pas.dto.request.SaleHistoryRequest;
import com.senla.pas.dto.response.SaleHistoryResponse;
import com.senla.pas.entity.Ad;
import com.senla.pas.entity.Chat;
import com.senla.pas.entity.SaleHistory;
import com.senla.pas.entity.User;
import com.senla.pas.exception.BadRequestException;
import com.senla.pas.exception.ForbiddenException;
import com.senla.pas.exception.ResourceNotFoundException;
import com.senla.pas.mapper.SaleHistoryMapper;
import com.senla.pas.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SaleHistoryService {

    private final SaleHistoryDao saleHistoryDao;
    private final AdDao adDao;
    private final UserDao userDao;
    private final ChatDao chatDao;
    private final SaleHistoryMapper saleHistoryMapper;
    private static final Logger logger = LoggerFactory.getLogger(SaleHistoryService.class);

    @Autowired
    public SaleHistoryService(SaleHistoryDao saleHistoryDao, AdDao adDao, UserDao userDao, ChatDao chatDao, SaleHistoryMapper saleHistoryMapper) {
        this.saleHistoryDao = saleHistoryDao;
        this.adDao = adDao;
        this.userDao = userDao;
        this.chatDao = chatDao;
        this.saleHistoryMapper = saleHistoryMapper;
    }

    public List<SaleHistoryResponse> getMySales() {
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("Получение истории продаж пользователя: {}", userId);
        return saleHistoryMapper.toResponseList(saleHistoryDao.findBySellerId(userId));
    }

    public List<SaleHistoryResponse> getMyPurchases() {
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("Получение истории покупок пользователя: {}", userId);
        return saleHistoryMapper.toResponseList(saleHistoryDao.findByBuyerId(userId));
    }

    public SaleHistoryResponse getSaleById(Long saleId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        SaleHistory sale = saleHistoryDao.findById(saleId).orElseThrow(() -> new ResourceNotFoundException("Запись о продаже не найдена: " + saleId));

        boolean isSeller = currentUserId.equals(sale.getSeller().getId());
        boolean isBuyer = currentUserId.equals(sale.getBuyer().getId());
        boolean isAdmin = SecurityUtils.hasRole("ROLE_ADMIN");

        if (!isSeller && !isBuyer && !isAdmin) {
            throw new ForbiddenException("Нет доступа к этой записи о продаже");
        }

        logger.info("Получена запись о продаже {} пользователем {}", saleId, currentUserId);
        return saleHistoryMapper.toResponse(sale);
    }

    @Transactional
    public SaleHistoryResponse buyViaChat(Long chatId, SaleHistoryRequest request) {
        Long buyerId = SecurityUtils.getCurrentUserId();

        Chat chat = chatDao.findById(chatId).orElseThrow(() -> new ResourceNotFoundException("Чат не найден: " + chatId));

        if (!buyerId.equals(chat.getBuyer().getId())) {
            throw new ForbiddenException("Завершить сделку может только покупатель");
        }

        Ad ad = chat.getAd();

        validateAdForSale(ad, buyerId);

        Integer finalPrice = request.getPrice() != null
                ? request.getPrice()
                : ad.getPrice();

        if (finalPrice > ad.getPrice()) {
            throw new BadRequestException("Цена сделки не может превышать цену объявления: " + ad.getPrice());
        }

        SaleHistory sale = buildSale(ad, chat.getBuyer(), chat.getSeller(), finalPrice);
        saleHistoryDao.save(sale);

        ad.setIsActive(false);
        adDao.update(ad);

        logger.info("Покупка через чат {}. Объявление: {}, покупатель: {}, цена: {}",chatId, ad.getId(), buyerId, finalPrice);
        return saleHistoryMapper.toResponse(sale);
    }

    @Transactional
    public SaleHistoryResponse buyDirectly(Long adId) {
        Long buyerId = SecurityUtils.getCurrentUserId();

        Ad ad = adDao.findById(adId).orElseThrow(() -> new ResourceNotFoundException("Объявление не найдено: " + adId));

        validateAdForSale(ad, buyerId);

        User buyer = userDao.findById(buyerId).orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден: " + buyerId));

        SaleHistory sale = buildSale(ad, buyer, ad.getUser(), ad.getPrice());
        saleHistoryDao.save(sale);

        ad.setIsActive(false);
        adDao.update(ad);

        logger.info("Прямая покупка. Объявление: {}, покупатель: {}, цена: {}", adId, buyerId, ad.getPrice());

        return saleHistoryMapper.toResponse(sale);
    }

    private void validateAdForSale(Ad ad, Long buyerId) {
        if (!ad.getIsActive()) {
            throw new BadRequestException("Объявление неактивно");
        }

        if (buyerId.equals(ad.getUser().getId())) {
            throw new BadRequestException("Нельзя купить своё объявление");
        }

        if (saleHistoryDao.existsByAdId(ad.getId())) {
            throw new BadRequestException("По этому объявлению уже была совершена продажа");
        }
    }

    private SaleHistory buildSale(Ad ad, User buyer, User seller, Integer price) {
        SaleHistory sale = new SaleHistory();
        sale.setAd(ad);
        sale.setAdTitle(ad.getTitle());
        sale.setSeller(seller);
        sale.setBuyer(buyer);
        sale.setPrice(price);
        sale.setSoldAt(LocalDateTime.now());
        return sale;
    }
}
