package hotel.service;

import enums.RoomStatus;
import enums.RoomType;
import exceptions.ValidationException;
import hotel.*;
import hotel.dto.GuestWithServicesDto;
import hotel.dto.ImportResult;
import hotel.dto.RoomWithGuestsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ImportExportServiceTest {

    @Mock
    private RoomService roomService;

    @Mock
    private GuestService guestService;

    @Mock
    private ServiceService serviceService;

    @Mock
    private HotelServiceFacade hotelFacade;

    @Mock
    private RoomCSVConverter roomCSVConverter;

    @Mock
    private GuestCSVConverter guestCSVConverter;

    @Mock
    private ServiceCSVConverter serviceCSVConverter;

    @InjectMocks
    private ImportExportService importExportService;

    private Room room101;
    private Room room102;
    private Room room103;
    private Guest guest1;
    private Guest guest2;
    private Service service1;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        room101 = new Room(101, RoomType.ECONOM, 1000, 2);
        room101.setStatus(RoomStatus.AVAILABLE);

        room102 = new Room(102, RoomType.STANDARD, 2000, 2);
        room102.setStatus(RoomStatus.OCCUPIED);

        room103 = new Room(103, RoomType.LUXURY, 3000, 1);
        room103.setStatus(RoomStatus.MAINTENANCE);

        guest1 = new Guest("G1", "John", "Doe");
        guest1.setRoomNumber(101);

        guest2 = new Guest("G2", "Jane", "Smith");
        guest2.setRoomNumber(102);

        service1 = new Service("S1", "Spa", 500, "Доступ в спа-зону");
    }

    @Test
    @DisplayName("Экспорт комнат. Позитивный сценарий")
    public void exportRooms_Positive() {
        when(roomService.getAllRooms()).thenReturn(List.of(room101, room102));
        when(guestService.getGuestsByRoom(101)).thenReturn(List.of(guest1));
        when(guestService.getGuestsByRoom(102)).thenReturn(List.of(guest2));

        try (MockedStatic<CSVService> csvMock = mockStatic(CSVService.class)) {
            assertDoesNotThrow(() -> importExportService.exportRooms("/tmp/rooms.csv"));

            csvMock.verify(() -> CSVService.exportToCSV(anyList(), eq("/tmp/rooms.csv"), eq(roomCSVConverter)));
        }
    }

    @Test
    @DisplayName("Экспорт гостей. Позитивный сценарий")
    public void exportGuests_Positive() {
        when(guestService.getAllGuests()).thenReturn(List.of(guest1, guest2));
        when(guestService.getGuestServices("G1")).thenReturn(List.of());
        when(guestService.getGuestServices("G2")).thenReturn(List.of());

        try (MockedStatic<CSVService> csvMock = mockStatic(CSVService.class)) {
            assertDoesNotThrow(() -> importExportService.exportGuests("/tmp/guests.csv"));

            csvMock.verify(() -> CSVService.exportToCSV(anyList(), eq("/tmp/guests.csv"), eq(guestCSVConverter)));
        }
    }

    @Test
    @DisplayName("Экспорт услуг. Позитивный сценарий")
    public void exportServices_Positive() {
        when(serviceService.getAllServices()).thenReturn(List.of(service1));

        try (MockedStatic<CSVService> csvMock = mockStatic(CSVService.class)) {
            assertDoesNotThrow(() -> importExportService.exportServices("/tmp/services.csv"));

            csvMock.verify(() -> CSVService.exportToCSV(anyList(), eq("/tmp/services.csv"), eq(serviceCSVConverter)));
        }
    }

    @Test
    @DisplayName("Импорт комнат с доступной комнатой и гостями. Позитивный сценарий")
    public void importRooms_AvailableRoomWithGuests_Positive() {
        room101.setDaysUnderStatus(5);
        RoomWithGuestsDto dto = new RoomWithGuestsDto(room101, List.of(guest1));

        try (MockedStatic<CSVService> csvMock = mockStatic(CSVService.class)) {
            csvMock.when(() -> CSVService.importFromCSV("/tmp/rooms.csv", roomCSVConverter))
                    .thenReturn(List.of(dto));

            when(roomService.isRoomExists(101)).thenReturn(true);
            when(roomService.updateRoom(room101)).thenReturn(room101);
            when(hotelFacade.checkIn(anyList(), eq(101), eq(5))).thenReturn(List.of(guest1));

            ImportResult result = importExportService.importRooms("/tmp/rooms.csv");

            assertEquals(1, result.getTotal());
            assertEquals(1, result.getImported());
            assertTrue(result.getFailed().isEmpty());
            verify(roomService).updateRoom(room101);
            verify(hotelFacade).checkIn(anyList(), eq(101), eq(5));
        }
    }

    @Test
    @DisplayName("Импорт комнат без гостей - новая комната. Позитивный сценарий")
    public void importRooms_NoGuests_NewRoom_Positive() {
        RoomWithGuestsDto dto = new RoomWithGuestsDto(room101, List.of());

        try (MockedStatic<CSVService> csvMock = mockStatic(CSVService.class)) {
            csvMock.when(() -> CSVService.importFromCSV("/tmp/rooms.csv", roomCSVConverter))
                    .thenReturn(List.of(dto));

            when(roomService.isRoomExists(101)).thenReturn(false);
            when(roomService.saveRoom(room101)).thenReturn(room101);

            ImportResult result = importExportService.importRooms("/tmp/rooms.csv");

            assertEquals(1, result.getTotal());
            assertEquals(1, result.getImported());
            assertTrue(result.getFailed().isEmpty());
            verify(roomService).saveRoom(room101);
        }
    }

    @Test
    @DisplayName("Импорт комнат - заселение выбрасывает ValidationException. Негативный сценарий")
    public void importRooms_CheckInValidationException_Negative() {
        room101.setDaysUnderStatus(3);
        RoomWithGuestsDto dto = new RoomWithGuestsDto(room101, List.of(guest1));

        try (MockedStatic<CSVService> csvMock = mockStatic(CSVService.class)) {
            csvMock.when(() -> CSVService.importFromCSV("/tmp/rooms.csv", roomCSVConverter))
                    .thenReturn(List.of(dto));

            when(roomService.isRoomExists(101)).thenReturn(true);
            when(roomService.updateRoom(room101)).thenReturn(room101);
            when(hotelFacade.checkIn(anyList(), eq(101), eq(3)))
                    .thenThrow(new ValidationException("Превышена вместимость"));

            ImportResult result = importExportService.importRooms("/tmp/rooms.csv");

            assertEquals(1, result.getTotal());
            assertEquals(0, result.getImported());
            assertEquals(1, result.getFailed().size());
            assertEquals("Превышена вместимость", result.getFailed().getFirst().getReason());
        }
    }

    @Test
    @DisplayName("Импорт комнат - занята теми же гостями. Позитивный сценарий")
    public void importRooms_OccupiedSameGuests_Positive() {
        RoomWithGuestsDto dto = new RoomWithGuestsDto(room102, List.of(guest2));

        try (MockedStatic<CSVService> csvMock = mockStatic(CSVService.class)) {
            csvMock.when(() -> CSVService.importFromCSV("/tmp/rooms.csv", roomCSVConverter))
                    .thenReturn(List.of(dto));

            when(roomService.isRoomExists(102)).thenReturn(true);
            when(roomService.updateRoom(room102)).thenReturn(room102);
            when(guestService.getGuestsByRoom(102)).thenReturn(List.of(guest2));

            ImportResult result = importExportService.importRooms("/tmp/rooms.csv");

            assertEquals(1, result.getTotal());
            assertEquals(1, result.getImported());
            assertTrue(result.getFailed().isEmpty());
            verify(guestService).updateGuest(guest2);
        }
    }

    @Test
    @DisplayName("Импорт комнат - занята другими гостями. Негативный сценарий")
    public void importRooms_OccupiedDifferentGuests_Negative() {
        Guest otherGuest = new Guest("G99", "Other", "Person");
        otherGuest.setRoomNumber(102);
        RoomWithGuestsDto dto = new RoomWithGuestsDto(room102, List.of(otherGuest));

        try (MockedStatic<CSVService> csvMock = mockStatic(CSVService.class)) {
            csvMock.when(() -> CSVService.importFromCSV("/tmp/rooms.csv", roomCSVConverter))
                    .thenReturn(List.of(dto));

            when(roomService.isRoomExists(102)).thenReturn(true);
            when(roomService.updateRoom(room102)).thenReturn(room102);
            when(guestService.getGuestsByRoom(102)).thenReturn(List.of(guest2));

            ImportResult result = importExportService.importRooms("/tmp/rooms.csv");

            assertEquals(1, result.getTotal());
            assertEquals(0, result.getImported());
            assertEquals(1, result.getFailed().size());
            assertEquals("занята другими", result.getFailed().getFirst().getReason());
        }
    }

    @Test
    @DisplayName("Импорт гостей в доступную комнату. Позитивный сценарий")
    public void importGuests_AvailableRoom_Positive() {
        GuestWithServicesDto dto = new GuestWithServicesDto(guest1, List.of());

        try (MockedStatic<CSVService> csvMock = mockStatic(CSVService.class)) {
            csvMock.when(() -> CSVService.importFromCSV("/tmp/guests.csv", guestCSVConverter))
                    .thenReturn(List.of(dto));

            when(roomService.isRoomExists(101)).thenReturn(true);
            when(roomService.getRoomByNumber(101)).thenReturn(room101);
            when(hotelFacade.checkIn(anyList(), eq(101), eq(1))).thenReturn(List.of(guest1));

            ImportResult result = importExportService.importGuests("/tmp/guests.csv");

            assertEquals(1, result.getTotal());
            assertEquals(1, result.getImported());
            assertTrue(result.getFailed().isEmpty());
        }
    }

    @Test
    @DisplayName("Импорт гостей в несуществующую комнату. Негативный сценарий")
    public void importGuests_RoomNotExists_Negative() {
        GuestWithServicesDto dto = new GuestWithServicesDto(guest1, List.of());

        try (MockedStatic<CSVService> csvMock = mockStatic(CSVService.class)) {
            csvMock.when(() -> CSVService.importFromCSV("/tmp/guests.csv", guestCSVConverter))
                    .thenReturn(List.of(dto));

            when(roomService.isRoomExists(101)).thenReturn(false);

            ImportResult result = importExportService.importGuests("/tmp/guests.csv");

            assertEquals(1, result.getTotal());
            assertEquals(0, result.getImported());
            assertEquals(1, result.getFailed().size());
            assertEquals("не существует", result.getFailed().getFirst().getReason());
        }
    }

    @Test
    @DisplayName("Импорт гостей в занятую комнату с теми же гостями. Позитивный сценарий")
    public void importGuests_OccupiedRoom_SameGuests_Positive() {
        GuestWithServicesDto dto = new GuestWithServicesDto(guest2, List.of());

        try (MockedStatic<CSVService> csvMock = mockStatic(CSVService.class)) {
            csvMock.when(() -> CSVService.importFromCSV("/tmp/guests.csv", guestCSVConverter))
                    .thenReturn(List.of(dto));

            when(roomService.isRoomExists(102)).thenReturn(true);
            when(roomService.getRoomByNumber(102)).thenReturn(room102);
            when(guestService.getGuestsByRoom(102)).thenReturn(List.of(guest2));

            ImportResult result = importExportService.importGuests("/tmp/guests.csv");

            assertEquals(1, result.getTotal());
            assertEquals(1, result.getImported());
            assertTrue(result.getFailed().isEmpty());
            verify(guestService).updateGuest(guest2);
        }
    }

    @Test
    @DisplayName("Импорт гостей в занятую комнату с другими гостями. Негативный сценарий")
    public void importGuests_OccupiedRoom_DifferentGuests_Negative() {
        Guest otherGuest = new Guest("G99", "Other", "Person");
        otherGuest.setRoomNumber(102);
        GuestWithServicesDto dto = new GuestWithServicesDto(otherGuest, List.of());

        try (MockedStatic<CSVService> csvMock = mockStatic(CSVService.class)) {
            csvMock.when(() -> CSVService.importFromCSV("/tmp/guests.csv", guestCSVConverter))
                    .thenReturn(List.of(dto));

            when(roomService.isRoomExists(102)).thenReturn(true);
            when(roomService.getRoomByNumber(102)).thenReturn(room102);
            when(guestService.getGuestsByRoom(102)).thenReturn(List.of(guest2));

            ImportResult result = importExportService.importGuests("/tmp/guests.csv");

            assertEquals(1, result.getTotal());
            assertEquals(0, result.getImported());
            assertEquals(1, result.getFailed().size());
            assertEquals("занята другими", result.getFailed().getFirst().getReason());
        }
    }

    @Test
    @DisplayName("Импорт гостей в комнату на обслуживании. Негативный сценарий")
    public void importGuests_MaintenanceRoom_Negative() {
        Guest guest3 = new Guest("G3", "Test", "Guest");
        guest3.setRoomNumber(103);
        GuestWithServicesDto dto = new GuestWithServicesDto(guest3, List.of());

        try (MockedStatic<CSVService> csvMock = mockStatic(CSVService.class)) {
            csvMock.when(() -> CSVService.importFromCSV("/tmp/guests.csv", guestCSVConverter))
                    .thenReturn(List.of(dto));

            when(roomService.isRoomExists(103)).thenReturn(true);
            when(roomService.getRoomByNumber(103)).thenReturn(room103);

            ImportResult result = importExportService.importGuests("/tmp/guests.csv");

            assertEquals(1, result.getTotal());
            assertEquals(0, result.getImported());
            assertEquals(1, result.getFailed().size());
            assertEquals("недоступна", result.getFailed().getFirst().getReason());
        }
    }

    @Test
    @DisplayName("Импорт гостей с услугами. Позитивный сценарий")
    public void importGuests_WithServices_Positive() {
        GuestServiceUsage usage = new GuestServiceUsage(service1, LocalDate.now(), guest1);
        GuestWithServicesDto dto = new GuestWithServicesDto(guest1, List.of(usage));

        try (MockedStatic<CSVService> csvMock = mockStatic(CSVService.class)) {
            csvMock.when(() -> CSVService.importFromCSV("/tmp/guests.csv", guestCSVConverter))
                    .thenReturn(List.of(dto));

            when(roomService.isRoomExists(101)).thenReturn(true);
            when(roomService.getRoomByNumber(101)).thenReturn(room101);
            when(hotelFacade.checkIn(anyList(), eq(101), eq(1))).thenReturn(List.of(guest1));
            when(guestService.addServiceToGuest(eq("G1"), eq("S1"), any(LocalDate.class)))
                    .thenReturn(usage);

            ImportResult result = importExportService.importGuests("/tmp/guests.csv");

            assertEquals(1, result.getTotal());
            assertEquals(1, result.getImported());
            assertTrue(result.getFailed().isEmpty());
            verify(guestService).addServiceToGuest(eq("G1"), eq("S1"), any(LocalDate.class));
        }
    }

    @Test
    @DisplayName("Импорт гостей - заселение выбрасывает ValidationException. Негативный сценарий")
    public void importGuests_CheckInValidationException_Negative() {
        GuestWithServicesDto dto = new GuestWithServicesDto(guest1, List.of());

        try (MockedStatic<CSVService> csvMock = mockStatic(CSVService.class)) {
            csvMock.when(() -> CSVService.importFromCSV("/tmp/guests.csv", guestCSVConverter))
                    .thenReturn(List.of(dto));

            when(roomService.isRoomExists(101)).thenReturn(true);
            when(roomService.getRoomByNumber(101)).thenReturn(room101);
            when(hotelFacade.checkIn(anyList(), eq(101), eq(1)))
                    .thenThrow(new ValidationException("Превышена вместимость"));

            ImportResult result = importExportService.importGuests("/tmp/guests.csv");

            assertEquals(1, result.getTotal());
            assertEquals(0, result.getImported());
            assertEquals(1, result.getFailed().size());
            assertEquals("Превышена вместимость", result.getFailed().getFirst().getReason());
        }
    }

    @Test
    @DisplayName("Импорт услуг - обновление существующей. Позитивный сценарий")
    public void importServices_UpdateExisting_Positive() {
        try (MockedStatic<CSVService> csvMock = mockStatic(CSVService.class)) {
            csvMock.when(() -> CSVService.importFromCSV("/tmp/services.csv", serviceCSVConverter))
                    .thenReturn(List.of(service1));

            when(serviceService.getServiceById("S1")).thenReturn(service1);

            int count = importExportService.importServices("/tmp/services.csv");

            assertEquals(1, count);
            verify(serviceService).updateService(service1);
            verify(serviceService, never()).saveService(any());
        }
    }

    @Test
    @DisplayName("Импорт услуг - сохранение новой. Позитивный сценарий")
    public void importServices_SaveNew_Positive() {
        try (MockedStatic<CSVService> csvMock = mockStatic(CSVService.class)) {
            csvMock.when(() -> CSVService.importFromCSV("/tmp/services.csv", serviceCSVConverter))
                    .thenReturn(List.of(service1));

            when(serviceService.getServiceById("S1")).thenReturn(null);

            int count = importExportService.importServices("/tmp/services.csv");

            assertEquals(1, count);
            verify(serviceService).saveService(service1);
            verify(serviceService, never()).updateService(any());
        }
    }

    @Test
    @DisplayName("Импорт нескольких комнат - частичный успех. Позитивный сценарий")
    public void importRooms_PartialSuccess_Positive() {
        RoomWithGuestsDto dto1 = new RoomWithGuestsDto(room101, List.of(guest1));
        RoomWithGuestsDto dto2 = new RoomWithGuestsDto(room102, List.of(guest2));

        Guest otherGuest = new Guest("G88", "Another", "Guest");
        otherGuest.setRoomNumber(102);

        try (MockedStatic<CSVService> csvMock = mockStatic(CSVService.class)) {
            csvMock.when(() -> CSVService.importFromCSV("/tmp/rooms.csv", roomCSVConverter))
                    .thenReturn(List.of(dto1, dto2));

            when(roomService.isRoomExists(101)).thenReturn(true);
            when(roomService.updateRoom(room101)).thenReturn(room101);
            when(hotelFacade.checkIn(anyList(), eq(101), anyInt())).thenReturn(List.of(guest1));

            when(roomService.isRoomExists(102)).thenReturn(true);
            when(roomService.updateRoom(room102)).thenReturn(room102);
            when(guestService.getGuestsByRoom(102)).thenReturn(List.of(otherGuest));

            ImportResult result = importExportService.importRooms("/tmp/rooms.csv");

            assertEquals(2, result.getTotal());
            assertEquals(1, result.getImported());
            assertEquals(1, result.getFailed().size());
        }
    }

    @Test
    @DisplayName("Импорт нескольких гостей в разные комнаты - частичный успех. Позитивный сценарий")
    public void importGuests_MultipleRooms_PartialSuccess_Positive() {
        Guest guest3 = new Guest("G3", "Test", "Guest");
        guest3.setRoomNumber(103);

        GuestWithServicesDto dto1 = new GuestWithServicesDto(guest1, List.of());
        GuestWithServicesDto dto2 = new GuestWithServicesDto(guest3, List.of());

        try (MockedStatic<CSVService> csvMock = mockStatic(CSVService.class)) {
            csvMock.when(() -> CSVService.importFromCSV("/tmp/guests.csv", guestCSVConverter))
                    .thenReturn(List.of(dto1, dto2));

            when(roomService.isRoomExists(101)).thenReturn(true);
            when(roomService.getRoomByNumber(101)).thenReturn(room101);
            when(hotelFacade.checkIn(anyList(), eq(101), eq(1))).thenReturn(List.of(guest1));

            when(roomService.isRoomExists(103)).thenReturn(true);
            when(roomService.getRoomByNumber(103)).thenReturn(room103);

            ImportResult result = importExportService.importGuests("/tmp/guests.csv");

            assertEquals(2, result.getTotal());
            assertEquals(1, result.getImported());
            assertEquals(1, result.getFailed().size());
            assertEquals("недоступна", result.getFailed().getFirst().getReason());
        }
    }
}