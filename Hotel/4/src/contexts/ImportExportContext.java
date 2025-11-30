package contexts;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ImportExportContext extends BaseContext {
    private static final String RESOURCES_PATH = "Hotel/4/resources/";

    ImportExportContext(ControllerInterface controller) {
        List<String> actions = Arrays.asList(
                "Импорт гостей",
                "Экспорт гостей",
                "Импорт номеров",
                "Экспорт номеров",
                "Импорт услуг",
                "Экспорт услуг"
        );

        super(controller, actions);
    }

    @Override
    public void handleInput(String operationIndex) {
        System.out.println("Импорт и экспорт происходит с помощью папки resources");
        switch (operationIndex) {
            case "1":
                importGuests();
                break;
            case "2":
                exportGuests();
                break;
            case "3":
                importRooms();
                break;
            case "4":
                exportRooms();
                break;
            case "5":
                importServices();
                break;
            case "6":
                exportServices();
                break;
            default:
                noSuchIndex();
                String input = scanner.nextLine();
                handleInput(input);
                break;
        }
    }

    private void importGuests() {
        String filePath = getFilePath("импорта гостей");
        controller.importGuests(filePath);
    }

    private void exportGuests() {
        String filePath = getFilePath("экспорта гостей");
        controller.exportGuests(filePath);
    }

    private void importRooms() {
        String filePath = getFilePath("импорта комнат");
        controller.importRooms(filePath);
    }

    private void exportRooms() {
        String filePath = getFilePath("экспорта комнат");
        controller.exportRooms(filePath);
    }

    private void importServices() {
        String filePath = getFilePath("импорта услуг");
        controller.importServices(filePath);
    }

    private void exportServices() {
        String filePath = getFilePath("экспорта услуг");
        controller.exportServices(filePath);
    }

    private String getFileNameFromUser(String operation) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите имя файла для " + operation + " (например: guests.csv): ");
        String fileName = scanner.nextLine().trim();

        if (fileName.isEmpty()) {
            System.out.println("Имя файла не может быть пустым");
            return null;
        }

        if (!fileName.toLowerCase().endsWith(".csv")) {
            fileName += ".csv";
        }

        return fileName;
    }

    private String getFilePath(String operation) {
        String fileName = getFileNameFromUser(operation);
        return RESOURCES_PATH + fileName;
    }
}
