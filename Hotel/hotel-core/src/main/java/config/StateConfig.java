package config;

import hotel.service.HotelState;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StateConfig {

    private final HotelState hotelState;

    public StateConfig(HotelState hotelState) {
        this.hotelState = hotelState;
    }

    @PostConstruct
    public void loadState() {
        hotelState.load();
    }
}