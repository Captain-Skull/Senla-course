package hotel.controller;

import enums.ServiceSort;
import enums.SortDirection;
import exceptions.DaoException;
import hotel.Service;
import hotel.dto.ServiceDto;
import hotel.mapper.DtoMapper;
import hotel.service.ImportExportService;
import hotel.service.ServiceService;
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

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ServiceControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private DtoMapper dtoMapper;

    @Mock
    private ServiceService serviceService;

    @Mock
    private ImportExportService importExportService;

    @InjectMocks
    private ServiceController serviceController;

    private Service service1;
    private Service service2;
    private ServiceDto serviceDto1;
    private ServiceDto serviceDto2;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = TestUtils.createObjectMapper();

        mockMvc = MockMvcBuilders.standaloneSetup(serviceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        service1 = new Service("S1", "Spa", 500, "Доступ в спа-зону");
        service2 = new Service("S2", "Dinner", 300, "Ужин в ресторане отеля");

        serviceDto1 = new ServiceDto("S1", "Spa", 500, "Доступ в спа-зону");
        serviceDto2 = new ServiceDto("S2", "Dinner", 300, "Ужин в ресторане отеля");
    }

    @Test
    @DisplayName("Получение всех услуг без сортировки. Позитивный сценарий")
    public void getServices_NoSort_Positive() throws Exception {
        when(serviceService.getAllServices()).thenReturn(List.of(service1, service2));
        when(dtoMapper.toServiceDtoList(anyList())).thenReturn(List.of(serviceDto1, serviceDto2));

        mockMvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("S1"))
                .andExpect(jsonPath("$[1].id").value("S2"));
    }

    @Test
    @DisplayName("Получение услуг с сортировкой по цене. Позитивный сценарий")
    public void getServices_SortByPrice_Positive() throws Exception {
        when(serviceService.getSortedServices(ServiceSort.PRICE, SortDirection.ASC))
                .thenReturn(List.of(service2, service1));
        when(dtoMapper.toServiceDtoList(anyList())).thenReturn(List.of(serviceDto2, serviceDto1));

        mockMvc.perform(get("/api/services")
                        .param("sortBy", "PRICE")
                        .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("S2"))
                .andExpect(jsonPath("$[1].id").value("S1"));
    }

    @Test
    @DisplayName("Получение услуг. Негативный сценарий - пустой список")
    public void getServices_Empty_Negative() throws Exception {
        when(serviceService.getAllServices()).thenReturn(List.of());
        when(dtoMapper.toServiceDtoList(anyList())).thenReturn(List.of());

        mockMvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Добавление новой услуги. Позитивный сценарий")
    public void addNewService_Positive() throws Exception {
        when(dtoMapper.toService(any(ServiceDto.class))).thenReturn(service1);
        when(serviceService.saveService(service1)).thenReturn(service1);
        when(dtoMapper.toServiceDto(service1)).thenReturn(serviceDto1);

        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceDto1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("S1"))
                .andExpect(jsonPath("$.name").value("Spa"))
                .andExpect(jsonPath("$.price").value(500));
    }

    @Test
    @DisplayName("Добавление новой услуги. Негативный сценарий - ошибка сохранения")
    public void addNewService_SaveError_Negative() throws Exception {
        when(dtoMapper.toService(any(ServiceDto.class))).thenReturn(service1);
        when(serviceService.saveService(service1))
                .thenThrow(new DaoException("Ошибка при сохранении услуги"));

        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceDto1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ошибка при сохранении услуги"));
    }

    @Test
    @DisplayName("Обновление цены услуги. Позитивный сценарий")
    public void setServicePrice_Positive() throws Exception {
        ServiceDto updatedDto = new ServiceDto("S1", "Spa", 750, "Доступ в спа-зону");

        doNothing().when(serviceService).updateServicePrice("S1", 750);
        when(serviceService.getServiceById("S1")).thenReturn(service1);
        when(dtoMapper.toServiceDto(service1)).thenReturn(updatedDto);

        mockMvc.perform(patch("/api/services/S1/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("price", 750))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("S1"))
                .andExpect(jsonPath("$.price").value(750));
    }

    @Test
    @DisplayName("Обновление цены услуги. Негативный сценарий - услуга не найдена")
    public void setServicePrice_NotFound_Negative() throws Exception {
        doThrow(new DaoException("Ошибка обновления цены услуги"))
                .when(serviceService).updateServicePrice("S999", 750);

        mockMvc.perform(patch("/api/services/S999/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("price", 750))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ошибка обновления цены услуги"));
    }

    @Test
    @DisplayName("Импорт услуг. Позитивный сценарий")
    public void importServices_Positive() throws Exception {
        when(importExportService.importServices("/tmp/services.csv")).thenReturn(3);

        mockMvc.perform(post("/api/services/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("filePath", "/tmp/services.csv"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Услуги импортированы"))
                .andExpect(jsonPath("$.count").value(3));
    }

    @Test
    @DisplayName("Импорт услуг. Негативный сценарий - ошибка чтения файла")
    public void importServices_ReadError_Negative() throws Exception {
        when(importExportService.importServices("/tmp/bad.csv"))
                .thenThrow(new RuntimeException("Ошибка чтения файла"));

        mockMvc.perform(post("/api/services/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("filePath", "/tmp/bad.csv"))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Ошибка чтения файла"));
    }

    @Test
    @DisplayName("Экспорт услуг. Позитивный сценарий")
    public void exportServices_Positive() throws Exception {
        doNothing().when(importExportService).exportServices("/tmp/services.csv");

        mockMvc.perform(post("/api/services/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("filePath", "/tmp/services.csv"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Услуги экспортированы"));
    }

    @Test
    @DisplayName("Экспорт услуг. Негативный сценарий - ошибка записи")
    public void exportServices_WriteError_Negative() throws Exception {
        doThrow(new RuntimeException("Ошибка записи файла"))
                .when(importExportService).exportServices("/tmp/services.csv");

        mockMvc.perform(post("/api/services/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("filePath", "/tmp/services.csv"))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Ошибка записи файла"));
    }
}