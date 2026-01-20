package hotel;

import annotations.Component;
import annotations.Inject;
import annotations.Singleton;
import contexts.ContextFactory;
import di.Injector;
import hotel.connection.ConnectionManager;
import hotel.dao.RoomDao;
import hotel.dao.GuestDao;
import hotel.dao.GuestServiceUsageDao;
import hotel.dao.RoomGuestHistoryDao;
import hotel.dao.ServiceDao;
import hotel.service.HotelService;

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

        Injector.registerComponent(ConnectionManager.class);
        Injector.registerComponent(RoomDao.class);
        Injector.registerComponent(GuestDao.class);
        Injector.registerComponent(ServiceDao.class);
        Injector.registerComponent(GuestServiceUsageDao.class);
        Injector.registerComponent(RoomGuestHistoryDao.class);

        Injector.registerComponent(HotelService.class);

        Injector.registerComponent(HotelView.class);
        Injector.registerComponent(ContextFactory.class);

        if (savedModel != null) {
            Injector.injectDependencies(savedModel);
            Injector.registerComponent(HotelModel.class, savedModel);
        } else {
            Injector.registerComponent(HotelModel.class);
        }

        Injector.registerComponent(GuestCSVConverter.class);
        Injector.registerComponent(RoomCSVConverter.class);
        Injector.registerComponent(ServiceCSVConverter.class);

        Injector.registerComponent(Controller.class);
        Injector.registerComponent(HotelApplication.class);
    }
}