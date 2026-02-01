package hotel.dao;

import annotations.Inject;
import exceptions.DaoException;
import hotel.connection.EntityManagerProvider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

public abstract class AbstractJpaDao<T, K> implements GenericDao<T, K> {

    protected static final Logger logger = LogManager.getLogger(AbstractJpaDao.class);

    protected static final String ERROR_FIND_BY_ID = "Ошибка поиска по ID: ";
    protected static final String ERROR_FIND_ALL = "Ошибка получения всех записей";
    protected static final String ERROR_SAVE = "Ошибка сохранения сущности";
    protected static final String ERROR_UPDATE = "Ошибка обновления сущности";
    protected static final String ERROR_DELETE = "Ошибка удаления сущности: ";
    protected static final String ERROR_COUNT = "Ошибка подсчёта записей";

    @Inject
    protected EntityManagerProvider entityManagerProvider;

    protected AbstractJpaDao() {
    }

    protected abstract Class<T> getEntityClass();

    protected EntityManager getEntityManager() {
        return entityManagerProvider.getEntityManager();
    }

    @Override
    public Optional<T> findById(K id) {
        try {
            EntityManager em = getEntityManager();
            T entity = em.find(getEntityClass(), id);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            logger.error(ERROR_FIND_BY_ID + id, e);
            throw new DaoException(ERROR_FIND_BY_ID + id, e);
        }
    }

    @Override
    public List<T> findAll() {
        try {
            EntityManager em = getEntityManager();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> query = cb.createQuery(getEntityClass());
            Root<T> root = query.from(getEntityClass());
            query.select(root);
            return em.createQuery(query).getResultList();
        } catch (Exception e) {
            logger.error(ERROR_FIND_ALL, e);
            throw new DaoException(ERROR_FIND_ALL, e);
        }
    }

    @Override
    public T save(T entity) {
        try {
            EntityManager em = getEntityManager();
            em.persist(entity);
            em.flush();
            logger.debug("Сущность сохранена: {}", entity);
            return entity;
        } catch (Exception e) {
            logger.error(ERROR_SAVE, e);
            throw new DaoException(ERROR_SAVE, e);
        }
    }

    @Override
    public T update(T entity) {
        try {
            EntityManager em = getEntityManager();
            T merged = em.merge(entity);
            em.flush();
            logger.debug("Сущность обновлена: {}", merged);
            return merged;
        } catch (Exception e) {
            logger.error(ERROR_UPDATE, e);
            throw new DaoException(ERROR_UPDATE, e);
        }
    }

    @Override
    public boolean delete(K id) {
        try {
            EntityManager em = getEntityManager();
            T entity = em.find(getEntityClass(), id);
            if (entity != null) {
                em.remove(entity);
                em.flush();
                logger.debug("Сущность удалена с ID: {}", id);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error(ERROR_DELETE + id, e);
            throw new DaoException(ERROR_DELETE + id, e);
        }
    }

    @Override
    public long count() {
        try {
            EntityManager em = getEntityManager();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Long> query = cb.createQuery(Long.class);
            Root<T> root = query.from(getEntityClass());
            query.select(cb.count(root));
            return em.createQuery(query).getSingleResult();
        } catch (Exception e) {
            logger.error(ERROR_COUNT, e);
            throw new DaoException(ERROR_COUNT, e);
        }
    }

    protected List<T> executeQuery(String jpql, Object... params) {
        try {
            EntityManager em = getEntityManager();
            TypedQuery<T> query = em.createQuery(jpql, getEntityClass());
            for (int i = 0; i < params.length; i++) {
                query.setParameter(i + 1, params[i]);
            }
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Ошибка выполнения запроса", e);
            throw new DaoException("Ошибка выполнения запроса", e);
        }
    }

    protected <R> List<R> executeTypedQuery(String jpql, Class<R> resultClass, Object... params) {
        try {
            EntityManager em = getEntityManager();
            TypedQuery<R> query = em.createQuery(jpql, resultClass);
            for (int i = 0; i < params.length; i++) {
                query.setParameter(i + 1, params[i]);
            }
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Ошибка выполнения запроса", e);
            throw new DaoException("Ошибка выполнения запроса", e);
        }
    }
}