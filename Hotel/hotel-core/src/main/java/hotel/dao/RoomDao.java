package hotel.dao;

import annotations.Component;
import annotations.Singleton;
import enums.RoomStatus;
import hotel.Room;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Component
@Singleton
public class RoomDao extends AbstractJpaDao<Room, Integer> {

    private static final String FIND_BY_STATUS_JPQL =
            "SELECT r FROM Room r WHERE r.status = ?1";

    private static final String FIND_AVAILABLE_JPQL =
            "SELECT r FROM Room r WHERE r.status = 'AVAILABLE'";

    @Override
    protected Class<Room> getEntityClass() {
        return Room.class;
    }

    public List<Room> findByStatus(RoomStatus status) {
        return executeQuery(FIND_BY_STATUS_JPQL, status);
    }

    public List<Room> findAvailable() {
        EntityManager em = getEntityManager();
        TypedQuery<Room> query = em.createQuery(FIND_AVAILABLE_JPQL, Room.class);
        return query.getResultList();
    }
}