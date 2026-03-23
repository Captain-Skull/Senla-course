package hotel;

import exceptions.ImportExportException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

public class CSVService {

    public static <T> void exportToCSV(List<T> entities, String filePath, CSVConverter<T> converter) {
        try {
            File file = getFile(filePath);

            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println(converter.getHeaders());

                for (T entity : entities) {
                    writer.println(converter.toCSV(entity));
                }
            }
        } catch (IOException e) {
            throw new ImportExportException("Ошибка при экспорте в CSV: " + e.getMessage(), e);
        }
    }

    private static File getFile(String filePath) throws IOException {
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean dirsCreated = parentDir.mkdirs();
            if (!dirsCreated) {
                throw new ImportExportException("Не удалось создать директорию: " + parentDir.getAbsolutePath());
            }
        }

        if (!file.exists()) {
            boolean fileCreated = file.createNewFile();
            if (!fileCreated) {
                throw new ImportExportException("Не удалось создать файл: " + filePath);
            }
        }
        return file;
    }

    public static <T> List<T> importFromCSV(String filePath, CSVConverter<T> converter) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            reader.readLine();

            return reader.lines()
                    .map(converter::fromCSV)
                    .toList();
        } catch (IOException e) {
            throw new ImportExportException("Ошибка при импорте из CSV: " + e.getMessage(), e);
        }
    }

    public interface CSVConverter<T> {
        String getHeaders();
        String toCSV(T entity);
        T fromCSV(String csvLine);
    }
}
