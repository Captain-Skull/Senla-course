package config;

import hotel.HotelConfig;
import hotel.HotelModel;
import hotel.StatePersistenceService;
import hotel.service.HotelService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan(basePackages = {
        "hotel",
        "hotel.connection",
        "hotel.dao",
        "hotel.service",
        "contexts"
})
@PropertySources({
        @PropertySource("classpath:hotel.properties"),
        @PropertySource("classpath:database.properties")
})
public class AppConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public HotelModel hotelModel(HotelConfig hotelConfig, HotelService hotelService) {
        HotelModel model = StatePersistenceService.loadHotelModel();

        if (model == null) {
            System.out.println("⚠️ Создаётся новая модель");
            model = new HotelModel();
        } else {
            System.out.println("✅ Загружено сохранённое состояние модели");
        }

        model.setHotelConfig(hotelConfig);
        model.setHotelService(hotelService);

        return model;
    }
}