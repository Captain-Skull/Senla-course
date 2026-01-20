package contexts;

import annotations.Inject;
import hotel.Controller;

import java.util.List;

public class ExitContext extends BaseContext {

    private static final List<String> actions = List.of("Для того чтобы вернуться в меню отправьте любую строку");

    @Inject
    private ContextFactory contextFactory;

    public ExitContext(Controller controller) {
        super(controller, actions);
    }

    @Override
    public void handleInput(String operationIndex) {
        MainMenuContext mainMenuContext = contextFactory.createMainMenuContext();
        controller.setContext(mainMenuContext);
    }
}
