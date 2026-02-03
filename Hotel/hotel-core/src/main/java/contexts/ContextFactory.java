package contexts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ContextFactory {

    private final ApplicationContext applicationContext;

    @Autowired
    public ContextFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public MainMenuContext createMainMenuContext() {
        return applicationContext.getBean(MainMenuContext.class);
    }

    public RoomManagementContext createRoomManagementContext() {
        return applicationContext.getBean(RoomManagementContext.class);
    }

    public GuestManagementContext createGuestManagementContext() {
        return applicationContext.getBean(GuestManagementContext.class);
    }

    public ServiceManagementContext createServiceManagementContext() {
        return applicationContext.getBean(ServiceManagementContext.class);
    }

    public ImportExportContext createImportExportContext() {
        return applicationContext.getBean(ImportExportContext.class);
    }

    public ExitContext createExitContext() {
        return applicationContext.getBean(ExitContext.class);
    }
}