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
import hotel.dao.UserDao;
import hotel.dto.GuestRequest;
import hotel.dto.RoomWithGuestsDto;
import hotel.dto.RoomInfoDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UserDao userDao;
    private final RoomGuestHistoryDao historyDao;
    private final HotelState hotelState;

    @Autowired
    HotelServiceFacade(GuestService guestService, RoomService roomService, RoomDao roomDao, GuestDao guestDao, UserDao userDao, RoomGuestHistoryDao historyDao, HotelState hotelState) {
        this.guestService = guestService;
        this.roomService = roomService;
        this.roomDao = roomDao;
        this.guestDao = guestDao;
        this.userDao = userDao;
        this.historyDao = historyDao;
        this.hotelState = hotelState;
    }


    public RoomInfoDto getRoomInformation(int roomNumber) {
        Room room = roomService.getRoomByNumber(roomNumber);

        RoomInfoDto info = new RoomInfoDto();
        info.setNumber(room.getNumber());
        info.setType(room.getType().toString());
        info.setPrice(room.getPrice());
        info.setCapacity(room.getCapacity());
        info.setStatus(room.getStatus().toString());

        List<Guest> guests = guestService.getGuestsByRoomIfAllowed(roomNumber);

        if (guests == null) {
            info.setGuests(null);
            info.setGuestsAccessMessage("Информация о гостях доступна только проживающим в этой комнате");
        } else if (guests.isEmpty()) {
            info.setGuests(List.of());
            info.setGuestsAccessMessage("В комнате нет гостей");
        } else {
            info.setGuests(
                    guests.stream()
                            .map(Guest::getInformation)
                            .toList()
            );
        }

        return info;
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
    public List<Guest> checkInRequest(List<GuestRequest> guestRequests, int roomNumber, int days) {
        try {
            LocalDate currentDay = hotelState.getCurrentDay();

            Room room = roomDao.findById(roomNumber)
                    .orElseThrow(() -> new DaoException("Комната не найдена: " + roomNumber));

            if (!room.canCheckIn(guestRequests.size())) {
                return new ArrayList<>();
            }

            room.markAsOccupied(currentDay, days);
            roomDao.update(room);

            List<Guest> savedGuests = new ArrayList<>();
            boolean currentUserLinked = false;

            for (GuestRequest gr : guestRequests) {
                Guest guest = new Guest(null, gr.getFirstname(), gr.getLastname());
                guest.setRoomNumber(roomNumber);

                if (gr.getUsername() != null && !gr.getUsername().isEmpty()) {
                    userDao.findByUsername(gr.getUsername()).ifPresent(guest::setUser);
                } else if (!isAdmin() && !currentUserLinked) {
                    String currentUsername = getCurrentUsername();
                    userDao.findByUsername(currentUsername).ifPresent(guest::setUser);
                    currentUserLinked = true;
                }

                Guest saved = guestDao.save(guest);
                savedGuests.add(saved);
            }

            return savedGuests;
        } catch (Exception e) {
            logger.error("Ошибка при заселении в комнату {}: {}", roomNumber, e.getMessage());
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

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }
}
