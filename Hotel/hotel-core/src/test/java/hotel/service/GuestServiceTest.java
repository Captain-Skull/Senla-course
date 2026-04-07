package hotel.service;

import enums.GuestSort;
import enums.SortDirection;
import enums.UsageServiceSort;
import exceptions.DaoException;
import hotel.Guest;
import hotel.GuestData;
import hotel.GuestServiceUsage;
import hotel.Room;
import hotel.Service;
import hotel.User;
import hotel.dao.GuestDao;
import hotel.dao.GuestServiceUsageDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GuestServiceTest {

    @Mock
    private GuestDao guestDao;

    @Mock
    private GuestServiceUsageDao usageDao;

    @Mock
    private RoomService roomService;

    @Mock
    private ServiceService serviceService;

    @Mock
    private HotelState hotelState;

    @InjectMocks
    private GuestService guestService;

    private Guest guest1;
    private Guest guest2;
    private User regularUser;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        regularUser = new User("user1", "pass", "user1@test.com");

        guest1 = new Guest("G1", "John", "Doe");
        guest1.setRoomNumber(101);
        guest1.setUser(regularUser);

        guest2 = new Guest("G2", "Jane", "Smith");
        guest2.setRoomNumber(102);

        when(hotelState.getCurrentDay()).thenReturn(LocalDate.now());
    }

    private void setAdminAuth() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", "pass",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }

    private void setUserAuth(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, "pass",
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    @Test
    @DisplayName("Получение всех гостей администратором")
    public void getAllGuests_Admin_Positive() {
        setAdminAuth();
        when(guestDao.findAll()).thenReturn(List.of(guest1, guest2));
        List<Guest> guests = guestService.getAllGuests();
        assertEquals(2, guests.size());
    }

    @Test
    @DisplayName("Получение всех гостей обычным пользователем")
    public void getAllGuests_User_Positive() {
        setUserAuth("user1");
        when(guestDao.findByUsername("user1")).thenReturn(Optional.of(guest1));
        List<Guest> guests = guestService.getAllGuests();
        assertEquals(1, guests.size());
        assertEquals(guest1, guests.get(0));
    }

    @Test
    @DisplayName("Получение всех гостей пользователем без привязки")
    public void getAllGuests_User_NoLinkedGuest() {
        setUserAuth("user1");
        when(guestDao.findByUsername("user1")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> guestService.getAllGuests());
    }

    @Test
    @DisplayName("Получение гостей по комнате администратором")
    public void getGuestsByRoom_Admin_Positive() {
        setAdminAuth();
        when(guestDao.findByRoomNumber(101)).thenReturn(List.of(guest1));
        List<Guest> guests = guestService.getGuestsByRoom(101);
        assertEquals(1, guests.size());
    }

    @Test
    @DisplayName("Получение гостей по номеру чужой комнаты")
    public void getGuestsByRoom_User_OtherRoom_Negative() {
        setUserAuth("user1");
        when(guestDao.findByUsername("user1")).thenReturn(Optional.of(guest1));
        assertThrows(AccessDeniedException.class, () -> guestService.getGuestsByRoom(102));
    }

    @Test
    @DisplayName("Проверка доступа к гостям комнаты администратором")
    public void canViewGuestsInRoom_Admin() {
        setAdminAuth();
        assertTrue(guestService.canViewGuestsInRoom(101));
    }

    @Test
    @DisplayName("Проверка доступа к гостям чужой комнаты")
    public void canViewGuestsInRoom_User_OtherRoom() {
        setUserAuth("user1");
        when(guestDao.findByUsername("user1")).thenReturn(Optional.of(guest1));
        assertFalse(guestService.canViewGuestsInRoom(102));
    }

    @Test
    @DisplayName("Получение гостя по ID администратором")
    public void getGuestById_Admin_Positive() {
        setAdminAuth();
        when(guestDao.findById("G1")).thenReturn(Optional.of(guest1));
        Guest guest = guestService.getGuestById("G1");
        assertEquals("G1", guest.getId());
    }

    @Test
    @DisplayName("Получение гостя по ID - не найден")
    public void getGuestById_NotFound() {
        setAdminAuth();
        when(guestDao.findById("G999")).thenReturn(Optional.empty());
        DaoException exception = assertThrows(DaoException.class, () -> guestService.getGuestById("G999"));
        assertEquals("Гость не найден: G999", exception.getMessage());
    }

    @Test
    @DisplayName("Получение гостя по ID пользователем без прав")
    public void getGuestById_User_NoOwnership() {
        setUserAuth("otherUser");
        when(guestDao.findById("G1")).thenReturn(Optional.of(guest1));
        assertThrows(AccessDeniedException.class, () -> guestService.getGuestById("G1"));
    }

    @Test
    @DisplayName("Получение количества гостей")
    public void getGuestsCount_Positive() {
        setAdminAuth();
        when(guestDao.findAll()).thenReturn(List.of(guest1, guest2));
        assertEquals(2, guestService.getGuestsCount());
    }

    @Test
    @DisplayName("Сортировка гостей по имени")
    public void getSortedGuests_ByName() {
        setAdminAuth();
        when(guestDao.findAll()).thenReturn(List.of(guest1, guest2));
        Room room101 = new Room(101, null, 1000, 1);
        room101.setEndDate(LocalDate.now().plusDays(3));
        Room room102 = new Room(102, null, 2000, 2);
        room102.setEndDate(LocalDate.now().plusDays(5));
        when(roomService.getRoomByNumber(101)).thenReturn(room101);
        when(roomService.getRoomByNumber(102)).thenReturn(room102);
        List<GuestData> sorted = guestService.getSortedGuests(GuestSort.NAME, SortDirection.ASC);
        assertNotNull(sorted);
        assertEquals(2, sorted.size());
    }

    @Test
    @DisplayName("Получение услуг гостя администратором")
    public void getGuestServices_Admin() {
        setAdminAuth();
        GuestServiceUsage usage = new GuestServiceUsage(new Service("S1", "Spa", 500, "Доступ в спа-зону"), LocalDate.now(), guest1);
        when(usageDao.findByGuestId("G1")).thenReturn(List.of(usage));
        List<GuestServiceUsage> usages = guestService.getGuestServices("G1");
        assertEquals(1, usages.size());
    }

    @Test
    @DisplayName("Получение услуг гостя пользователем без прав")
    public void getGuestServices_User_NoOwnership() {
        setUserAuth("otherUser");
        when(guestDao.findById("G1")).thenReturn(Optional.of(guest1));
        assertThrows(AccessDeniedException.class, () -> guestService.getGuestServices("G1"));
    }

    @Test
    @DisplayName("Сортировка услуг по цене")
    public void getGuestServiceUsageList_SortByPrice() {
        setAdminAuth();
        Service s1 = new Service("S1", "Spa", 500, "Доступ в спа-зону");
        Service s2 = new Service("S2", "Dinner", 300, "Ужин в ресторане отеля");
        GuestServiceUsage usage1 = new GuestServiceUsage(s1, LocalDate.now(), guest1);
        GuestServiceUsage usage2 = new GuestServiceUsage(s2, LocalDate.now().minusDays(1), guest1);
        when(usageDao.findByGuestId("G1")).thenReturn(List.of(usage1, usage2));
        List<GuestServiceUsage> sorted = guestService.getGuestServiceUsageList("G1", UsageServiceSort.PRICE, SortDirection.ASC);
        assertEquals(2, sorted.size());
        assertTrue(sorted.get(0).getPrice() <= sorted.get(1).getPrice());
    }

    @Test
    @DisplayName("Обновление гостя администратором")
    public void updateGuest_Admin_Positive() {
        setAdminAuth();
        when(guestDao.update(any(Guest.class))).thenReturn(guest1);
        assertDoesNotThrow(() -> guestService.updateGuest(guest1));
        verify(guestDao).update(guest1);
    }

    @Test
    @DisplayName("Обновление гостя пользователем с правами")
    public void updateGuest_User_OwnGuest_Positive() {
        setUserAuth("user1");
        when(guestDao.findById("G1")).thenReturn(Optional.of(guest1));
        when(guestDao.update(any(Guest.class))).thenReturn(guest1);
        assertDoesNotThrow(() -> guestService.updateGuest(guest1));
        verify(guestDao).update(guest1);
    }

    @Test
    @DisplayName("Обновление гостя пользователем без прав")
    public void updateGuest_User_NoOwnership() {
        setUserAuth("otherUser");
        when(guestDao.findById("G1")).thenReturn(Optional.of(guest1));
        assertThrows(AccessDeniedException.class, () -> guestService.updateGuest(guest1));
    }

    @Test
    @DisplayName("Обновление гостя - ошибка DAO")
    public void updateGuest_DaoException() {
        setAdminAuth();
        when(guestDao.update(any(Guest.class))).thenThrow(new RuntimeException("DB error"));
        DaoException exception = assertThrows(DaoException.class, () -> guestService.updateGuest(guest1));
        assertEquals("Ошибка обновления гостя", exception.getMessage());
    }

    @Test
    @DisplayName("Добавление услуги гостю")
    public void addServiceToGuest_Positive() {
        setAdminAuth();
        Service service = new Service("S1", "Spa", 500, "Доступ в спа-зону");
        when(guestDao.findById("G1")).thenReturn(Optional.of(guest1));
        when(serviceService.getServiceById("S1")).thenReturn(service);
        GuestServiceUsage savedUsage = new GuestServiceUsage(service, LocalDate.now(), guest1);
        when(usageDao.save(any(GuestServiceUsage.class))).thenReturn(savedUsage);
        GuestServiceUsage result = guestService.addServiceToGuest("G1", "S1");
        assertNotNull(result);
        assertEquals("S1", result.getService().getId());
    }

    @Test
    @DisplayName("Добавление услуги гостю - гость не найден")
    public void addServiceToGuest_GuestNotFound() {
        setAdminAuth();
        when(guestDao.findById("G999")).thenReturn(Optional.empty());
        assertThrows(DaoException.class, () -> guestService.addServiceToGuest("G999", "S1"));
    }

    @Test
    @DisplayName("Добавление услуги гостю пользователем без прав")
    public void addServiceToGuest_User_NoOwnership() {
        setUserAuth("otherUser");
        Service service = new Service("S1", "Spa", 500, "Доступ в спа-зону");
        when(guestDao.findById("G1")).thenReturn(Optional.of(guest1));
        when(serviceService.getServiceById("S1")).thenReturn(service);
        assertThrows(AccessDeniedException.class, () -> guestService.addServiceToGuest("G1", "S1"));
    }

    @Test
    @DisplayName("Получение гостей по комнате если разрешено - администратор")
    public void getGuestsByRoomIfAllowed_Admin() {
        setAdminAuth();
        when(guestDao.findByRoomNumber(101)).thenReturn(List.of(guest1));
        List<Guest> guests = guestService.getGuestsByRoomIfAllowed(101);
        assertNotNull(guests);
        assertEquals(1, guests.size());
    }

    @Test
    @DisplayName("Получение гостей по комнате если разрешено - чужая комната")
    public void getGuestsByRoomIfAllowed_User_OtherRoom() {
        setUserAuth("user1");
        when(guestDao.findByUsername("user1")).thenReturn(Optional.of(guest1));
        List<Guest> guests = guestService.getGuestsByRoomIfAllowed(102);
        assertNull(guests);
    }
}