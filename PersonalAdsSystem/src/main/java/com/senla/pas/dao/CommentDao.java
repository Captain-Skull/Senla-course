package com.senla.pas.dao;

import com.senla.pas.entity.Comment;
import com.senla.pas.exception.DaoException;

import java.util.List;

public class CommentDao extends AbstractJpaDao<Comment, Long>{

    private static final String FIND_BY_AD_ID_JPQL = "SELECT c FROM Comment c WHERE c.ad.id = :adId";
    @Override
    protected Class<Comment> getEntityClass() {
        return Comment.class;
    }

    public List<Comment> getCommentsByAdId(Long adId) {
        try {
            return entityManager.createQuery(FIND_BY_AD_ID_JPQL, Comment.class)
                    .setParameter("adId", adId)
                    .getResultList();
        } catch (Exception e) {
            logger.error("Ошибка при получении комментариев для рекламы с ID: {}", adId, e);
            throw new DaoException("Ошибка при получении комментариев для рекламы с ID: " + adId);
        }
    }
}
