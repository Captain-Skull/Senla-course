package hotel.service;

import enums.RoomSort;
import enums.RoomStatus;
import enums.RoomType;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class HotelServiceFacadeTest {

    @Mock
    private GuestService guestService;
    @Mock
    private RoomService roomService;
    @Mock
    private RoomDao roomDao;
    @Mock
    private GuestDao guestDao;
    @Mock
    private UserDao userDao;
    @Mock
    private RoomGuestHistoryDao historyDao;
    @Mock
    private HotelState hotelState;

    @InjectMocks
    private HotelServiceFacade hotelServiceFacade;

    private Room room101;
    private Room room102;
    private Guest guest1;
    private Guest guest2;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        room101 = new Room(101, RoomType.ECONOM, 1000, 2);
        room101.setStatus(RoomStatus.AVAILABLE);
        room102 = new Room(102, RoomType.STANDARD, 2000, 2);
        room102.setStatus(RoomStatus.OCCUPIED);
        room102.setEndDate(LocalDate.now().plusDays(3));
        guest1 = new Guest("G1", "John", "Doe");
        guest1.setRoomNumber(101);
        guest2 = new Guest("G2", "Jane", "Smith");
        guest2.setRoomNumber(102);
        when(hotelState.getCurrentDay()).thenReturn(LocalDate.now());
    }

    private void setAdminAuth() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", "pass",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }

    @Test
    @DisplayName("Получение информации о комнате")
    public void getRoomInformation_Positive() {
        when(roomService.getRoomByNumber(101)).thenReturn(room101);
        when(guestService.getGuestsByRoomIfAllowed(101)).thenReturn(List.of(guest1));
        RoomInfoDto info = hotelServiceFacade.getRoomInformation(101);
        assertEquals(101, info.getNumber());
        assertNotNull(info.getGuests());
    }

    @Test
    @DisplayName("Получение информации - нет доступа к гостям")
    public void getRoomInformation_NoGuestAccess() {
        when(roomService.getRoomByNumber(101)).thenReturn(room101);
        when(guestService.getGuestsByRoomIfAllowed(101)).thenReturn(null);
        RoomInfoDto info = hotelServiceFacade.getRoomInformation(101);
        assertNull(info.getGuests());
    }

    @Test
    @DisplayName("Получение отсортированных комнат")
    public void getSortedRooms_Positive() {
        when(roomService.getAllRooms()).thenReturn(List.of(room101, room102));
        Map<Integer, Room> sortedMap = new LinkedHashMap<>();
        sortedMap.put(101, room101);
        sortedMap.put(102, room102);
        when(roomService.sortRooms(anyList(), eq(RoomSort.PRICE), eq(SortDirection.ASC))).thenReturn(sortedMap);
        when(guestService.getGuestsByRoom(101)).thenReturn(List.of(guest1));
        when(guestService.getGuestsByRoom(102)).thenReturn(List.of(guest2));
        List<RoomWithGuestsDto> result = hotelServiceFacade.getSortedRooms(RoomSort.PRICE, SortDirection.ASC);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Заселение гостей")
    public void checkIn_Positive() {
        when(roomDao.findById(101)).thenReturn(Optional.of(room101));
        when(roomDao.update(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));
        when(guestDao.save(any(Guest.class))).thenReturn(guest1);
        List<Guest> result = hotelServiceFacade.checkIn(List.of(guest1), 101, 5);
        assertFalse(result.isEmpty());
        verify(roomDao).update(any(Room.class));
    }

    @Test
    @DisplayName("Заселение - комната не найдена")
    public void checkIn_RoomNotFound() {
        when(roomDao.findById(999)).thenReturn(Optional.empty());
        DaoException exception = assertThrows(DaoException.class,
                () -> hotelServiceFacade.checkIn(List.of(guest1), 999, 5));
        assertEquals("Ошибка заселения", exception.getMessage());
    }

    @Test
    @DisplayName("Заселение по запросу администратором")
    public void checkInRequest_Admin() {
        setAdminAuth();
        GuestRequest gr = new GuestRequest();
        gr.setFirstname("John");
        gr.setLastname("Doe");
        when(roomDao.findById(101)).thenReturn(Optional.of(room101));
        when(roomDao.update(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));
        when(guestDao.save(any(Guest.class))).thenReturn(guest1);
        List<Guest> result = hotelServiceFacade.checkInRequest(List.of(gr), 101, 5);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Выселение")
    public void checkOut_Positive() {
        room102.setStatus(RoomStatus.OCCUPIED);
        when(roomDao.findById(102)).thenReturn(Optional.of(room102));
        when(guestDao.findByRoomNumber(102)).thenReturn(List.of(guest2));
        when(historyDao.getNextGroupId(102)).thenReturn(1);
        when(historyDao.save(any(RoomGuestHistory.class))).thenReturn(null);
        when(guestDao.delete("G2")).thenReturn(true);
        when(roomDao.update(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));
        assertTrue(hotelServiceFacade.checkOut(102));
    }

    @Test
    @DisplayName("Выселение - комната не найдена")
    public void checkOut_RoomNotFound() {
        when(roomDao.findById(999)).thenReturn(Optional.empty());
        DaoException exception = assertThrows(DaoException.class,
                () -> hotelServiceFacade.checkOut(999));
        assertEquals("Ошибка выселения", exception.getMessage());
    }

    @Test
    @DisplayName("Выселение - нет гостей")
    public void checkOut_NoGuests() {
        room102.setStatus(RoomStatus.OCCUPIED);
        when(roomDao.findById(102)).thenReturn(Optional.of(room102));
        when(guestDao.findByRoomNumber(102)).thenReturn(List.of());
        assertFalse(hotelServiceFacade.checkOut(102));
    }

    @Test
    @DisplayName("Следующий день")
    public void nextDay_Positive() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        when(hotelState.nextDay()).thenReturn(tomorrow);
        when(hotelState.getCurrentDay()).thenReturn(tomorrow);
        when(roomService.getAllRooms()).thenReturn(List.of());
        assertEquals(tomorrow, hotelServiceFacade.nextDay());
    }

    @Test
    @DisplayName("Следующий день с автовыселением")
    public void nextDay_WithAutoCheckout() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        room102.setEndDate(tomorrow);
        when(hotelState.nextDay()).thenReturn(tomorrow);
        when(hotelState.getCurrentDay()).thenReturn(tomorrow);
        when(roomService.getAllRooms()).thenReturn(List.of(room102));
        when(roomDao.findById(102)).thenReturn(Optional.of(room102));
        when(guestDao.findByRoomNumber(102)).thenReturn(List.of(guest2));
        when(historyDao.getNextGroupId(102)).thenReturn(1);
        when(historyDao.save(any())).thenReturn(null);
        when(guestDao.delete("G2")).thenReturn(true);
        when(roomDao.update(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));
        assertEquals(tomorrow, hotelServiceFacade.nextDay());
    }
}