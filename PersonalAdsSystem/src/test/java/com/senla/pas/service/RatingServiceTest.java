package com.senla.pas.service;

import com.senla.pas.dao.RatingDao;
import com.senla.pas.dao.UserDao;
import com.senla.pas.dto.request.RatingRequest;
import com.senla.pas.dto.response.RatingResponse;
import com.senla.pas.entity.Rating;
import com.senla.pas.entity.User;
import com.senla.pas.exception.BadRequestException;
import com.senla.pas.exception.ResourceNotFoundException;
import com.senla.pas.mapper.RatingMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RatingServiceTest extends AbstractServiceTest {

    @Mock
    private RatingDao ratingDao;
    @Mock
    private UserDao userDao;
    @Mock
    private RatingMapper ratingMapper;
    @InjectMocks
    private RatingService ratingService;


    @Test
    void getUserRatings_positive() {
        when(ratingDao.findByRecipientId(1L)).thenReturn(List.of(new Rating()));
        when(ratingMapper.toResponseList(anyList())).thenReturn(List.of(new RatingResponse()));
        assertEquals(1, ratingService.getUserRatings(1L).size());
    }

    @Test
    void getUserRatings_negative_daoFailure() {
        when(ratingDao.findByRecipientId(1L)).thenThrow(new RuntimeException("db"));
        assertThrows(RuntimeException.class, () -> ratingService.getUserRatings(1L));
    }

    @Test
    void getUserRatings_npeSafety_nullUserId() {
        when(ratingDao.findByRecipientId(null)).thenReturn(Collections.emptyList());
        when(ratingMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());
        assertDoesNotThrow(() -> ratingService.getUserRatings(null));
    }

    @Test
    void addRatingToUser_positive_create() {
        authenticate(2L, "ROLE_USER");
        User reviewer = new User();
        reviewer.setId(2L);
        User recipient = new User();
        recipient.setId(5L);
        Rating rating = new Rating();
        when(userDao.findById(2L)).thenReturn(Optional.of(reviewer));
        when(userDao.findById(5L)).thenReturn(Optional.of(recipient));
        when(ratingDao.findByReviewerIdAndRecipientId(2L, 5L)).thenReturn(Optional.empty());
        when(ratingMapper.toEntity(any())).thenReturn(rating);
        when(ratingDao.calculateAverageRating(5L)).thenReturn(4.5);
        when(userDao.findById(5L)).thenReturn(Optional.of(recipient));
        when(ratingMapper.toResponse(rating)).thenReturn(new RatingResponse());

        assertDoesNotThrow(() -> ratingService.addRatingToUser(5L, new RatingRequest((short) 4)));
        verify(ratingDao).save(rating);
        verify(userDao).updateAverageRating(5L, 4.5);
    }

    @Test
    void addRatingToUser_negative_selfRating() {
        authenticate(2L, "ROLE_USER");
        assertThrows(BadRequestException.class, () -> ratingService.addRatingToUser(2L, new RatingRequest((short) 5)));
    }

    @Test
    void addRatingToUser_npeSafety_nullRecipientId() {
        authenticate(2L, "ROLE_USER");
        User reviewer = new User();
        reviewer.setId(2L);
        when(userDao.findById(2L)).thenReturn(Optional.of(reviewer));
        when(userDao.findById(null)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> ratingService.addRatingToUser(null, new RatingRequest((short) 3)));
    }

}
