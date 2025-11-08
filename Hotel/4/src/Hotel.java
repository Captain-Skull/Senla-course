import enums.RoomStatus;
import enums.RoomType;
import enums.SortDirection;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Hotel {
    private String name;
    private List<Room> rooms;
    private List<Service> services;
    private Map<String, Guest> guests;
    private Map<String, Guest> previousGuests;
    private int nextGuestIndex = 1;
    private LocalDate currentDay;


    public Hotel(String name) {
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

        services.add(new Service("S1", "Завтрак", 200, "Завтрак в 8:00"));
        services.add(new Service("S2", "Обед", 300, "Обед в 13:00"));
        services.add(new Service("S3", "Ужин", 275, "Ужин в 20:00"));
        services.add(new Service("S4", "Тренажерный зал", 200, "Пропуск в тренажерный зал на день"));
        services.add(new Service("S5", "SPA-зона", 250, "Пропуск в зону спа на день"));
        services.add(new Service("S6", "Бассейн", 150, "Пропуск в бассейн на день"));
    }

    public String getName() {
        return name;
    }

    public Guest initializeGuest(String firstname, String lastname) {
        String guestId = "G" + nextGuestIndex;
        nextGuestIndex++;
        return new Guest(guestId, firstname, lastname);
    }

    public void nextDay() {
        currentDay = currentDay.plusDays(1);
        System.out.println("Наступил новый день: " + getFormattedDate(currentDay));
        performEndOfDayOperations();
    }

    public String getFormattedDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return date.format(formatter);
    }

    public LocalDate getCurrentDay() {
        return currentDay;
    }

    public void checkIn(List<Guest> newGuests, int roomId, int days) {
        Room room = findRoom(roomId);
        if (room == null) {
            System.out.println("Такого номера не существует!");
            return;
        };

        for (Guest guest : newGuests) {
            this.guests.put(guest.getId(), guest);
        }

        room.checkIn(newGuests, currentDay, days);
    }

    public void checkOut(int roomNumber) {
        Room room = findRoom(roomNumber);
        if (room == null) {
            System.out.println("Произошла ошибка, невозможно выселить постояльца");
            return;
        }

        List<Guest> roomGuests = new ArrayList<>(room.getGuests());

        if (room.checkOut()) {
            for (Guest guest : roomGuests) {
                String guestId = guest.getId();
                this.guests.remove(guestId);
                guest.setRoomNumber(0);
                previousGuests.put(guestId, guest);
            }
        }
    }

    public void addServiceToGuest(Guest guest, String serviceId) {
        Service service = findService(serviceId);
        guest.addService(service, currentDay);
    }

    public void setRoomUnderMaintenance(int roomId, int days) {
        Room room = findRoom(roomId);
        room.setUnderMaintenance(currentDay, days);
    }

    public void setRoomCleaning(int roomId) {
        Room room = findRoom(roomId);
        room.setCleaning(currentDay);
    }

    public void setRoomAvailable(int roomId) {
        Room room = findRoom(roomId);
        room.setAvailable();
    }

    public void setRoomPrice(int roomId, int price) {
        Room room = findRoom(roomId);
        if (room == null) {
            System.out.println("Ошибка при поиске команты по номеру");
            return;
        }

        room.setPrice(price);
        System.out.println("Цена комнаты " + roomId + " изменена на " + price);
    }

    public void setServicePrice(String serviceId, int price) {
        Service service = findService(serviceId);
        if (service == null) {
            System.out.println("Ошибка при поиске идентификатора услуги");
            return;
        }

        service.setPrice(price);
        System.out.println("Цена услуги " + serviceId + " изменена на " + price);
    }

    public void displayAllServices() {
        System.out.println("=====ВСЕ УСЛУГИ=====");
        services.forEach(service -> {
            System.out.println(service.getFullDescription());
        });
        System.out.println("=================");
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

    public void displayGuestServicesSorted(Guest guest, String sortBy, SortDirection direction) {
        guest.displayServicesSorted(sortBy, direction);
    }

    public void displaySortedAvailableRooms(String sortBy, SortDirection direction) {
        List<Room> sortedAvailableRooms = sortRooms(sortBy, getAvailableRooms(), direction);
        String directionText = direction == SortDirection.ASC ? "возрастанию" : "убыванию";
        System.out.println("=============Доступные комнаты отсортированные по " + sortBy + " по " + directionText);
        displayRooms(sortedAvailableRooms);
    }

    public void displaySortedRooms(String sortBy, SortDirection direction) {
        String directionText = direction == SortDirection.ASC ? "возрастанию" : "убыванию";
        System.out.println("===========Все комнаты отсортированные по " + sortBy + " по " + directionText);
        List<Room> sortedRooms = sortRooms(sortBy, rooms, direction);
        displayRooms(sortedRooms);
    }

    public void displayGuests(String sortBy, SortDirection direction) {
        List<Guest> sortedGuests = sortGuests(sortBy, direction);

        String sortByText = sortBy.equals("name") ? "имени" : "дате освобождения номера";
        String directionText = direction == SortDirection.ASC ? "возрастанию" : "убыванию";
        System.out.println("=============Сортировка гостей по " + sortByText + " по " + directionText);
        sortedGuests.forEach(guest -> {
            Room room = findRoom(guest.getRoomNumber());
            System.out.println(guest.getDescription() + "Дата выезда" + getFormattedDate(room.getEndDate()));
        });
        System.out.println("=======================");
    }

    public void displayRoomInformation(int roomNumber) {
        Room room = findRoom(roomNumber);
        System.out.println(room.getDescription());
    }

    public void displayAvailableRoomsByDate(LocalDate date) {
        List<Room> availableRoomsByDate = getAvailableRoomsByDate(date);
        System.out.println("===========Список доступных комнат на " + getFormattedDate(date));
        for (Room room : availableRoomsByDate) {
            System.out.println(room.getDescription());
        }
        System.out.println("================");
    }

    public void displayPreviousGuests(int roomNumber) {
        Room room = findRoom(roomNumber);
        List<List<Guest>> previousGuests = room.getPreviousGuests();
        System.out.println("=======Список последних трех постояльцев номера " + roomNumber + "=========");

        for (int i = 0; i < previousGuests.size(); i++) {
            StringBuilder text = new StringBuilder();
            text.append(i+1).append(".");
            for (Guest g : previousGuests.get(i)) {
                text.append(" ").append(g.getFullName());
            }
            System.out.println(text.toString());
        }
        System.out.println("===============");
    }

    public void displayPricesOfRoomsAndServices(String sortBy, SortDirection direction) {
        List<IdPricePair> roomsAndServices = sortRoomsAndServices(sortBy, direction);
        System.out.println("=======Цены комнат и услуг============");
        for (IdPricePair roomAndService : roomsAndServices) {
            System.out.println(roomAndService.getDescription());
        }
        System.out.println("===============");
    }

    private Room findRoom(int roomId) {
        return rooms.stream()
                .filter(room -> room.getNumber() == roomId)
                .findFirst()
                .orElse(null);
    }

    private Service findService(String serviceId) {
        return services.stream()
                .filter(service -> service.getId() == serviceId)
                .findFirst()
                .orElse(null);
    }

    private List<Room> getOccupiedRooms() {
        return rooms.stream()
                .filter(room -> room.getStatus() == RoomStatus.OCCUPIED)
                .collect(Collectors.toList());
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

    private void displayRooms(List<Room> roomsToDisplay) {
        roomsToDisplay.forEach(room -> {
            System.out.println(room.getDescription());
        });
    }

    private List<Room> sortRoomsByPrice(List<Room> roomsToSort, SortDirection direction) {
        Comparator<Room> comparator = Comparator.comparingInt(Room::getPrice);

        if (direction == SortDirection.DESC) {
            comparator = comparator.reversed();
        }

        return roomsToSort.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private List<Room> sortRoomsByNumber(List<Room> roomsToSort, SortDirection direction) {
        Comparator<Room> comparator = Comparator.comparingInt(Room::getNumber);

        if (direction == SortDirection.DESC) {
            comparator = comparator.reversed();
        }

        return roomsToSort.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private List<Room> sortRoomsByType(List<Room> roomsToSort, SortDirection direction) {
        Comparator<Room> comparator = Comparator.comparing(Room::getType);

        if (direction == SortDirection.DESC) {
            comparator = comparator.reversed();
        }

        return roomsToSort.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private List<Room> sortRooms(String sortBy, List<Room> roomsToSort, SortDirection direction) {
        List<Room> sortedList = new ArrayList<>();
        switch (sortBy.toLowerCase()) {
            case "price":
                sortedList = sortRoomsByPrice(roomsToSort, direction);
                break;
            case "number":
                sortedList = sortRoomsByNumber(roomsToSort, direction);
                break;
            case "type":
                sortedList = sortRoomsByType(roomsToSort, direction);
                break;
        }
        
        return sortedList;
    }

    private List<Guest> sortGuestsByName(SortDirection direction) {
        Comparator<Guest> comparator = Comparator.comparing(Guest::getFullName);

        if (direction == SortDirection.DESC) {
            comparator = comparator.reversed();
        }

        return guests.values().stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private List<Guest> sortGuestsByCheckoutDate(SortDirection direction) {
        Comparator<Guest> comparator = Comparator.comparing(guest -> {
            Room room = findRoom(guest.getRoomNumber());
            if (room == null) {
                System.out.println(guest.getFullName());
            }
            return room.getEndDate();
        });

        if (direction == SortDirection.DESC) {
            comparator = comparator.reversed();
        }

        return guests.values().stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private List<Guest> sortGuests(String sortBy, SortDirection direction) {
        List<Guest> sortedList = new ArrayList<>();
        switch (sortBy.toLowerCase()) {
            case "name":
                sortedList = sortGuestsByName(direction);
                break;
            case "date":
                sortedList = sortGuestsByCheckoutDate(direction);
                break;
        }

        return sortedList;
    }

    private List<IdPricePair> sortRoomsAndServices(String sortBy, SortDirection direction) {
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

    private static Comparator<IdPricePair> getIdPricePairComparator(String sortBy, SortDirection direction) {
        Comparator<IdPricePair> comparator = null;
        switch (sortBy.toLowerCase()) {
            case "type":
                comparator = Comparator.comparing(IdPricePair::getId);
                break;
            case "price":
                comparator = Comparator.comparingInt(IdPricePair::getPrice);
                break;
        }

        if (direction == SortDirection.DESC) {
            assert comparator != null;
            comparator = comparator.reversed();
        }

        assert comparator != null;
        return comparator;
    }

    private List<Room> getAvailableRoomsByDate(LocalDate date) {
        List<Room> availableRoomsByDate = new ArrayList<>();
        for (Room room : rooms) {
            LocalDate endDate = room.getEndDate();
            if (date.isAfter(endDate)) {
                availableRoomsByDate.add(room);
            }
        }

        return availableRoomsByDate;
    }
}
