package com.senla.pas.dao;

import com.senla.pas.entity.Payment;
import com.senla.pas.exception.DaoException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class PaymentDao extends AbstractJpaDao<Payment, Long> {

    private static final String FIND_ACTIVE_BY_AD_ID_JPQL = "SELECT p FROM Payment p WHERE p.ad.id = :adId AND p.expireAt > :now";
    private static final String FIND_BY_USER_ID_JPQL = "SELECT p FROM Payment p WHERE p.user.id = :userId";

    @Override
    protected Class<Payment> getEntityClass() {
        return Payment.class;
    }

    public List<Payment> findActiveByAdId(Long adId) {
        try {
            return entityManager.createQuery(FIND_ACTIVE_BY_AD_ID_JPQL, Payment.class)
                    .setParameter("adId", adId)
                    .setParameter("now", LocalDateTime.now())
                    .getResultList();
        } catch (Exception e) {
            logger.error("Ошибка получения активных платежей по adId: {}", adId, e);
            throw new DaoException("Ошибка получения активных платежей по adId: " + adId, e);
        }
    }

    public List<Payment> findByUserId(Long userId) {
        try {
            return entityManager.createQuery(FIND_BY_USER_ID_JPQL, Payment.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } catch (Exception e) {
            logger.error("Ошибка получения платежей по userId: {}", userId, e);
            throw new DaoException("Ошибка получения платежей по userId: " + userId, e);
        }
    }
}
