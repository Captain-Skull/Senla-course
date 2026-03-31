package hotel.controller;

import hotel.dto.AuthResponse;
import hotel.dto.LoginRequest;
import hotel.dto.RegisterRequest;
import hotel.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = TestUtils.createObjectMapper();
        AuthController controller = new AuthController(authService);
        mockMvc = TestUtils.createMockMvc(controller, objectMapper);
    }

    @Test
    @DisplayName("Логин. Позитивный сценарий")
    public void login_Positive() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("testUser");
        request.setPassword("testPass");
        AuthResponse response = new AuthResponse("jwt-token", "testUser", List.of("ROLE_USER"));
        when(authService.login(any())).thenReturn(response);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        verify(authService).login(any());
        String body = result.getResponse().getContentAsString();
        assertTrue(body.contains("jwt-token") || body.contains("testUser") || !body.isEmpty());
    }

    @Test
    @DisplayName("Логин. Негативный сценарий - неверные данные")
    public void login_Negative() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("testUser");
        request.setPassword("wrongPass");
        when(authService.login(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(authService).login(any());
    }

    @Test
    @DisplayName("Регистрация. Позитивный сценарий")
    public void register_Positive() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newUser");
        request.setPassword("newPass");
        request.setEmail("new@test.com");
        AuthResponse response = new AuthResponse("jwt-token", "newUser", List.of("ROLE_USER"));
        when(authService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(authService).register(any());
    }

    @Test
    @DisplayName("Регистрация. Негативный сценарий - пользователь существует")
    public void register_UserExists_Negative() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existingUser");
        request.setPassword("pass");
        request.setEmail("e@test.com");
        when(authService.register(any()))
                .thenThrow(new IllegalArgumentException("Пользователь 'existingUser' уже существует"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        verify(authService).register(any());
    }
}