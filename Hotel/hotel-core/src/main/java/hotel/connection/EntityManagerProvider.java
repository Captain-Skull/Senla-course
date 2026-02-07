package hotel.connection;

import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PreDestroy;
import exceptions.DaoException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Component
public class EntityManagerProvider {

    private static final Logger logger = LogManager.getLogger(EntityManagerProvider.class);
    @Value("${persistence.unit.name:hotelPU}")
    private String PERSISTENCE_UNIT_NAME;

    private EntityManagerFactory entityManagerFactory;
    private final ThreadLocal<EntityManager> entityManagerHolder = new ThreadLocal<>();

    public EntityManagerProvider() {
    }

    @PostConstruct
    public void init() {
        try {
            entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
            logger.info("EntityManagerFactory создан успешно");
        } catch (Exception e) {
            logger.error("Ошибка создания EntityManagerFactory", e);
            throw new DaoException("Не удалось создать EntityManagerFactory", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        closeEntityManager();
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    public EntityManager getEntityManager() {
        EntityManager em = entityManagerHolder.get();
        if (em == null || !em.isOpen()) {
            em = entityManagerFactory.createEntityManager();
            entityManagerHolder.set(em);
        }
        return em;
    }

    public void beginTransaction() {
        EntityManager em = getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        if (!transaction.isActive()) {
            transaction.begin();
            logger.debug("Транзакция начата");
        }
    }

    public void commit() {
        EntityManager em = entityManagerHolder.get();
        if (em != null) {
            EntityTransaction transaction = em.getTransaction();
            if (transaction.isActive()) {
                try {
                    transaction.commit();
                    logger.debug("Транзакция зафиксирована");
                } catch (Exception e) {
                    logger.error("Ошибка фиксации транзакции", e);
                    rollback();
                    throw new DaoException("Ошибка фиксации транзакции", e);
                }
            }
        }
    }

    public void rollback() {
        EntityManager em = entityManagerHolder.get();
        if (em != null) {
            EntityTransaction transaction = em.getTransaction();
            if (transaction.isActive()) {
                try {
                    transaction.rollback();
                    logger.debug("Транзакция откачена");
                } catch (Exception e) {
                    logger.error("Ошибка отката транзакции", e);
                }
            }
        }
    }

    public void closeEntityManager() {
        EntityManager em = entityManagerHolder.get();
        if (em != null && em.isOpen()) {
            em.close();
            entityManagerHolder.remove();
            logger.debug("EntityManager закрыт");
        }
    }
}