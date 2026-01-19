package hotel.connection;

import annotations.Component;
import annotations.PostConstruct;
import annotations.Singleton;
import exceptions.DaoException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Component
@Singleton
public class ConnectionManager {

    private static final String PROPERTIES_PATH = "database.properties";
    private static final String URL_KEY = "db.url";
    private static final String USER_KEY = "db.user";
    private static final String PASSWORD_KEY = "db.password";
    private static final String DRIVER_KEY = "db.driver";

    private static final String ERROR_PROPERTIES_NOT_FOUND = "Файл конфигурации не найден: ";
    private static final String ERROR_LOADING_PROPERTIES = "Ошибка загрузки конфигурации";
    private static final String ERROR_DRIVER_NOT_FOUND = "Драйвер БД не найден";
    private static final String ERROR_CONNECTION = "Ошибка подключения к БД";
    private static final String ERROR_TRANSACTION_ALREADY_ACTIVE = "Транзакция уже активна";
    private static final String ERROR_NO_ACTIVE_TRANSACTION = "Нет активной транзакции";
    private static final String ERROR_BEGIN_TRANSACTION = "Ошибка начала транзакции";
    private static final String ERROR_COMMIT = "Ошибка коммита транзакции";
    private static final String ERROR_ROLLBACK = "Ошибка отката транзакции";

    private static volatile ConnectionManager instance;

    private Properties properties;
    private final ThreadLocal<Connection> transactionConnection = new ThreadLocal<>();

    public ConnectionManager() {
    }

    @PostConstruct
    private void init() {
        this.properties = loadProperties();
        loadDriver();
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(PROPERTIES_PATH)) {
            if (input == null) {
                throw new DaoException(ERROR_PROPERTIES_NOT_FOUND + PROPERTIES_PATH);
            }
            props.load(input);
        } catch (IOException e) {
            throw new DaoException(ERROR_LOADING_PROPERTIES, e);
        }
        return props;
    }

    private void loadDriver() {
        try {
            Class.forName(properties.getProperty(DRIVER_KEY));
        } catch (ClassNotFoundException e) {
            throw new DaoException(ERROR_DRIVER_NOT_FOUND, e);
        }
    }

    public Connection getConnection() {
        Connection txConnection = transactionConnection.get();
        if (txConnection != null) {
            return txConnection;
        }

        try {
            return DriverManager.getConnection(
                    properties.getProperty(URL_KEY),
                    properties.getProperty(USER_KEY),
                    properties.getProperty(PASSWORD_KEY)
            );
        } catch (SQLException e) {
            throw new DaoException(ERROR_CONNECTION, e);
        }
    }

    public void beginTransaction() {
        if (transactionConnection.get() != null) {
            throw new DaoException(ERROR_TRANSACTION_ALREADY_ACTIVE);
        }

        try {
            Connection connection = DriverManager.getConnection(
                    properties.getProperty(URL_KEY),
                    properties.getProperty(USER_KEY),
                    properties.getProperty(PASSWORD_KEY)
            );
            connection.setAutoCommit(false);
            transactionConnection.set(connection);
        } catch (SQLException e) {
            throw new DaoException(ERROR_BEGIN_TRANSACTION, e);
        }
    }

    public void commit() {
        Connection connection = transactionConnection.get();
        if (connection == null) {
            throw new DaoException(ERROR_NO_ACTIVE_TRANSACTION);
        }

        try {
            connection.commit();
        } catch (SQLException e) {
            throw new DaoException(ERROR_COMMIT, e);
        } finally {
            closeTransaction();
        }
    }

    public void rollback() {
        Connection connection = transactionConnection.get();
        if (connection == null) {
            return;
        }

        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DaoException(ERROR_ROLLBACK, e);
        } finally {
            closeTransaction();
        }
    }

    private void closeTransaction() {
        Connection connection = transactionConnection.get();
        if (connection != null) {
            try {
                connection.setAutoCommit(true);
                connection.close();
            } catch (SQLException e) {
            }
            transactionConnection.remove();
        }
    }

    public boolean isTransactionActive() {
        return transactionConnection.get() != null;
    }
}