package com.senla.pas.service;

import com.senla.pas.dao.UserDao;
import com.senla.pas.dto.request.UpdateUserRequest;
import com.senla.pas.dto.response.UserResponse;
import com.senla.pas.entity.User;
import com.senla.pas.enums.SortDirection;
import com.senla.pas.exception.ForbiddenException;
import com.senla.pas.exception.ResourceAlreadyExistsException;
import com.senla.pas.exception.ResourceNotFoundException;
import com.senla.pas.mapper.UserMapper;
import com.senla.pas.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserDao userDao;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(UserDao userDao, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> getAllUsers() {
        logger.info("Получение всех пользователей");
        return userMapper.toResponseList(userDao.findAll());
    }

    public UserResponse getUserById(long userId) {
        logger.info("Получение пользователя по ID: {}", userId);
        return userMapper.toResponse(userDao.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден: " + userId)));
    }

    public List<UserResponse> getUsersFilteredByRating(SortDirection direction, Double minRating, Double maxRating) {
        logger.info("Получение пользователей отфильтрованных по рейтингу");
        return userMapper.toResponseList(userDao.findFiltered(direction, minRating, maxRating));
    }

    public UserResponse getMyProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("Получение профиля пользователя: {}", userId);
        User user = userDao.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден: " + userId));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateUser(UpdateUserRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userDao.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден: " + userId));
        if (request.getNewUsername() != null && !request.getNewUsername().equals(user.getUsername())){
            if (userDao.existsByUsername(request.getNewUsername())) {
                throw new ResourceAlreadyExistsException(
                        "Пользователь с именем '" + request.getNewUsername() + "' уже существует"
                );
            }
            user.setUsername(request.getNewUsername());
        }
        if (request.getNewUsername() != null && !request.getNewEmail().equals(user.getEmail())){
            if (userDao.existsByEmail(request.getNewEmail())) {
                throw new ResourceAlreadyExistsException(
                        "Пользователь с почтой '" + request.getNewEmail() + "' уже существует"
                );
            }
            user.setEmail(request.getNewEmail());
        }
        if (request.getNewAboutMe() != null) {
            user.setAboutMe(request.getNewAboutMe());
        }
        if (request.getNewPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }
        userDao.update(user);

        logger.info("Пользователь {} обновлен", userId);
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse deleteUser(Long targetUserId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        logger.info("Удаление пользователя: {}", targetUserId);

        if (!currentUserId.equals(targetUserId) && !SecurityUtils.hasRole("ROLE_ADMIN")) {
            throw new ForbiddenException("Нет прав для удаления этого пользователя");
        }

        User user = userDao.findById(targetUserId).orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден: " + targetUserId));

        userDao.delete(targetUserId);
        logger.info("Пользователь {} успешно удалён", targetUserId);
        return userMapper.toResponse(user);
    }
}
