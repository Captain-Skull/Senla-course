package com.senla.pas.service;

import com.senla.pas.dao.RoleDao;
import com.senla.pas.dao.UserDao;
import com.senla.pas.dto.request.LoginRequest;
import com.senla.pas.dto.request.RegisterRequest;
import com.senla.pas.dto.response.AuthResponse;
import com.senla.pas.dto.response.UserResponse;
import com.senla.pas.entity.Role;
import com.senla.pas.entity.User;
import com.senla.pas.exception.PasException;
import com.senla.pas.exception.ResourceAlreadyExistsException;
import com.senla.pas.mapper.UserMapper;
import com.senla.pas.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserDao userDao;
    @Mock
    private RoleDao roleDao;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private AuthService authService;

    @Test
    void register_positive() {
        RegisterRequest request = new RegisterRequest("john", "john@mail.com", "secret123");
        Role role = new Role();
        role.setName("ROLE_USER");
        Authentication authentication = mock(Authentication.class);
        UserResponse userResponse = new UserResponse();
        when(userDao.existsByUsername("john")).thenReturn(false);
        when(userDao.existsByEmail("john@mail.com")).thenReturn(false);
        when(roleDao.findByName("ROLE_USER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("secret123")).thenReturn("encoded");
        doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(55L);
            return null;
        }).when(userDao).save(any(User.class));
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication, 55L)).thenReturn("token");
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        AuthResponse result = authService.register(request);

        assertEquals("token", result.getAccessToken());
        assertSame(userResponse, result.getUser());
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userDao).save(captor.capture());
        assertEquals("encoded", captor.getValue().getPassword());
    }

    @Test
    void register_negative_usernameExists() {
        when(userDao.existsByUsername("john")).thenReturn(true);
        assertThrows(ResourceAlreadyExistsException.class,
                () -> authService.register(new RegisterRequest("john", "e@mail.com", "pwd")));
    }

    @Test
    void register_npeSafety_nullFields() {
        RegisterRequest request = new RegisterRequest(null, null, null);
        when(userDao.existsByUsername(null)).thenReturn(false);
        when(userDao.existsByEmail(null)).thenReturn(false);
        when(roleDao.findByName("ROLE_USER")).thenReturn(Optional.empty());
        assertThrows(PasException.class, () -> authService.register(request));
    }

    @Test
    void login_positive() {
        LoginRequest request = new LoginRequest("john", "pwd");
        Authentication authentication = mock(Authentication.class);
        User user = new User();
        user.setId(10L);
        UserResponse userResponse = new UserResponse();
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userDao.findByUsernameOrEmail("john")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(authentication, 10L)).thenReturn("jwt");
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        AuthResponse result = authService.login(request);

        assertEquals("jwt", result.getAccessToken());
        assertSame(userResponse, result.getUser());
    }

    @Test
    void login_negative_userNotFound() {
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(userDao.findByUsernameOrEmail("missing")).thenReturn(Optional.empty());
        assertThrows(PasException.class, () -> authService.login(new LoginRequest("missing", "pwd")));
    }

    @Test
    void login_npeSafety_nullUsernameOrEmail() {
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(userDao.findByUsernameOrEmail(null)).thenReturn(Optional.empty());
        assertThrows(PasException.class, () -> authService.login(new LoginRequest(null, "pwd")));
        verify(userDao).findByUsernameOrEmail(eq(null));
    }
}
