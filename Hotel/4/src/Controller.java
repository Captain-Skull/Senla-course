import contexts.*;
import enums.*;

import java.time.LocalDate;
import java.util.List;

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
        currentContext.initializeMenu();
    }

    @Override
    public void setContext(BaseContext newContext) {
        this.currentContext = newContext;
        currentContext.initializeMenu();
    }

    @Override
    public void nextDay() {
        LocalDate date = hotelModel.nextDay();
        hotelView.nextDay(date);
        currentContext.initializeMenu();
    }

    @Override
    public void displayGuestsCount() {
        int guestsCount = hotelModel.getGuestsCount();
        hotelView.displayGuestsCount(guestsCount);
        setExitContext();
    }

    @Override
    public void displayGuests(GuestSort sortBy, SortDirection direction) {
        List<GuestData> sortedGuests = hotelModel.getSortedGuests(sortBy, direction);
        hotelView.displayGuests(sortedGuests, sortBy, direction);
        setExitContext();
    }

    @Override
    public boolean isGuestIdValid(String guestId) {
        Guest guest = hotelModel.getGuestById(guestId);
        return guest != null;
    }

    @Override
    public void displayGuestServices(String guestId, ServiceSort sortBy, SortDirection direction) {
        Guest guest = hotelModel.getGuestById(guestId);
        List<GuestServiceUsage> guestServiceUsageList = hotelModel.getGuestServiseUsageList(guest, sortBy, direction);
        hotelView.displayGuestServicesSorted(guestServiceUsageList, guest, sortBy, direction);
        setExitContext();
    }

    @Override
    public void displayAvailableRoomsCount() {
        int availableRoomsCount = hotelModel.getAvailableRoomsCount();
        hotelView.displayAvailableRoomCount(availableRoomsCount);
        setExitContext();
    }

    @Override
    public void displaySortedRooms(RoomSort sortBy, SortDirection direction) {
        List<Room> rooms = hotelModel.getSortedRooms(sortBy, direction);
        hotelView.displaySortedRooms(rooms, sortBy, direction);
        setExitContext();
    }

    @Override
    public void displaySortedAvailableRooms(RoomSort sortBy, SortDirection direction) {
        List<Room> rooms = hotelModel.getSortedAvailableRooms(sortBy, direction);
        hotelView.displaySortedAvailableRooms(rooms, sortBy, direction);
        setExitContext();
    }

    @Override
    public void displayAvailableRoomsByDate(int days) {
        LocalDate currentDate = hotelModel.getCurrentDay();
        LocalDate date = currentDate.plusDays(days);
        List<Room> rooms = hotelModel.getAvailableRoomsByDate(date);
        hotelView.displayAvailableRoomsByDate(rooms, date);
        setExitContext();
    }

    @Override
    public void displayPreviousGuests(int roomNumber) {
        List<List<Guest>> previousGuests = hotelModel.getPreviousGuests(roomNumber);
        hotelView.displayPreviousGuests(previousGuests, roomNumber);
        setExitContext();
    }

    @Override
    public void displayRoomInformation(int roomNumber) {
        String roomInformation = hotelModel.getRoomInformation(roomNumber);
        hotelView.displayRoomInformation(roomInformation);
        setExitContext();
    }

    @Override
    public void displayPricesOfRoomsAndServices(IdPriceSort sortBy, SortDirection direction) {
        List<IdPricePair> roomsAndServices = hotelModel.getPricesOfRoomsAndServices(sortBy, direction);
        hotelView.displayPricesOfRoomsAndServices(roomsAndServices);
        setExitContext();
    }

    @Override
    public void setRoomPrice(int roomNumber, int price) {
        hotelModel.setRoomPrice(roomNumber, price);
        hotelView.displayNewPriceForRoom(roomNumber, price);
        setExitContext();
    }

    @Override
    public void setRoomAvailable(int roomNumber) {
        boolean success = hotelModel.setRoomAvailable(roomNumber);
        hotelView.displayRoomAvailable(success, roomNumber);
        setExitContext();
    }

    @Override
    public void setRoomCleaning(int roomNumber) {
        boolean success = hotelModel.setRoomCleaning(roomNumber);
        hotelView.displayCleaningStarted(success, roomNumber);
        setExitContext();
    }

    @Override
    public void setRoomUnderMaintenance(int roomNumber, int days) {
        boolean success = hotelModel.setRoomUnderMaintenance(roomNumber, days);
        hotelView.displayMaintenanceStarted(success, roomNumber);
        setExitContext();
    }

    @Override
    public void checkoutGuest(int roomNumber) {
        Room room = hotelModel.getRoomByNumber(roomNumber);
        boolean success = hotelModel.checkOut(roomNumber);
        hotelView.displayCheckout(success, room);
        setExitContext();
    }

    @Override
    public boolean isRoomExists(int roomNumber) {
        return hotelModel.isRoomExists(roomNumber);
    }

    @Override
    public void addNewRoom(int roomNumber, RoomType roomType, int price, int capacity) {
        hotelModel.addNewRoom(roomNumber, roomType, price, capacity);
        hotelView.displayNewRoomAddition(roomNumber);
        setExitContext();
    }

    @Override
    public boolean isEnoughCapacity(int roomNumber, int guestsCount) {
        Room room = hotelModel.getRoomByNumber(roomNumber);
        return room.getCapacity() >= guestsCount;
    }

    @Override
    public void checkInGuests(int roomNumber, List<GuestDraft> newGuestsDraft, int days) {
        List<Guest> guests = hotelModel.initializeGuests(newGuestsDraft);
        boolean success = hotelModel.checkIn(guests, roomNumber, days);
        hotelView.displayCheckIn(success, guests, roomNumber);
        setExitContext();
    }

    @Override
    public void setServicePrice(String serviceId, int servicePrice) {
        hotelModel.setServicePrice(serviceId, servicePrice);
        hotelView.displayNewPriceForService(serviceId, servicePrice);
        setExitContext();
    }

    @Override
    public void addNewService(String name, int price, String description) {
        hotelModel.addService(name, price, description);
        hotelView.displayNewServiceAddition(name);
        setExitContext();
    }

    @Override
    public void addServiceToGuest(String guestId, String serviceId) {
        Guest guest = hotelModel.getGuestById(guestId);
        hotelModel.addServiceToGuest(guest, serviceId);
        hotelView.displayAdditionServiceToGuest(guest.getFullName(), serviceId);
        setExitContext();
    }

    private void setExitContext() {
        setContext(new ExitContext(this));
    }
}
