import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Hotel {
    private String name;
    private List<Room> rooms;
    private List<Service> services;
    private Map<String, Guest> guests;
    private Map<String, Guest> previousGuests;
    private int nextGuestIndex = 1;


    public Hotel(String name) {
        this.name = name;
        this.rooms = new ArrayList<>();
        this.services = new ArrayList<>();
        this.guests = new HashMap<>();
        this.previousGuests = new HashMap<>();
        initializeHotelData();
    }

    private void initializeHotelData() {
        rooms.add(new Room(101, RoomType.ECONOM, 1000));
        rooms.add(new Room(102, RoomType.ECONOM, 1000));
        rooms.add(new Room(103, RoomType.ECONOM, 1000));
        rooms.add(new Room(104, RoomType.STANDARD, 2500));
        rooms.add(new Room(105, RoomType.STANDARD, 2600));
        rooms.add(new Room(201, RoomType.STANDARD, 2700));
        rooms.add(new Room(202, RoomType.STANDARD, 2800));
        rooms.add(new Room(203, RoomType.STANDARD, 2950));
        rooms.add(new Room(204, RoomType.LUXURY, 5000));
        rooms.add(new Room(301, RoomType.LUXURY, 5200));
        rooms.add(new Room(302, RoomType.LUXURY, 5500));
        rooms.add(new Room(401, RoomType.PRESEDENTIAL, 11000));
        rooms.add(new Room(501, RoomType.PRESEDENTIAL, 15000));

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

    public void checkIn(Guest newGuest, int roomId) {
        Room room = findRoom(roomId);
        if (room == null) {
            System.out.println("Такого номера не существует!");
            return;
        }

        if (newGuest.getRoomNumber() > 0) {
            checkOut(newGuest);
        }

        room.checkIn(newGuest);
    }

    public void checkOut(Guest guest) {
        Room room = findRoom(guest.getRoomNumber());
        if (room == null) {
            System.out.println("Произошла ошибка, невозможно выселить постояльца");
            return;
        }

        if (room.checkOut()) {
            String guestId = guest.getId();
            guests.remove(guestId);
            guest.setRoomNumber(-1);
            previousGuests.put(guestId, guest);
        }
    }

    public void addServiceToGuest(Guest guest, String serviceId) {
        Service service = findService(serviceId);
        guest.addService(service);
    }

    public void setRoomUnderMaintenance(int roomId) {
        Room room = findRoom(roomId);
        room.setUnderMaintenance();
    }

    public void setRoomCleaning(int roomId) {
        Room room = findRoom(roomId);
        room.setCleaning();
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

    public void displayAllRooms() {
        System.out.println("=====ВСЕ НОМЕРА=====");
        rooms.forEach(room -> {
            System.out.println(room.getDescription());
        });
        System.out.println("=================");
    }

    public void displayAllServices() {
        System.out.println("=====ВСЕ УСЛУГИ=====");
        services.forEach(service -> {
            System.out.println(service.getFullDescription());
        });
        System.out.println("=================");
    }
}
