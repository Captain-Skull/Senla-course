package exceptions;

public class ImportExportException extends HotelException {
    public ImportExportException(String message) {
        super(message);
    }

    public ImportExportException(String message, Throwable cause) {
        super(message, cause);
    }
}