import contexts.GuestDraft;
import enums.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class HotelModel {
    private String name;
    private List<Room> rooms;
    private List<Service> services;
    private Map<String, Guest> guests;
    private Map<String, Guest> previousGuests;
    private int nextGuestIndex = 1;
    private int nextServiceIndex = 1;
    private LocalDate currentDay;


    public HotelModel(String name) {
        this.name = name;
        this.rooms = new ArrayList<>();
        this.services = new ArrayList<>();
        this.guests = new HashMap<>();
        this.previousGuests = new HashMap<>();
        this.currentDay = LocalDate.now();
        initializeHotelData();
    }

    private void initializeHotelData() {
        rooms.add(new Room(101, RoomType.ECONOM, 1000, 1));
        rooms.add(new Room(102, RoomType.ECONOM, 1000, 1));
        rooms.add(new Room(103, RoomType.ECONOM, 1000, 1));
        rooms.add(new Room(104, RoomType.STANDARD, 2500, 2));
        rooms.add(new Room(105, RoomType.STANDARD, 2600, 3));
        rooms.add(new Room(201, RoomType.STANDARD, 2700, 2));
        rooms.add(new Room(202, RoomType.STANDARD, 2800, 2));
        rooms.add(new Room(203, RoomType.STANDARD, 2950, 3));
        rooms.add(new Room(204, RoomType.LUXURY, 5000, 4));
        rooms.add(new Room(301, RoomType.LUXURY, 5200, 4));
        rooms.add(new Room(302, RoomType.LUXURY, 5500, 4));
        rooms.add(new Room(401, RoomType.PRESEDENTIAL, 11000, 6));
        rooms.add(new Room(501, RoomType.PRESEDENTIAL, 15000, 6));

        addService("Завтрак", 200, "Завтрак в 8:00");
        addService("Обед", 300, "Обед в 13:00");
        addService("Ужин", 275, "Ужин в 20:00");
        addService("Тренажерный зал", 200, "Пропуск в тренажерный зал на день");
        addService("SPA-зона", 250, "Пропуск в зону спа на день");
        addService("Бассейн", 150, "Пропуск в бассейн на день");
    }

    public String getName() {
        return name;
    }

    public List<Guest> initializeGuests(List<GuestDraft> newGuestsDraft) {
        List<Guest> newGuests = new ArrayList<>();

        for (GuestDraft draft : newGuestsDraft) {
            String guestId = "G" + nextGuestIndex;
            nextGuestIndex++;

            Guest guest = new Guest(guestId, draft.firstName(), draft.lastName());
            newGuests.add(guest);
        }

        return newGuests;
    }

    public void addService(String name, int price, String description) {
        String serviceIndex = "S" + nextServiceIndex;
        nextServiceIndex++;

        services.add(new Service(serviceIndex, name, price, description));
    }

    public LocalDate nextDay() {
        currentDay = currentDay.plusDays(1);
        performEndOfDayOperations();
        return currentDay;
    }

    public LocalDate getCurrentDay() {
        return currentDay;
    }

    public Room getGuestRoom(Guest guest) {
        return findRoom(guest.getRoomNumber());
    }

    public String getRoomInformation(int roomNumber) {
        return findRoom(roomNumber).getDescription();
    }

    public int getAvailableRoomsCount() {
        int count = 0;
        for (Room room : rooms) {
            if (room.getStatus() == RoomStatus.AVAILABLE) {
                count++;
            }
        }
        return count;
    }

    public int getGuestsCount() {
        return guests.size();
    }

    public Guest getGuestById(String guestId) {
        return guests.get(guestId);
    }

    public Room getRoomByNumber(int number) {
        return findRoom(number);
    }

    public List<GuestServiceUsage> getGuestServiseUsageList(Guest guest, ServiceSort sortBy, SortDirection direction) {
        return guest.getServicesSorted(sortBy, direction);
    }

    public List<Room> getSortedAvailableRooms(RoomSort sortBy, SortDirection direction) {
        return sortRooms(getAvailableRooms(), sortBy, direction);
    }

    public List<Room> getSortedRooms(RoomSort sortBy, SortDirection direction) {
        return sortRooms(rooms, sortBy, direction);
    }

    public List<GuestData> getSortedGuests(GuestSort sortBy, SortDirection direction) {
        return sortGuests(sortBy, direction);
    }

    public List<Room> getAvailableRoomsByDate(LocalDate date) {
        List<Room> availableRoomsByDate = new ArrayList<>();
        for (Room room : rooms) {
            LocalDate endDate = room.getEndDate();
            if (date.isAfter(endDate)) {
                availableRoomsByDate.add(room);
            }
        }

        return availableRoomsByDate;
    }

    public List<List<Guest>> getPreviousGuests(int roomNumber) {
        return findRoom(roomNumber).getPreviousGuests();
    }

    public List<IdPricePair> getPricesOfRoomsAndServices(IdPriceSort sortBy, SortDirection direction) {
        return sortRoomsAndServices(sortBy, direction);
    }

    public boolean isRoomExists(int roomNumber) {
        return rooms.stream()
                .anyMatch(room -> room.getNumber() == roomNumber);
    }

    public boolean checkIn(List<Guest> newGuests, int roomId, int days) {
        Room room = findRoom(roomId);

        for (Guest guest : newGuests) {
            this.guests.put(guest.getId(), guest);
        }

        return room.checkIn(newGuests, currentDay, days);
    }

    public boolean checkOut(int roomNumber) {
        Room room = findRoom(roomNumber);

        List<Guest> roomGuests = new ArrayList<>(room.getGuests());

        if (room.checkOut()) {
            for (Guest guest : roomGuests) {
                String guestId = guest.getId();
                this.guests.remove(guestId);
                guest.setRoomNumber(0);
                previousGuests.put(guestId, guest);
            }
            return true;
        }

        return false;
    }

    public void addServiceToGuest(Guest guest, String serviceId) {
        Service service = findService(serviceId);
        guest.addService(service, currentDay);
    }

    public boolean setRoomUnderMaintenance(int roomId, int days) {
        Room room = findRoom(roomId);
        return room.setUnderMaintenance(currentDay, days);
    }

    public boolean setRoomCleaning(int roomId) {
        Room room = findRoom(roomId);
        return room.setCleaning(currentDay);
    }

    public boolean setRoomAvailable(int roomId) {
        Room room = findRoom(roomId);
        return room.setAvailable();
    }

    public void setRoomPrice(int roomId, int price) {
        Room room = findRoom(roomId);
        if (room == null) {
            System.out.println("Ошибка при поиске команты по номеру");
            return;
        }

        room.setPrice(price);
    }

    public void setServicePrice(String serviceId, int price) {
        Service service = findService(serviceId);

        service.setPrice(price);
    }

    public void addNewRoom(int roomNumber, RoomType roomType, int price, int capacity) {
        rooms.add(new Room(roomNumber, roomType, price, capacity));
    }

    private Room findRoom(int roomId) {
        return rooms.stream()
                .filter(room -> room.getNumber() == roomId)
                .findFirst()
                .orElseThrow();
    }

    private Service findService(String serviceId) {
        return services.stream()
                .filter(service -> Objects.equals(service.getId(), serviceId))
                .findFirst()
                .orElseThrow();
    }

    private List<Room> getAvailableRooms() {
        return rooms.stream()
                .filter(room -> room.getStatus() == RoomStatus.AVAILABLE)
                .collect(Collectors.toList());
    }

    private void performEndOfDayOperations() {
        for (Room room : rooms) {
            if (room.getEndDate().equals(currentDay)) {
                if (room.getStatus() == RoomStatus.OCCUPIED) {
                    checkOut(room.getNumber());
                } else if (room.getStatus() == RoomStatus.CLEANING || room.getStatus() == RoomStatus.MAINTENANCE) {
                    room.setAvailable();
                }
            }
        }
    }

    private List<Room> sortRooms(List<Room> roomsToSort, RoomSort sortBy, SortDirection direction) {
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
                .collect(Collectors.toList());
    }

    private List<GuestData> sortGuests(GuestSort sortBy, SortDirection direction) {
        Comparator<Guest> comparator = switch (sortBy) {
            case GuestSort.NAME -> Comparator.comparing(Guest::getFullName);
            case GuestSort.CHECKOUT_DATE -> Comparator.comparing(guest -> {
                Room room = findRoom(guest.getRoomNumber());
                return room.getEndDate();
            });
        };

        if (direction == SortDirection.DESC) {
            comparator = comparator.reversed();
        }

        List<Guest> sortedGuests = guests.values().stream().sorted(comparator).toList();
        return createGuestDataList(sortedGuests);
    }

    private List<GuestData> createGuestDataList(List<Guest> sortedGuests) {
        return sortedGuests.stream()
                .map(guest -> {
                    Room room = findRoom(guest.getRoomNumber());
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
        for (Room room : rooms) {
            roomsAndServices.add(new IdPricePair(Integer.toString(room.getNumber()), room.getPrice()));
        }
        for (Service service : services) {
            roomsAndServices.add(new IdPricePair(service.getId(), service.getPrice()));
        }

        Comparator<IdPricePair> comparator = getIdPricePairComparator(sortBy, direction);
        return roomsAndServices.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private static Comparator<IdPricePair> getIdPricePairComparator(IdPriceSort sortBy, SortDirection direction) {
        Comparator<IdPricePair> comparator = switch (sortBy) {
            case IdPriceSort.TYPE-> Comparator.comparing(IdPricePair::getId);
            case IdPriceSort.PRICE -> Comparator.comparingInt(IdPricePair::getPrice);
        };

        if (direction == SortDirection.DESC) {
            comparator = comparator.reversed();
        }

        return comparator;
    }
}
