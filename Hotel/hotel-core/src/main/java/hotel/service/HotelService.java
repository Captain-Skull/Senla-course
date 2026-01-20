package hotel.service;

import annotations.Component;
import annotations.Inject;
import annotations.Singleton;
import enums.RoomStatus;
import exceptions.DaoException;
import hotel.Guest;
import hotel.GuestServiceUsage;
import hotel.Room;
import hotel.Service;
import hotel.RoomGuestHistory;
import hotel.connection.ConnectionManager;
import hotel.dao.GuestDao;
import hotel.dao.RoomDao;
import hotel.dao.GuestServiceUsageDao;
import hotel.dao.RoomGuestHistoryDao;
import hotel.dao.ServiceDao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@Singleton
public class HotelService {

    private static final String ERROR_ROOM_NOT_FOUND = "Комната не найдена: ";
    private static final String ERROR_GUEST_NOT_FOUND = "Гость не найден: ";
    private static final String ERROR_SERVICE_NOT_FOUND = "Услуга не найдена: ";
    private static final String ERROR_ROOM_NOT_AVAILABLE = "Комната недоступна";
    private static final String ERROR_ROOM_CAPACITY = "Превышена вместимость";
    private static final String ERROR_ROOM_NOT_OCCUPIED = "Комната не занята";


    @Inject
    private RoomDao roomDao;

    @Inject
    private GuestDao guestDao;

    @Inject
    private ServiceDao serviceDao;

    @Inject
    private GuestServiceUsageDao usageDao;

    @Inject
    private RoomGuestHistoryDao historyDao;

    @Inject
    private ConnectionManager connectionManager;

    public HotelService() {
    }

    public boolean checkIn(List<Guest> guests, int roomNumber, int days, LocalDate currentDay) {
        try {
            connectionManager.beginTransaction();

            Room room = roomDao.findById(roomNumber)
                    .orElseThrow(() -> new DaoException(ERROR_ROOM_NOT_FOUND + roomNumber));

            if (room.getStatus() != RoomStatus.AVAILABLE) {
                connectionManager.rollback();
                throw new DaoException(ERROR_ROOM_NOT_AVAILABLE);
            }

            if (guests.size() > room.getCapacity()) {
                connectionManager.rollback();
                throw new DaoException(ERROR_ROOM_CAPACITY);
            }

            room.setStatus(RoomStatus.OCCUPIED);
            room.setEndDate(currentDay.plusDays(days));
            room.setDaysUnderStatus(days);
            roomDao.update(room);

            List<Guest> savedGuests = new ArrayList<>();
            for (Guest guest : guests) {
                guest.setRoomNumber(roomNumber);
                Guest saved = guestDao.save(guest);
                savedGuests.add(saved);
            }

            connectionManager.commit();

            guests.clear();
            guests.addAll(savedGuests);
            return true;
        } catch (Exception e) {
            connectionManager.rollback();
            throw new DaoException("Ошибка заселения", e);
        }
    }

    public boolean checkOut(int roomNumber) {
        try {
            connectionManager.beginTransaction();

            Room room = roomDao.findById(roomNumber)
                    .orElseThrow(() -> new DaoException(ERROR_ROOM_NOT_FOUND + roomNumber));

            if (room.getStatus() != RoomStatus.OCCUPIED) {
                connectionManager.rollback();
                throw new DaoException(ERROR_ROOM_NOT_OCCUPIED);
            }

            List<Guest> guests = guestDao.findByRoomNumber(roomNumber);
            int nextGroupId = historyDao.getNextGroupId(roomNumber);

            for (Guest guest : guests) {
                RoomGuestHistory history = new RoomGuestHistory(
                        guest.getId(),
                        guest.getFirstName(),
                        guest.getLastName(),
                        roomNumber,
                        nextGroupId
                );
                historyDao.save(history);
                guestDao.delete(guest.getId());
            }

            room.setStatus(RoomStatus.AVAILABLE);
            room.setEndDate(null);
            room.setDaysUnderStatus(0);
            roomDao.update(room);

            connectionManager.commit();
            return true;
        } catch (Exception e) {
            connectionManager.rollback();
            throw new DaoException("Ошибка выселения", e);
        }
    }

    public void addServiceToGuest(String guestId, String serviceId, LocalDate usageDate) {
        try {
            connectionManager.beginTransaction();

            Guest guest = guestDao.findById(guestId)
                    .orElseThrow(() -> new DaoException(ERROR_GUEST_NOT_FOUND + guestId));

            Service service = serviceDao.findById(serviceId)
                    .orElseThrow(() -> new DaoException(ERROR_SERVICE_NOT_FOUND + serviceId));

            GuestServiceUsage usage = new GuestServiceUsage(service, usageDate, guest);
            usage.setId(usageDao.getNextId());
            usageDao.save(usage);

            connectionManager.commit();
        } catch (Exception e) {
            connectionManager.rollback();
            throw new DaoException("Ошибка добавления услуги", e);
        }
    }

    public List<Room> getAllRooms() {
        return roomDao.findAll();
    }

    public List<Room> getAvailableRooms() {
        return roomDao.findAvailable();
    }

    public Room getRoomByNumber(int n) {
        return roomDao.findById(n).orElse(null);
    }

    public List<Guest> getAllGuests() {
        return guestDao.findAll();
    }

    public Guest getGuestById(String id) {
        return guestDao.findById(id).orElse(null);
    }

    public List<Guest> getGuestsByRoom(int roomNumber) {
        return guestDao.findByRoomNumber(roomNumber);
    }

    public List<Service> getAllServices() {
        return serviceDao.findAll();
    }

    public Service getServiceById(String id) {
        return serviceDao.findById(id).orElse(null);
    }

    public List<GuestServiceUsage> getGuestServices(String guestId) {
        return usageDao.findByGuestId(guestId);
    }

    public List<List<RoomGuestHistory>> getPreviousGuests(int roomNumber, int maxGroups) {
        return historyDao.getPreviousGuestGroups(roomNumber, maxGroups);
    }

    public void updateRoomPrice(int roomNumber, int price) {
        Room room = getRoomByNumber(roomNumber);
        if (room != null) {
            room.setPrice(price);
            roomDao.update(room);
        }
    }

    public void updateServicePrice(String serviceId, int price) {
        Service service = getServiceById(serviceId);
        if (service != null) {
            service.setPrice(price);
            serviceDao.update(service);
        }
    }

    public void updateGuest(Guest guest) {
        guestDao.update(guest);
    }

    public boolean setRoomUnderMaintenance(int roomNumber, LocalDate date, int days) {
        Room room = getRoomByNumber(roomNumber);
        if (room == null) {
            return false;
        }
        if (room.setUnderMaintenance(date, days)) {
            roomDao.update(room);
            return true;
        }
        return false;
    }

    public boolean setRoomCleaning(int roomNumber, LocalDate date) {
        Room room = getRoomByNumber(roomNumber);
        if (room == null) {
            return false;
        }
        if (room.setCleaning(date)) {
            roomDao.update(room);
            return true;
        }
        return false;
    }

    public boolean setRoomAvailable(int roomNumber) {
        Room room = getRoomByNumber(roomNumber);
        if (room == null) {
            return false;
        }
        if (room.setAvailable()) {
            roomDao.update(room);
            return true;
        }
        return false;
    }

    public Room saveRoom(Room room) {
        return roomDao.save(room);
    }

    public Service saveService(Service service) {
        return serviceDao.save(service);
    }
}