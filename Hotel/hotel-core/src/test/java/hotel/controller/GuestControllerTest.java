package hotel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import enums.GuestSort;
import enums.SortDirection;
import enums.UsageServiceSort;
import exceptions.DaoException;
import hotel.Guest;
import hotel.GuestData;
import hotel.GuestServiceUsage;
import hotel.Service;
import hotel.dto.FailedImportItem;
import hotel.dto.GuestDto;
import hotel.dto.ImportResult;
import hotel.mapper.DtoMapper;
import hotel.service.GuestService;
import hotel.service.ImportExportService;
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
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class GuestControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private DtoMapper dtoMapper;

    @Mock
    private GuestService guestService;

    @Mock
    private ImportExportService importExportService;

    @InjectMocks
    private GuestController guestController;

    private Guest guest1;
    private Guest guest2;
    private GuestDto guestDto1;
    private GuestDto guestDto2;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = TestUtils.createObjectMapper();

        mockMvc = MockMvcBuilders.standaloneSetup(guestController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        guest1 = new Guest("G1", "John", "Doe");
        guest1.setRoomNumber(101);
        guest2 = new Guest("G2", "Jane", "Smith");
        guest2.setRoomNumber(102);

        guestDto1 = new GuestDto("G1", "John", "Doe", 101);
        guestDto2 = new GuestDto("G2", "Jane", "Smith", 102);
    }

    @Test
    @DisplayName("Получение всех гостей без сортировки. Позитивный сценарий")
    public void getGuests_NoSort_Positive() throws Exception {
        when(guestService.getAllGuests()).thenReturn(List.of(guest1, guest2));
        when(dtoMapper.toGuestDtoList(anyList())).thenReturn(List.of(guestDto1, guestDto2));

        mockMvc.perform(get("/api/guests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("G1"))
                .andExpect(jsonPath("$[1].id").value("G2"));
    }

    @Test
    @DisplayName("Получение гостей с сортировкой по имени. Позитивный сценарий")
    public void getGuests_SortByName_Positive() throws Exception {
        GuestData data1 = new GuestData("G1", "Doe John", 101, LocalDate.now().plusDays(3));
        GuestData data2 = new GuestData("G2", "Smith Jane", 102, LocalDate.now().plusDays(5));
        when(guestService.getSortedGuests(GuestSort.NAME, SortDirection.DESC))
                .thenReturn(List.of(data2, data1));

        mockMvc.perform(get("/api/guests")
                        .param("sortBy", "NAME")
                        .param("direction", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].guestId").value("G2"))
                .andExpect(jsonPath("$[1].guestId").value("G1"));
    }

    @Test
    @DisplayName("Получение гостей с сортировкой по дате выезда. Позитивный сценарий")
    public void getGuests_SortByCheckoutDate_Positive() throws Exception {
        GuestData data1 = new GuestData("G1", "Doe John", 101, LocalDate.now().plusDays(3));
        GuestData data2 = new GuestData("G2", "Smith Jane", 102, LocalDate.now().plusDays(5));
        when(guestService.getSortedGuests(GuestSort.CHECKOUT_DATE, SortDirection.DESC))
                .thenReturn(List.of(data1, data2));

        mockMvc.perform(get("/api/guests")
                        .param("sortBy", "CHECKOUT_DATE")
                        .param("direction", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].guestId").value("G1"))
                .andExpect(jsonPath("$[1].guestId").value("G2"));
    }

    @Test
    @DisplayName("Получение гостей без результатов. Негативный сценарий - пустой список")
    public void getGuests_Empty_Negative() throws Exception {
        when(guestService.getAllGuests()).thenReturn(List.of());
        when(dtoMapper.toGuestDtoList(anyList())).thenReturn(List.of());

        mockMvc.perform(get("/api/guests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Получение гостя по ID. Позитивный сценарий")
    public void getGuestById_Positive() throws Exception {
        when(guestService.getGuestById("G1")).thenReturn(guest1);
        when(dtoMapper.toGuestDto(guest1)).thenReturn(guestDto1);

        mockMvc.perform(get("/api/guests/G1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("G1"))
                .andExpect(jsonPath("$.firstname").value("John"))
                .andExpect(jsonPath("$.lastname").value("Doe"));
    }

    @Test
    @DisplayName("Получение гостя по ID. Негативный сценарий - гость не найден")
    public void getGuestById_NotFound_Negative() throws Exception {
        when(guestService.getGuestById("G999"))
                .thenThrow(new DaoException("Гость не найден: G999"));

        mockMvc.perform(get("/api/guests/G999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Гость не найден: G999"));
    }

    @Test
    @DisplayName("Получение количества гостей. Позитивный сценарий")
    public void getGuestsCount_Positive() throws Exception {
        when(guestService.getGuestsCount()).thenReturn(5);

        mockMvc.perform(get("/api/guests/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    @DisplayName("Получение количества гостей. Негативный сценарий - нет гостей")
    public void getGuestsCount_Zero_Negative() throws Exception {
        when(guestService.getGuestsCount()).thenReturn(0);

        mockMvc.perform(get("/api/guests/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    @Test
    @DisplayName("Получение услуг гостя. Позитивный сценарий")
    public void getServiceUsage_Positive() throws Exception {
        Service service = new Service("S1", "Spa", 500, "Доступ в спа-зону");
        GuestServiceUsage usage = new GuestServiceUsage(service, LocalDate.now(), guest1);
        when(guestService.getGuestServiceUsageList("G1", UsageServiceSort.PRICE, SortDirection.ASC))
                .thenReturn(List.of(usage));

        mockMvc.perform(get("/api/guests/G1/services")
                        .param("sortBy", "PRICE")
                        .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price").value(500));
    }

    @Test
    @DisplayName("Получение услуг гостя. Негативный сценарий - гость не найден")
    public void getServiceUsage_GuestNotFound_Negative() throws Exception {
        when(guestService.getGuestServiceUsageList("G999", UsageServiceSort.PRICE, SortDirection.ASC))
                .thenThrow(new DaoException("Не найден гость с таким id: G999"));

        mockMvc.perform(get("/api/guests/G999/services")
                        .param("sortBy", "PRICE")
                        .param("direction", "ASC"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Не найден гость с таким id: G999"));
    }

    @Test
    @DisplayName("Получение услуг гостя. Негативный сценарий - нет услуг")
    public void getServiceUsage_Empty_Negative() throws Exception {
        when(guestService.getGuestServiceUsageList("G1", UsageServiceSort.PRICE, SortDirection.ASC))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/guests/G1/services")
                        .param("sortBy", "PRICE")
                        .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Добавление услуги гостю. Позитивный сценарий")
    public void addServiceToGuest_Positive() throws Exception {
        Service service = new Service("S1", "Spa", 500, "Доступ в спа-зону");
        GuestServiceUsage usage = new GuestServiceUsage(service, LocalDate.now(), guest1);
        when(guestService.addServiceToGuest("G1", "S1")).thenReturn(usage);

        mockMvc.perform(post("/api/guests/G1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("serviceId", "S1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(500));
    }

    @Test
    @DisplayName("Добавление услуги гостю. Негативный сценарий - гость не найден")
    public void addServiceToGuest_GuestNotFound_Negative() throws Exception {
        when(guestService.addServiceToGuest("G999", "S1"))
                .thenThrow(new DaoException("Гость не найденG999"));

        mockMvc.perform(post("/api/guests/G999/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("serviceId", "S1"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Гость не найденG999"));
    }

    @Test
    @DisplayName("Добавление услуги гостю. Негативный сценарий - услуга не найдена")
    public void addServiceToGuest_ServiceNotFound_Negative() throws Exception {
        when(guestService.addServiceToGuest("G1", "S999"))
                .thenThrow(new DaoException("Услуга не найдена: S999"));

        mockMvc.perform(post("/api/guests/G1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("serviceId", "S999"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Услуга не найдена: S999"));
    }

    @Test
    @DisplayName("Импорт гостей. Позитивный сценарий")
    public void importGuests_Positive() throws Exception {
        ImportResult result = new ImportResult(5, 5);
        when(importExportService.importGuests("/tmp/guests.csv")).thenReturn(result);

        mockMvc.perform(post("/api/guests/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("filePath", "/tmp/guests.csv"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(5))
                .andExpect(jsonPath("$.imported").value(5));
    }

    @Test
    @DisplayName("Импорт гостей с частичным успехом. Негативный сценарий")
    public void importGuests_PartialSuccess_Negative() throws Exception {
        ImportResult result = new ImportResult(3, 2);
        result.addFailed(new FailedImportItem(103, "недоступна"));
        when(importExportService.importGuests("/tmp/guests.csv")).thenReturn(result);

        mockMvc.perform(post("/api/guests/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("filePath", "/tmp/guests.csv"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.imported").value(2));
    }

    @Test
    @DisplayName("Экспорт гостей. Позитивный сценарий")
    public void exportGuests_Positive() throws Exception {
        doNothing().when(importExportService).exportGuests("/tmp/guests.csv");

        mockMvc.perform(post("/api/guests/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("filePath", "/tmp/guests.csv"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Гости экспортированы"));
    }

    @Test
    @DisplayName("Экспорт гостей. Негативный сценарий - ошибка записи")
    public void exportGuests_WriteError_Negative() throws Exception {
        doThrow(new RuntimeException("Ошибка записи файла"))
                .when(importExportService).exportGuests("/tmp/guests.csv");

        mockMvc.perform(post("/api/guests/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("filePath", "/tmp/guests.csv"))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Ошибка записи файла"));
    }
}
