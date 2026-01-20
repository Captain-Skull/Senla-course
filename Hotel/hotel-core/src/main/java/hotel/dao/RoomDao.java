package hotel.dao;

import annotations.Component;
import annotations.Singleton;
import enums.RoomStatus;
import enums.RoomType;
import hotel.Room;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@Singleton
public class RoomDao extends AbstractDao<Room, Integer> {

    private static final String TABLE_NAME = "rooms";
    private static final String ID_COLUMN = "number";

    private static final String COLUMN_NUMBER = "number";
    private static final String COLUMN_TYPE = "room_type";
    private static final String COLUMN_PRICE = "price";
    private static final String COLUMN_CAPACITY = "capacity";
    private static final String COLUMN_STATUS = "room_status";
    private static final String COLUMN_END_DATE = "end_date";
    private static final String COLUMN_DAYS_UNDER_STATUS = "days_under_status";

    private static final String INSERT_SQL =
            "INSERT INTO rooms (number, room_type, price, capacity, room_status, end_date, days_under_status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL =
            "UPDATE rooms SET room_type = ?, price = ?, capacity = ?, room_status = ?, " +
                    "end_date = ?, days_under_status = ? WHERE number = ?";

    private static final String FIND_BY_STATUS_SQL =
            "SELECT * FROM rooms WHERE room_status = ?";

    private static final String FIND_AVAILABLE_SQL =
            "SELECT * FROM rooms WHERE room_status = 'AVAILABLE'";

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getIdColumn() {
        return ID_COLUMN;
    }

    @Override
    protected String getInsertSql() {
        return INSERT_SQL;
    }

    @Override
    protected String getUpdateSql() {
        return UPDATE_SQL;
    }

    @Override
    protected Integer getId(Room entity) {
        return entity.getNumber();
    }

    @Override
    protected Room mapRow(ResultSet rs) throws SQLException {
        Room room = new Room(
                rs.getInt(COLUMN_NUMBER),
                RoomType.valueOf(rs.getString(COLUMN_TYPE)),
                rs.getInt(COLUMN_PRICE),
                rs.getInt(COLUMN_CAPACITY)
        );

        room.setStatus(RoomStatus.valueOf(rs.getString(COLUMN_STATUS)));

        Date endDate = rs.getDate(COLUMN_END_DATE);
        if (endDate != null) {
            room.setEndDate(endDate.toLocalDate());
        }

        room.setDaysUnderStatus(rs.getInt(COLUMN_DAYS_UNDER_STATUS));

        return room;
    }

    @Override
    protected void setInsertParameters(PreparedStatement stmt, Room room) throws SQLException {
        int idx = 1;
        stmt.setInt(idx++, room.getNumber());
        stmt.setString(idx++, room.getType().name());
        stmt.setInt(idx++, room.getPrice());
        stmt.setInt(idx++, room.getCapacity());
        stmt.setString(idx++, room.getStatus().name());
        stmt.setDate(idx++, room.getEndDate() != null ? Date.valueOf(room.getEndDate()) : null);
        stmt.setInt(idx, room.getDaysUnderStatus());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement stmt, Room room) throws SQLException {
        int idx = 1;
        stmt.setString(idx++, room.getType().name());
        stmt.setInt(idx++, room.getPrice());
        stmt.setInt(idx++, room.getCapacity());
        stmt.setString(idx++, room.getStatus().name());
        stmt.setDate(idx++, room.getEndDate() != null ? Date.valueOf(room.getEndDate()) : null);
        stmt.setInt(idx++, room.getDaysUnderStatus());
        stmt.setInt(idx, room.getNumber());
    }

    public List<Room> findByStatus(RoomStatus status) {
        return executeQuery(FIND_BY_STATUS_SQL, status.name());
    }

    public List<Room> findAvailable() {
        return executeQuery(FIND_AVAILABLE_SQL);
    }
}