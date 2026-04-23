package com.senla.pas.dao;

import com.senla.pas.entity.Ad;
import com.senla.pas.enums.AdCategory;
import com.senla.pas.enums.AdSort;
import com.senla.pas.enums.SortDirection;
import com.senla.pas.exception.DaoException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AdDao extends AbstractJpaDao<Ad, Long> {

    private static final String FIND_BY_CATEGORY_JPQL = "SELECT a FROM Ad a WHERE a.category = :category";
    private static final String FIND_BY_USER_ID_JPQL = "SELECT a FROM Ad a WHERE a.user.id = :userId";
    private static final String CHECK_OWNERSHIP_JPQL = "SELECT COUNT(a) FROM Ad a WHERE a.id = :adId AND a.user.id = :userId";
    private static final String UPDATE_PREMIUM_STATUS_JPQL = "UPDATE Ad a SET a.isPremium = :isPremium WHERE a.id = :adId";
    private static final String DEACTIVATE_EXPIRED_PREMIUM_JPQL = "UPDATE Ad a SET a.isPremium = false WHERE a.isPremium = true AND NOT EXISTS (SELECT p FROM Payment p WHERE p.ad.id = a.id AND p.expireAt > :now)";

    @Override
    protected Class<Ad> getEntityClass() {
        return Ad.class;
    }

    public List<Ad> findByCategory(AdCategory category) {
        try {
            return entityManager.createQuery(FIND_BY_CATEGORY_JPQL, Ad.class)
                    .setParameter("category", category)
                    .getResultList();
        } catch (Exception e) {
            logger.error("Ошибка поиска объявлений по категории: {}", category, e);
            throw new DaoException("Ошибка поиска объявлений по категории: " + category, e);
        }
    }

    public List<Ad> findByUserId(long userId) {
        try {
            return entityManager.createQuery(FIND_BY_USER_ID_JPQL, Ad.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } catch (Exception e) {
            logger.error("Ошибка поиска объявлений по ID пользователя: {}", userId, e);
            throw new DaoException("Ошибка поиска объявлений по ID пользователя: " + userId, e);
        }
    }

    public List<Ad> findWithFilter(
            AdCategory category,
            String searchText,
            Integer minPrice,
            Integer maxPrice,
            Boolean isActive,
            AdSort sortBy,
            SortDirection direction,
            int page,
            int size
    ) {
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Ad> cq = cb.createQuery(Ad.class);
            Root<Ad> ad = cq.from(Ad.class);

            Join<Object, Object> user = ad.join("user", JoinType.LEFT);

            List<Predicate> predicates = buildPredicates(cb, ad, category, searchText, minPrice, maxPrice, isActive);

            cq.where(predicates.toArray(new Predicate[0]));

            AdSort effectiveSortBy = sortBy != null ? sortBy : AdSort.DATE;
            SortDirection effectiveDirection = direction != null ? direction : SortDirection.DESC;

            cq.orderBy(getOrders(cb, ad, user, effectiveSortBy, effectiveDirection));

            TypedQuery<Ad> query = entityManager.createQuery(cq);
            query.setFirstResult(page * size);
            query.setMaxResults(size);

            return query.getResultList();
        } catch (Exception e) {
            logger.error("Ошибка поиска объявлений с фильтром", e);
            throw new DaoException("Ошибка поиска объявлений с фильтром", e);
        }
    }

    public long countWithFilter(
            AdCategory category,
            String searchText,
            Integer minPrice,
            Integer maxPrice,
            Boolean isActive
    ) {
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<Ad> ad = cq.from(Ad.class);

            List<Predicate> predicates = buildPredicates(cb, ad, category, searchText, minPrice, maxPrice, isActive);

            cq.select(cb.count(ad));
            cq.where(predicates.toArray(new Predicate[0]));

            return entityManager.createQuery(cq).getSingleResult();
        } catch (Exception e) {
            logger.error("Ошибка подсчета объявлений с фильтром", e);
            throw new DaoException("Ошибка подсчета объявлений с фильтром", e);
        }
    }

    public boolean isOwner(Long adId, Long userId) {
        try {
            Long count = entityManager.createQuery(CHECK_OWNERSHIP_JPQL, Long.class)
                    .setParameter("adId", adId)
                    .setParameter("userId", userId)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            logger.error("Ошибка проверки владения объявлением. Ad ID: {}, User ID: {}", adId, userId, e);
            throw new DaoException("Ошибка проверки владения объявлением. Ad ID: " + adId + ", User ID: " + userId, e);
        }
    }

    public void updatePremiumStatus(Long adId, Boolean isPremium) {
        try {
            entityManager.createQuery(UPDATE_PREMIUM_STATUS_JPQL)
                    .setParameter("isPremium", isPremium)
                    .setParameter("adId", adId)
                    .executeUpdate();
        } catch (Exception e) {
            logger.error("Ошибка обновления премиум статуса объявления. Ad ID: {}, isPremium: {}", adId, isPremium, e);
            throw new DaoException("Ошибка обновления премиум статуса объявления. Ad ID: " + adId + ", isPremium: " + isPremium, e);
        }
    }

    public int deactivateExpiredPremium() {
        try {
            return entityManager.createQuery(DEACTIVATE_EXPIRED_PREMIUM_JPQL)
                    .setParameter("now", LocalDateTime.now())
                    .executeUpdate();
        } catch (Exception e) {
            logger.error("Ошибка деактивации просроченных премиум объявлений", e);
            throw new DaoException("Ошибка деактивации просроченных премиум объявлений", e);
        }
    }

    private List<Order> getOrders(CriteriaBuilder cb, Root<Ad> ad, Join<Object, Object> user, AdSort sortBy, SortDirection direction) {

        List<Order> orders = new ArrayList<>();

        orders.add(cb.desc(ad.get("isPremium")));

        Expression<?> field = sortBy.isUserField()
                ? user.get(sortBy.getFieldName())
                : ad.get(sortBy.getFieldName());

        orders.add(direction == SortDirection.ASC
                ? cb.asc(field)
                : cb.desc(field));

        if (sortBy != AdSort.RATING) {
            orders.add(cb.desc(user.get("averageRating")));
        }

        if (sortBy != AdSort.DATE) {
            orders.add(cb.desc(ad.get("createdAt")));
        }

        return orders;
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Ad> ad, AdCategory category, String searchText, Integer minPrice, Integer maxPrice, Boolean isActive) {
        List<Predicate> predicates = new ArrayList<>();

        if (isActive != null) {
            predicates.add(cb.equal(ad.get("isActive"), isActive));
        }

        if (category != null) {
            predicates.add(cb.equal(ad.get("category"), category));
        }

        if (searchText != null && !searchText.isEmpty()) {
            String likePattern = "%" + searchText.toLowerCase() + "%";
            Predicate titleLike = cb.like(cb.lower(ad.get("title")), likePattern);
            Predicate descriptionLike = cb.like(cb.lower(ad.get("description")), likePattern);
            predicates.add(cb.or(titleLike, descriptionLike));
        }

        if (minPrice != null) {
            predicates.add(cb.greaterThanOrEqualTo(ad.get("price"), minPrice));
        }

        if (maxPrice != null) {
            predicates.add(cb.lessThanOrEqualTo(ad.get("price"), maxPrice));
        }

        return predicates;
    }
}
