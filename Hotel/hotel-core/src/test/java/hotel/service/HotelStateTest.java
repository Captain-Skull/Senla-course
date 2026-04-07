package hotel.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class HotelStateTest {

    @Test
    @DisplayName("Получение текущего дня. Позитивный сценарий")
    public void getCurrentDay_Positive() {
        HotelState hotelState = new HotelState();

        assertNotNull(hotelState.getCurrentDay());
    }

    @Test
    @DisplayName("Переход к следующему дню. Позитивный сценарий")
    public void nextDay_Positive() {
        HotelState hotelState = new HotelState();
        LocalDate current = hotelState.getCurrentDay();

        LocalDate next = hotelState.nextDay();

        assertEquals(current.plusDays(1), next);
        assertEquals(current.plusDays(1), hotelState.getCurrentDay());
    }

    @Test
    @DisplayName("Сохранение состояния. Позитивный сценарий")
    public void save_Positive() {
        HotelState hotelState = new HotelState();

        assertDoesNotThrow(hotelState::save);
    }

    @Test
    @DisplayName("Загрузка состояния. Позитивный сценарий")
    public void load_Positive() {
        HotelState hotelState = new HotelState();
        hotelState.save();

        HotelState loaded = new HotelState();

        assertDoesNotThrow(loaded::load);
    }
}