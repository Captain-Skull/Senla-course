package hotel.dao;

import annotations.Component;
import annotations.Singleton;
import exceptions.DaoException;
import hotel.Service;

import java.sql.*;

@Component
@Singleton
public class ServiceDao extends AbstractDao<Service, String> {

    private static final String TABLE_NAME = "services";
    private static final String ID_COLUMN = "id";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PRICE = "price";
    private static final String COLUMN_DESCRIPTION = "description";

    private static final String INSERT_SQL =
            "INSERT INTO services (name, price, description) VALUES (?, ?, ?) RETURNING id";

    private static final String UPDATE_SQL =
            "UPDATE services SET name = ?, price = ?, description = ? WHERE id = ?";

    @Override
    protected String getTableName() { return TABLE_NAME; }

    @Override
    protected String getIdColumn() { return ID_COLUMN; }

    @Override
    protected String getInsertSql() { return INSERT_SQL; }

    @Override
    protected String getUpdateSql() { return UPDATE_SQL; }

    @Override
    protected String getId(Service entity) { return entity.getId(); }

    @Override
    protected Service mapRow(ResultSet rs) throws SQLException {
        return new Service(
                rs.getString(COLUMN_ID),
                rs.getString(COLUMN_NAME),
                rs.getInt(COLUMN_PRICE),
                rs.getString(COLUMN_DESCRIPTION)
        );
    }

    @Override
    protected void setInsertParameters(PreparedStatement stmt, Service service) throws SQLException {
        stmt.setString(1, service.getName());
        stmt.setInt(2, service.getPrice());
        stmt.setString(3, service.getDescription());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement stmt, Service service) throws SQLException {
        stmt.setString(1, service.getName());
        stmt.setInt(2, service.getPrice());
        stmt.setString(3, service.getDescription());
        stmt.setString(4, service.getId());
    }

    @Override
    public Service save(Service entity) {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(getInsertSql())) {

            setInsertParameters(stmt, entity);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String generatedId = rs.getString(1);
                    return new Service(generatedId, entity.getName(), entity.getPrice(), entity.getDescription());
                }
            }
            throw new DaoException(ERROR_SAVE);

        } catch (SQLException e) {
            throw new DaoException(ERROR_SAVE, e);
        }
    }
}