package contexts;

import hotel.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
public class ExitContext extends BaseContext {

    private static final List<String> actions = List.of("Для того чтобы вернуться в меню отправьте любую строку");

    private final ContextFactory contextFactory;

    @Autowired
    public ExitContext(Controller controller, ContextFactory contextFactory) {
        super(controller, actions);
        this.contextFactory = contextFactory;
    }

    @Override
    public void handleInput(String operationIndex) {
        MainMenuContext mainMenuContext = contextFactory.createMainMenuContext();
        controller.setContext(mainMenuContext);
    }
}
