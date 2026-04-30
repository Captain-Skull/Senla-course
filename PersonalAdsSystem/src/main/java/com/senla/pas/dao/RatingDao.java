package com.senla.pas.dao;

import com.senla.pas.entity.Rating;
import com.senla.pas.exception.DaoException;
import jakarta.persistence.NoResultException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class RatingDao extends AbstractJpaDao<Rating, Long> {

    private static final String FIND_BY_REVIEWER_AND_RECIPIENT_JPQL = "SELECT r FROM Rating r WHERE r.reviewer.id = :reviewerId AND r.recipient.id = :recipientId";
    private static final String FIND_BY_RECIPIENT_ID_JPQL = "SELECT r FROM Rating r WHERE r.recipient.id = :recipientId";
    private static final String CALCULATE_AVERAGE_JPQL = "SELECT AVG(r.rating) FROM Rating r WHERE r.recipient.id = :recipientId";

    @Override
    protected Class<Rating> getEntityClass() {
        return Rating.class;
    }

    public List<Rating> findByRecipientId(Long recipientId) {
        try {
            return entityManager.createQuery(FIND_BY_RECIPIENT_ID_JPQL, Rating.class)
                    .setParameter("recipientId", recipientId)
                    .getResultList();
        } catch (Exception e) {
            logger.error("Ошибка при получении отзывов пользователя {}", recipientId);
            throw new DaoException("Ошибка при получении отзывов пользователя " + recipientId);
        }
    }

    public Optional<Rating> findByReviewerIdAndRecipientId(Long reviewerId, Long recipientId) {
        try {
            return Optional.of(entityManager.createQuery(FIND_BY_REVIEWER_AND_RECIPIENT_JPQL, Rating.class)
                    .setParameter("reviewerId", reviewerId)
                    .setParameter("recipientId", recipientId)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Ошибка проверки существования рейтинга от пользователя {} к пользователю {}", reviewerId, recipientId, e);
            throw new DaoException("Ошибка проверки существования рейтинга от пользователя " + reviewerId + " к пользователю " + recipientId, e);
        }
    }

    public Double calculateAverageRating(Long userId) {
        try {
            Double avg = entityManager.createQuery(CALCULATE_AVERAGE_JPQL, Double.class)
                    .setParameter("recipientId", userId)
                    .getSingleResult();
            return avg != null ? avg : 0.0;
        } catch (Exception e) {
            logger.error("Ошибка при подсчете среднего рейтинга пользователя {}", userId);
            throw new DaoException("Ошибка при подсчете среднего рейтинга пользователя " + userId);
        }
    }
}
