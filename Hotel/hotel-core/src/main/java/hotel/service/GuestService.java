package hotel.service;

import enums.GuestSort;
import enums.UsageServiceSort;
import enums.SortDirection;
import exceptions.DaoException;
import hotel.Guest;
import hotel.Service;
import hotel.Room;
import hotel.GuestServiceUsage;
import hotel.GuestData;
import hotel.dao.GuestDao;
import hotel.dao.GuestServiceUsageDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@Transactional(readOnly = true)
public class GuestService {

    private GuestDao guestDao;
    private GuestServiceUsageDao usageDao;
    private RoomService roomService;
    private ServiceService serviceService;
    private HotelState hotelState;

    @Autowired
    public GuestService(GuestDao guestDao, GuestServiceUsageDao usageDao, RoomService roomService, ServiceService serviceService, HotelState hotelState) {
        this.guestDao = guestDao;
        this.usageDao = usageDao;
        this.roomService = roomService;
        this.serviceService = serviceService;
        this.hotelState = hotelState;
    }

    public List<Guest> getAllGuests() {
        if (isAdmin()) {
            return guestDao.findAll();
        }

        String username = getCurrentUsername();
        Guest myGuest = guestDao.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(
                        "У вас нет привязанной записи гостя"
                ));

        return List.of(myGuest);
    }

    public List<Guest> getGuestsByRoom(int roomNumber) {
        if (!isAdmin()) {
            Guest myGuest = getMyGuest();
            if (myGuest != null && myGuest.getRoomNumber() != roomNumber) {
                throw new AccessDeniedException("У вас нет прав на просмотр гостей комнаты" + roomNumber);
            }
        }

        return guestDao.findByRoomNumber(roomNumber);
    }

    public List<Guest> getGuestsByRoomIfAllowed(int roomNumber) {
        if (isAdmin()) {
            return guestDao.findByRoomNumber(roomNumber);
        }

        try {
            Guest myGuest = getMyGuest();
            if (myGuest != null && myGuest.getRoomNumber() != null && myGuest.getRoomNumber() == roomNumber) {
                return guestDao.findByRoomNumber(roomNumber);
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    public boolean canViewGuestsInRoom(int roomNumber) {
        if (isAdmin()) {
            return true;
        }

        try {
            Guest myGuest = getMyGuest();
            return myGuest != null && myGuest.getRoomNumber() != null && myGuest.getRoomNumber() == roomNumber;
        } catch (Exception e) {
            return false;
        }
    }

    public List<GuestData> getSortedGuests(GuestSort sortBy, SortDirection direction) {
        Comparator<Guest> comparator = switch (sortBy) {
            case GuestSort.NAME -> Comparator.comparing(Guest::getFullName);
            case GuestSort.CHECKOUT_DATE -> Comparator.comparing(guest -> {
                Room room = roomService.getRoomByNumber(guest.getRoomNumber());
                return room.getEndDate();
            });
        };

        if (direction == SortDirection.DESC) {
            comparator = comparator.reversed();
        }

        List<Guest> sortedGuests = getAllGuests().stream().sorted(comparator).toList();
        return createGuestDataList(sortedGuests);
    }

    public int getGuestsCount() {
        return getAllGuests().size();
    }

    public Guest getGuestById(String id) {
        Guest guest = guestDao.findById(id)
                .orElseThrow(() -> new DaoException("Гость не найден: " + id));

        if (!isAdmin()) {
            checkOwnership(guest);
        }

        return guest;
    }

    public List<GuestServiceUsage> getGuestServiceUsageList(String guestId, UsageServiceSort sortBy, SortDirection direction) {
        List<GuestServiceUsage> usages = getGuestServices(guestId);
        return sortServices(usages, sortBy, direction);
    }

    public List<GuestServiceUsage> getGuestServices(String guestId) {
        if (!isAdmin()) {
            Guest guest = guestDao.findById(guestId)
                    .orElseThrow(() -> new DaoException("Не найден гость с таким id: " + guestId));
            checkOwnership(guest);
        }
        return usageDao.findByGuestId(guestId);
    }

    @Transactional
    public void updateGuest(Guest guest) {
        if (!isAdmin()) {
            Guest existing = guestDao.findById(guest.getId())
                    .orElseThrow(() -> new DaoException("Гость не найден: " + guest.getId()));
            checkOwnership(existing);
        }
        try {
            guestDao.update(guest);
        } catch (Exception e) {
            throw new DaoException("Ошибка обновления гостя", e);
        }
    }

    @Transactional
    public GuestServiceUsage addServiceToGuest(String guestId, String serviceId) {
        Guest guest = guestDao.findById(guestId).orElseThrow(() -> new DaoException("Гость не найден" + guestId));
        Service service = serviceService.getServiceById(serviceId);

        if (!isAdmin()) {
            checkOwnership(guest);
        }

        GuestServiceUsage usage = new GuestServiceUsage(service, hotelState.getCurrentDay(), guest);
        GuestServiceUsage saved = usageDao.save(usage);
        return saved;
    }

    @Transactional
    public GuestServiceUsage addServiceToGuest(String guestId, String serviceId, LocalDate date) {
        Guest guest = guestDao.findById(guestId).orElseThrow(() -> new DaoException("Гость не найден" + guestId));
        Service service = serviceService.getServiceById(serviceId);

        if (!isAdmin()) {
            checkOwnership(guest);
        }

        GuestServiceUsage usage = new GuestServiceUsage(service, date, guest);
        GuestServiceUsage saved = usageDao.save(usage);
        return saved;
    }

    @Transactional
    private List<GuestData> createGuestDataList(List<Guest> sortedGuests) {
        return sortedGuests.stream()
                .map(guest -> {
                    Room room = roomService.getRoomByNumber(guest.getRoomNumber());
                    return new GuestData(
                            guest.getId(),
                            guest.getFullName(),
                            guest.getRoomNumber(),
                            room.getEndDate()
                    );
                })
                .collect(Collectors.toList());
    }

    private List<GuestServiceUsage> sortServices(List<GuestServiceUsage> usages,
                                                 UsageServiceSort sortBy,
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

    private Guest getMyGuest() {
        String username = getCurrentUsername();
        return guestDao.findByUsername(username)
                .orElse(null);
    }

    private void checkOwnership(Guest guest) {
        String currentUsername = getCurrentUsername();

        if (guest.getUser() == null || !guest.getUser().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("У вас нет прав на просмотр или изменение данных этого гостя");
        }
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}
