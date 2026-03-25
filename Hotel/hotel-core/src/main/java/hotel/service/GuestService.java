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
import org.springframework.transaction.annotation.Transactional;

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
        return guestDao.findAll();
    }

    public List<Guest> getGuestsByRoom(int roomNumber) {
        return guestDao.findByRoomNumber(roomNumber);
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
        return guestDao.findById(id).orElse(null);
    }

    public List<GuestServiceUsage> getGuestServiceUsageList(String guestId, UsageServiceSort sortBy, SortDirection direction) {
        List<GuestServiceUsage> usages = getGuestServices(guestId);
        return sortServices(usages, sortBy, direction);
    }

    public List<GuestServiceUsage> getGuestServices(String guestId) {
        return usageDao.findByGuestId(guestId);
    }

    @Transactional
    public void updateGuest(Guest guest) {
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

        GuestServiceUsage usage = new GuestServiceUsage(service, hotelState.getCurrentDay(), guest);
        GuestServiceUsage saved = usageDao.save(usage);
        return saved;
    }

    @Transactional
    public GuestServiceUsage addServiceToGuest(String guestId, String serviceId, LocalDate date) {
        Guest guest = guestDao.findById(guestId).orElseThrow(() -> new DaoException("Гость не найден" + guestId));
        Service service = serviceService.getServiceById(serviceId);

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
}
