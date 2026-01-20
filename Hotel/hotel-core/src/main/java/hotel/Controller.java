package hotel;

import annotations.Component;
import annotations.Inject;
import annotations.PostConstruct;
import contexts.BaseContext;
import contexts.ContextFactory;
import contexts.ControllerInterface;
import contexts.GuestDraft;
import enums.GuestSort;
import enums.RoomSort;
import enums.ServiceSort;
import enums.SortDirection;
import enums.IdPriceSort;
import enums.RoomType;
import exceptions.HotelException;
import exceptions.ImportExportException;
import exceptions.ValidationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
public class Controller implements ControllerInterface {

    private static final Logger logger = LogManager.getLogger(Controller.class);

    @Inject
    private HotelModel hotelModel;

    @Inject
    private HotelView hotelView;

    @Inject
    private GuestCSVConverter guestCSVConverter;

    @Inject
    private RoomCSVConverter roomCSVConverter;

    @Inject
    private ServiceCSVConverter serviceCSVConverter;

    @Inject
    private ContextFactory contextFactory;

    private BaseContext currentContext;

    public Controller() {
    }

    @PostConstruct
    private void init() {
        this.currentContext = contextFactory.createMainMenuContext();
    }

    @Override
    public void start() {
        try {
            logger.info("Запуск приложения");
            currentContext.initializeMenu();
            logger.info("Приложение запущено");
        } catch (HotelException e) {
            logger.error("Критическая ошибка при запуске: {}", e.getMessage());
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void setContext(BaseContext newContext) {
        try {
            this.currentContext = newContext;
            currentContext.initializeMenu();
        } catch (HotelException e) {
            logger.error("Ошибка смены контекста: {}", e.getMessage());
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void nextDay() {
        try {
            logger.info("Переход в следующий день");
            LocalDate date = hotelModel.nextDay();
            hotelView.nextDay(date);
            currentContext.initializeMenu();
            logger.info("Следующий день наступил");
        } catch (HotelException e) {
            logger.error("Ошибка при смене дня: {}", e.getMessage());
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void displayGuestsCount() {
        try {
            logger.info("Обработка команды: displayGuestsCount");
            int guestsCount = hotelModel.getGuestsCount();
            hotelView.displayGuestsCount(guestsCount);
            setExitContext();
            logger.info("Команда выполнена: displayGuestsCount");
        } catch (HotelException e) {
            logger.error("Ошибка displayGuestsCount: {}", e.getMessage());
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void displayGuests(GuestSort sortBy, SortDirection direction) {
        try {
            logger.info("Обработка команды: displayGuests");
            List<GuestData> sortedGuests = hotelModel.getSortedGuests(sortBy, direction);
            hotelView.displayGuests(sortedGuests, sortBy, direction);
            setExitContext();
            logger.info("Команда выполена: displayGuests");
        } catch (HotelException e) {
            logger.error("Ошибка displayGuests: {}", e.getMessage());
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public boolean isGuestIdValid(String guestId) {
        try {
            Guest guest = hotelModel.getGuestById(guestId);
            return guest != null;
        } catch (ValidationException e) {
            return false;
        }
    }

    @Override
    public void displayGuestServices(String guestId, ServiceSort sortBy, SortDirection direction) {
        try {
            logger.info("Обработка команды: displayGuestServices");
            Guest guest = hotelModel.getGuestById(guestId);
            List<GuestServiceUsage> guestServiceUsageList = hotelModel.getGuestServiceUsageList(guest, sortBy, direction);
            hotelView.displayGuestServicesSorted(guestServiceUsageList, guest, sortBy, direction);
            setExitContext();
            logger.info("Команда выполнена: displayGuestService");
        } catch (HotelException e) {
            logger.error("Ошибка displayGuestService: {}", e.getMessage());
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void displayAvailableRoomsCount() {
        try {
            logger.info("Обработка команды: displayAvailableRoomsCount");
            int availableRoomsCount = hotelModel.getAvailableRoomsCount();
            hotelView.displayAvailableRoomCount(availableRoomsCount);
            setExitContext();
            logger.info("Команда выполнена: displayAvailableRoomsCount");
        } catch (HotelException e) {
            logger.error("Ошибка displayAvailableRoomsCount: {}", e.getMessage());
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void displaySortedRooms(RoomSort sortBy, SortDirection direction) {
        try {
            logger.info("Обработка команды: displaySortedRooms");
            Map<Integer, Room> rooms = hotelModel.getSortedRooms(sortBy, direction);
            hotelView.displaySortedRooms(rooms, sortBy, direction);
            setExitContext();
            logger.info("Команда выполнена: displaySortedRooms");
        } catch (HotelException e) {
            logger.error("Ошибка displaySortedRooms: {}", e.getMessage());
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void displaySortedAvailableRooms(RoomSort sortBy, SortDirection direction) {
        try {
            logger.info("Обработка команды: displaySortedAvailableRooms");
            Map<Integer, Room> rooms = hotelModel.getSortedAvailableRooms(sortBy, direction);
            hotelView.displaySortedAvailableRooms(rooms, sortBy, direction);
            setExitContext();
            logger.info("Команда выполнена: displaySortedAvailableRooms");
        } catch (HotelException e) {
            logger.error("Ошибка displaySortedAvailableRooms: {}", e.getMessage());
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void displayAvailableRoomsByDate(int days) {
        try {
            logger.info("Обработка команды: displayAvailableRoomsByDate");
            LocalDate currentDate = hotelModel.getCurrentDay();
            LocalDate date = currentDate.plusDays(days);
            Map<Integer, Room> rooms = hotelModel.getAvailableRoomsByDate(date);
            hotelView.displayAvailableRoomsByDate(rooms, date);
            setExitContext();
            logger.info("Команда выполнена: displayAvailableRoomsByDate");
        } catch (HotelException e) {
            logger.error("Ошибка displayAvailableRoomsByDate: {}", e.getMessage());
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void displayPreviousGuests(int roomNumber) {
        try {
            logger.info("Обработка команды: displayPreviousGuests");
            List<List<RoomGuestHistory>> previousGuests = hotelModel.getPreviousGuests(roomNumber);
            hotelView.displayPreviousGuests(previousGuests, roomNumber);
            setExitContext();
            logger.info("Команда выполнена: displayPreviousGuests");
        } catch (HotelException e) {
            logger.error("Ошибка displayPreviousGuests: {}", e.getMessage());
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void displayRoomInformation(int roomNumber) {
        try {
            logger.info("Обработка команды: displayRoomInformation");
            String roomInformation = hotelModel.getRoomInformation(roomNumber);
            hotelView.displayRoomInformation(roomInformation);
            setExitContext();
            logger.info("Команда выполнена: displayRoomInformation");
        } catch (HotelException e) {
            logger.error("Ошибка displayRoomInformation: {}", e.getMessage());
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void displayPricesOfRoomsAndServices(IdPriceSort sortBy, SortDirection direction) {
        try {
            logger.info("Обработка команды: displayPricesOfRoomsAndServices");
            List<IdPricePair> roomsAndServices = hotelModel.getPricesOfRoomsAndServices(sortBy, direction);
            hotelView.displayPricesOfRoomsAndServices(roomsAndServices);
            setExitContext();
            logger.info("Команда выполнена: displayPricesOfRoomsAndServices");
        } catch (HotelException e) {
            logger.error("Ошибка displayPricesOfRoomsAndServices: {}", e.getMessage());
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void setRoomPrice(int roomNumber, int price) {
        try {
            logger.info("Обработка команды: setRoomPrice");
            hotelModel.setRoomPrice(roomNumber, price);
            hotelView.displayNewPriceForRoom(roomNumber, price);
            setExitContext();
            logger.info("Команда выполнена: setRoomPrice");
        } catch (HotelException e) {
            logger.error("Ошибка setRoomPrice: {}", e.getMessage());
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void setRoomAvailable(int roomNumber) {
        try {
            logger.info("Обработка команды: setRoomAvailable");
            boolean success = hotelModel.setRoomAvailable(roomNumber);
            hotelView.displayRoomAvailable(success, roomNumber);
            setExitContext();
            logger.info("Команда выполнена: setRoomAvailable");
        } catch (HotelException e) {
            logger.error("Ошибка setRoomAvailable: {}", e.getMessage());
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void setRoomCleaning(int roomNumber) {
        try {
            logger.info("Обработка команды: setRoomCleaning");
            boolean success = hotelModel.setRoomCleaning(roomNumber);
            hotelView.displayCleaningStarted(success, roomNumber);
            setExitContext();
            logger.info("Команда выполнена: setRoomCleaning");
        } catch (HotelException e) {
            logger.error("Ошибка setRoomCleaning: {}", e.getMessage());
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void setRoomUnderMaintenance(int roomNumber, int days) {
        try {
            logger.info("Обработка команды: setRoomUnderMaintenance");
            boolean success = hotelModel.setRoomUnderMaintenance(roomNumber, days);
            hotelView.displayMaintenanceStarted(success, roomNumber);
            setExitContext();
            logger.info("Команда выполнена: setRoomUnderMaintenance");
        } catch (HotelException e) {
            logger.error("Ошибка setRoomUnderMaintenance: {}", e.getMessage());
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void checkoutGuest(int roomNumber) {
        try {
            logger.info("Обработка команды: checkoutGuest");
            Room room = hotelModel.getRoomByNumber(roomNumber);

            List<Guest> guests = hotelModel.getGuestsByRoom(roomNumber);
            int totalCost = room.calculateCost();

            boolean success = hotelModel.checkOut(roomNumber);

            hotelView.displayCheckout(success, roomNumber, guests, totalCost);
            setExitContext();
            logger.info("Команда выполнена: checkoutGuest");
        } catch (HotelException e) {
            logger.error("Ошибка checkoutGuest: {}", e.getMessage());
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public boolean isRoomExists(int roomNumber) {
        return hotelModel.isRoomExists(roomNumber);
    }

    @Override
    public void addNewRoom(int roomNumber, RoomType roomType, int price, int capacity) {
        try {
            logger.info("Обработка команды: addNewRoom");
            hotelModel.addRoom(roomNumber, roomType, price, capacity);
            hotelView.displayNewRoomAddition(roomNumber);
            setExitContext();
            logger.info("Команда выполнена: addNewRoom");
        } catch (HotelException e) {
            logger.error("Ошибка addNewRoom: {}", e.getMessage());
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public boolean isEnoughCapacity(int roomNumber, int guestsCount) {
        try {
            Room room = hotelModel.getRoomByNumber(roomNumber);
            return room.getCapacity() >= guestsCount;
        } catch (ValidationException e) {
            return false;
        }
    }

    @Override
    public void checkInGuests(int roomNumber, List<GuestDraft> newGuestsDraft, int days) {
        try {
            logger.info("Обработка команды: checkInGuests");
            List<Guest> guests = hotelModel.initializeGuests(newGuestsDraft);
            boolean success = hotelModel.checkIn(guests, roomNumber, days);
            hotelView.displayCheckIn(success, guests, roomNumber);
            setExitContext();
            logger.info("Команда выполнена: checkInGuests");
        } catch (HotelException e) {
            logger.error("Ошибка checkInGuests: {}", e.getMessage());
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void setServicePrice(String serviceId, int servicePrice) {
        try {
            logger.info("Обработка команды: setServicePrice");
            hotelModel.setServicePrice(serviceId, servicePrice);
            hotelView.displayNewPriceForService(serviceId, servicePrice);
            setExitContext();
            logger.info("Команда выполнена: setServicePrice");
        } catch (HotelException e) {
            logger.error("Ошибка setServicePrice: {}", e.getMessage());
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void addNewService(String name, int price, String description) {
        try {
            logger.info("Обработка команды: addNewService");
            hotelModel.addService(name, price, description);
            hotelView.displayNewServiceAddition(name);
            setExitContext();
            logger.info("Команда выполнена: addNewService");
        } catch (HotelException e) {
            logger.error("Ошибка addNewService: {}", e.getMessage());
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void addServiceToGuest(String guestId, String serviceId) {
        try {
            logger.info("Обработка команды: addServiceToGuest");
            Guest guest = hotelModel.getGuestById(guestId);
            hotelModel.addServiceToGuest(guestId, serviceId);
            hotelView.displayAdditionServiceToGuest(guest.getFullName(), serviceId);
            setExitContext();
            logger.info("Команда выполнена: addServiceToGuest");
        } catch (HotelException e) {
            logger.error("Ошибка addServiceToGuest: {}", e.getMessage());
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void importGuests(String filePath) {
        try {
            logger.info("Обработка команды: importGuests");
            List<Guest> guests = CSVService.importFromCSV(filePath, guestCSVConverter);
            hotelModel.importGuests(guests);
            hotelView.displayImportSuccess("Гости", filePath, guests.size());
            setExitContext();
            logger.info("Команда выполнена: importGuests");
        } catch (ImportExportException e) {
            logger.error("Ошибка importGuests: {}", e.getMessage());
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void exportGuests(String filePath) {
        try {
            logger.info("Обработка команды: exportGuests");
            List<Guest> guests = hotelModel.getGuestsList();
            CSVService.exportToCSV(guests, filePath, guestCSVConverter);
            hotelView.displayExportSuccess("Гости", filePath);
            setExitContext();
            logger.info("Команда выполнена: exportGuests");
        } catch (ImportExportException e) {
            logger.error("Ошибка exportGuests: {}", e.getMessage());
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void importRooms(String filePath) {
        try {
            logger.info("Обработка команды: importRooms");
            List<Room> rooms = CSVService.importFromCSV(filePath, roomCSVConverter);
            hotelModel.importRooms(rooms);
            hotelView.displayImportSuccess("Комнаты", filePath, rooms.size());
            setExitContext();
            logger.info("Команда выполнена: importRooms");
        } catch (ImportExportException e) {
            logger.error("Ошибка importRooms: {}", e.getMessage());
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void exportRooms(String filePath) {
        try {
            logger.info("Обработка команды: exportRooms");
            List<Room> rooms = hotelModel.getRoomsList();
            CSVService.exportToCSV(rooms, filePath, roomCSVConverter);
            hotelView.displayExportSuccess("Комнаты", filePath);
            setExitContext();
            logger.info("Команда выполнена: exportRooms");
        } catch (ImportExportException e) {
            logger.error("Ошибка exportRooms: {}", e.getMessage());
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void importServices(String filePath) {
        try {
            logger.info("Обработка команды: importServices");
            List<Service> services = CSVService.importFromCSV(filePath, serviceCSVConverter);
            hotelModel.importServices(services);
            hotelView.displayImportSuccess("Услуги", filePath, services.size());
            setExitContext();
            logger.info("Команда выполнена: importServices");
        } catch (ImportExportException e) {
            logger.error("Ошибка importServices: {}", e.getMessage());
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void exportServices(String filePath) {
        try {
            logger.info("Обработка команды: exportServices");
            List<Service> services = hotelModel.getServicesList();
            CSVService.exportToCSV(services, filePath, serviceCSVConverter);
            hotelView.displayExportSuccess("Услуги", filePath);
            setExitContext();
            logger.info("Команда выполнена: exportServices");
        } catch (ImportExportException e) {
            logger.error("Ошибка exportServices: {}", e.getMessage());
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void saveAndExit() {
        try {
            logger.info("Обработка команды: saveAndExit");
            StatePersistenceService.saveHotelModel(hotelModel);
            logger.info("Команда выполнена: saveAndExit");
            System.exit(0);
        } catch (Exception e) {
            logger.error("Ошибка saveAndExit: {}", e.getMessage());
            hotelView.displayError("Ошибка при сохранении: " + e.getMessage());
            System.exit(1);
        }
    }

    private void setExitContext() {
        setContext(contextFactory.createExitContext());
    }
}
