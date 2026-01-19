package hotel.dao;

import annotations.Component;
import annotations.Inject;
import annotations.Singleton;
import exceptions.DaoException;
import hotel.Guest;
import hotel.GuestServiceUsage;
import hotel.Service;

import java.sql.*;
import java.util.List;

@Component
@Singleton
public class GuestServiceUsageDao extends AbstractDao<GuestServiceUsage, Integer> {

    private static final String TABLE_NAME = "guest_service_usage";
    private static final String ID_COLUMN = "id";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_GUEST_ID = "guest_id";
    private static final String COLUMN_SERVICE_ID = "service_id";
    private static final String COLUMN_USAGE_DATE = "usage_date";

    private static final String INSERT_SQL =
            "INSERT INTO guest_service_usage (id, guest_id, service_id, usage_date) VALUES (?, ?, ?, ?)";

    private static final String UPDATE_SQL =
            "UPDATE guest_service_usage SET guest_id = ?, service_id = ?, usage_date = ? WHERE id = ?";

    private static final String FIND_BY_GUEST_SQL =
            "SELECT * FROM guest_service_usage WHERE guest_id = ? ORDER BY usage_date DESC";

    private static final String FIND_MAX_ID_SQL =
            "SELECT COALESCE(MAX(id), 0) FROM guest_service_usage";

    @Inject
    private ServiceDao serviceDao;

    @Inject
    private GuestDao guestDao;

    @Override
    protected String getTableName() { return TABLE_NAME; }

    @Override
    protected String getIdColumn() { return ID_COLUMN; }

    @Override
    protected String getInsertSql() { return INSERT_SQL; }

    @Override
    protected String getUpdateSql() { return UPDATE_SQL; }

    @Override
    protected Integer getId(GuestServiceUsage entity) { return entity.getId(); }

    @Override
    protected GuestServiceUsage mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt(COLUMN_ID);
        String guestId = rs.getString(COLUMN_GUEST_ID);
        String serviceId = rs.getString(COLUMN_SERVICE_ID);
        java.time.LocalDate usageDate = rs.getDate(COLUMN_USAGE_DATE).toLocalDate();

        Service service = serviceDao.findById(serviceId).orElse(null);
        Guest guest = guestDao.findById(guestId).orElse(null);

        GuestServiceUsage usage = new GuestServiceUsage(service, usageDate, guest);
        usage.setId(id);
        return usage;
    }

    @Override
    protected void setInsertParameters(PreparedStatement stmt, GuestServiceUsage usage) throws SQLException {
        stmt.setInt(1, usage.getId());
        stmt.setString(2, usage.getGuest().getId());
        stmt.setString(3, usage.getService().getId());
        stmt.setDate(4, Date.valueOf(usage.getUsageDate()));
    }

    @Override
    protected void setUpdateParameters(PreparedStatement stmt, GuestServiceUsage usage) throws SQLException {
        stmt.setString(1, usage.getGuest().getId());
        stmt.setString(2, usage.getService().getId());
        stmt.setDate(3, Date.valueOf(usage.getUsageDate()));
        stmt.setInt(4, usage.getId());
    }

    public List<GuestServiceUsage> findByGuestId(String guestId) {
        return executeQuery(FIND_BY_GUEST_SQL, guestId);
    }

    public int getNextId() {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_MAX_ID_SQL);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка получения следующего ID", e);
        }
        return 1;
    }
}