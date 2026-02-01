package hotel.dao;

import annotations.Component;
import annotations.Singleton;
import hotel.GuestServiceUsage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Component
@Singleton
public class GuestServiceUsageDao extends AbstractJpaDao<GuestServiceUsage, Integer> {

    private static final String FIND_BY_GUEST_JPQL =
            "SELECT u FROM GuestServiceUsage u WHERE u.guest.id = :guestId ORDER BY u.usageDate DESC";

    @Override
    protected Class<GuestServiceUsage> getEntityClass() {
        return GuestServiceUsage.class;
    }

    public List<GuestServiceUsage> findByGuestId(String guestId) {
        EntityManager em = getEntityManager();
        TypedQuery<GuestServiceUsage> query = em.createQuery(FIND_BY_GUEST_JPQL, GuestServiceUsage.class);
        query.setParameter("guestId", guestId);
        return query.getResultList();
    }
}