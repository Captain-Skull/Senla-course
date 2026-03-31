package hotel.service;

import enums.RoomSort;
import enums.RoomStatus;
import enums.RoomType;
import enums.SortDirection;
import exceptions.DaoException;
import hotel.HotelConfig;
import hotel.Room;
import hotel.RoomGuestHistory;
import hotel.dao.RoomDao;
import hotel.dao.RoomGuestHistoryDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RoomServiceTest {

    @Mock
    private RoomDao roomDao;

    @Mock
    private RoomGuestHistoryDao historyDao;

    @Mock
    private HotelState hotelState;

    @Mock
    private HotelConfig config;

    @InjectMocks
    private RoomService roomService;

    private Room room101;
    private Room room102;
    private Room room103;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(hotelState.getCurrentDay()).thenReturn(LocalDate.now());

        room101 = new Room(101, RoomType.ECONOM, 1000, 1);
        room102 = new Room(102, RoomType.STANDARD, 2000, 2);
        room103 = new Room(103, RoomType.LUXURY, 1500, 3);
        room102.setStatus(RoomStatus.OCCUPIED);
        room102.setEndDate(hotelState.getCurrentDay().plusDays(5));
        room102.setDaysUnderStatus(3);
    }

    @Test
    @DisplayName("Получение всех комнат. Позитивный сценарий")
    public void getAllRooms_Positive() {
        when(roomDao.findAll()).thenReturn(List.of(room101, room102));
        List<Room> rooms = roomService.getAllRooms();
        assertNotNull(rooms);
        assertEquals(2, rooms.size());
        assertTrue(rooms.contains(room101));
        assertTrue(rooms.contains(room102));
    }

    @Test
    @DisplayName("Получение всех комнат. Негативный сценарий - нет комнат")
    public void getAllRooms_Negative() {
        when(roomDao.findAll()).thenReturn(List.of());
        List<Room> rooms = roomService.getAllRooms();
        assertTrue(rooms.isEmpty());
    }

    @Test
    @DisplayName("Получение доступных комнат. Позитивный сценарий")
    public void getAvailableRooms_Positive() {
        when(roomDao.findAvailable()).thenReturn(List.of(room101));
        List<Room> rooms = roomService.getAvailableRooms();
        assertNotNull(rooms);
        assertEquals(1, rooms.size());
        assertTrue(rooms.contains(room101));
    }

    @Test
    @DisplayName("Получение доступных комнат. Негативный сценарий - нет доступных комнат")
    public void getAvailableRooms_Negative() {
        when(roomDao.findAvailable()).thenReturn(List.of());
        List<Room> rooms = roomService.getAvailableRooms();
        assertTrue(rooms.isEmpty());
    }

    @Test
    @DisplayName("Получение комнаты по номеру. Позитивный сценарий")
    public void getRoomByNumber_Positive() {
        when(roomDao.findById(101)).thenReturn(Optional.of(room101));
        Room room = roomService.getRoomByNumber(101);
        assertNotNull(room);
        assertEquals(101, room.getNumber());
        assertEquals(RoomType.ECONOM, room.getType());
        assertEquals(1000, room.getPrice());
        assertEquals(1, room.getCapacity());
    }

    @Test
    @DisplayName("Получение комнаты по номеру. Негативный сценарий - комната не найдена")
    public void getRoomByNumber_Negative() {
        when(roomDao.findById(999)).thenReturn(Optional.empty());
        DaoException exception = assertThrows(DaoException.class, () -> roomService.getRoomByNumber(999));
        assertEquals("Комната не найдена: 999", exception.getMessage());
    }

    @Test
    @DisplayName("Проверка существования комнаты. Позитивный сценарий")
    public void isRoomExists_Positive() {
        when(roomDao.findById(101)).thenReturn(Optional.of(room101));
        assertTrue(roomService.isRoomExists(101));
    }

    @Test
    @DisplayName("Проверка существования комнаты. Негативный сценарий")
    public void isRoomExists_Negative() {
        when(roomDao.findById(999)).thenReturn(Optional.empty());
        assertFalse(roomService.isRoomExists(999));
    }

    @Test
    @DisplayName("Обновление цены комнаты. Позитивный сценарий")
    public void updateRoomPrice_Positive() {
        when(roomDao.findById(101)).thenReturn(Optional.of(room101));
        when(roomDao.update(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));
        Room updatedRoom = roomService.updateRoomPrice(101, 1500);
        assertNotNull(updatedRoom);
        assertEquals(1500, updatedRoom.getPrice());
    }

    @Test
    @DisplayName("Обновление цены комнаты. Негативный сценарий - комната не найдена")
    public void updateRoomPrice_RoomNotFound() {
        when(roomDao.findById(999)).thenReturn(Optional.empty());
        DaoException exception = assertThrows(DaoException.class, () -> roomService.updateRoomPrice(999, 1500));
        assertEquals("Ошибка обновления цены комнаты", exception.getMessage());
    }

    @Test
    @DisplayName("Обновление цены комнаты. Негативный сценарий - ошибка при обновлении")
    public void updateRoomPrice_DaoException() {
        when(roomDao.findById(101)).thenReturn(Optional.of(room101));
        when(roomDao.update(any(Room.class))).thenThrow(new RuntimeException("DB error"));
        DaoException exception = assertThrows(DaoException.class, () -> roomService.updateRoomPrice(101, 1500));
        assertEquals("Ошибка обновления цены комнаты", exception.getMessage());
    }

    @Test
    @DisplayName("Обновление комнаты. Позитивный сценарий")
    public void updateRoom_Positive() {
        when(roomDao.update(any(Room.class))).thenReturn(room101);
        Room updatedRoom = roomService.updateRoom(room101);
        assertNotNull(updatedRoom);
        assertEquals(room101, updatedRoom);
    }

    @Test
    @DisplayName("Обновление комнаты. Негативный сценарий - ошибка при обновлении")
    public void updateRoom_DaoException() {
        when(roomDao.update(any(Room.class))).thenThrow(new RuntimeException("DB error"));
        DaoException exception = assertThrows(DaoException.class, () -> roomService.updateRoom(room101));
        assertEquals("Ошибка обновления комнаты", exception.getMessage());
    }

    @Test
    @DisplayName("Установка комнаты на обслуживание. Позитивный сценарий")
    public void setRoomUnderMaintenance_Positive() {
        when(roomDao.findById(101)).thenReturn(Optional.of(room101));
        when(roomDao.update(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));
        Room updatedRoom = roomService.setRoomUnderMaintenance(101, 5);
        assertNotNull(updatedRoom);
        assertEquals(RoomStatus.MAINTENANCE, updatedRoom.getStatus());
        assertEquals(5, updatedRoom.getDaysUnderStatus());
    }

    @Test
    @DisplayName("Установка комнаты на обслуживание. Негативный сценарий - комната не найдена")
    public void setRoomUnderMaintenance_RoomNotFound() {
        when(roomDao.findById(999)).thenReturn(Optional.empty());
        DaoException exception = assertThrows(DaoException.class, () -> roomService.setRoomUnderMaintenance(999, 5));
        assertEquals("Ошибка перевода комнаты на обслуживание", exception.getMessage());
    }

    @Test
    @DisplayName("Установка комнаты на обслуживание. Негативный сценарий - ошибка при обновлении")
    public void setRoomUnderMaintenance_DaoException() {
        when(roomDao.findById(101)).thenReturn(Optional.of(room101));
        when(roomDao.update(any(Room.class))).thenThrow(new RuntimeException("DB error"));
        DaoException exception = assertThrows(DaoException.class, () -> roomService.setRoomUnderMaintenance(101, 5));
        assertEquals("Ошибка перевода комнаты на обслуживание", exception.getMessage());
    }

    @Test
    @DisplayName("Установка комнаты на уборку. Позитивный сценарий")
    public void setRoomCleaning_Positive() {
        when(roomDao.findById(101)).thenReturn(Optional.of(room101));
        when(roomDao.update(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));
        Room updatedRoom = roomService.setRoomCleaning(101);
        assertNotNull(updatedRoom);
        assertEquals(RoomStatus.CLEANING, updatedRoom.getStatus());
        assertEquals(1, updatedRoom.getDaysUnderStatus());
    }

    @Test
    @DisplayName("Установка комнаты на уборку. Негативный сценарий - комната не найдена")
    public void setRoomCleaning_RoomNotFound() {
        when(roomDao.findById(999)).thenReturn(Optional.empty());
        DaoException exception = assertThrows(DaoException.class, () -> roomService.setRoomCleaning(999));
        assertEquals("Ошибка уборки комнаты", exception.getMessage());
    }

    @Test
    @DisplayName("Установка комнаты на уборку. Негативный сценарий - ошибка при обновлении")
    public void setRoomCleaning_DaoException() {
        when(roomDao.findById(101)).thenReturn(Optional.of(room101));
        when(roomDao.update(any(Room.class))).thenThrow(new RuntimeException("DB error"));
        DaoException exception = assertThrows(DaoException.class, () -> roomService.setRoomCleaning(101));
        assertEquals("Ошибка уборки комнаты", exception.getMessage());
    }

    @Test
    @DisplayName("Сделать комнату доступной. Позитивный сценарий")
    public void setRoomAvailable_Positive() {
        room103.setCleaning(LocalDate.now());
        when(roomDao.findById(103)).thenReturn(Optional.of(room103));
        when(roomDao.update(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));
        Room updatedRoom = roomService.setRoomAvailable(103);
        assertNotNull(updatedRoom);
        assertEquals(RoomStatus.AVAILABLE, updatedRoom.getStatus());
        assertEquals(0, updatedRoom.getDaysUnderStatus());
    }

    @Test
    @DisplayName("Сделать комнату доступной. Негативный сценарий - комната не найдена")
    public void setRoomAvailable_RoomNotFound() {
        when(roomDao.findById(999)).thenReturn(Optional.empty());
        DaoException exception = assertThrows(DaoException.class, () -> roomService.setRoomAvailable(999));
        assertEquals("Ошибка перевода комнаты в доступный режим", exception.getMessage());
    }

    @Test
    @DisplayName("Сделать комнату доступной. Негативный сценарий - комната занята")
    public void setRoomAvailable_RoomOccupied() {
        when(roomDao.findById(102)).thenReturn(Optional.of(room102));

        Room updatedRoom = roomService.setRoomAvailable(102);

        assertNotNull(updatedRoom);
        assertEquals(RoomStatus.OCCUPIED, updatedRoom.getStatus());
        verify(roomDao, never()).update(any(Room.class));
    }

    @Test
    @DisplayName("Сделать комнату доступной. Негативный сценарий - ошибка при обновлении")
    public void setRoomAvailable_DaoException() {
        when(roomDao.findById(103)).thenReturn(Optional.of(room103));
        when(roomDao.update(any(Room.class))).thenThrow(new RuntimeException("DB error"));
        DaoException exception = assertThrows(DaoException.class, () -> roomService.setRoomAvailable(103));
        assertEquals("Ошибка перевода комнаты в доступный режим", exception.getMessage());
    }

    @Test
    @DisplayName("Получение доступных комнат по дате. Позитивный сценарий")
    public void getAvailableRoomsByDate_Positive() {
        when(hotelState.getCurrentDay()).thenReturn(LocalDate.now());
        when(roomDao.findAll()).thenReturn(List.of(room101, room102));
        Map<Integer, Room> availableRoomsByDate = roomService.getAvailableRoomsByDate(4);
        assertNotNull(availableRoomsByDate);
        assertTrue(availableRoomsByDate.containsKey(101));
    }

    @Test
    @DisplayName("Получение доступных комнат по дате. Негативный сценарий - нет доступных комнат")
    public void getAvailableRoomsByDate_Negative() {
        when(hotelState.getCurrentDay()).thenReturn(LocalDate.now());
        when(roomDao.findAll()).thenReturn(List.of(room102));
        Map<Integer, Room> availableRoomsByDate = roomService.getAvailableRoomsByDate(1);
        assertFalse(availableRoomsByDate.containsKey(102));
    }

    @Test
    @DisplayName("Получение истории гостей комнаты. Позитивный сценарий")
    public void getRoomGuestHistory_Positive() {
        RoomGuestHistory guest1 = new RoomGuestHistory("G1", "John", "Doe", 101, 1);
        RoomGuestHistory guest2 = new RoomGuestHistory("G2", "Jane", "Smith", 101, 1);
        RoomGuestHistory guest3 = new RoomGuestHistory("G3", "Ded", "Moroz", 101, 2);
        List<List<RoomGuestHistory>> expectedHistory = List.of(List.of(guest1, guest2), List.of(guest3));
        when(config.getRoomHistorySize()).thenReturn(2);
        when(historyDao.getPreviousGuestGroups(102, 2)).thenReturn(expectedHistory);
        List<List<RoomGuestHistory>> actualHistory = roomService.getRoomHistory(102);
        assertNotNull(actualHistory);
        assertEquals(2, actualHistory.size());
        verify(historyDao).getPreviousGuestGroups(102, 2);
        verify(config).getRoomHistorySize();
    }

    @Test
    @DisplayName("Получение истории гостей комнаты. Негативный сценарий - нет истории")
    public void getRoomGuestHistory_Negative() {
        when(config.getRoomHistorySize()).thenReturn(3);
        when(historyDao.getPreviousGuestGroups(101, 3)).thenReturn(List.of());
        List<List<RoomGuestHistory>> actualHistory = roomService.getRoomHistory(101);
        assertTrue(actualHistory.isEmpty());
        verify(historyDao).getPreviousGuestGroups(101, 3);
    }

    @Test
    @DisplayName("Сортировка комнат по цене по возрастанию. Позитивный сценарий")
    public void sortRoomsByPrice_Positive() {
        List<Room> rooms = new ArrayList<>(List.of(room101, room102, room103));
        Map<Integer, Room> result = roomService.sortRooms(rooms, RoomSort.PRICE, SortDirection.ASC);
        List<Integer> order = new ArrayList<>(result.keySet());
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(101, order.get(0));
        assertEquals(103, order.get(1));
        assertEquals(102, order.get(2));
    }

    @Test
    @DisplayName("Сортировка комнат по вместимости по убыванию. Позитивный сценарий")
    public void sortRoomsByCapacity_Positive() {
        List<Room> rooms = new ArrayList<>(List.of(room101, room102, room103));
        Map<Integer, Room> result = roomService.sortRooms(rooms, RoomSort.CAPACITY, SortDirection.DESC);
        List<Integer> order = new ArrayList<>(result.keySet());
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(103, order.get(0));
        assertEquals(102, order.get(1));
        assertEquals(101, order.get(2));
    }

    @Test
    @DisplayName("Сортировка комнат по типу по возрастанию. Позитивный сценарий")
    public void sortRoomsByType_Positive() {
        List<Room> rooms = new ArrayList<>(List.of(room101, room102, room103));
        Map<Integer, Room> result = roomService.sortRooms(rooms, RoomSort.TYPE, SortDirection.ASC);
        List<Integer> order = new ArrayList<>(result.keySet());
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(101, order.get(0));
        assertEquals(102, order.get(1));
        assertEquals(103, order.get(2));
    }

    @Test
    @DisplayName("Сортировка комнат по цене. Негативный сценарий - пустой список")
    public void sortRoomsByPrice_EmptyList() {
        List<Room> rooms = List.of();
        Map<Integer, Room> result = roomService.sortRooms(rooms, RoomSort.PRICE, SortDirection.ASC);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}