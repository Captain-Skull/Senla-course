package hotel.dao;

import annotations.Component;
import annotations.Singleton;
import hotel.RoomGuestHistory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.List;

@Component
@Singleton
public class RoomGuestHistoryDao extends AbstractJpaDao<RoomGuestHistory, Long> {

    private static final String FIND_BY_ROOM_JPQL =
            "SELECT h FROM RoomGuestHistory h WHERE h.roomNumber = :roomNumber ORDER BY h.groupId DESC";

    private static final String FIND_MAX_GROUP_ID_JPQL =
            "SELECT COALESCE(MAX(h.groupId), 0) FROM RoomGuestHistory h WHERE h.roomNumber = :roomNumber";

    private static final String FIND_DISTINCT_GROUPS_JPQL =
            "SELECT DISTINCT h.groupId FROM RoomGuestHistory h WHERE h.roomNumber = :roomNumber ORDER BY h.groupId DESC";

    private static final String FIND_BY_ROOM_AND_GROUP_JPQL =
            "SELECT h FROM RoomGuestHistory h WHERE h.roomNumber = :roomNumber AND h.groupId = :groupId";

    @Override
    protected Class<RoomGuestHistory> getEntityClass() {
        return RoomGuestHistory.class;
    }

    public List<RoomGuestHistory> findByRoomNumber(int roomNumber) {
        EntityManager em = getEntityManager();
        TypedQuery<RoomGuestHistory> query = em.createQuery(FIND_BY_ROOM_JPQL, RoomGuestHistory.class);
        query.setParameter("roomNumber", roomNumber);
        return query.getResultList();
    }

    public int getNextGroupId(int roomNumber) {
        EntityManager em = getEntityManager();
        TypedQuery<Integer> query = em.createQuery(FIND_MAX_GROUP_ID_JPQL, Integer.class);
        query.setParameter("roomNumber", roomNumber);
        Integer maxGroupId = query.getSingleResult();
        return (maxGroupId != null ? maxGroupId : 0) + 1;
    }

    public List<List<RoomGuestHistory>> getPreviousGuestGroups(int roomNumber, int maxGroups) {
        EntityManager em = getEntityManager();

        TypedQuery<Integer> groupQuery = em.createQuery(FIND_DISTINCT_GROUPS_JPQL, Integer.class);
        groupQuery.setParameter("roomNumber", roomNumber);
        groupQuery.setMaxResults(maxGroups);
        List<Integer> groupIds = groupQuery.getResultList();

        List<List<RoomGuestHistory>> result = new ArrayList<>();

        for (Integer groupId : groupIds) {
            TypedQuery<RoomGuestHistory> historyQuery =
                    em.createQuery(FIND_BY_ROOM_AND_GROUP_JPQL, RoomGuestHistory.class);
            historyQuery.setParameter("roomNumber", roomNumber);
            historyQuery.setParameter("groupId", groupId);
            result.add(historyQuery.getResultList());
        }

        return result;
    }
}