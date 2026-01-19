package hotel.dao;

import annotations.Inject;
import exceptions.DaoException;
import hotel.connection.ConnectionManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractDao<T, K> implements GenericDao<T, K> {

    protected static final String SELECT_ALL_TEMPLATE = "SELECT * FROM %s";
    protected static final String SELECT_BY_ID_TEMPLATE = "SELECT * FROM %s WHERE %s = ?";
    protected static final String DELETE_BY_ID_TEMPLATE = "DELETE FROM %s WHERE %s = ?";
    protected static final String COUNT_TEMPLATE = "SELECT COUNT(*) FROM %s";

    protected static final String ERROR_FIND_BY_ID = "Ошибка поиска по ID: ";
    protected static final String ERROR_FIND_ALL = "Ошибка получения всех записей из ";
    protected static final String ERROR_SAVE = "Ошибка сохранения сущности";
    protected static final String ERROR_UPDATE = "Ошибка обновления сущности";
    protected static final String ERROR_UPDATE_NOT_FOUND = "Сущность не найдена для обновления";
    protected static final String ERROR_DELETE = "Ошибка удаления сущности: ";
    protected static final String ERROR_COUNT = "Ошибка подсчёта записей";
    protected static final String ERROR_EXECUTE_QUERY = "Ошибка выполнения запроса";
    protected static final String ERROR_EXECUTE_UPDATE = "Ошибка выполнения обновления";

    @Inject
    protected ConnectionManager connectionManager;

    protected AbstractDao() {
    }

    protected abstract String getTableName();
    protected abstract String getIdColumn();
    protected abstract T mapRow(ResultSet rs) throws SQLException;
    protected abstract String getInsertSql();
    protected abstract String getUpdateSql();
    protected abstract void setInsertParameters(PreparedStatement stmt, T entity) throws SQLException;
    protected abstract void setUpdateParameters(PreparedStatement stmt, T entity) throws SQLException;
    protected abstract K getId(T entity);

    @Override
    public Optional<T> findById(K id) {
        String sql = String.format(SELECT_BY_ID_TEMPLATE, getTableName(), getIdColumn());

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameter(stmt, 1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DaoException(ERROR_FIND_BY_ID + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<T> findAll() {
        String sql = String.format(SELECT_ALL_TEMPLATE, getTableName());
        List<T> result = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DaoException(ERROR_FIND_ALL + getTableName(), e);
        }
        return result;
    }

    @Override
    public T save(T entity) {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(getInsertSql())) {

            setInsertParameters(stmt, entity);
            stmt.executeUpdate();
            return entity;

        } catch (SQLException e) {
            throw new DaoException(ERROR_SAVE, e);
        }
    }

    @Override
    public T update(T entity) {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(getUpdateSql())) {

            setUpdateParameters(stmt, entity);
            int updated = stmt.executeUpdate();

            if (updated == 0) {
                throw new DaoException(ERROR_UPDATE_NOT_FOUND);
            }
            return entity;

        } catch (SQLException e) {
            throw new DaoException(ERROR_UPDATE, e);
        }
    }

    @Override
    public boolean delete(K id) {
        String sql = String.format(DELETE_BY_ID_TEMPLATE, getTableName(), getIdColumn());

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameter(stmt, 1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException(ERROR_DELETE + id, e);
        }
    }

    @Override
    public long count() {
        String sql = String.format(COUNT_TEMPLATE, getTableName());

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new DaoException(ERROR_COUNT, e);
        }
        return 0;
    }

    protected List<T> executeQuery(String sql, Object... params) {
        List<T> result = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                setParameter(stmt, i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DaoException(ERROR_EXECUTE_QUERY, e);
        }
        return result;
    }

    protected int executeUpdate(String sql, Object... params) {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                setParameter(stmt, i + 1, params[i]);
            }
            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DaoException(ERROR_EXECUTE_UPDATE, e);
        }
    }

    protected void setParameter(PreparedStatement stmt, int index, Object value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.NULL);
        } else if (value instanceof String) {
            stmt.setString(index, (String) value);
        } else if (value instanceof Integer) {
            stmt.setInt(index, (Integer) value);
        } else if (value instanceof Long) {
            stmt.setLong(index, (Long) value);
        } else if (value instanceof Boolean) {
            stmt.setBoolean(index, (Boolean) value);
        } else if (value instanceof LocalDate) {
            stmt.setDate(index, Date.valueOf((LocalDate) value));
        } else if (value instanceof Enum) {
            stmt.setString(index, ((Enum<?>) value).name());
        } else {
            stmt.setObject(index, value);
        }
    }
}