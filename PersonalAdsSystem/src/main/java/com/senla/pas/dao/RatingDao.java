package com.senla.pas.dao;

import com.senla.pas.entity.Rating;
import com.senla.pas.exception.DaoException;

public class RatingDao extends AbstractJpaDao<Rating, Long> {

    private static final String EXISTS_BY_REVIEWER_AND_RECIPIENT_JPQL = "SELECT COUNT(r) FROM Rating r WHERE r.reviewer.id = :reviewerId AND r.recipient.id = :recipientId";

    @Override
    protected Class<Rating> getEntityClass() {
        return Rating.class;
    }

    public boolean existsByReviewerIdAndRecipientId(long reviewerId, long recipientId) {
        try {
            Long count = entityManager.createQuery(EXISTS_BY_REVIEWER_AND_RECIPIENT_JPQL, Long.class)
                    .setParameter("reviewerId", reviewerId)
                    .setParameter("recipientId", recipientId)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            logger.error("Ошибка проверки существования рейтинга от пользователя {} к пользователю {}", reviewerId, recipientId, e);
            throw new DaoException("Ошибка проверки существования рейтинга от пользователя " + reviewerId + " к пользователю " + recipientId, e);
        }
    }
}
