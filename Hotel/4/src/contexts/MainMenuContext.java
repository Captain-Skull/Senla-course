package contexts;

import enums.Context;

import java.util.Arrays;
import java.util.List;

public class MainMenuContext extends BaseContext{

    public MainMenuContext(ControllerInterface controller) {
        List<String> actions = Arrays.asList(
          "Управление номерами",
          "Управление гостями",
          "Управление услугами",
          "Следующий день",
          "Импорт/экспорт",
          "Выход"
        );

        super(controller, actions);
    }

    @Override
    public void handleInput(String operationIndex) {
        switch (operationIndex) {
            case "1":
                controller.setContext(new RoomManagementContext(controller));
                break;
            case "2":
                controller.setContext(new GuestManagementContext(controller));
                break;
            case "3":
                controller.setContext(new ServiceManagementContext(controller));
                break;
            case "4":
                controller.nextDay();
                break;
            case "5":
                controller.setContext(new ImportExportContext(controller));
            case "6":
                break;
            default:
                System.out.println("Такого кода нет. Попробуйте еще раз");
                String input = scanner.nextLine();
                handleInput(input);
                break;
        }
    }
}
