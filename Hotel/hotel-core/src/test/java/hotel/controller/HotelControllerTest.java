package hotel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hotel.service.HotelServiceFacade;
import hotel.service.HotelState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class HotelControllerTest {

    private ObjectMapper objectMapper;
    private MockMvc mockMvc;

    @Mock
    private HotelState hotelState;

    @Mock
    private HotelServiceFacade hotelService;

    @InjectMocks
    private HotelController hotelController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = TestUtils.createObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(hotelController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    @DisplayName("Получение текущей даты. Позитивный сценарий")
    public void getCurrentDate_Positive() throws Exception {
        when(hotelState.getCurrentDay()).thenReturn(LocalDate.of(2025, 1, 15));

        mockMvc.perform(get("/api/hotel/date"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentDay").value("2025-01-15"));
    }

    @Test
    @DisplayName("Переход на следующий день. Позитивный сценарий")
    public void nextDay_Positive() throws Exception {
        when(hotelService.nextDay()).thenReturn(LocalDate.of(2025, 1, 16));

        mockMvc.perform(post("/api/hotel/next-day"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentDay").value("2025-01-16"));
    }

    @Test
    @DisplayName("Переход на следующий день. Негативный сценарий - ошибка при обработке")
    public void nextDay_Error_Negative() throws Exception {
        when(hotelService.nextDay()).thenThrow(new RuntimeException("Ошибка при переходе на следующий день"));

        mockMvc.perform(post("/api/hotel/next-day"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Ошибка при переходе на следующий день"));
    }

    @Test
    @DisplayName("Сохранение состояния. Позитивный сценарий")
    public void saveState_Positive() throws Exception {
        doNothing().when(hotelState).save();

        mockMvc.perform(post("/api/hotel/save"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Состояние сохранено"));
    }

    @Test
    @DisplayName("Сохранение состояния. Негативный сценарий - ошибка сохранения")
    public void saveState_Error_Negative() throws Exception {
        doThrow(new RuntimeException("Ошибка сохранения состояния"))
                .when(hotelState).save();

        mockMvc.perform(post("/api/hotel/save"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Ошибка сохранения состояния"));
    }
}