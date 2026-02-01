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
import hotel.dto.GuestWithServicesDto;
import hotel.dto.RoomWithGuestsDto;
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
            logger.info("Приложение запущено");
            currentContext.initializeMenu();
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
            logger.info("Следующий день наступил");
            currentContext.initializeMenu();
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
            logger.info("Команда выполнена: displayGuestsCount");
            setExitContext();
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
            logger.info("Команда выполена: displayGuests");
            setExitContext();
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
            logger.info("Команда выполнена: displayGuestService");
            setExitContext();
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
            logger.info("Команда выполнена: displayAvailableRoomsCount");
            setExitContext();
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
            List<RoomWithGuestsDto> rooms = hotelModel.getSortedRooms(sortBy, direction);
            hotelView.displaySortedRooms(rooms, sortBy, direction);
            logger.info("Команда выполнена: displaySortedRooms");
            setExitContext();
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
            logger.info("Команда выполнена: displaySortedAvailableRooms");
            setExitContext();
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
            logger.info("Команда выполнена: displayAvailableRoomsByDate");
            setExitContext();
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
            logger.info("Команда выполнена: displayPreviousGuests");
            setExitContext();
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
            logger.info("Команда выполнена: displayRoomInformation");
            setExitContext();
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
            logger.info("Команда выполнена: displayPricesOfRoomsAndServices");
            setExitContext();
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
            logger.info("Команда выполнена: setRoomPrice");
            setExitContext();
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
            logger.info("Команда выполнена: setRoomAvailable");
            setExitContext();
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
            logger.info("Команда выполнена: setRoomCleaning");
            setExitContext();
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
            logger.info("Команда выполнена: setRoomUnderMaintenance");
            setExitContext();
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
            logger.info("Команда выполнена: checkoutGuest");
            setExitContext();
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
            logger.info("Команда выполнена: addNewRoom");
            setExitContext();
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
            logger.info("Команда выполнена: checkInGuests");
            setExitContext();
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
            logger.info("Команда выполнена: setServicePrice");
            setExitContext();
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
            logger.info("Команда выполнена: addNewService");
            setExitContext();
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
            logger.info("Команда выполнена: addServiceToGuest");
            setExitContext();
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
            List<GuestWithServicesDto> guests = CSVService.importFromCSV(filePath, guestCSVConverter);
            hotelModel.importGuests(guests);
            hotelView.displayImportSuccess("Гости", filePath, guests.size());
            logger.info("Команда выполнена: importGuests");
            setExitContext();
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
            List<GuestWithServicesDto> guestsWithServices = hotelModel.getGuestsForExport();
            CSVService.exportToCSV(guestsWithServices, filePath, guestCSVConverter);
            hotelView.displayExportSuccess("Гости", filePath);
            logger.info("Команда выполнена: exportGuests");
            setExitContext();
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
            List<RoomWithGuestsDto> rooms = CSVService.importFromCSV(filePath, roomCSVConverter);
            hotelModel.importRooms(rooms);
            hotelView.displayImportSuccess("Комнаты", filePath, rooms.size());
            logger.info("Команда выполнена: importRooms");
            setExitContext();
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
            List<RoomWithGuestsDto> roomsWithGuests = hotelModel.getRoomsForExport();
            CSVService.exportToCSV(roomsWithGuests, filePath, roomCSVConverter);
            hotelView.displayExportSuccess("Комнаты", filePath);
            logger.info("Команда выполнена: exportRooms");
            setExitContext();
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
            logger.info("Команда выполнена: importServices");
            setExitContext();
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
            logger.info("Команда выполнена: exportServices");
            setExitContext();
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
