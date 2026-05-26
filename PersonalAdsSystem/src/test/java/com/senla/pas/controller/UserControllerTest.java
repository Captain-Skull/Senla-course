package com.senla.pas.controller;

import com.senla.pas.dto.request.UpdateUserRequest;
import com.senla.pas.dto.response.UserResponse;
import com.senla.pas.enums.SortDirection;
import com.senla.pas.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;
    @InjectMocks
    private UserController userController;

    @Test
    void getAllUsers_positive() {
        List<UserResponse> responses = List.of(new UserResponse());
        when(userService.getAllUsers()).thenReturn(responses);

        ResponseEntity<List<UserResponse>> result = userController.getAllUsers();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(responses, result.getBody());
        verify(userService).getAllUsers();
    }

    @Test
    void getAllUsers_negative_serviceThrows() {
        when(userService.getAllUsers()).thenThrow(new IllegalStateException("fail"));

        assertThrows(IllegalStateException.class, () -> userController.getAllUsers());
    }

    @Test
    void getAllUsers_npeSafety_nullBodyAllowed() {
        when(userService.getAllUsers()).thenReturn(null);

        assertDoesNotThrow(() -> userController.getAllUsers());
    }

    @Test
    void getUserById_positive() {
        UserResponse response = new UserResponse();
        when(userService.getUserById(1L)).thenReturn(response);

        ResponseEntity<UserResponse> result = userController.getUserById(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(userService).getUserById(1L);
    }

    @Test
    void getUserById_negative_serviceThrows() {
        when(userService.getUserById(1L)).thenThrow(new IllegalArgumentException("missing"));

        assertThrows(IllegalArgumentException.class, () -> userController.getUserById(1L));
    }

    @Test
    void getUserById_npeSafety_nullId() {
        when(userService.getUserById(null)).thenReturn(new UserResponse());

        assertDoesNotThrow(() -> userController.getUserById(null));
    }

    @Test
    void getMyProfile_positive() {
        UserResponse response = new UserResponse();
        when(userService.getMyProfile()).thenReturn(response);

        ResponseEntity<UserResponse> result = userController.getMyProfile();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(userService).getMyProfile();
    }

    @Test
    void getMyProfile_negative_serviceThrows() {
        when(userService.getMyProfile()).thenThrow(new IllegalStateException("fail"));

        assertThrows(IllegalStateException.class, () -> userController.getMyProfile());
    }

    @Test
    void getMyProfile_npeSafety_nullBodyAllowed() {
        when(userService.getMyProfile()).thenReturn(null);

        assertDoesNotThrow(() -> userController.getMyProfile());
    }

    @Test
    void getUsersFilteredByRating_positive() {
        List<UserResponse> responses = List.of(new UserResponse());
        when(userService.getUsersFilteredByRating(SortDirection.DESC, 1.0, 5.0)).thenReturn(responses);

        ResponseEntity<List<UserResponse>> result = userController.getUsersFilteredByRating(SortDirection.DESC, 1.0, 5.0);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(responses, result.getBody());
        verify(userService).getUsersFilteredByRating(SortDirection.DESC, 1.0, 5.0);
    }

    @Test
    void getUsersFilteredByRating_negative_serviceThrows() {
        when(userService.getUsersFilteredByRating(SortDirection.ASC, 2.0, 4.0)).thenThrow(new IllegalStateException("fail"));

        assertThrows(IllegalStateException.class, () -> userController.getUsersFilteredByRating(SortDirection.ASC, 2.0, 4.0));
    }

    @Test
    void getUsersFilteredByRating_npeSafety_nullFilters() {
        when(userService.getUsersFilteredByRating(null, null, null)).thenReturn(List.of());

        assertDoesNotThrow(() -> userController.getUsersFilteredByRating(null, null, null));
    }

    @Test
    void updateMyProfile_positive() {
        UpdateUserRequest request = new UpdateUserRequest();
        UserResponse response = new UserResponse();
        when(userService.updateUser(request)).thenReturn(response);

        ResponseEntity<UserResponse> result = userController.updateMyProfile(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(userService).updateUser(request);
    }

    @Test
    void updateMyProfile_negative_serviceThrows() {
        UpdateUserRequest request = new UpdateUserRequest();
        when(userService.updateUser(request)).thenThrow(new IllegalStateException("fail"));

        assertThrows(IllegalStateException.class, () -> userController.updateMyProfile(request));
    }

    @Test
    void updateMyProfile_npeSafety_nullRequest() {
        when(userService.updateUser(null)).thenReturn(new UserResponse());

        assertDoesNotThrow(() -> userController.updateMyProfile(null));
    }

    @Test
    void deleteUser_positive() {
        UserResponse response = new UserResponse();
        when(userService.deleteUser(10L)).thenReturn(response);

        ResponseEntity<UserResponse> result = userController.deleteUser(10L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(userService).deleteUser(10L);
    }

    @Test
    void deleteUser_negative_serviceThrows() {
        when(userService.deleteUser(10L)).thenThrow(new IllegalStateException("fail"));

        assertThrows(IllegalStateException.class, () -> userController.deleteUser(10L));
    }

    @Test
    void deleteUser_npeSafety_nullId() {
        when(userService.deleteUser(null)).thenReturn(new UserResponse());

        assertDoesNotThrow(() -> userController.deleteUser(null));
    }
}
