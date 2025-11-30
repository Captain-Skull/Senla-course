package contexts;

import enums.Context;

import java.util.List;
import java.util.Scanner;

public abstract class BaseContext {
    protected ControllerInterface controller;
    private List<String> actions;
    protected Scanner scanner;

    public BaseContext(ControllerInterface controller, List<String> actions) {
        this.controller = controller;
        this.actions = actions;
        this.scanner = new Scanner(System.in);
    }

    public void initializeMenu() {
        String actionsMenu = getActionsMenu();
        System.out.println(actionsMenu);
        String input = scanner.nextLine();
        handleInput(input);
    }

    private String getActionsMenu() {
        StringBuilder menu = new StringBuilder();
        for (int i = 0; i < actions.size(); i++) {
            menu.append(i+1).append(". ").append(actions.get(i)).append("\n");
        }

        return menu.toString();
    }

    protected void noSuchIndex() {
        System.out.println("Нет такого индекса. Попробуйте еще раз");
    }

    public abstract void handleInput(String operationIndex);
}
