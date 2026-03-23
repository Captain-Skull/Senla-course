package com.bank.consumer.config;

import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan(basePackages = "com.bank.consumer")
@PropertySources({
        @PropertySource("classpath:application.properties"),
        @PropertySource("classpath:database.properties")
})
@Import({LiquibaseConfig.class, JpaConfig.class, KafkaConsumerConfig.class})
public class AppConfig {
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
