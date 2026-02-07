package contexts;

import hotel.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Scope("prototype")
public class MainMenuContext extends BaseContext {

    private static final List<String> actions = Arrays.asList(
            "Управление номерами",
            "Управление гостями",
            "Управление услугами",
            "Следующий день",
            "Импорт/экспорт",
            "Выход"
    );

    private final ContextFactory contextFactory;

    @Autowired
    public MainMenuContext(Controller controller, ContextFactory contextFactory) {
        super(controller, actions);
        this.contextFactory = contextFactory;
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
