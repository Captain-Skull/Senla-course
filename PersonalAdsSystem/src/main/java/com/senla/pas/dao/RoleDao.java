package com.senla.pas.dao;

import com.senla.pas.entity.Role;
import com.senla.pas.exception.DaoException;
import jakarta.persistence.NoResultException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class RoleDao extends AbstractJpaDao<Role, Long>{

    private static final String FIND_BY_NAME_JPQL = "SELECT r FROM Role r WHERE r.name = :name";
    @Override
    protected Class<Role> getEntityClass() {
        return Role.class;
    }

    public Optional<Role> findByName(String name) {
        try {
            Role role = entityManager.createQuery(FIND_BY_NAME_JPQL, Role.class)
                    .setParameter("name", name)
                    .getSingleResult();
            return Optional.of(role);
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Ошибка при поиске роли по названию: {}", name, e);
            throw new DaoException("Ошибка при поиске роли по названию: " + name, e);
        }
    }
}
