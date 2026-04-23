package com.senla.pas.dao;

import com.senla.pas.entity.User;
import com.senla.pas.exception.DaoException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserDao extends AbstractJpaDao<User, Long> {

    private static final String FIND_BY_USERNAME_JPQL = "SELECT u FROM users u WHERE u.username = :username";
    private static final String FIND_BY_USERNAME_WITH_ROLES_JPQL = "SELECT u FROM users u LEFT JOIN FETCH u.roles WHERE u.username = :username";
    private static final String FIND_BY_EMAIL_JPQL = "SELECT u FROM users u LEFT JOIN FETCH u.roles WHERE u.email = :email";
    private static final String EXISTS_BY_USERNAME_JPQL = "SELECT COUNT(u) FROM users u WHERE u.username = :username";
    private static final String EXISTS_BY_EMAIL_JPQL = "SELECT COUNT(u) FROM users u WHERE u.email = :email";
    private static final String UPDATE_AVERAGE_RATING_JPQL = "UPDATE User u SET u.averageRating = :averageRating WHERE u.id = :userId";

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
        } catch (Exception e) {
            logger.error("Ошибка поиска пользователя по имени с ролями: " + username, e);
            throw new DaoException("Ошибка поиска пользователя по имени с ролями: " + username, e);
        }
    }

    public Optional<User> findByUsername(String username) {
        try {
            User user = entityManager.createQuery(FIND_BY_USERNAME_JPQL, User.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return Optional.of(user);
        } catch (Exception e) {
            logger.error("Ошибка поиска пользователя по имени: " + username, e);
            throw new DaoException("Ошибка поиска пользователя по имени: " + username, e);
        }
    }

    public Optional<User> findByEmail(String email) {
        try {
            User user = entityManager.createQuery(FIND_BY_EMAIL_JPQL, User.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return Optional.of(user);
        } catch (Exception e) {
            logger.error("Ошибка поиска пользователя по email: " + email, e);
            throw new DaoException("Ошибка поиска пользователя по email: " + email, e);
        }
    }

    public boolean existsByUsername(String username) {
        try {
            Long count = entityManager.createQuery(EXISTS_BY_USERNAME_JPQL, Long.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            logger.error("Ошибка проверки существования пользователя по имени: " + username, e);
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
            logger.error("Ошибка проверки существования пользователя по email: " + email, e);
            throw new DaoException("Ошибка проверки существования пользователя по email: " + email, e);
        }
    }

    public void updateAverageRating (Long userId, double newRating) {
        try {
            entityManager.createQuery(UPDATE_AVERAGE_RATING_JPQL, Double.class)
                    .setParameter("averageRating", newRating)
                    .setParameter("userId", userId)
                    .executeUpdate();
            logger.debug("Средний рейтинг пользователя с ID {} обновлен до {}", userId, newRating);
        } catch (Exception e) {
            logger.error("Ошибка обновления среднего рейтинга пользователя с ID: " + userId, e);
            throw new DaoException("Ошибка обновления среднего рейтинга пользователя с ID: " + userId, e);
        }
    }
}
