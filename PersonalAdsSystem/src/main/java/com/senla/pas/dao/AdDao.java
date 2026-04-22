package com.senla.pas.dao;

import com.senla.pas.entity.Ad;
import com.senla.pas.enums.AdCategory;
import com.senla.pas.enums.AdSort;
import com.senla.pas.enums.SortDirection;
import com.senla.pas.exception.DaoException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import java.util.ArrayList;
import java.util.List;

public class AdDao extends AbstractJpaDao<Ad, Long> {

    private static final String FIND_BY_CATEGORY_JPQL = "SELECT a FROM Ad a WHERE a.category = :category";
    private static final String FIND_BY_USER_ID_JPQL = "SELECT a FROM Ad a WHERE a.user.id = :userId";
    private static final String CHECK_OWNERSHIP_JPQL = "SELECT COUNT(a) FROM Ad a WHERE a.id = :adId AND a.user.id = :userId";

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

            Order order = getOrder(cb, ad, user, sortBy, direction);
            if (order != null) {
                cq.orderBy(order);
            }

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

    private Order getOrder(CriteriaBuilder cb, Root<Ad> ad, Join<Object, Object> user, AdSort sortBy, SortDirection direction) {
        if (sortBy == null) {
            return cb.desc(ad.get("createdAt"));
        }

        Expression<?> field = sortBy.isUserField()
                ? user.get(sortBy.getFieldName())
                : ad.get(sortBy.getFieldName());

        return direction == SortDirection.ASC
                ? cb.asc(field)
                : cb.desc(field);
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
