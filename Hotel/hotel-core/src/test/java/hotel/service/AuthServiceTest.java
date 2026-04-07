package hotel.service;

import hotel.Role;
import hotel.User;
import hotel.dao.RoleDao;
import hotel.dao.UserDao;
import hotel.dto.AuthResponse;
import hotel.dto.LoginRequest;
import hotel.dto.RegisterRequest;
import hotel.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDao userDao;

    @Mock
    private RoleDao roleDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Логин. Позитивный сценарий")
    public void login_Positive() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("testpassword");
        Authentication authentication = new UsernamePasswordAuthenticationToken("testuser", "testpassword", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt-token-123");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token-123", response.getToken());
        assertEquals("testuser", response.getUsername());
        assertTrue(response.getAuthorities().contains("ROLE_USER"));
    }

    @Test
    @DisplayName("Логин. Негативный сценарий - неверные данные")
    public void login_Negative() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Неверные данные для входа"));

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> authService.login(request));
        assertEquals("Неверные данные для входа", exception.getMessage());
    }

    @Test
    @DisplayName("Регистрация. Позитивный сценарий")
    public void register_Positive() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("newpassword");
        request.setEmail("new@test.com");

        Role userRole = new Role("ROLE_USER");

        when(userDao.existsByUsername("newuser")).thenReturn(false);
        when(roleDao.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedpassword");

        Authentication authentication = new UsernamePasswordAuthenticationToken("newuser", "newpassword", List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("jwt-token-456");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt-token-456", response.getToken());
        assertEquals("newuser", response.getUsername());
        assertTrue(response.getAuthorities().contains("ROLE_USER"));
    }

    @Test
    @DisplayName("Регистрация. Негативный сценарий - пользователь уже существует")
    public void register_UserExists_Negative() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existingUser");
        request.setPassword("pass");
        request.setEmail("existing@test.com");

        when(userDao.existsByUsername("existingUser")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.register(request));

        assertEquals("Пользователь 'existingUser' уже существует", exception.getMessage());
        verify(userDao, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Регистрация. Негативный сценарий - роль не найдена")
    public void register_RoleNotFound_Negative() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newUser");
        request.setPassword("pass");
        request.setEmail("new@test.com");

        when(userDao.existsByUsername("newUser")).thenReturn(false);
        when(roleDao.findByName("ROLE_USER")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.register(request));

        assertEquals("Роль ROLE_USER не найдена", exception.getMessage());
    }
}
