import contexts.*;
import enums.*;
import exceptions.HotelException;
import exceptions.ImportExportException;
import exceptions.ValidationException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller implements ControllerInterface {
    private HotelModel hotelModel;
    private HotelView hotelView;
    private BaseContext currentContext;

    public Controller(HotelModel hotelModel, HotelView hotelView) {
        this.hotelModel  = hotelModel;
        this.hotelView = hotelView;
        this.currentContext = new MainMenuContext(this);
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
            List<GuestServiceUsage> guestServiceUsageList = hotelModel.getGuestServiseUsageList(guest, sortBy, direction);
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
            Map<String, Room> rooms = hotelModel.getSortedRooms(sortBy, direction);
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
            Map<String, Room> rooms = hotelModel.getSortedAvailableRooms(sortBy, direction);
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
            Map<String, Room> rooms = hotelModel.getAvailableRoomsByDate(date);
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
            List<List<Guest>> previousGuests = hotelModel.getPreviousGuests(roomNumber);
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
            boolean success = hotelModel.checkOut(roomNumber);
            hotelView.displayCheckout(success, room);
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
            hotelModel.addServiceToGuest(guest, serviceId);
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
            List<Guest> guests = CSVService.importFromCSV(filePath, new GuestCSVConverter(hotelModel));
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
            CSVService.exportToCSV(guests, filePath, new GuestCSVConverter(hotelModel));
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
            List<Room> rooms = CSVService.importFromCSV(filePath, new RoomCSVConverter(hotelModel));
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
            CSVService.exportToCSV(rooms, filePath, new RoomCSVConverter(hotelModel));
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
            List<Service> services = CSVService.importFromCSV(filePath, new ServiceCSVConverter());
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
            CSVService.exportToCSV(services, filePath, new ServiceCSVConverter());
            hotelView.displayExportSuccess("Услуги", filePath);
            setExitContext();
        } catch (ImportExportException e) {
            hotelView.displayError(e.getMessage());
            setExitContext();
        }
    }

    private void setExitContext() {
        setContext(new ExitContext(this));
    }
}
