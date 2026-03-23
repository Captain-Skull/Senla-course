package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan(basePackages = {
        "hotel",
        "hotel.controller",
        "hotel.dao",
        "hotel.service",
        "hotel.mapper"
})
@PropertySources({
        @PropertySource("classpath:hotel.properties"),
        @PropertySource("classpath:database.properties")
})
@Import({JpaConfig.class, LiquibaseConfig.class, StateConfig.class})
public class AppConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}