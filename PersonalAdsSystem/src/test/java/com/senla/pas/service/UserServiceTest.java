package com.senla.pas.service;

import com.senla.pas.dao.UserDao;
import com.senla.pas.dto.request.UpdateUserRequest;
import com.senla.pas.dto.response.UserResponse;
import com.senla.pas.entity.User;
import com.senla.pas.enums.SortDirection;
import com.senla.pas.exception.AuthenticationException;
import com.senla.pas.exception.ForbiddenException;
import com.senla.pas.exception.ResourceAlreadyExistsException;
import com.senla.pas.exception.ResourceNotFoundException;
import com.senla.pas.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest extends AbstractServiceTest {

    @Mock
    private UserDao userDao;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;


    @Test
    void getAllUsers_positive() {
        when(userDao.findAll()).thenReturn(List.of(new User()));
        when(userMapper.toResponseList(anyList())).thenReturn(List.of(new UserResponse()));
        assertEquals(1, userService.getAllUsers().size());
    }

    @Test
    void getAllUsers_negative_daoFailure() {
        when(userDao.findAll()).thenThrow(new RuntimeException("db"));
        assertThrows(RuntimeException.class, () -> userService.getAllUsers());
    }

    @Test
    void getAllUsers_npeSafety_empty() {
        when(userDao.findAll()).thenReturn(Collections.emptyList());
        when(userMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());
        assertDoesNotThrow(() -> userService.getAllUsers());
    }

    @Test
    void getUserById_positive() {
        User user = new User();
        UserResponse response = new UserResponse();
        when(userDao.findById(5L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(response);
        assertSame(response, userService.getUserById(5L));
    }

    @Test
    void getUserById_negative_notFound() {
        when(userDao.findById(5L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(5L));
    }

    @Test
    void getUserById_npeSafety_nullId() {
        when(userDao.findById(null)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(null));
    }

    @Test
    void getUsersFilteredByRating_positive() {
        when(userDao.findFiltered(SortDirection.ASC, 1.0, 5.0)).thenReturn(List.of(new User()));
        when(userMapper.toResponseList(anyList())).thenReturn(List.of(new UserResponse()));
        assertEquals(1, userService.getUsersFilteredByRating(SortDirection.ASC, 1.0, 5.0).size());
    }

    @Test
    void getUsersFilteredByRating_negative_daoFailure() {
        when(userDao.findFiltered(any(), any(), any())).thenThrow(new RuntimeException("db"));
        assertThrows(RuntimeException.class, () -> userService.getUsersFilteredByRating(SortDirection.DESC, null, null));
    }

    @Test
    void getUsersFilteredByRating_npeSafety_nullParams() {
        when(userDao.findFiltered(null, null, null)).thenReturn(Collections.emptyList());
        when(userMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());
        assertDoesNotThrow(() -> userService.getUsersFilteredByRating(null, null, null));
    }

    @Test
    void getMyProfile_positive() {
        authenticate(2L, "ROLE_USER");
        User user = new User();
        when(userDao.findById(2L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(new UserResponse());
        assertDoesNotThrow(() -> userService.getMyProfile());
    }

    @Test
    void getMyProfile_negative_unauthenticated() {
        assertThrows(AuthenticationException.class, () -> userService.getMyProfile());
    }

    @Test
    void getMyProfile_npeSafety_wrongPrincipal() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("u", "p"));
        assertThrows(AuthenticationException.class, () -> userService.getMyProfile());
    }

    @Test
    void updateUser_positive() {
        authenticate(1L, "ROLE_USER");
        User user = new User();
        user.setUsername("old");
        user.setEmail("old@mail.com");
        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        when(userDao.existsByUsername("new")).thenReturn(false);
        when(userDao.existsByEmail("new@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("pwd")).thenReturn("enc");
        when(userMapper.toResponse(user)).thenReturn(new UserResponse());

        userService.updateUser(new UpdateUserRequest("new", "new@mail.com", "pwd", "about"));

        assertEquals("new", user.getUsername());
        assertEquals("new@mail.com", user.getEmail());
        assertEquals("enc", user.getPassword());
        verify(userDao).update(user);
    }

    @Test
    void updateUser_negative_duplicateUsername() {
        authenticate(1L, "ROLE_USER");
        User user = new User();
        user.setUsername("old");
        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        when(userDao.existsByUsername("taken")).thenReturn(true);
        assertThrows(ResourceAlreadyExistsException.class,
                () -> userService.updateUser(new UpdateUserRequest("taken", null, null, null)));
    }

    @Test
    void updateUser_npeSafety_nullFields() {
        authenticate(1L, "ROLE_USER");
        User user = new User();
        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(new UserResponse());
        assertDoesNotThrow(() -> userService.updateUser(new UpdateUserRequest(null, null, null, null)));
    }

    @Test
    void deleteUser_positive_selfDelete() {
        authenticate(1L, "ROLE_USER");
        User user = new User();
        UserResponse response = new UserResponse();
        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(response);
        assertSame(response, userService.deleteUser(1L));
        verify(userDao).delete(1L);
    }

    @Test
    void deleteUser_negative_forbidden() {
        authenticate(1L, "ROLE_USER");
        assertThrows(ForbiddenException.class, () -> userService.deleteUser(2L));
    }

    @Test
    void deleteUser_npeSafety_nullTargetId() {
        authenticate(1L, "ROLE_USER");
        assertThrows(ForbiddenException.class, () -> userService.deleteUser(null));
    }

}
