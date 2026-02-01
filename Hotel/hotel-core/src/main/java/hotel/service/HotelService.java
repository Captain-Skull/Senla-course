package hotel.service;

import annotations.Component;
import annotations.Inject;
import annotations.Singleton;
import exceptions.DaoException;
import hotel.Guest;
import hotel.GuestServiceUsage;
import hotel.Room;
import hotel.Service;
import hotel.RoomGuestHistory;
import hotel.connection.EntityManagerProvider;
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
    private EntityManagerProvider emProvider;

    public HotelService() {
    }

    public boolean checkIn(List<Guest> guests, int roomNumber, int days, LocalDate currentDay) {
        try {
            emProvider.beginTransaction();

            Room room = roomDao.findById(roomNumber)
                    .orElseThrow(() -> new DaoException("Комната не найдена: " + roomNumber));

            if (!room.canCheckIn(guests.size())) {
                emProvider.rollback();
                return false;
            }

            room.markAsOccupied(currentDay, days);
            roomDao.update(room);

            List<Guest> savedGuests = new ArrayList<>();
            for (Guest guest : guests) {
                guest.setRoomNumber(roomNumber);
                Guest saved = guestDao.save(guest);
                savedGuests.add(saved);
            }

            emProvider.commit();

            guests.clear();
            guests.addAll(savedGuests);
            return true;
        } catch (Exception e) {
            emProvider.rollback();
            throw new DaoException("Ошибка заселения", e);
        }
    }

    public boolean checkOut(int roomNumber) {
        try {
            emProvider.beginTransaction();

            Room room = roomDao.findById(roomNumber)
                    .orElseThrow(() -> new DaoException("Комната не найдена: " + roomNumber));

            if (!room.canCheckOut()) {
                emProvider.rollback();
                return false;
            }

            List<Guest> guests = guestDao.findByRoomNumber(roomNumber);

            if (guests.isEmpty()) {
                emProvider.rollback();
                return false;
            }

            int nextGroupId = historyDao.getNextGroupId(roomNumber);
            for (Guest guest : guests) {
                RoomGuestHistory history = RoomGuestHistory.fromGuest(guest, roomNumber, nextGroupId);
                historyDao.save(history);
                guestDao.delete(guest.getId());
            }

            room.markAsAvailable();
            roomDao.update(room);

            emProvider.commit();
            return true;
        } catch (Exception e) {
            emProvider.rollback();
            throw new DaoException("Ошибка выселения", e);
        }
    }

    public void addServiceToGuest(String guestId, String serviceId, LocalDate usageDate) {
        try {
            emProvider.beginTransaction();

            Guest guest = guestDao.findById(guestId)
                    .orElseThrow(() -> new DaoException(ERROR_GUEST_NOT_FOUND + guestId));

            Service service = serviceDao.findById(serviceId)
                    .orElseThrow(() -> new DaoException(ERROR_SERVICE_NOT_FOUND + serviceId));

            GuestServiceUsage usage = new GuestServiceUsage(service, usageDate, guest);
            usageDao.save(usage);

            emProvider.commit();
        } catch (Exception e) {
            emProvider.rollback();
            throw new DaoException("Ошибка добавления услуги", e);
        }
    }

    public List<Room> getAllRooms() {
        return roomDao.findAll();
    }

    public List<Room> getAvailableRooms() {
        return roomDao.findAvailable();
    }

    public Room getRoomByNumber(int roomNumber) {
        return roomDao.findById(roomNumber).orElseThrow(() -> new DaoException("Комната не найдена: " + roomNumber));
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
        return serviceDao.findById(id).orElseThrow(() -> new DaoException("Услуга не найдена: " + id));
    }

    public List<GuestServiceUsage> getGuestServices(String guestId) {
        return usageDao.findByGuestId(guestId);
    }

    public List<List<RoomGuestHistory>> getPreviousGuests(int roomNumber, int maxGroups) {
        return historyDao.getPreviousGuestGroups(roomNumber, maxGroups);
    }

    public void updateRoomPrice(int roomNumber, int price) {
        try {
            emProvider.beginTransaction();

            Room room = getRoomByNumber(roomNumber);

            room.setPrice(price);
            roomDao.update(room);

            emProvider.commit();
        } catch (Exception e) {
            emProvider.rollback();
            throw new DaoException("Ошибка обновления цены комнаты", e);
        }
    }

    public void updateServicePrice(String serviceId, int price) {
        try {
            emProvider.beginTransaction();

            Service service = getServiceById(serviceId);

            service.setPrice(price);
            serviceDao.update(service);

            emProvider.commit();
        } catch (Exception e) {
            emProvider.rollback();
            throw new DaoException("Ошибка обновления цены услуги", e);
        }
    }

    public void updateGuest(Guest guest) {
        try {
            emProvider.beginTransaction();
            guestDao.update(guest);
            emProvider.commit();
        } catch (Exception e) {
            emProvider.rollback();
            throw new DaoException("Ошибка обновления гостя", e);
        }
    }

    public void updateRoom(Room room) {
        try {
            emProvider.beginTransaction();
            roomDao.update(room);
            emProvider.commit();
        } catch (Exception e) {
            emProvider.rollback();
            throw new DaoException("Ошибка обновления комнаты", e);
        }
    }

    public void updateService(Service service) {
        try {
            emProvider.beginTransaction();
            serviceDao.update(service);
            emProvider.commit();
        } catch (Exception e) {
            emProvider.rollback();
            throw new DaoException("Ошибка обновления услуги", e);
        }
    }

    public boolean setRoomUnderMaintenance(int roomNumber, LocalDate date, int days) {
        try {
            emProvider.beginTransaction();
            Room room = getRoomByNumber(roomNumber);

            if (room.setUnderMaintenance(date, days)) {
                roomDao.update(room);
                emProvider.commit();
                return true;
            }

            emProvider.rollback();
            return false;
        } catch (Exception e) {
            emProvider.rollback();
            throw new DaoException("Ошибка перевода комнаты на обслуживание", e);
        }
    }

    public boolean setRoomCleaning(int roomNumber, LocalDate date) {
        try {
            emProvider.beginTransaction();
            Room room = getRoomByNumber(roomNumber);

            if (room.setCleaning(date)) {
                roomDao.update(room);
                emProvider.commit();
                return true;
            }

            emProvider.rollback();
            return false;
        } catch (Exception e) {
            emProvider.rollback();
            throw new DaoException("Ошибка уборки комнаты", e);
        }
    }

    public boolean setRoomAvailable(int roomNumber) {
        try {
            emProvider.beginTransaction();

            Room room = getRoomByNumber(roomNumber);

            if (room.setAvailable()) {
                roomDao.update(room);
                emProvider.commit();
                return true;
            }

            emProvider.rollback();
            return false;
        } catch (Exception e) {
            emProvider.rollback();
            throw new DaoException("Ошибка перевода комнаты в доступный режим");
        }
    }

    public Room saveRoom(Room room) {
        try {
            emProvider.beginTransaction();
            Room savedRoom = roomDao.save(room);
            emProvider.commit();
            return savedRoom;
        } catch (Exception e) {
            emProvider.rollback();
            throw new DaoException("Ошибка при сохранении комнаты", e);
        }
    }

    public Service saveService(Service service) {
        try {
            emProvider.beginTransaction();
            Service savedService = serviceDao.save(service);
            emProvider.commit();
            return savedService;
        } catch (Exception e) {
            emProvider.rollback();
            throw new DaoException("Ошибка при сохранении услуги", e);
        }
    }
}