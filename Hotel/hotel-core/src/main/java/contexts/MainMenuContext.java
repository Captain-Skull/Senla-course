package contexts;

import annotations.Inject;
import hotel.Controller;

import java.util.Arrays;
import java.util.List;

public class MainMenuContext extends BaseContext {

    private static final List<String> actions = Arrays.asList(
            "Управление номерами",
            "Управление гостями",
            "Управление услугами",
            "Следующий день",
            "Импорт/экспорт",
            "Выход"
    );

    @Inject
    private Controller controller;

    @Inject
    private ContextFactory contextFactory;

    public MainMenuContext(Controller controller) {
        super(controller, actions);
    }

    @Override
    public void handleInput(String operationIndex) {
        switch (operationIndex) {
            case "1":
                controller.setContext(contextFactory.createRoomManagementContext());
                break;
            case "2":
                controller.setContext(contextFactory.createGuestManagementContext());
                break;
            case "3":
                controller.setContext(contextFactory.createServiceManagementContext());
                break;
            case "4":
                controller.nextDay();
                break;
            case "5":
                controller.setContext(contextFactory.createImportExportContext());
            case "6":
                controller.saveAndExit();
                break;
            default:
                System.out.println("Такого кода нет. Попробуйте еще раз");
                String input = scanner.nextLine();
                handleInput(input);
                break;
        }
    }
}
