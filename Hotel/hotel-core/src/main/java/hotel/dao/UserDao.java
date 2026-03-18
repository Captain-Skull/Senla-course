package hotel.dao;

import hotel.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class UserDao {

    @PersistenceContext
    private EntityManager entityManager;

    public Optional<User> findByUsername(String username) {
        try {
            User user = entityManager.createQuery(
                            "SELECT DISTINCT u FROM User u " +
                                    "LEFT JOIN FETCH u.roles r " +
                                    "LEFT JOIN FETCH r.privileges " +
                                    "WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public boolean existsByUsername(String username) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class)
                .setParameter("username", username)
                .getSingleResult();
        return count > 0;
    }

    @Transactional
    public User save(User user) {
        if (user.getId() == null) {
            entityManager.persist(user);
            return user;
        } else {
            return entityManager.merge(user);
        }
    }
}