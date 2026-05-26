package com.senla.pas.controller;

import com.senla.pas.dto.request.CommentRequest;
import com.senla.pas.dto.response.CommentResponse;
import com.senla.pas.service.CommentService;
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
class CommentControllerTest {

    @Mock
    private CommentService commentService;
    @InjectMocks
    private CommentController commentController;

    @Test
    void getCommentsByAd_positive() {
        List<CommentResponse> responses = List.of(new CommentResponse());
        when(commentService.getCommentsByAd(1L)).thenReturn(responses);

        ResponseEntity<List<CommentResponse>> result = commentController.getCommentsByAd(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(responses, result.getBody());
        verify(commentService).getCommentsByAd(1L);
    }

    @Test
    void getCommentsByAd_negative_serviceThrows() {
        when(commentService.getCommentsByAd(1L)).thenThrow(new IllegalStateException("db"));

        assertThrows(IllegalStateException.class, () -> commentController.getCommentsByAd(1L));
    }

    @Test
    void getCommentsByAd_npeSafety_nullAdId() {
        when(commentService.getCommentsByAd(null)).thenReturn(List.of());

        assertDoesNotThrow(() -> commentController.getCommentsByAd(null));
    }

    @Test
    void addComment_positive() {
        CommentRequest request = new CommentRequest();
        CommentResponse response = new CommentResponse();
        when(commentService.addCommentToAd(2L, request)).thenReturn(response);

        ResponseEntity<CommentResponse> result = commentController.addComment(2L, request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(commentService).addCommentToAd(2L, request);
    }

    @Test
    void addComment_negative_serviceThrows() {
        CommentRequest request = new CommentRequest();
        when(commentService.addCommentToAd(2L, request)).thenThrow(new IllegalArgumentException("bad"));

        assertThrows(IllegalArgumentException.class, () -> commentController.addComment(2L, request));
    }

    @Test
    void addComment_npeSafety_nullContent() {
        CommentRequest request = new CommentRequest();
        when(commentService.addCommentToAd(2L, request)).thenReturn(new CommentResponse());

        assertDoesNotThrow(() -> commentController.addComment(2L, request));
    }

    @Test
    void updateComment_positive() {
        CommentRequest request = new CommentRequest();
        CommentResponse response = new CommentResponse();
        when(commentService.updateComment(4L, request)).thenReturn(response);

        ResponseEntity<CommentResponse> result = commentController.updateComment(3L, 4L, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(commentService).updateComment(4L, request);
    }

    @Test
    void updateComment_negative_serviceThrows() {
        CommentRequest request = new CommentRequest();
        when(commentService.updateComment(4L, request)).thenThrow(new IllegalStateException("fail"));

        assertThrows(IllegalStateException.class, () -> commentController.updateComment(3L, 4L, request));
    }

    @Test
    void updateComment_npeSafety_nullArgs() {
        when(commentService.updateComment(null, null)).thenReturn(new CommentResponse());

        assertDoesNotThrow(() -> commentController.updateComment(null, null, null));
    }

    @Test
    void deleteComment_positive() {
        CommentResponse response = new CommentResponse();
        when(commentService.deleteComment(7L)).thenReturn(response);

        ResponseEntity<CommentResponse> result = commentController.deleteComment(5L, 7L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(commentService).deleteComment(7L);
    }

    @Test
    void deleteComment_negative_serviceThrows() {
        when(commentService.deleteComment(7L)).thenThrow(new IllegalStateException("fail"));

        assertThrows(IllegalStateException.class, () -> commentController.deleteComment(5L, 7L));
    }

    @Test
    void deleteComment_npeSafety_nullCommentId() {
        when(commentService.deleteComment(null)).thenReturn(new CommentResponse());

        assertDoesNotThrow(() -> commentController.deleteComment(null, null));
    }
}
