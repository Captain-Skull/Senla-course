package hotel.dao;

import annotations.Component;
import annotations.Singleton;
import exceptions.DaoException;
import hotel.RoomGuestHistory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
@Singleton
public class RoomGuestHistoryDao extends AbstractDao<RoomGuestHistory, String> {

    private static final String TABLE_NAME = "room_guests_history";
    private static final String ID_COLUMN = "id";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_FIRSTNAME = "firstname";
    private static final String COLUMN_LASTNAME = "lastname";
    private static final String COLUMN_ROOM_NUMBER = "room_number";
    private static final String COLUMN_GROUP_ID = "guest_group_id";

    private static final String INSERT_SQL =
            "INSERT INTO room_guests_history (id, firstname, lastname, room_number, guest_group_id) " +
                    "VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL =
            "UPDATE room_guests_history SET firstname = ?, lastname = ?, room_number = ?, " +
                    "guest_group_id = ? WHERE id = ?";

    private static final String FIND_BY_ROOM_SQL =
            "SELECT * FROM room_guests_history WHERE room_number = ? ORDER BY guest_group_id DESC";

    private static final String FIND_NEXT_GROUP_ID_SQL =
            "SELECT COALESCE(MAX(guest_group_id), 0) + 1 FROM room_guests_history WHERE room_number = ?";

    private static final String FIND_GROUPS_SQL =
            "SELECT DISTINCT guest_group_id FROM room_guests_history WHERE room_number = ? " +
                    "ORDER BY guest_group_id DESC LIMIT ?";

    private static final String FIND_BY_GROUP_SQL =
            "SELECT * FROM room_guests_history WHERE room_number = ? AND guest_group_id = ?";

    @Override
    protected String getTableName() { return TABLE_NAME; }

    @Override
    protected String getIdColumn() { return ID_COLUMN; }

    @Override
    protected String getInsertSql() { return INSERT_SQL; }

    @Override
    protected String getUpdateSql() { return UPDATE_SQL; }

    @Override
    protected String getId(RoomGuestHistory entity) { return entity.getId(); }

    @Override
    protected RoomGuestHistory mapRow(ResultSet rs) throws SQLException {
        return new RoomGuestHistory(
                rs.getString(COLUMN_ID),
                rs.getString(COLUMN_FIRSTNAME),
                rs.getString(COLUMN_LASTNAME),
                rs.getInt(COLUMN_ROOM_NUMBER),
                rs.getInt(COLUMN_GROUP_ID)
        );
    }

    @Override
    protected void setInsertParameters(PreparedStatement stmt, RoomGuestHistory h) throws SQLException {
        stmt.setString(1, h.getId());
        stmt.setString(2, h.getFirstname());
        stmt.setString(3, h.getLastname());
        stmt.setInt(4, h.getRoomNumber());
        stmt.setInt(5, h.getGuestGroupId());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement stmt, RoomGuestHistory h) throws SQLException {
        stmt.setString(1, h.getFirstname());
        stmt.setString(2, h.getLastname());
        stmt.setInt(3, h.getRoomNumber());
        stmt.setInt(4, h.getGuestGroupId());
        stmt.setString(5, h.getId());
    }

    public List<RoomGuestHistory> findByRoomNumber(int roomNumber) {
        return executeQuery(FIND_BY_ROOM_SQL, roomNumber);
    }

    public int getNextGroupId(int roomNumber) {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_NEXT_GROUP_ID_SQL)) {

            stmt.setInt(1, roomNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка получения ID группы", e);
        }
        return 1;
    }

    public List<List<RoomGuestHistory>> getPreviousGuestGroups(int roomNumber, int maxGroups) {
        List<List<RoomGuestHistory>> result = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement groupStmt = conn.prepareStatement(FIND_GROUPS_SQL)) {

            groupStmt.setInt(1, roomNumber);
            groupStmt.setInt(2, maxGroups);

            try (ResultSet groupRs = groupStmt.executeQuery()) {
                while (groupRs.next()) {
                    int groupId = groupRs.getInt(1);

                    try (PreparedStatement guestStmt = conn.prepareStatement(FIND_BY_GROUP_SQL)) {
                        guestStmt.setInt(1, roomNumber);
                        guestStmt.setInt(2, groupId);

                        List<RoomGuestHistory> group = new ArrayList<>();
                        try (ResultSet guestRs = guestStmt.executeQuery()) {
                            while (guestRs.next()) {
                                group.add(mapRow(guestRs));
                            }
                        }
                        result.add(group);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка получения истории", e);
        }
        return result;
    }
}