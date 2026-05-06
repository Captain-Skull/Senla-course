package com.senla.pas.controller;

import com.senla.pas.dto.request.RatingRequest;
import com.senla.pas.dto.response.RatingResponse;
import com.senla.pas.service.RatingService;
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
class RatingControllerTest {

    @Mock
    private RatingService ratingService;
    @InjectMocks
    private RatingController ratingController;

    @Test
    void getRatingsByUser_positive() {
        List<RatingResponse> responses = List.of(new RatingResponse());
        when(ratingService.getUserRatings(1L)).thenReturn(responses);

        ResponseEntity<List<RatingResponse>> result = ratingController.getRatingsByUser(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(responses, result.getBody());
        verify(ratingService).getUserRatings(1L);
    }

    @Test
    void getRatingsByUser_negative_serviceThrows() {
        when(ratingService.getUserRatings(1L)).thenThrow(new IllegalStateException("fail"));

        assertThrows(IllegalStateException.class, () -> ratingController.getRatingsByUser(1L));
    }

    @Test
    void getRatingsByUser_npeSafety_nullUserId() {
        when(ratingService.getUserRatings(null)).thenReturn(List.of());

        assertDoesNotThrow(() -> ratingController.getRatingsByUser(null));
    }

    @Test
    void addRating_positive() {
        RatingRequest request = new RatingRequest();
        RatingResponse response = new RatingResponse();
        when(ratingService.addRatingToUser(2L, request)).thenReturn(response);

        ResponseEntity<RatingResponse> result = ratingController.addRating(2L, request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(ratingService).addRatingToUser(2L, request);
    }

    @Test
    void addRating_negative_serviceThrows() {
        RatingRequest request = new RatingRequest();
        when(ratingService.addRatingToUser(2L, request)).thenThrow(new IllegalStateException("fail"));

        assertThrows(IllegalStateException.class, () -> ratingController.addRating(2L, request));
    }

    @Test
    void addRating_npeSafety_nullFields() {
        RatingRequest request = new RatingRequest();
        when(ratingService.addRatingToUser(2L, request)).thenReturn(new RatingResponse());

        assertDoesNotThrow(() -> ratingController.addRating(2L, request));
    }
}
