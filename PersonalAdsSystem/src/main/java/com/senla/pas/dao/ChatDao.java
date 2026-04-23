package com.senla.pas.dao;

import com.senla.pas.entity.Chat;
import com.senla.pas.exception.DaoException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ChatDao extends AbstractJpaDao<Chat, Long> {

    private static final String FIND_BY_USER_ID_JPQL = "SELECT c FROM Chat c WHERE c.buyer.id = :userId OR c.seller.id = :userId";
    private static final String CHECK_USER_ACCESS_JPQL = "SELECT COUNT(c) FROM Chat c WHERE c.user.id = :userId AND c.id = :chatId";
    private static final String FIND_BY_AD_AND_BUYER_JPQL = "SELECT c FROM Chat c WHERE c.ad.id = :adId AND c.buyer.id = :buyerId";

    @Override
    protected Class<Chat> getEntityClass() {
        return Chat.class;
    }

    public Optional<Chat> findByAdAndBuyer(Long adId, Long buyerId) {
        try {
            Chat chat = entityManager.createQuery(FIND_BY_AD_AND_BUYER_JPQL, Chat.class)
                    .setParameter("adId", adId)
                    .setParameter("buyerId", buyerId)
                    .getSingleResult();
            return Optional.of(chat);
        } catch (Exception e) {
            logger.error("Ошибка получения чата по adId и buyerId. Ad ID: {}, Buyer ID: {}", adId, buyerId, e);
            throw new DaoException("Ошибка получения чата по adId и buyerId. Ad ID: " + adId + ", Buyer ID: " + buyerId, e);
        }
    }

    public List<Chat> findByUserId(Long userId) {
        try {
            return entityManager.createQuery(FIND_BY_USER_ID_JPQL, Chat.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } catch (Exception e) {
            logger.error("Ошибка получения чатов по userId. ID: {}", userId, e);
            throw new DaoException("Ошибка получения чатов по userId. ID: " + userId, e);
        }
    }

    public boolean isChatAvailableForUser(Long chatId, Long userId) {
        try {
            Long count = entityManager.createQuery(CHECK_USER_ACCESS_JPQL, Long.class)
                    .setParameter("userId", userId)
                    .setParameter("chatId", chatId)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            logger.error("Ошибка проверки доступа к чату. Chat ID: {}, User ID: {}", chatId, userId, e);
            throw new DaoException("Ошибка проверки доступа к чату. Chat ID: " + chatId + ", User ID: " + userId, e);
        }
    }
}
