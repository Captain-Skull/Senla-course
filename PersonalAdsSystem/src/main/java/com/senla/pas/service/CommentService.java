package com.senla.pas.service;

import com.senla.pas.dao.AdDao;
import com.senla.pas.dao.CommentDao;
import com.senla.pas.dao.UserDao;
import com.senla.pas.dto.request.CommentRequest;
import com.senla.pas.dto.response.CommentResponse;
import com.senla.pas.entity.Ad;
import com.senla.pas.entity.Comment;
import com.senla.pas.entity.User;
import com.senla.pas.exception.ForbiddenException;
import com.senla.pas.exception.ResourceNotFoundException;
import com.senla.pas.mapper.CommentMapper;
import com.senla.pas.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CommentService {

    private final CommentDao commentDao;
    private final AdDao adDao;
    private final UserDao userDao;
    private final CommentMapper commentMapper;
    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);

    @Autowired
    public CommentService(CommentDao commentDao, AdDao adDao, UserDao userDao, CommentMapper commentMapper) {
        this.commentDao = commentDao;
        this.adDao = adDao;
        this.userDao = userDao;
        this.commentMapper = commentMapper;
    }

    public List<CommentResponse> getCommentsByAdId(Long adId) {
        logger.info("Получение комментариев объявления {}", adId);
        return commentMapper.toResponseList(commentDao.getCommentsByAdId(adId));
    }

    @Transactional
    public CommentResponse addCommentToAd(Long adId, CommentRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        Ad ad = adDao.findById(adId).orElseThrow(() -> new ResourceNotFoundException("Объявление не найдено: " + adId));
        User user = userDao.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден: " + userId));

        if (!ad.getIsActive()) {
            throw new ForbiddenException("Нельзя добавлять комментарии к неактивному объявлению");
        }

        Comment comment = commentMapper.toEntity(request);
        comment.setAd(ad);
        comment.setUser(user);

        commentDao.save(comment);

        logger.info("Добавлен комментарий {} к объявлению {}, пользователем {}", comment.getId(), adId, userId);
        return commentMapper.toResponse(comment);
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, CommentRequest request) {
        Comment comment = commentDao.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Комментарий не найден: " + commentId));
        Long userId = SecurityUtils.getCurrentUserId();

        if (!userId.equals(comment.getUser().getId())) {
            throw new ForbiddenException("Комментарий может изменять только автор");
        }

        comment.setContent(request.getContent());

        commentDao.update(comment);

        logger.info("Комментарий {} объявления {} обновлен пользователем {}", commentId, comment.getAd().getId(), userId);
        return commentMapper.toResponse(comment);
    }

    @Transactional
    public CommentResponse deleteComment(Long commentId) {
        Comment comment = commentDao.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Комментарий не найден: " + commentId));

        Long userId = SecurityUtils.getCurrentUserId();

        if (!userId.equals(comment.getUser().getId()) && !SecurityUtils.hasRole("ROLE_ADMIN")) {
            throw new ForbiddenException("Недостаточно прав для удаления комментария");
        }

        commentDao.delete(commentId);
        logger.info("Комментарий {} объявления {} удален пользователем {}", commentId, comment.getAd().getId(), userId);
        return commentMapper.toResponse(comment);
    }
}
