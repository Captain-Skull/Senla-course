package hotel.dao;

import annotations.Component;
import annotations.Singleton;
import hotel.Guest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Component
@Singleton
public class GuestDao extends AbstractJpaDao<Guest, String> {

    private static final String FIND_BY_ROOM_JPQL =
            "SELECT g FROM Guest g WHERE g.roomNumber = :roomNumber";

    private static final String SAVE_GUEST_SQL = "INSERT INTO guests (firstname, lastname, room_number) " + "VALUES (?1, ?2, ?3) RETURNING id";

    @Override
    protected Class<Guest> getEntityClass() {
        return Guest.class;
    }

    @Override
    public Guest save(Guest entity) {
        String generatedId = (String) getEntityManager()
                .createNativeQuery(SAVE_GUEST_SQL)
                .setParameter(1, entity.getFirstName())
                .setParameter(2, entity.getLastName())
                .setParameter(3, entity.getRoomNumber())
                .getSingleResult();

        entity.setId(generatedId);
        return entity;
    }

    public List<Guest> findByRoomNumber(int roomNumber) {
        EntityManager em = getEntityManager();
        TypedQuery<Guest> query = em.createQuery(FIND_BY_ROOM_JPQL, Guest.class);
        query.setParameter("roomNumber", roomNumber);
        return query.getResultList();
    }
}