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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest extends AbstractServiceTest {

    @Mock
    private CommentDao commentDao;
    @Mock
    private AdDao adDao;
    @Mock
    private UserDao userDao;
    @Mock
    private CommentMapper commentMapper;
    @InjectMocks
    private CommentService commentService;

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCommentsByAd_positive() {
        when(commentDao.getCommentsByAdId(1L)).thenReturn(List.of(new Comment()));
        when(commentMapper.toResponseList(anyList())).thenReturn(List.of(new CommentResponse()));
        assertEquals(1, commentService.getCommentsByAd(1L).size());
    }

    @Test
    void getCommentsByAd_negative_daoFailure() {
        when(commentDao.getCommentsByAdId(1L)).thenThrow(new RuntimeException("db"));
        assertThrows(RuntimeException.class, () -> commentService.getCommentsByAd(1L));
    }

    @Test
    void getCommentsByAd_npeSafety_nullId() {
        when(commentDao.getCommentsByAdId(null)).thenReturn(Collections.emptyList());
        when(commentMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());
        assertDoesNotThrow(() -> commentService.getCommentsByAd(null));
    }

    @Test
    void addCommentToAd_positive() {
        authenticate(2L, "ROLE_USER");
        Ad ad = new Ad();
        ad.setIsActive(true);
        User user = new User();
        Comment comment = new Comment();
        CommentResponse response = new CommentResponse();
        CommentRequest request = new CommentRequest("ok");
        when(adDao.findById(7L)).thenReturn(Optional.of(ad));
        when(userDao.findById(2L)).thenReturn(Optional.of(user));
        when(commentMapper.toEntity(request)).thenReturn(comment);
        when(commentMapper.toResponse(comment)).thenReturn(response);

        assertSame(response, commentService.addCommentToAd(7L, request));
        verify(commentDao).save(comment);
        assertSame(ad, comment.getAd());
        assertSame(user, comment.getUser());
    }

    @Test
    void addCommentToAd_negative_inactiveAd() {
        authenticate(2L, "ROLE_USER");
        Ad ad = new Ad();
        ad.setIsActive(false);
        when(adDao.findById(7L)).thenReturn(Optional.of(ad));
        when(userDao.findById(2L)).thenReturn(Optional.of(new User()));
        assertThrows(ForbiddenException.class, () -> commentService.addCommentToAd(7L, new CommentRequest("x")));
    }

    @Test
    void addCommentToAd_npeSafety_nullRequest() {
        authenticate(2L, "ROLE_USER");
        when(adDao.findById(7L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> commentService.addCommentToAd(7L, null));
    }

    @Test
    void updateComment_positive() {
        authenticate(2L, "ROLE_USER");
        User author = new User();
        author.setId(2L);
        Ad ad = new Ad();
        ad.setId(100L);
        Comment comment = new Comment();
        comment.setUser(author);
        comment.setAd(ad);
        CommentResponse response = new CommentResponse();
        when(commentDao.findById(3L)).thenReturn(Optional.of(comment));
        when(commentMapper.toResponse(comment)).thenReturn(response);
        assertSame(response, commentService.updateComment(3L, new CommentRequest("new")));
        assertEquals("new", comment.getContent());
        verify(commentDao).update(comment);
    }

    @Test
    void updateComment_negative_forbidden() {
        authenticate(2L, "ROLE_USER");
        User author = new User();
        author.setId(5L);
        Comment comment = new Comment();
        comment.setUser(author);
        when(commentDao.findById(3L)).thenReturn(Optional.of(comment));
        assertThrows(ForbiddenException.class, () -> commentService.updateComment(3L, new CommentRequest("x")));
    }

    @Test
    void updateComment_npeSafety_nullRequest() {
        when(commentDao.findById(3L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> commentService.updateComment(3L, null));
    }

    @Test
    void deleteComment_positive_author() {
        authenticate(2L, "ROLE_USER");
        User author = new User();
        author.setId(2L);
        Ad ad = new Ad();
        ad.setId(5L);
        Comment comment = new Comment();
        comment.setUser(author);
        comment.setAd(ad);
        when(commentDao.findById(10L)).thenReturn(Optional.of(comment));
        when(commentMapper.toResponse(comment)).thenReturn(new CommentResponse());
        assertDoesNotThrow(() -> commentService.deleteComment(10L));
        verify(commentDao).delete(10L);
    }

    @Test
    void deleteComment_negative_forbidden() {
        authenticate(2L, "ROLE_USER");
        User author = new User();
        author.setId(8L);
        Comment comment = new Comment();
        comment.setUser(author);
        when(commentDao.findById(10L)).thenReturn(Optional.of(comment));
        assertThrows(ForbiddenException.class, () -> commentService.deleteComment(10L));
    }

    @Test
    void deleteComment_npeSafety_nullId() {
        when(commentDao.findById(null)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> commentService.deleteComment(null));
    }

}
