package hotel;

import annotations.Component;
import annotations.Inject;
import annotations.PostConstruct;
import annotations.Singleton;
import di.ContextFactory;
import di.Injector;

@Component
@Singleton
public class HotelApplication {
    @Inject
    private Controller controller;

    @Inject
    private HotelConfig hotelConfig;

    public HotelApplication() {
    }

    public void start() {
        System.out.println(hotelConfig);
        controller.start();
    }

    public static void main(String[] args) {
        HotelModel savedModel = StatePersistenceService.loadHotelModel();

        registerComponents(savedModel);

        Injector.initialize();

        HotelApplication app = Injector.getInstance(HotelApplication.class);
        app.start();
    }

    private static void registerComponents(HotelModel savedModel) {
        Injector.registerComponent(HotelConfig.class);
        Injector.registerComponent(HotelView.class);

        Injector.registerComponent(ContextFactory.class);

        if (savedModel != null) {
            Injector.injectDependencies(savedModel);
            Injector.registerComponent(HotelModel.class, savedModel);
        } else {
            System.out.println("⚠️ No saved state found, will create new HotelModel");
        }

        Injector.registerComponent(GuestCSVConverter.class);
        Injector.registerComponent(RoomCSVConverter.class);
        Injector.registerComponent(ServiceCSVConverter.class);

        Injector.registerComponent(Controller.class);
        Injector.registerComponent(HotelApplication.class);
    }
}