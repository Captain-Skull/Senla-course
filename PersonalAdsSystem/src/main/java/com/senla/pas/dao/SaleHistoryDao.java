package com.senla.pas.dao;

import com.senla.pas.entity.SaleHistory;
import com.senla.pas.exception.DaoException;

import java.util.List;

public class SaleHistoryDao extends AbstractJpaDao<SaleHistory, Long>{

    private static final String FIND_BY_SELLER_ID_JPQL = "SELECT h FROM SaleHistory h WHERE h.seller.id = :sellerId ORDER BY h.soldAt DESC";
    private static final String FIND_BY_BUYER_ID_JPQL = "SELECT h FROM SaleHistory h WHERE h.buyer.id = :buyerId ORDER BY h.soldAt DESC";
    private static final String EXISTS_BY_AD_ID_JPQL = "SELECT COUNT(s) FROM SaleHistory s WHERE s.ad.id = :adId";

    @Override
    protected Class<SaleHistory> getEntityClass() {
        return SaleHistory.class;
    }

    public List<SaleHistory> findBySellerId(Long sellerId) {
        try {
            return entityManager.createQuery(FIND_BY_SELLER_ID_JPQL, SaleHistory.class)
                    .setParameter("sellerId", sellerId)
                    .getResultList();
        } catch (Exception e) {
            logger.error("Ошибка получения истории продаж продавца {}", sellerId, e);
            throw new DaoException("Ошибка получения истории продаж", e);
        }
    }

    public List<SaleHistory> findByBuyerId(Long buyerId) {
        try {
            return entityManager.createQuery(FIND_BY_BUYER_ID_JPQL, SaleHistory.class)
                    .setParameter("buyerId", buyerId)
                    .getResultList();
        } catch (Exception e) {
            logger.error("Ошибка получения истории покупок покупателя {}", buyerId, e);
            throw new DaoException("Ошибка получения истории покупок", e);
        }
    }

    public boolean existsByAdId(Long adId) {
        try {
            Long count = entityManager.createQuery(EXISTS_BY_AD_ID_JPQL, Long.class)
                    .setParameter("adId", adId)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            logger.error("Ошибка проверки существования продажи по объявлению {}", adId, e);
            throw new DaoException("Ошибка проверки существования продажи", e);
        }
    }
}
