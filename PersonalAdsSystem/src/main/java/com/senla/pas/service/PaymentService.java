package com.senla.pas.service;

import com.senla.pas.dao.AdDao;
import com.senla.pas.dao.PaymentDao;
import com.senla.pas.dao.UserDao;
import com.senla.pas.dto.request.PaymentRequest;
import com.senla.pas.dto.response.PaymentResponse;
import com.senla.pas.entity.Ad;
import com.senla.pas.entity.Payment;
import com.senla.pas.entity.User;
import com.senla.pas.enums.PromotionPlan;
import com.senla.pas.exception.BadRequestException;
import com.senla.pas.exception.ForbiddenException;
import com.senla.pas.exception.ResourceNotFoundException;
import com.senla.pas.mapper.PaymentMapper;
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
public class PaymentService {

    private final PaymentDao paymentDao;
    private final AdDao adDao;
    private final UserDao userDao;
    private final PaymentMapper paymentMapper;
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    public PaymentService(PaymentDao paymentDao, AdDao adDao, UserDao userDao, PaymentMapper paymentMapper) {
        this.paymentDao = paymentDao;
        this.adDao = adDao;
        this.userDao = userDao;
        this.paymentMapper = paymentMapper;
    }

    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentDao.findById(paymentId).orElseThrow(() -> new ResourceNotFoundException("Платеж не найден: " + paymentId));
        Long userId = SecurityUtils.getCurrentUserId();

        if (!userId.equals(payment.getUser().getId()) && !SecurityUtils.hasRole("ROLE_ADMIN")) {
            throw new ForbiddenException("Недостаточно прав для просмотра платежа");
        }

        logger.info("Получение продвижения по ID: {}", paymentId);
        return paymentMapper.toResponse(payment);
    }

    public List<PaymentResponse> getPaymentsByAd(Long adId) {
        Long userId = SecurityUtils.getCurrentUserId();

        Ad ad = adDao.findById(adId).orElseThrow(() -> new ResourceNotFoundException("Объявление не найдено: " + adId));

        if (!ad.getUser().getId().equals(userId) && !SecurityUtils.hasRole("ROLE_ADMIN")) {
            throw new ForbiddenException("Нет доступа к платежам этого объявления");
        }

        logger.info("Получение всех продвижений объявления: {}", adId);
        return paymentMapper.toResponseList(paymentDao.findByAdId(adId));
    }

    public PaymentResponse getActivePaymentByAd(Long adId) {
        Long userId = SecurityUtils.getCurrentUserId();

        Ad ad = adDao.findById(adId).orElseThrow(() -> new ResourceNotFoundException("Объявление не найдено: " + adId));

        if (!ad.getUser().getId().equals(userId) && !SecurityUtils.hasRole("ROLE_ADMIN")) {
            throw new ForbiddenException("Нет доступа к платежам этого объявления");
        }

        logger.info("Получение активного продвижения объявления {}", adId);
        return paymentMapper.toResponse(paymentDao.findActiveByAdId(adId).orElseThrow(() -> new ResourceNotFoundException("Активное продвижение по объявлению не найдено")));
    }

    public List<PaymentResponse> getMyPayments() {
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("Получение собственных продвижений пользователем: {}", userId);
        return paymentMapper.toResponseList(paymentDao.findByUserId(userId));
    }

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        Ad ad = adDao.findByIdWithLock(request.getAdId()).orElseThrow(() -> new ResourceNotFoundException("Объявление не найдено: " + request.getAdId()));

        if (!userId.equals(ad.getUser().getId())) {
            throw new ForbiddenException("Продвигать можно только свое объявление");
        }

        if (!ad.getIsActive()) {
            throw new BadRequestException("Нельзя продвигать неактивное объявление");
        }

        if (ad.getIsPremium()) {
            throw new BadRequestException("У объявления уже есть активное продвижение");
        }

        User user = userDao.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Не найден пользователь: " + userId));

        LocalDateTime now = LocalDateTime.now();
        PromotionPlan plan = request.getPlan();

        Payment payment = new Payment();
        payment.setAd(ad);
        payment.setUser(user);
        payment.setPlan(plan);
        payment.setAmount(plan.getPrice());
        payment.setConfirmedAt(now);
        payment.setExpireAt(now.plusDays(plan.getDays()));

        paymentDao.save(payment);

        adDao.updatePremiumStatus(request.getAdId(), true);

        logger.info("Создан платёж для объявления {} пользователем {}. Тариф: {}, срок: {} дней", request.getAdId(), userId, plan, plan.getDays());

        return paymentMapper.toResponse(payment);
    }
}
