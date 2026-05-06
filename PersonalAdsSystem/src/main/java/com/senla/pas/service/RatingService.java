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
import com.senla.pas.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class RatingService {

    private final RatingDao ratingDao;
    private final UserDao userDao;
    private final RatingMapper ratingMapper;
    private static final Logger logger = LoggerFactory.getLogger(RatingService.class);

    @Autowired
    public RatingService(RatingDao ratingDao, UserDao userDao, RatingMapper ratingMapper) {
        this.ratingDao = ratingDao;
        this.userDao = userDao;
        this.ratingMapper = ratingMapper;
    }

    public List<RatingResponse> getUserRatings(Long userId) {
        logger.info("Получение отзывов пользователя {}", userId);
        return ratingMapper.toResponseList(ratingDao.findByRecipientId(userId));
    }

    @Transactional
    public RatingResponse addRatingToUser(Long recipientId, RatingRequest request) {
        Long reviewerId = SecurityUtils.getCurrentUserId();

        if (reviewerId.equals(recipientId)) {
            throw new BadRequestException("Нельзя оставить отзыв самому себе");
        }

        User reviewer = userDao.findById(reviewerId).orElseThrow(() -> new ResourceNotFoundException("Не найден отправитель отзыва: " + reviewerId));
        User recipient = userDao.findById(recipientId).orElseThrow(() -> new ResourceNotFoundException("Не найден получатель отзыва: " + recipientId));

        Optional<Rating> existingRating = ratingDao.findByReviewerIdAndRecipientId(reviewerId, recipientId);
        if (existingRating.isPresent()) {
            Rating newRating = existingRating.get();
            newRating.setRating(request.getRating());
            ratingDao.update(newRating);

            updateUserAverageRating(recipientId);

            logger.info("Изменение отзыва {} пользователем {} для пользователя {}. Новый рейтинг: {}", newRating.getId(), reviewerId, recipientId, request.getRating());
            return ratingMapper.toResponse(newRating);
        }

        Rating rating = ratingMapper.toEntity(request);
        rating.setReviewer(reviewer);
        rating.setRecipient(recipient);

        ratingDao.save(rating);

        updateUserAverageRating(recipientId);

        User updatedRecipient = userDao.findById(recipientId).orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        rating.setRecipient(updatedRecipient);

        logger.info("Новый отзыв {} от пользователя {} к пользователю {}. Рейтинг: {}", rating.getId(), reviewerId, recipientId, request.getRating());
        return ratingMapper.toResponse(rating);
    }

    private void updateUserAverageRating(Long userId) {
        Double newAverage = ratingDao.calculateAverageRating(userId);
        userDao.updateAverageRating(userId, newAverage);
        logger.info("Обновлён средний рейтинг пользователя {}: {}", userId, newAverage);
    }
}
