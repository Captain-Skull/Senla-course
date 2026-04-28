package com.senla.pas.dao;

import com.senla.pas.entity.User;
import com.senla.pas.enums.SortDirection;
import com.senla.pas.exception.DaoException;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDao extends AbstractJpaDao<User, Long> {

    private static final String FIND_BY_USERNAME_JPQL = "SELECT u FROM User u WHERE u.username = :username";
    private static final String FIND_BY_USERNAME_WITH_ROLES_JPQL = "SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username";
    private static final String FIND_BY_EMAIL_JPQL = "SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email";
    private static final String FIND_BY_USERNAME_OR_EMAIL_JPQL = "SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail";
    private static final String EXISTS_BY_USERNAME_JPQL = "SELECT COUNT(u) FROM User u WHERE u.username = :username";
    private static final String EXISTS_BY_EMAIL_JPQL = "SELECT COUNT(u) FROM User u WHERE u.email = :email";
    private static final String UPDATE_AVERAGE_RATING_JPQL = "UPDATE User u SET u.averageRating = :averageRating WHERE u.id = :userId";
    private static final String UPDATE_ABOUT_ME_JPQL = "UPDATE User u SET u.aboutMe = :aboutMe WHERE u.id = :userId";

    @Override
    protected Class<User> getEntityClass() {
        return User.class;
    }

    public Optional<User> findByUsernameWithRoles(String username) {
        try {
            User user = entityManager.createQuery(FIND_BY_USERNAME_WITH_ROLES_JPQL, User.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Ошибка поиска пользователя по имени с ролями: {}", username, e);
            throw new DaoException("Ошибка поиска пользователя по имени с ролями: " + username, e);
        }
    }

    public Optional<User> findByUsername(String username) {
        try {
            User user = entityManager.createQuery(FIND_BY_USERNAME_JPQL, User.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Ошибка поиска пользователя по имени: {}", username, e);
            throw new DaoException("Ошибка поиска пользователя по имени: " + username, e);
        }
    }

    public Optional<User> findByEmail(String email) {
        try {
            User user = entityManager.createQuery(FIND_BY_EMAIL_JPQL, User.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Ошибка поиска пользователя по email: {}", email, e);
            throw new DaoException("Ошибка поиска пользователя по email: " + email, e);
        }
    }

    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        try {
            User user = entityManager.createQuery(FIND_BY_USERNAME_OR_EMAIL_JPQL, User.class)
                    .setParameter("usernameOrEmail", usernameOrEmail)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Ошибка получения пользователя по почте или имени: {}", usernameOrEmail, e);
            throw new DaoException("Ошибка получения пользователя по почте или имени: " + usernameOrEmail, e);
        }
    }

    public List<User> findFiltered(SortDirection direction, Double minPrice, Double maxPrice) {
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<User> cq = cb.createQuery(User.class);
            Root<User> user = cq.from(User.class);
            List<Predicate> predicates = new ArrayList<>();

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(user.get("averageRating"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(user.get("averageRating"), maxPrice));
            }

            cq.where(predicates.toArray(new Predicate[0]));

            List<Order> orders = new ArrayList<>();
            orders.add(direction == SortDirection.DESC || direction == null
                    ? cb.desc(user.get("averageRating"))
                    : cb.asc(user.get("averageRating"))
            );

            cq.orderBy(orders);

            return entityManager.createQuery(cq).getResultList();
        } catch (Exception e) {
            logger.error("Ошибка при получении отфильтрованных пользователей.", e);
            throw new DaoException("Ошибка при полуении отфильтрованных пользователей.", e);
        }
    }

    public boolean existsByUsername(String username) {
        try {
            Long count = entityManager.createQuery(EXISTS_BY_USERNAME_JPQL, Long.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            logger.error("Ошибка проверки существования пользователя по имени: {}", username, e);
            throw new DaoException("Ошибка проверки существования пользователя по имени: " + username, e);
        }
    }

    public boolean existsByEmail(String email) {
        try {
            Long count = entityManager.createQuery(EXISTS_BY_EMAIL_JPQL, Long.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            logger.error("Ошибка проверки существования пользователя по email: {}", email, e);
            throw new DaoException("Ошибка проверки существования пользователя по email: " + email, e);
        }
    }

    public void updateAverageRating (Long userId, double newRating) {
        try {
            entityManager.createQuery(UPDATE_AVERAGE_RATING_JPQL)
                    .setParameter("averageRating", newRating)
                    .setParameter("userId", userId)
                    .executeUpdate();
            logger.debug("Средний рейтинг пользователя с ID {} обновлен до {}", userId, newRating);
        } catch (Exception e) {
            logger.error("Ошибка обновления среднего рейтинга пользователя с ID: {}", userId, e);
            throw new DaoException("Ошибка обновления среднего рейтинга пользователя с ID: " + userId, e);
        }
    }
}
