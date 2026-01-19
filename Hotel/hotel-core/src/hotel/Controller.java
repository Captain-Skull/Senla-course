package hotel;

import annotations.Component;
import annotations.Inject;
import annotations.PostConstruct;
import contexts.*;
import enums.*;
import exceptions.HotelException;
import exceptions.ImportExportException;
import exceptions.ValidationException;
import di.ContextFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
public class Controller implements ControllerInterface {
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
            currentContext.initializeMenu();
        } catch (HotelException e) {
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
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void nextDay() {
        try {
            LocalDate date = hotelModel.nextDay();
            hotelView.nextDay(date);
            currentContext.initializeMenu();
        } catch (HotelException e) {
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void displayGuestsCount() {
        try {
            int guestsCount = hotelModel.getGuestsCount();
            hotelView.displayGuestsCount(guestsCount);
            setExitContext();
        } catch (HotelException e) {
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void displayGuests(GuestSort sortBy, SortDirection direction) {
        try {
            List<GuestData> sortedGuests = hotelModel.getSortedGuests(sortBy, direction);
            hotelView.displayGuests(sortedGuests, sortBy, direction);
            setExitContext();
        } catch (HotelException e) {
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
            Guest guest = hotelModel.getGuestById(guestId);
            List<GuestServiceUsage> guestServiceUsageList = hotelModel.getGuestServiceUsageList(guest, sortBy, direction);
            hotelView.displayGuestServicesSorted(guestServiceUsageList, guest, sortBy, direction);
            setExitContext();
        } catch (HotelException e) {
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void displayAvailableRoomsCount() {
        try {
            int availableRoomsCount = hotelModel.getAvailableRoomsCount();
            hotelView.displayAvailableRoomCount(availableRoomsCount);
            setExitContext();
        } catch (HotelException e) {
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void displaySortedRooms(RoomSort sortBy, SortDirection direction) {
        try {
            Map<Integer, Room> rooms = hotelModel.getSortedRooms(sortBy, direction);
            hotelView.displaySortedRooms(rooms, sortBy, direction);
            setExitContext();
        } catch (HotelException e) {
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void displaySortedAvailableRooms(RoomSort sortBy, SortDirection direction) {
        try {
            Map<Integer, Room> rooms = hotelModel.getSortedAvailableRooms(sortBy, direction);
            hotelView.displaySortedAvailableRooms(rooms, sortBy, direction);
            setExitContext();
        } catch (HotelException e) {
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void displayAvailableRoomsByDate(int days) {
        try {
            LocalDate currentDate = hotelModel.getCurrentDay();
            LocalDate date = currentDate.plusDays(days);
            Map<Integer, Room> rooms = hotelModel.getAvailableRoomsByDate(date);
            hotelView.displayAvailableRoomsByDate(rooms, date);
            setExitContext();
        } catch (HotelException e) {
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void displayPreviousGuests(int roomNumber) {
        try {
            List<List<RoomGuestHistory>> previousGuests = hotelModel.getPreviousGuests(roomNumber);
            hotelView.displayPreviousGuests(previousGuests, roomNumber);
            setExitContext();
        } catch (HotelException e) {
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void displayRoomInformation(int roomNumber) {
        try {
            String roomInformation = hotelModel.getRoomInformation(roomNumber);
            hotelView.displayRoomInformation(roomInformation);
            setExitContext();
        } catch (HotelException e) {
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void displayPricesOfRoomsAndServices(IdPriceSort sortBy, SortDirection direction) {
        try {
            List<IdPricePair> roomsAndServices = hotelModel.getPricesOfRoomsAndServices(sortBy, direction);
            hotelView.displayPricesOfRoomsAndServices(roomsAndServices);
            setExitContext();
        } catch (HotelException e) {
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void setRoomPrice(int roomNumber, int price) {
        try {
            hotelModel.setRoomPrice(roomNumber, price);
            hotelView.displayNewPriceForRoom(roomNumber, price);
            setExitContext();
        } catch (HotelException e) {
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void setRoomAvailable(int roomNumber) {
        try {
            boolean success = hotelModel.setRoomAvailable(roomNumber);
            hotelView.displayRoomAvailable(success, roomNumber);
            setExitContext();
        } catch (HotelException e) {
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void setRoomCleaning(int roomNumber) {
        try {
            boolean success = hotelModel.setRoomCleaning(roomNumber);
            hotelView.displayCleaningStarted(success, roomNumber);
            setExitContext();
        } catch (HotelException e) {
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void setRoomUnderMaintenance(int roomNumber, int days) {
        try {
            boolean success = hotelModel.setRoomUnderMaintenance(roomNumber, days);
            hotelView.displayMaintenanceStarted(success, roomNumber);
            setExitContext();
        } catch (HotelException e) {
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void checkoutGuest(int roomNumber) {
        try {
            Room room = hotelModel.getRoomByNumber(roomNumber);

            List<Guest> guests = hotelModel.getGuestsByRoom(roomNumber);
            int totalCost = room.calculateCost();

            boolean success = hotelModel.checkOut(roomNumber);

            hotelView.displayCheckout(success, roomNumber, guests, totalCost);
            setExitContext();
        } catch (HotelException e) {
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
            hotelModel.addRoom(roomNumber, roomType, price, capacity);
            hotelView.displayNewRoomAddition(roomNumber);
            setExitContext();
        } catch (HotelException e) {
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
            List<Guest> guests = hotelModel.initializeGuests(newGuestsDraft);
            boolean success = hotelModel.checkIn(guests, roomNumber, days);
            hotelView.displayCheckIn(success, guests, roomNumber);
            setExitContext();
        } catch (HotelException e) {
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void setServicePrice(String serviceId, int servicePrice) {
        try {
            hotelModel.setServicePrice(serviceId, servicePrice);
            hotelView.displayNewPriceForService(serviceId, servicePrice);
            setExitContext();
        } catch (HotelException e) {
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void addNewService(String name, int price, String description) {
        try {
            hotelModel.addService(name, price, description);
            hotelView.displayNewServiceAddition(name);
            setExitContext();
        } catch (HotelException e) {
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void addServiceToGuest(String guestId, String serviceId) {
        try {
            Guest guest = hotelModel.getGuestById(guestId);
            hotelModel.addServiceToGuest(guestId, serviceId);
            hotelView.displayAdditionServiceToGuest(guest.getFullName(), serviceId);
            setExitContext();
        } catch (HotelException e) {
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void importGuests(String filePath) {
        try {
            List<Guest> guests = CSVService.importFromCSV(filePath, guestCSVConverter);
            hotelModel.importGuests(guests);
            hotelView.displayImportSuccess("Гости", filePath, guests.size());
            setExitContext();
        } catch (ImportExportException e) {
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void exportGuests(String filePath) {
        try {
            List<Guest> guests = hotelModel.getGuestsList();
            CSVService.exportToCSV(guests, filePath, guestCSVConverter);
            hotelView.displayExportSuccess("Гости", filePath);
            setExitContext();
        } catch (ImportExportException e) {
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void importRooms(String filePath) {
        try {
            List<Room> rooms = CSVService.importFromCSV(filePath, roomCSVConverter);
            hotelModel.importRooms(rooms);
            hotelView.displayImportSuccess("Комнаты", filePath, rooms.size());
            setExitContext();
        } catch (ImportExportException e) {
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void exportRooms(String filePath) {
        try {
            List<Room> rooms = hotelModel.getRoomsList();
            CSVService.exportToCSV(rooms, filePath, roomCSVConverter);
            hotelView.displayExportSuccess("Комнаты", filePath);
            setExitContext();
        } catch (ImportExportException e) {
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void importServices(String filePath) {
        try {
            List<Service> services = CSVService.importFromCSV(filePath, serviceCSVConverter);
            hotelModel.importServices(services);
            hotelView.displayImportSuccess("Услуги", filePath, services.size());
            setExitContext();
        } catch (ImportExportException e) {
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void exportServices(String filePath) {
        try {
            List<Service> services = hotelModel.getServicesList();
            CSVService.exportToCSV(services, filePath, serviceCSVConverter);
            hotelView.displayExportSuccess("Услуги", filePath);
            setExitContext();
        } catch (ImportExportException e) {
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    @Override
    public void saveAndExit() {
        try {
            StatePersistenceService.saveHotelModel(hotelModel);
            System.exit(0);
        } catch (Exception e) {
            hotelView.displayError("Ошибка при сохранении: " + e.getMessage());
            System.exit(1);
        }
    }

    private void setExitContext() {
        setContext(contextFactory.createExitContext());
    }
}
