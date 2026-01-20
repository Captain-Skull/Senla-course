package contexts;

import annotations.Component;
import di.Injector;
import hotel.Controller;

@Component
public class ContextFactory {

    public ContextFactory() {
    }

    public MainMenuContext createMainMenuContext() {
        Controller controller = Injector.getInstance(Controller.class);
        MainMenuContext context = new MainMenuContext(controller);
        Injector.injectDependencies(context);
        return context;
    }

    public RoomManagementContext createRoomManagementContext() {
        Controller controller = Injector.getInstance(Controller.class);
        RoomManagementContext context = new RoomManagementContext(controller);
        Injector.injectDependencies(context);

        return context;
    }

    public GuestManagementContext createGuestManagementContext() {
        Controller controller = Injector.getInstance(Controller.class);
        GuestManagementContext context = new GuestManagementContext(controller);
        Injector.injectDependencies(context);
        return context;
    }

    public ServiceManagementContext createServiceManagementContext() {
        Controller controller = Injector.getInstance(Controller.class);
        ServiceManagementContext context = new ServiceManagementContext(controller);
        Injector.injectDependencies(context);
        return context;
    }

    public ImportExportContext createImportExportContext() {
        Controller controller = Injector.getInstance(Controller.class);
        ImportExportContext context = new ImportExportContext(controller);
        Injector.injectDependencies(context);
        return context;
    }

    public ExitContext createExitContext() {
        Controller controller = Injector.getInstance(Controller.class);
        ExitContext context = new ExitContext(controller);
        Injector.injectDependencies(context);
        return context;
    }
}