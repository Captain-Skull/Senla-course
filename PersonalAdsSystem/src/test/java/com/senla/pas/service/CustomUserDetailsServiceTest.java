package com.senla.pas.service;

import com.senla.pas.dao.UserDao;
import com.senla.pas.entity.Privilege;
import com.senla.pas.entity.Role;
import com.senla.pas.entity.User;
import com.senla.pas.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private UserDao userDao;
    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    void loadUserByUsername_positive() {
        Privilege read = new Privilege(1L, "READ_AD");
        Role role = new Role();
        role.setName("ROLE_USER");
        role.setPrivileges(Set.of(read));
        User user = new User();
        user.setId(10L);
        user.setUsername("john");
        user.setPassword("pwd");
        user.setRoles(Set.of(role));
        when(userDao.findByUsernameOrEmail("john")).thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername("john");

        assertInstanceOf(CustomUserDetails.class, result);
        assertEquals(2, result.getAuthorities().size());
    }

    @Test
    void loadUserByUsername_negative_notFound() {
        when(userDao.findByUsernameOrEmail("x")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("x"));
    }

    @Test
    void loadUserByUsername_npeSafety_nullInput() {
        when(userDao.findByUsernameOrEmail(null)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername(null));
    }
}
