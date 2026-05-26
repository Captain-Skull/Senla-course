package com.senla.pas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

public abstract class AbstractControllerTest {

    protected MockMvc mockMvc;

    protected ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
    }

    protected RequestPostProcessor authUser(String username, String... roles) {
        return SecurityMockMvcRequestPostProcessors.user(username).roles(roles);
    }

    protected MockMvc buildMockMvc(Object controller, Object... advices) {
        var builder = MockMvcBuilders.standaloneSetup(controller);
        if (advices != null && advices.length > 0) {
            builder.setControllerAdvice(advices);
        }
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        builder.setValidator(validator);
        return builder.build();
    }
}
