package hotel.service;

import hotel.Role;
import hotel.User;
import hotel.dao.UserDao;
import hotel.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class CustomUserDetailsServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User user;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User("user", "password", "user@test.com");
        Role role = new Role("ROLE_USER");
        user.addRole(role);
    }

    @Test
    @DisplayName("Загрузка пользователя по имени. Позитивный сценарий")
    public void testLoadUserByUsername_Positive() {
        when(userDao.findByUsername("user")).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("user");
        CustomUserDetails expected = new CustomUserDetails(user);

        assertNotNull(userDetails);
        assertEquals(expected.getUsername(), userDetails.getUsername());
        assertEquals(expected.getPassword(), userDetails.getPassword());
        assertInstanceOf(CustomUserDetails.class, userDetails);
    }

    @Test
    @DisplayName("Загрузка пользователя по имени. Негативный сценарий - пользователь не найден")
    public void testLoadUserByUsername_UserNotFound() {
        when(userDao.findByUsername("nonexistent")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("nonexistent");
        });

        assertEquals("Пользователь не найден: nonexistent", exception.getMessage());
    }
}
