package exceptions;

public class DaoException extends HotelException {

    private static final String DEFAULT_MESSAGE = "Ошибка работы с базой данных";

    public DaoException() {
        super(DEFAULT_MESSAGE);
    }

    public DaoException(String message) {
        super(message);
    }

    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }

    public DaoException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause);
    }
}