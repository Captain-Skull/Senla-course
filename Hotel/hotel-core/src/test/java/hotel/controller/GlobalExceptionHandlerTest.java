package hotel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.DaoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GlobalExceptionHandlerTest {

    private ObjectMapper objectMapper;
    private MockMvc mockMvc;

    @RestController
    @RequestMapping("/test")
    static class TestController {
        @RequestMapping("/dao-error")
        public void throwDaoException() {
            throw new DaoException("DAO error");
        }

        @RequestMapping("/validation-error")
        public void throwValidationException() {
            throw new IllegalArgumentException("Validation error");
        }

        @RequestMapping("/illegal-argument")
        public void throwIllegalArgumentException() {
            throw new IllegalArgumentException("Illegal argument");
        }

        @RequestMapping("/general-error")
        public void throwGeneralException() throws Exception {
            throw new Exception("General error");
        }
    }

    @BeforeEach
    public void setUp() {
        objectMapper = TestUtils.createObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    @DisplayName("Обработка DaoException")
    public void testHandleDaoException() throws Exception {
        mockMvc.perform(get("/test/dao-error"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("DAO error"));
    }



    @Test
    @DisplayName("Обработка ValidationException")
    public void testHandleValidationException() throws Exception {
        mockMvc.perform(get("/test/validation-error"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation error"));
    }

    @Test
    @DisplayName("Обработка IllegalArgumentException")
    public void testHandleIllegalArgumentException() throws Exception {
        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Illegal argument"));
    }

    @Test
    @DisplayName("Обработка общего Exception")
    public void testHandleGeneralException() throws Exception {
        mockMvc.perform(get("/test/general-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("General error"))
                .andExpect(jsonPath("$.cause").value("unknown"));
    }
}
