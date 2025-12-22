package contexts;

import annotations.Inject;
import di.ContextFactory;
import hotel.Controller;

import java.util.List;

public class ExitContext extends BaseContext {

    @Inject
    private ContextFactory contextFactory;

    public ExitContext(Controller controller) {
        List<String> actions = List.of("Для того чтобы вернуться в меню отправьте любую строку");

        super(controller, actions);
    }

    @Override
    public void handleInput(String operationIndex) {
        MainMenuContext mainMenuContext = contextFactory.createMainMenuContext();
        controller.setContext(mainMenuContext);
    }
}
