package contexts;

import enums.GuestSort;
import enums.RoomSort;
import enums.ServiceSort;
import enums.SortDirection;
import enums.IdPriceSort;
import enums.RoomType;

import java.util.List;

public interface ControllerInterface {

    void start();
    void setContext(BaseContext newContext);
    void nextDay();
    void displayGuestsCount();
    void displayGuests(GuestSort sortBy, SortDirection direction);
    boolean isGuestIdValid(String guestId);
    void displayGuestServices(String guestId, ServiceSort sortBy, SortDirection direction);
    void displayAvailableRoomsCount();
    void displaySortedRooms(RoomSort sortBy, SortDirection direction);
    void displaySortedAvailableRooms(RoomSort sortBy, SortDirection direction);
    void displayAvailableRoomsByDate(int days);
    void displayPreviousGuests(int roomNumber);
    void displayRoomInformation(int roomNumber);
    void displayPricesOfRoomsAndServices(IdPriceSort sortBy, SortDirection direction);
    void setRoomPrice(int roomNumber, int price);
    void setRoomAvailable(int roomNumber);
    void setRoomCleaning(int roomNumber);
    void setRoomUnderMaintenance(int roomNumber, int days);
    void checkoutGuest(int roomNumber);
    boolean isRoomExists(int roomNumber);
    void addNewRoom(int roomNumber, RoomType roomType, int price, int capacity);
    boolean isEnoughCapacity(int roomNumber, int guestsCount);
    void checkInGuests(int roomNumber, List<GuestDraft> newGuests, int days);
    void setServicePrice(String serviceId, int servicePrice);
    void addNewService(String name, int price, String description);
    void addServiceToGuest(String guestId, String serviceId);
    void importGuests(String filePath);
    void exportGuests(String filePath);
    void importRooms(String filePath);
    void exportRooms(String filePath);
    void importServices(String filePath);
    void exportServices(String filePath);
    void saveAndExit();
}
