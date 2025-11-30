import contexts.GuestDraft;
import enums.*;
import exceptions.ImportExportException;
import exceptions.ValidationException;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class HotelModel {
    private String name;
//    private List<Room> rooms;
//    private List<Service> services;
    private Map<String, Room> rooms;
    private Map<String, Service> services;
    private Map<String, Guest> guests;
    private Map<String, Guest> previousGuests;
    private LocalDate currentDay;


    public HotelModel(String name) {
        this.name = name;
        this.rooms = new HashMap<>();
        this.services = new HashMap<>();
        this.guests = new HashMap<>();
        this.previousGuests = new HashMap<>();
        this.currentDay = LocalDate.now();
        initializeHotelData();
    }

    private void initializeHotelData() {
        addRoom(101, RoomType.ECONOM, 1000, 1);
        addRoom(102, RoomType.ECONOM, 1000, 1);
        addRoom(103, RoomType.ECONOM, 1000, 1);
        addRoom(104, RoomType.STANDARD, 2500, 2);
        addRoom(105, RoomType.STANDARD, 2600, 3);
        addRoom(201, RoomType.STANDARD, 2700, 2);
        addRoom(202, RoomType.STANDARD, 2800, 2);
        addRoom(203, RoomType.STANDARD, 2950, 3);
        addRoom(204, RoomType.LUXURY, 5000, 4);
        addRoom(301, RoomType.LUXURY, 5200, 4);
        addRoom(302, RoomType.LUXURY, 5500, 4);
        addRoom(401, RoomType.PRESEDENTIAL, 11000, 6);
        addRoom(501, RoomType.PRESEDENTIAL, 15000, 6);

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
        List<String> allGuestIds = new ArrayList<>();
        allGuestIds.addAll(guests.keySet());
        allGuestIds.addAll(previousGuests.keySet());
        int nextGuestIndex = getNextIndex(allGuestIds);

        for (GuestDraft draft : newGuestsDraft) {
            String guestId = "G" + nextGuestIndex;
            nextGuestIndex++;

            Guest guest = new Guest(guestId, draft.firstName(), draft.lastName());
            newGuests.add(guest);
        }

        return newGuests;
    }

    public void addService(String name, int price, String description) {
        int nextServiceIndex = getNextIndex(new ArrayList<>(services.keySet()));
        String serviceId = "S" + nextServiceIndex;

        services.put(serviceId, new Service(serviceId, name, price, description));
    }

    public void addRoom(int number, RoomType type, int price, int capacity) {
        String roomId = "R" + number;
        Room room = new Room(roomId, number, type, price, capacity);
        this.rooms.put(roomId, room);
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
        String roomId = "R" + roomNumber;
        return rooms.get(roomId).getDescription();
    }

    public int getAvailableRoomsCount() {
        int count = 0;
        for (Room room : rooms.values()) {
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
        Guest guest = guests.get(guestId);
        if (guest == null) {
            throw  new ValidationException("Гостя с id " + guestId + " не найдено");
        }
        return guests.get(guestId);
    }

    public Room getRoomByNumber(int number) {
        String roomId = "R" + number;
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new ValidationException("Комната с номером " + number + " не найдено");
        }
        return room;
    }

    public List<GuestServiceUsage> getGuestServiseUsageList(Guest guest, ServiceSort sortBy, SortDirection direction) {
        return guest.getServicesSorted(sortBy, direction);
    }

    public Map<String, Room> getSortedAvailableRooms(RoomSort sortBy, SortDirection direction) {
        return sortRooms(getAvailableRooms(), sortBy, direction);
    }

    public Map<String, Room> getSortedRooms(RoomSort sortBy, SortDirection direction) {
        return sortRooms(rooms, sortBy, direction);
    }

    public List<GuestData> getSortedGuests(GuestSort sortBy, SortDirection direction) {
        return sortGuests(sortBy, direction);
    }

    public Map<String, Room> getAvailableRoomsByDate(LocalDate date) {
        Map<String, Room> availableRoomsByDate = new HashMap<>();
        for (Room room : rooms.values()) {
            LocalDate endDate = room.getEndDate();
            if (date.isAfter(endDate)) {
                availableRoomsByDate.put(room.getId(), room);
            }
        }

        return availableRoomsByDate;
    }

    public List<List<Guest>> getPreviousGuests(int roomNumber) {
        return getRoomByNumber(roomNumber).getPreviousGuests();
    }

    public Map<String, Guest> getPreviousGuests() {
        return previousGuests;
    }

    public Map<String, Guest> getGuests() {
        return guests;
    }

    public List<IdPricePair> getPricesOfRoomsAndServices(IdPriceSort sortBy, SortDirection direction) {
        return sortRoomsAndServices(sortBy, direction);
    }

    public Service getServiceById(String serviceId) {
        Service service = services.get(serviceId);
        if (service == null) {
            throw new ValidationException("Услуга с id " + serviceId + " не найдена");
        }
        return service;
    }

    public List<Room> getRoomsList() {
        return new ArrayList<>(rooms.values());
    }

    public List<Service> getServicesList() {
        return new ArrayList<>(services.values());
    }

    public List<Guest> getGuestsList() {
        return new ArrayList<>(guests.values());
    }

    public boolean isRoomExists(int roomNumber) {
        Room room = rooms.get("R" + roomNumber);
        return room != null;
    }

    public boolean checkIn(List<Guest> newGuests, int roomId, int days) {
        Room room = getRoomByNumber(roomId);

        for (Guest guest : newGuests) {
            this.guests.put(guest.getId(), guest);
        }

        return room.checkIn(newGuests, currentDay, days);
    }

    public boolean checkOut(int roomNumber) {
        Room room = getRoomByNumber(roomNumber);

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
        Service service = getServiceById(serviceId);
        guest.addService(service, currentDay);
    }

    public boolean setRoomUnderMaintenance(int roomId, int days) {
        Room room = getRoomByNumber(roomId);
        return room.setUnderMaintenance(currentDay, days);
    }

    public boolean setRoomCleaning(int roomId) {
        Room room = getRoomByNumber(roomId);
        return room.setCleaning(currentDay);
    }

    public boolean setRoomAvailable(int roomId) {
        Room room = getRoomByNumber(roomId);
        return room.setAvailable();
    }

    public void setRoomPrice(int roomId, int price) {
        Room room = getRoomByNumber(roomId);

        room.setPrice(price);
    }

    public void setServicePrice(String serviceId, int price) {
        Service service = getServiceById(serviceId);

        service.setPrice(price);
    }

    public void importRooms(List<Room> importedRooms) {
        for (Room room : importedRooms) {
            rooms.put(room.getId(), room);
        }
    }

    public void importServices(List<Service> importedServices) {
        for (Service service : importedServices) {
            services.put(service.getId(), service);
        }
    }

    public void importGuests(List<Guest> importedGuests) {
        boolean isErrorOcurred = false;
        StringBuilder errorRooms = new StringBuilder();

        Map<Integer, List<Guest>> guestsByRoom = importedGuests.stream()
                .filter(guest -> guest.getRoomNumber() > 0)
                .collect(Collectors.groupingBy(Guest::getRoomNumber));

        for (Map.Entry<Integer, List<Guest>> entry : guestsByRoom.entrySet()) {
            int roomNumber = entry.getKey();
            List<Guest> roomGuests = entry.getValue();

            Room room = getRoomByNumber(roomNumber);
            if (room.getStatus() == RoomStatus.AVAILABLE) {
                for (Guest guest : roomGuests) {
                    guests.put(guest.getId(), guest);
                }
                room.checkIn(roomGuests, currentDay, 1);
            } else if (room.getStatus() == RoomStatus.OCCUPIED) {
                List<Guest> currentGuests = room.getGuests();
                if (areGuestGroupsIdentical(roomGuests, currentGuests)) {
                    for (Guest guest : roomGuests) {
                        guests.put(guest.getId(), guest);
                    }
                } else {
                    isErrorOcurred = true;
                    errorRooms.append(roomNumber + " ");
                    continue;
                }
            } else {
                isErrorOcurred = true;
                errorRooms.append(roomNumber + " ");
                continue;
            }
        }

        importedGuests.stream()
                .filter(guest -> guest.getRoomNumber() == 0)
                .forEach(guest -> previousGuests.put(guest.getId(), guest));

        if (isErrorOcurred) {
            throw new ImportExportException("Не удалось расселить всех постояльцев. Комнаты, которые выдали ошибку: " + errorRooms.toString());
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

    private int getNextIndex(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return 1;
        }

        return ids.stream()
                .map(id -> id.substring(1))
                .mapToInt(id -> {
                    try {
                        return Integer.parseInt(id);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0) + 1;
    }

    private Map<String, Room> getAvailableRooms() {
        return rooms.entrySet().stream()
                .filter(entry -> entry.getValue().getStatus() == RoomStatus.AVAILABLE)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void performEndOfDayOperations() {
        for (Room room : rooms.values()) {
            if (room.getEndDate().equals(currentDay)) {
                if (room.getStatus() == RoomStatus.OCCUPIED) {
                    checkOut(room.getNumber());
                } else if (room.getStatus() == RoomStatus.CLEANING || room.getStatus() == RoomStatus.MAINTENANCE) {
                    room.setAvailable();
                }
            }
        }
    }

    private Map<String, Room> sortRooms(Map<String, Room> roomsToSort, RoomSort sortBy, SortDirection direction) {
        Comparator<Map.Entry<String, Room>> comparator = switch (sortBy) {
            case RoomSort.PRICE -> Comparator.comparingInt(entry -> entry.getValue().getPrice());
            case RoomSort.CAPACITY -> Comparator.comparingInt(entry -> entry.getValue().getCapacity());
            case RoomSort.TYPE -> Comparator.comparing(entry -> entry.getValue().getType());
        };

        if (direction == SortDirection.DESC) {
            comparator = comparator.reversed();
        }

        return roomsToSort.entrySet().stream()
                .sorted(comparator)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
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

        List<Guest> sortedGuests = guests.values().stream().sorted(comparator).toList();
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
        for (Room room : rooms.values()) {
            roomsAndServices.add(new IdPricePair(room.getId(), room.getPrice()));
        }
        for (Service service : services.values()) {
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
