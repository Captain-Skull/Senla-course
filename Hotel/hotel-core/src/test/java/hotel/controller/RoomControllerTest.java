package hotel.controller;

import enums.RoomSort;
import enums.RoomStatus;
import enums.RoomType;
import enums.SortDirection;
import exceptions.DaoException;
import hotel.Guest;
import hotel.Room;
import hotel.RoomGuestHistory;
import hotel.dto.*;
import hotel.mapper.DtoMapper;
import hotel.service.GuestService;
import hotel.service.HotelServiceFacade;
import hotel.service.ImportExportService;
import hotel.service.RoomService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class RoomControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private RoomService roomService;

    @Mock
    private GuestService guestService;

    @Mock
    private DtoMapper dtoMapper;

    @Mock
    private HotelServiceFacade hotelFacade;

    @Mock
    private ImportExportService importExportService;

    @InjectMocks
    private RoomController roomController;

    private Room room101;
    private Room room102;
    private RoomDto roomDto101;
    private RoomDto roomDto102;
    private Guest guest1;
    private GuestDto guestDto1;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = TestUtils.createObjectMapper();

        mockMvc = MockMvcBuilders.standaloneSetup(roomController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        room101 = new Room(101, RoomType.ECONOM, 1000, 2);
        room101.setStatus(RoomStatus.AVAILABLE);

        room102 = new Room(102, RoomType.STANDARD, 2000, 2);
        room102.setStatus(RoomStatus.OCCUPIED);

        roomDto101 = new RoomDto(101, RoomType.ECONOM, 1000, 2, RoomStatus.AVAILABLE, 0, null);
        roomDto102 = new RoomDto(102, RoomType.STANDARD, 2000, 2, RoomStatus.OCCUPIED, 3, LocalDate.now().plusDays(3));

        guest1 = new Guest("G1", "John", "Doe");
        guest1.setRoomNumber(101);

        guestDto1 = new GuestDto("G1", "John", "Doe", 101);
    }

    @Test
    @DisplayName("Получение всех комнат без сортировки. Позитивный сценарий")
    public void getAllRooms_NoSort_Positive() throws Exception {
        when(roomService.getAllRooms()).thenReturn(List.of(room101, room102));

        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].number").value(101))
                .andExpect(jsonPath("$[1].number").value(102));
    }

    @Test
    @DisplayName("Получение всех комнат с сортировкой. Позитивный сценарий")
    public void getAllRooms_WithSort_Positive() throws Exception {
        RoomWithGuestsDto dto1 = new RoomWithGuestsDto(room101, List.of());
        RoomWithGuestsDto dto2 = new RoomWithGuestsDto(room102, List.of());
        when(hotelFacade.getSortedRooms(RoomSort.PRICE, SortDirection.ASC))
                .thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/rooms")
                        .param("sortBy", "PRICE")
                        .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Получение всех комнат. Негативный сценарий - пустой список")
    public void getAllRooms_Empty_Negative() throws Exception {
        when(roomService.getAllRooms()).thenReturn(List.of());

        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Получение доступных комнат без сортировки. Позитивный сценарий")
    public void getAvailableRooms_NoSort_Positive() throws Exception {
        when(roomService.getAvailableRooms()).thenReturn(List.of(room101));

        mockMvc.perform(get("/api/rooms/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].number").value(101));
    }

    @Test
    @DisplayName("Получение доступных комнат с сортировкой. Позитивный сценарий")
    public void getAvailableRooms_WithSort_Positive() throws Exception {
        RoomWithGuestsDto dto = new RoomWithGuestsDto(room101, List.of());
        when(hotelFacade.getSortedAvailableRooms(RoomSort.PRICE, SortDirection.ASC))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/rooms/available")
                        .param("sortBy", "PRICE")
                        .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Получение доступных комнат. Негативный сценарий - нет доступных")
    public void getAvailableRooms_Empty_Negative() throws Exception {
        when(roomService.getAvailableRooms()).thenReturn(List.of());

        mockMvc.perform(get("/api/rooms/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Получение доступных комнат по дате. Позитивный сценарий")
    public void getAvailableRoomsByDate_Positive() throws Exception {
        Map<Integer, Room> roomMap = new LinkedHashMap<>();
        roomMap.put(101, room101);
        Map<Integer, RoomDto> dtoMap = new LinkedHashMap<>();
        dtoMap.put(101, roomDto101);

        when(roomService.getAvailableRoomsByDate(5)).thenReturn(roomMap);
        when(dtoMapper.toRoomDtoMap(roomMap)).thenReturn(dtoMap);

        mockMvc.perform(get("/api/rooms/available/by-date")
                        .param("days", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.101.number").value(101));
    }

    @Test
    @DisplayName("Получение доступных комнат по дате. Негативный сценарий - нет доступных")
    public void getAvailableRoomsByDate_Empty_Negative() throws Exception {
        when(roomService.getAvailableRoomsByDate(1)).thenReturn(Map.of());
        when(dtoMapper.toRoomDtoMap(Map.of())).thenReturn(Map.of());

        mockMvc.perform(get("/api/rooms/available/by-date")
                        .param("days", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));
    }

    @Test
    @DisplayName("Получение количества доступных комнат. Позитивный сценарий")
    public void getAvailableRoomsCount_Positive() throws Exception {
        when(roomService.getAvailableRooms()).thenReturn(List.of(room101));

        mockMvc.perform(get("/api/rooms/available/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    @DisplayName("Получение информации о комнате. Позитивный сценарий")
    public void getRoomInformation_Positive() throws Exception {
        RoomInfoDto info = new RoomInfoDto();
        info.setNumber(101);
        info.setType("ECONOM");
        info.setPrice(1000);
        info.setCapacity(2);
        info.setStatus("AVAILABLE");
        info.setGuests(List.of());
        info.setGuestsAccessMessage("В комнате нет гостей");

        when(hotelFacade.getRoomInformation(101)).thenReturn(info);

        mockMvc.perform(get("/api/rooms/information/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(101))
                .andExpect(jsonPath("$.type").value("ECONOM"))
                .andExpect(jsonPath("$.price").value(1000));
    }

    @Test
    @DisplayName("Получение информации о комнате. Негативный сценарий - комната не найдена")
    public void getRoomInformation_NotFound_Negative() throws Exception {
        when(hotelFacade.getRoomInformation(999))
                .thenThrow(new DaoException("Комната не найдена: 999"));

        mockMvc.perform(get("/api/rooms/information/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Комната не найдена: 999"));
    }

    @Test
    @DisplayName("Обновление цены комнаты. Позитивный сценарий")
    public void setRoomPrice_Positive() throws Exception {
        Room updated = new Room(101, RoomType.ECONOM, 1500, 2);
        RoomDto updatedDto = new RoomDto(101, RoomType.ECONOM, 1500, 2, RoomStatus.AVAILABLE, 0, null);

        when(roomService.updateRoomPrice(101, 1500)).thenReturn(updated);
        when(dtoMapper.toRoomDto(updated)).thenReturn(updatedDto);

        mockMvc.perform(patch("/api/rooms/101/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("price", 1500))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(1500));
    }

    @Test
    @DisplayName("Обновление цены комнаты. Негативный сценарий - комната не найдена")
    public void setRoomPrice_NotFound_Negative() throws Exception {
        when(roomService.updateRoomPrice(999, 1500))
                .thenThrow(new DaoException("Комната не найдена: 999"));

        mockMvc.perform(patch("/api/rooms/999/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("price", 1500))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Комната не найдена: 999"));
    }

    @Test
    @DisplayName("Установка комнаты в доступный статус. Позитивный сценарий")
    public void setRoomAvailable_Positive() throws Exception {
        when(roomService.setRoomAvailable(101)).thenReturn(room101);
        when(dtoMapper.toRoomDto(room101)).thenReturn(roomDto101);

        mockMvc.perform(patch("/api/rooms/101/status/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("Установка комнаты в доступный статус. Негативный сценарий - комната не найдена")
    public void setRoomAvailable_NotFound_Negative() throws Exception {
        when(roomService.setRoomAvailable(999))
                .thenThrow(new DaoException("Комната не найдена: 999"));

        mockMvc.perform(patch("/api/rooms/999/status/available"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Комната не найдена: 999"));
    }

    @Test
    @DisplayName("Установка комнаты на уборку. Позитивный сценарий")
    public void setRoomCleaning_Positive() throws Exception {
        Room cleaningRoom = new Room(101, RoomType.ECONOM, 1000, 2);
        cleaningRoom.setStatus(RoomStatus.CLEANING);
        RoomDto cleaningDto = new RoomDto(101, RoomType.ECONOM, 1000, 2, RoomStatus.CLEANING, 1, LocalDate.now().plusDays(1));

        when(roomService.setRoomCleaning(101)).thenReturn(cleaningRoom);
        when(dtoMapper.toRoomDto(cleaningRoom)).thenReturn(cleaningDto);

        mockMvc.perform(patch("/api/rooms/101/status/cleaning"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLEANING"));
    }

    @Test
    @DisplayName("Установка комнаты на уборку. Негативный сценарий - комната не найдена")
    public void setRoomCleaning_NotFound_Negative() throws Exception {
        when(roomService.setRoomCleaning(999))
                .thenThrow(new DaoException("Комната не найдена: 999"));

        mockMvc.perform(patch("/api/rooms/999/status/cleaning"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Комната не найдена: 999"));
    }

    @Test
    @DisplayName("Установка комнаты на обслуживание. Позитивный сценарий")
    public void setRoomUnderMaintenance_Positive() throws Exception {
        Room maintenanceRoom = new Room(101, RoomType.ECONOM, 1000, 2);
        maintenanceRoom.setStatus(RoomStatus.MAINTENANCE);
        RoomDto maintenanceDto = new RoomDto(101, RoomType.ECONOM, 1000, 2, RoomStatus.MAINTENANCE, 5, LocalDate.now().plusDays(5));

        when(roomService.setRoomUnderMaintenance(101, 5)).thenReturn(maintenanceRoom);
        when(dtoMapper.toRoomDto(maintenanceRoom)).thenReturn(maintenanceDto);

        mockMvc.perform(patch("/api/rooms/101/status/maintenance")
                        .param("days", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MAINTENANCE"));
    }

    @Test
    @DisplayName("Установка комнаты на обслуживание. Негативный сценарий - комната не найдена")
    public void setRoomUnderMaintenance_NotFound_Negative() throws Exception {
        when(roomService.setRoomUnderMaintenance(999, 5))
                .thenThrow(new DaoException("Комната не найдена: 999"));

        mockMvc.perform(patch("/api/rooms/999/status/maintenance")
                        .param("days", "5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Комната не найдена: 999"));
    }

    @Test
    @DisplayName("Добавление новой комнаты. Позитивный сценарий")
    public void addNewRoom_Positive() throws Exception {
        when(dtoMapper.toRoom(any(RoomDto.class))).thenReturn(room101);
        when(roomService.saveRoom(room101)).thenReturn(room101);
        when(dtoMapper.toRoomDto(room101)).thenReturn(roomDto101);

        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomDto101)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.number").value(101));
    }

    @Test
    @DisplayName("Добавление новой комнаты. Негативный сценарий - ошибка сохранения")
    public void addNewRoom_SaveError_Negative() throws Exception {
        when(dtoMapper.toRoom(any(RoomDto.class))).thenReturn(room101);
        when(roomService.saveRoom(room101))
                .thenThrow(new DaoException("Ошибка сохранения комнаты"));

        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomDto101)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ошибка сохранения комнаты"));
    }

    @Test
    @DisplayName("Получение истории комнаты. Позитивный сценарий")
    public void getRoomHistory_Positive() throws Exception {
        RoomGuestHistory history = new RoomGuestHistory("G1", "John", "Doe", 101, 1);
        when(roomService.getRoomHistory(101)).thenReturn(List.of(List.of(history)));

        mockMvc.perform(get("/api/rooms/101/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].length()").value(1));
    }

    @Test
    @DisplayName("Получение истории комнаты. Негативный сценарий - пустая история")
    public void getRoomHistory_Empty_Negative() throws Exception {
        when(roomService.getRoomHistory(101)).thenReturn(List.of());

        mockMvc.perform(get("/api/rooms/101/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Заселение в комнату. Позитивный сценарий")
    public void checkIn_Positive() throws Exception {
        when(hotelFacade.checkInRequest(anyList(), eq(101), eq(5))).thenReturn(List.of(guest1));
        when(dtoMapper.toGuestDtoList(anyList())).thenReturn(List.of(guestDto1));

        CheckInRequest request = new CheckInRequest();
        GuestRequest guestRequest = new GuestRequest();
        guestRequest.setFirstname("John");
        guestRequest.setLastname("Doe");
        request.setGuests(List.of(guestRequest));
        request.setDays(5);

        mockMvc.perform(post("/api/rooms/101/checkIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.roomNumber").value(101))
                .andExpect(jsonPath("$.guests[0].id").value("G1"));
    }

    @Test
    @DisplayName("Заселение в комнату. Негативный сценарий - невозможно заселить")
    public void checkIn_Failure_Negative() throws Exception {
        when(hotelFacade.checkInRequest(anyList(), eq(101), eq(5))).thenReturn(List.of());
        when(dtoMapper.toGuestDtoList(anyList())).thenReturn(List.of());

        CheckInRequest request = new CheckInRequest();
        GuestRequest guestRequest = new GuestRequest();
        guestRequest.setFirstname("John");
        guestRequest.setLastname("Doe");
        request.setGuests(List.of(guestRequest));
        request.setDays(5);

        mockMvc.perform(post("/api/rooms/101/checkIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Заселение в комнату. Негативный сценарий - комната не найдена")
    public void checkIn_RoomNotFound_Negative() throws Exception {
        when(hotelFacade.checkInRequest(anyList(), eq(999), eq(5)))
                .thenThrow(new DaoException("Комната не найдена: 999"));

        CheckInRequest request = new CheckInRequest();
        GuestRequest guestRequest = new GuestRequest();
        guestRequest.setFirstname("John");
        guestRequest.setLastname("Doe");
        request.setGuests(List.of(guestRequest));
        request.setDays(5);

        mockMvc.perform(post("/api/rooms/999/checkIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Комната не найдена: 999"));
    }

    @Test
    @DisplayName("Выселение из комнаты. Позитивный сценарий")
    public void checkOut_Positive() throws Exception {
        room102.setDaysUnderStatus(3);
        when(roomService.getRoomByNumber(102)).thenReturn(room102);
        when(guestService.getGuestsByRoom(102)).thenReturn(List.of(guest1));
        when(hotelFacade.checkOut(102)).thenReturn(true);
        when(dtoMapper.toGuestDtoList(anyList())).thenReturn(List.of(guestDto1));

        mockMvc.perform(post("/api/rooms/102/checkOut"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.roomNumber").value(102));
    }

    @Test
    @DisplayName("Выселение из комнаты. Негативный сценарий - невозможно выселить")
    public void checkOut_Failure_Negative() throws Exception {
        when(roomService.getRoomByNumber(101)).thenReturn(room101);
        when(guestService.getGuestsByRoom(101)).thenReturn(List.of());
        when(hotelFacade.checkOut(101)).thenReturn(false);

        mockMvc.perform(post("/api/rooms/101/checkOut"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Не удалось выселить из комнаты 101"));
    }

    @Test
    @DisplayName("Выселение из комнаты. Негативный сценарий - комната не найдена")
    public void checkOut_RoomNotFound_Negative() throws Exception {
        when(roomService.getRoomByNumber(999))
                .thenThrow(new DaoException("Комната не найдена: 999"));

        mockMvc.perform(post("/api/rooms/999/checkOut"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Комната не найдена: 999"));
    }

    @Test
    @DisplayName("Импорт комнат. Позитивный сценарий")
    public void importRooms_Positive() throws Exception {
        ImportResult result = new ImportResult(3, 3);
        when(importExportService.importRooms("/tmp/rooms.csv")).thenReturn(result);

        mockMvc.perform(post("/api/rooms/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("filePath", "/tmp/rooms.csv"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.imported").value(3));
    }

    @Test
    @DisplayName("Импорт комнат с частичным успехом. Негативный сценарий")
    public void importRooms_PartialSuccess_Negative() throws Exception {
        ImportResult result = new ImportResult(3, 2);
        result.addFailed(new FailedImportItem(103, "занята другими"));
        when(importExportService.importRooms("/tmp/rooms.csv")).thenReturn(result);

        mockMvc.perform(post("/api/rooms/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("filePath", "/tmp/rooms.csv"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.imported").value(2));
    }

    @Test
    @DisplayName("Экспорт комнат. Позитивный сценарий")
    public void exportRooms_Positive() throws Exception {
        doNothing().when(importExportService).exportRooms("/tmp/rooms.csv");

        mockMvc.perform(post("/api/rooms/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("filePath", "/tmp/rooms.csv"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Комнаты экспортированы"));
    }

    @Test
    @DisplayName("Экспорт комнат. Негативный сценарий - ошибка записи")
    public void exportRooms_WriteError_Negative() throws Exception {
        doThrow(new RuntimeException("Ошибка записи файла"))
                .when(importExportService).exportRooms("/tmp/rooms.csv");

        mockMvc.perform(post("/api/rooms/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("filePath", "/tmp/rooms.csv"))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Ошибка записи файла"));
    }
}