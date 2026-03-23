package hotel.controller;

import hotel.service.HotelServiceFacade;
import hotel.service.HotelState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/hotel")
public class HotelController {

    private final HotelState hotelState;
    private final HotelServiceFacade hotelService;

    @Autowired
    public HotelController(HotelState hotelState, HotelServiceFacade hotelService) {
        this.hotelState = hotelState;
        this.hotelService = hotelService;
    }

    @GetMapping("/date")
    public Map<String, LocalDate> getCurrentDate() {
        return Map.of("currentDay", hotelState.getCurrentDay());
    }

    @PostMapping("/next-day")
    public Map<String, LocalDate> nextDay() {
        LocalDate newDay = hotelService.nextDay();
        return Map.of("currentDay", newDay);
    }

    @PostMapping("/save")
    public ResponseEntity<Map<String, String>> saveState() {
        hotelState.save();
        return ResponseEntity.ok(Map.of("message", "Состояние сохранено"));
    }
}
