package hotel.dao;

import hotel.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class RoleDao {

    @PersistenceContext
    private EntityManager entityManager;

    public Optional<Role> findByName(String name) {
        try {
            Role role = entityManager.createQuery(
                            "SELECT r FROM Role r " +
                                    "LEFT JOIN FETCH r.privileges " +
                                    "WHERE r.name = :name", Role.class)
                    .setParameter("name", name)
                    .getSingleResult();
            return Optional.of(role);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}