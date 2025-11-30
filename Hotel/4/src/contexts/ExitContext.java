package contexts;

import java.util.Arrays;
import java.util.List;

public class ExitContext extends BaseContext {
    public ExitContext(ControllerInterface controller) {
        List<String> actions = List.of("Для того чтобы вернуться в меню отправьте любую строку");

        super(controller, actions);
    }

    @Override
    public void handleInput(String operationIndex) {
        controller.setContext(new MainMenuContext(controller));
    }
}
