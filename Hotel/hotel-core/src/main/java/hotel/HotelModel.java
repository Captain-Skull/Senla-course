package hotel;

import annotations.Component;
import annotations.Inject;
import annotations.PostConstruct;
import annotations.Singleton;
import contexts.GuestDraft;
import enums.RoomSort;
import enums.RoomType;
import enums.ServiceSort;
import enums.SortDirection;
import enums.GuestSort;
import enums.IdPriceSort;
import enums.RoomStatus;
import exceptions.DaoException;
import exceptions.ImportExportException;
import exceptions.ValidationException;
import hotel.service.HotelService;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Component
@Singleton
public class HotelModel implements Serializable {

    private static final long serialVersionUID = 1111L;

    private String name;

    private LocalDate currentDay;
    @Inject
    private HotelConfig config;

    @Inject
    private transient HotelService hotelService;

    public HotelModel() {
    }

    @PostConstruct
    private void init() {
        if (this.name == null) {
            this.name = config.getHotelName();
        }
        if (this.currentDay == null) {
            this.currentDay = LocalDate.now();
        }
    }

    public String getName() {
        return name;
    }

    public List<Room> getRoomsList() {
        return hotelService.getAllRooms();
    }

    public List<Guest> getGuestsList() {
        return hotelService.getAllGuests();
    }

    public List<Service> getServicesList() {
        return hotelService.getAllServices();
    }

    public List<Guest> getGuestsByRoom(int roomNumber) {
        return hotelService.getGuestsByRoom(roomNumber);
    }

    public List<Guest> initializeGuests(List<GuestDraft> newGuestsDraft) {
        List<Guest> newGuests = new ArrayList<>();
        for (GuestDraft draft : newGuestsDraft) {
            Guest guest = new Guest(null, draft.firstName(), draft.lastName());
            newGuests.add(guest);
        }

        return newGuests;
    }

    public void addService(String name, int price, String description) {
        Service service = new Service(null, name, price, description);
        hotelService.saveService(service);
    }

    public void addRoom(int number, RoomType type, int price, int capacity) {
        Room room = new Room(number, type, price, capacity);
        hotelService.saveRoom(room);
    }

    public LocalDate nextDay() {
        currentDay = currentDay.plusDays(1);
        performEndOfDayOperations();
        return currentDay;
    }

    public LocalDate getCurrentDay() {
        return currentDay;
    }

    public String getRoomInformation(int roomNumber) {
        return getRoomByNumber(roomNumber).getDescription();
    }

    public int getAvailableRoomsCount() {
        return hotelService.getAvailableRooms().size();
    }

    public int getGuestsCount() {
        return hotelService.getAllGuests().size();
    }

    public Guest getGuestById(String guestId) {
        Guest guest = hotelService.getGuestById(guestId);
        if (guest == null) {
            throw new ValidationException("Гость с id " + guestId + " не найден");
        }
        return guest;
    }

    public Room getRoomByNumber(int number) {
        Room room = hotelService.getRoomByNumber(number);
        if (room == null) {
            throw new ValidationException("Комната с номером " + number + " не найдена");
        }
        return room;
    }

    public List<GuestServiceUsage> getGuestServiceUsageList(Guest guest, ServiceSort sortBy, SortDirection direction) {
        List<GuestServiceUsage> usages = hotelService.getGuestServices(guest.getId());
        return sortServices(usages, sortBy, direction);
    }

    public Map<Integer, Room> getSortedAvailableRooms(RoomSort sortBy, SortDirection direction) {
        List<Room> availableRooms = hotelService.getAvailableRooms();
        return sortRooms(availableRooms, sortBy, direction);
    }

    public Map<Integer, Room> getSortedRooms(RoomSort sortBy, SortDirection direction) {
        List<Room> rooms = hotelService.getAllRooms();
        return sortRooms(rooms, sortBy, direction);
    }

    public List<GuestData> getSortedGuests(GuestSort sortBy, SortDirection direction) {
        return sortGuests(sortBy, direction);
    }

    public Map<Integer, Room> getAvailableRoomsByDate(LocalDate date) {
        Map<Integer, Room> availableRoomsByDate = new HashMap<>();
        for (Room room : hotelService.getAllRooms()) {
            LocalDate endDate = room.getEndDate();
            if (endDate == null || date.isAfter(endDate)) {
                availableRoomsByDate.put(room.getNumber(), room);
            }
        }
        return availableRoomsByDate;
    }

    public List<List<RoomGuestHistory>> getPreviousGuests(int roomNumber) {
        return hotelService.getPreviousGuests(roomNumber, config.getRoomHistorySize());
    }

    public List<IdPricePair> getPricesOfRoomsAndServices(IdPriceSort sortBy, SortDirection direction) {
        return sortRoomsAndServices(sortBy, direction);
    }

    public boolean isRoomExists(int roomNumber) {
        return hotelService.getRoomByNumber(roomNumber) != null;
    }

    public boolean checkIn(List<Guest> newGuests, int roomNumber, int days) {
        try {
            return hotelService.checkIn(newGuests, roomNumber, days, currentDay);
        } catch (DaoException e) {
            return false;
        }
    }

    public boolean checkOut(int roomNumber) {
        try {
            return hotelService.checkOut(roomNumber);
        } catch (DaoException e) {
            return false;
        }
    }

    public void addServiceToGuest(String guestId, String serviceId) {
        hotelService.addServiceToGuest(guestId, serviceId, currentDay);
    }

    public boolean setRoomUnderMaintenance(int roomNumber, int days) {
        if (config.isAllowRoomStatusChange()) {
            return hotelService.setRoomUnderMaintenance(roomNumber, currentDay, days);
        }
        return false;
    }

    public boolean setRoomCleaning(int roomNumber) {
        if (config.isAllowRoomStatusChange()) {
            return hotelService.setRoomCleaning(roomNumber, currentDay);
        }
        return false;
    }

    public boolean setRoomAvailable(int roomNumber) {
        if (config.isAllowRoomStatusChange()) {
            return hotelService.setRoomAvailable(roomNumber);
        }
        return false;
    }

    public void setRoomPrice(int roomNumber, int price) {
        hotelService.updateRoomPrice(roomNumber, price);
    }

    public void setServicePrice(String serviceId, int price) {
        hotelService.updateServicePrice(serviceId, price);
    }

    public void importRooms(List<Room> importedRooms) {
        for (Room room : importedRooms) {
            hotelService.saveRoom(room);
        }
    }

    public void importServices(List<Service> importedServices) {
        for (Service service : importedServices) {
            hotelService.saveService(service);
        }
    }

    public void importGuests(List<Guest> importedGuests) {
        boolean isErrorOccurred = false;
        StringBuilder errorRooms = new StringBuilder();

        Map<Integer, List<Guest>> guestsByRoom = importedGuests.stream()
                .filter(guest -> guest.getRoomNumber() > 0)
                .collect(Collectors.groupingBy(Guest::getRoomNumber));

        for (Map.Entry<Integer, List<Guest>> entry : guestsByRoom.entrySet()) {
            int roomNumber = entry.getKey();
            List<Guest> roomGuests = entry.getValue();

            Room room = getRoomByNumber(roomNumber);
            if (room.getStatus() == RoomStatus.AVAILABLE) {
                hotelService.checkIn(roomGuests, roomNumber, 1, currentDay);
            } else if (room.getStatus() == RoomStatus.OCCUPIED) {
                List<Guest> currentGuests = hotelService.getGuestsByRoom(roomNumber);
                if (areGuestGroupsIdentical(roomGuests, currentGuests)) {
                    for (Guest guest : roomGuests) {
                        hotelService.updateGuest(guest);
                    }
                } else {
                    isErrorOccurred = true;
                    errorRooms.append(roomNumber).append(" ");
                }
            } else {
                isErrorOccurred = true;
                errorRooms.append(roomNumber).append(" ");
            }
        }

        if (isErrorOccurred) {
            throw new ImportExportException("Не удалось расселить всех постояльцев. Комнаты: " + errorRooms);
        }
    }

    private boolean areGuestGroupsIdentical(List<Guest> group1, List<Guest> group2) {
        if (group1.size() != group2.size()) {
            return false;
        }

        Set<String> group1Ids = group1.stream()
                .map(Guest::getId)
                .collect(Collectors.toSet());

        Set<String> group2Ids = group2.stream()
                .map(Guest::getId)
                .collect(Collectors.toSet());

        return group1Ids.equals(group2Ids);
    }

    private void performEndOfDayOperations() {
        for (Room room : hotelService.getAllRooms()) {
            LocalDate endDate = room.getEndDate();
            if (endDate != null && endDate.equals(currentDay)) {
                if (room.getStatus() == RoomStatus.OCCUPIED) {
                    checkOut(room.getNumber());
                } else if (room.getStatus() == RoomStatus.CLEANING || room.getStatus() == RoomStatus.MAINTENANCE) {
                    hotelService.setRoomAvailable(room.getNumber());
                }
            }
        }
    }

    private Map<Integer, Room> sortRooms(List<Room> roomsToSort, RoomSort sortBy, SortDirection direction) {
        Comparator<Room> comparator = switch (sortBy) {
            case RoomSort.PRICE -> Comparator.comparingInt(Room::getPrice);
            case RoomSort.CAPACITY -> Comparator.comparingInt(Room::getCapacity);
            case RoomSort.TYPE -> Comparator.comparing(Room::getType);
        };

        if (direction == SortDirection.DESC) {
            comparator = comparator.reversed();
        }

        return roomsToSort.stream()
                .sorted(comparator)
                .collect(Collectors.toMap(
                        Room::getNumber,
                        room -> room,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    private List<GuestData> sortGuests(GuestSort sortBy, SortDirection direction) {
        Comparator<Guest> comparator = switch (sortBy) {
            case GuestSort.NAME -> Comparator.comparing(Guest::getFullName);
            case GuestSort.CHECKOUT_DATE -> Comparator.comparing(guest -> {
                Room room = getRoomByNumber(guest.getRoomNumber());
                return room.getEndDate();
            });
        };

        if (direction == SortDirection.DESC) {
            comparator = comparator.reversed();
        }

        List<Guest> sortedGuests = hotelService.getAllGuests().stream().sorted(comparator).toList();
        return createGuestDataList(sortedGuests);
    }

    private List<GuestData> createGuestDataList(List<Guest> sortedGuests) {
        return sortedGuests.stream()
                .map(guest -> {
                    Room room = getRoomByNumber(guest.getRoomNumber());
                    return new GuestData(
                            guest.getId(),
                            guest.getFullName(),
                            guest.getRoomNumber(),
                            room.getEndDate()
                    );
                })
                .collect(Collectors.toList());
    }

    private List<IdPricePair> sortRoomsAndServices(IdPriceSort sortBy, SortDirection direction) {
        List<IdPricePair> roomsAndServices = new ArrayList<>();
        for (Room room : hotelService.getAllRooms()) {
            String roomId = String.valueOf('R' + room.getNumber());
            roomsAndServices.add(new IdPricePair(roomId, room.getPrice()));
        }
        for (Service service : hotelService.getAllServices()) {
            roomsAndServices.add(new IdPricePair(service.getId(), service.getPrice()));
        }

        Comparator<IdPricePair> comparator = getIdPricePairComparator(sortBy, direction);
        return roomsAndServices.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private static Comparator<IdPricePair> getIdPricePairComparator(IdPriceSort sortBy, SortDirection direction) {
        Comparator<IdPricePair> comparator = switch (sortBy) {
            case IdPriceSort.TYPE -> Comparator.comparing(IdPricePair::getId);
            case IdPriceSort.PRICE -> Comparator.comparingInt(IdPricePair::getPrice);
        };

        if (direction == SortDirection.DESC) {
            comparator = comparator.reversed();
        }

        return comparator;
    }

    private List<GuestServiceUsage> sortServices(List<GuestServiceUsage> usages,
                                                 ServiceSort sortBy,
                                                 SortDirection direction) {
        Comparator<GuestServiceUsage> comparator = switch (sortBy) {
            case PRICE -> Comparator.comparingInt(GuestServiceUsage::getPrice);
            case DATE -> Comparator.comparing(GuestServiceUsage::getUsageDate);
        };

        if (direction == SortDirection.DESC) {
            comparator = comparator.reversed();
        }

        return usages.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }
}
