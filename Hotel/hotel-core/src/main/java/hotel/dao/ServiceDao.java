package hotel.dao;

import annotations.Component;
import annotations.Singleton;
import hotel.Service;

@Component
@Singleton
public class ServiceDao extends AbstractJpaDao<Service, String> {

    private static final String SAVE_SERVICE_SQL = "INSERT INTO services (name, price, description) " + "VALUES (?1, ?2, ?3) RETURNING id";

    @Override
    protected Class<Service> getEntityClass() {
        return Service.class;
    }

    @Override
    public Service save(Service entity) {
        String generatedId = (String) getEntityManager()
                .createNativeQuery(SAVE_SERVICE_SQL)
                .setParameter(1, entity.getName())
                .setParameter(2, entity.getPrice())
                .setParameter(3, entity.getDescription())
                .getSingleResult();

        entity.setId(generatedId);
        return entity;
    }
}