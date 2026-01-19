package hotel.dao;

import annotations.Component;
import annotations.Singleton;
import exceptions.DaoException;
import hotel.Guest;

import java.sql.*;
import java.util.List;

@Component
@Singleton
public class GuestDao extends AbstractDao<Guest, String> {

    private static final String TABLE_NAME = "guests";
    private static final String ID_COLUMN = "id";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_FIRSTNAME = "firstname";
    private static final String COLUMN_LASTNAME = "lastname";
    private static final String COLUMN_ROOM_NUMBER = "room_number";

    private static final String INSERT_SQL =
            "INSERT INTO guests (firstname, lastname, room_number) VALUES (?, ?, ?) RETURNING id";

    private static final String UPDATE_SQL =
            "UPDATE guests SET firstname = ?, lastname = ?, room_number = ? WHERE id = ?";

    private static final String FIND_BY_ROOM_SQL =
            "SELECT * FROM guests WHERE room_number = ?";

    @Override
    protected String getTableName() { return TABLE_NAME; }

    @Override
    protected String getIdColumn() { return ID_COLUMN; }

    @Override
    protected String getInsertSql() { return INSERT_SQL; }

    @Override
    protected String getUpdateSql() { return UPDATE_SQL; }

    @Override
    protected String getId(Guest entity) { return entity.getId(); }

    @Override
    protected Guest mapRow(ResultSet rs) throws SQLException {
        Guest guest = new Guest(
                rs.getString(COLUMN_ID),
                rs.getString(COLUMN_FIRSTNAME),
                rs.getString(COLUMN_LASTNAME)
        );
        guest.setRoomNumber(rs.getInt(COLUMN_ROOM_NUMBER));
        return guest;
    }

    @Override
    protected void setInsertParameters(PreparedStatement stmt, Guest guest) throws SQLException {
        stmt.setString(1, guest.getFirstName());
        stmt.setString(2, guest.getLastName());
        stmt.setInt(3, guest.getRoomNumber());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement stmt, Guest guest) throws SQLException {
        stmt.setString(1, guest.getFirstName());
        stmt.setString(2, guest.getLastName());
        stmt.setInt(3, guest.getRoomNumber());
        stmt.setString(4, guest.getId());
    }

    @Override
    public Guest save(Guest entity) {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(getInsertSql())) {

            setInsertParameters(stmt, entity);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String generatedId = rs.getString(1);
                    Guest saved = new Guest(generatedId, entity.getFirstName(), entity.getLastName());
                    saved.setRoomNumber(entity.getRoomNumber());
                    return saved;
                }
            }
            throw new DaoException(ERROR_SAVE);

        } catch (SQLException e) {
            throw new DaoException(ERROR_SAVE, e);
        }
    }

    public List<Guest> findByRoomNumber(int roomNumber) {
        return executeQuery(FIND_BY_ROOM_SQL, roomNumber);
    }
}