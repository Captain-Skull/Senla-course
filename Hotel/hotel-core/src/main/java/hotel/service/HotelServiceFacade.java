package hotel.service;

import enums.RoomSort;
import enums.RoomStatus;
import enums.SortDirection;
import exceptions.DaoException;
import hotel.Guest;
import hotel.Room;
import hotel.RoomGuestHistory;
import hotel.dao.GuestDao;
import hotel.dao.RoomDao;
import hotel.dao.RoomGuestHistoryDao;
import hotel.dto.RoomWithGuestsDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class HotelServiceFacade {

    private static final Logger logger = LogManager.getLogger(HotelServiceFacade.class);

    private final GuestService guestService;
    private final RoomService roomService;
    private final RoomDao roomDao;
    private final GuestDao guestDao;
    private final RoomGuestHistoryDao historyDao;
    private final HotelState hotelState;

    @Autowired
    HotelServiceFacade(GuestService guestService, RoomService roomService, RoomDao roomDao, GuestDao guestDao, RoomGuestHistoryDao historyDao, HotelState hotelState) {
        this.guestService = guestService;
        this.roomService = roomService;
        this.roomDao = roomDao;
        this.guestDao = guestDao;
        this.historyDao = historyDao;
        this.hotelState = hotelState;
    }

    public String getRoomInformation(int roomNumber) {
        Room room = roomService.getRoomByNumber(roomNumber);
        List<Guest> guests = guestService.getGuestsByRoom(roomNumber);

        StringBuilder info = new StringBuilder();
        info.append("Номер ").append(room.getNumber())
                .append(" тип: ").append(room.getType())
                .append("\nСтоимость: ").append(room.getPrice())
                .append(" вместимость: ").append(room.getCapacity())
                .append("\nСтатус: ").append(room.getStatus());

        if (!guests.isEmpty()) {
            info.append("\nГости:");
            for (Guest guest : guests) {
                info.append("\n  - ").append(guest.getInformation());
            }
        }

        return info.toString();
    }

    public List<RoomWithGuestsDto> getSortedRooms(RoomSort sortBy, SortDirection direction) {
        List<Room> rooms = roomService.getAllRooms();
        Map<Integer, Room> sortedRooms = roomService.sortRooms(rooms, sortBy, direction);

        List<RoomWithGuestsDto> result = new ArrayList<>();
        for (Room room : sortedRooms.values()) {
            List<Guest> guests = guestService.getGuestsByRoom(room.getNumber());
            result.add(new RoomWithGuestsDto(room, guests));
        }

        return result;
    }

    public List<RoomWithGuestsDto> getSortedAvailableRooms(RoomSort sortBy, SortDirection direction) {
        List<Room> rooms = roomService.getAvailableRooms();
        Map<Integer, Room> sortedRooms = roomService.sortRooms(rooms, sortBy, direction);

        List<RoomWithGuestsDto> result = new ArrayList<>();
        for (Room room : sortedRooms.values()) {
            List<Guest> guests = guestService.getGuestsByRoom(room.getNumber());
            result.add(new RoomWithGuestsDto(room, guests));
        }

        return result;
    }

    @Transactional
    public List<Guest> checkIn(List<Guest> guests, int roomNumber, int days) {
        try {
            LocalDate currentDay = hotelState.getCurrentDay();

            Room room = roomDao.findById(roomNumber)
                    .orElseThrow(() -> new DaoException("Комната не найдена: " + roomNumber));

            if (!room.canCheckIn(guests.size())) {
                return new ArrayList<>();
            }

            room.markAsOccupied(currentDay, days);
            roomDao.update(room);

            List<Guest> savedGuests = new ArrayList<>();
            for (Guest guest : guests) {
                guest.setRoomNumber(roomNumber);
                Guest saved = guestDao.save(guest);
                savedGuests.add(saved);
            }

            return savedGuests;
        } catch (Exception e) {
            throw new DaoException("Ошибка заселения", e);
        }
    }

    @Transactional
    public boolean checkOut(int roomNumber) {
        try {
            Room room = roomDao.findById(roomNumber)
                    .orElseThrow(() -> new DaoException("Комната не найдена: " + roomNumber));

            if (!room.canCheckOut()) {
                return false;
            }

            List<Guest> guests = guestDao.findByRoomNumber(roomNumber);

            if (guests.isEmpty()) {
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
            return true;
        } catch (Exception e) {
            throw new DaoException("Ошибка выселения", e);
        }
    }

    @Transactional
    public LocalDate nextDay() {
        LocalDate newDay = hotelState.nextDay();

        performEndOfDayOperations();

        return newDay;
    }

    @Transactional
    private void performEndOfDayOperations() {
        for (Room room : roomService.getAllRooms()) {
            LocalDate endDate = room.getEndDate();
            if (endDate != null && endDate.equals(hotelState.getCurrentDay())) {
                if (room.getStatus() == RoomStatus.OCCUPIED) {
                    checkOut(room.getNumber());
                } else if (room.getStatus() == RoomStatus.CLEANING || room.getStatus() == RoomStatus.MAINTENANCE) {
                    roomService.setRoomAvailable(room.getNumber());
                }
            }
        }
    }
}
