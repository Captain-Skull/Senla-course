package hotel.dao;

import hotel.Guest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class GuestDao extends AbstractJpaDao<Guest, String> {

    private static final String FIND_BY_ROOM_JPQL =
            "SELECT g FROM Guest g WHERE g.roomNumber = :roomNumber";

    private static final String SAVE_GUEST_SQL = "INSERT INTO guests (firstname, lastname, room_number, user_id) " + "VALUES (?1, ?2, ?3, ?4) RETURNING id";

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
                .setParameter(4, entity.getUser() != null ? entity.getUser().getId() : null)
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

    public Optional<Guest> findByUsername(String username) {
        try {
            Guest guest = entityManager.createQuery(
                            "SELECT g FROM Guest g " +
                                    "JOIN g.user u " +
                                    "WHERE u.username = :username", Guest.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return Optional.of(guest);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}