package com.senla.pas.service;

import com.senla.pas.dao.AdDao;
import com.senla.pas.dao.ChatDao;
import com.senla.pas.dao.UserDao;
import com.senla.pas.dto.response.ChatResponse;
import com.senla.pas.entity.Ad;
import com.senla.pas.entity.Chat;
import com.senla.pas.entity.User;
import com.senla.pas.exception.AuthenticationException;
import com.senla.pas.exception.ForbiddenException;
import com.senla.pas.exception.ResourceNotFoundException;
import com.senla.pas.mapper.ChatMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatServiceTest extends AbstractServiceTest {

    @Mock
    private ChatDao chatDao;
    @Mock
    private AdDao adDao;
    @Mock
    private UserDao userDao;
    @Mock
    private ChatMapper chatMapper;
    @InjectMocks
    private ChatService chatService;


    @Test
    void getChatById_positive() {
        authenticate(1L, "ROLE_USER");
        Chat chat = new Chat();
        ChatResponse response = new ChatResponse();
        when(chatDao.findById(5L)).thenReturn(Optional.of(chat));
        when(chatDao.isChatAvailableForUser(5L, 1L)).thenReturn(true);
        when(chatMapper.toResponse(chat)).thenReturn(response);
        assertSame(response, chatService.getChatById(5L));
    }

    @Test
    void getChatById_negative_forbidden() {
        authenticate(1L, "ROLE_USER");
        when(chatDao.findById(5L)).thenReturn(Optional.of(new Chat()));
        when(chatDao.isChatAvailableForUser(5L, 1L)).thenReturn(false);
        assertThrows(ForbiddenException.class, () -> chatService.getChatById(5L));
    }

    @Test
    void getChatById_npeSafety_nullId() {
        authenticate(1L, "ROLE_USER");
        when(chatDao.findById(null)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> chatService.getChatById(null));
    }

    @Test
    void getMyChats_positive() {
        authenticate(2L, "ROLE_USER");
        when(chatDao.findByUserId(2L)).thenReturn(List.of(new Chat()));
        when(chatMapper.toResponseList(anyList())).thenReturn(List.of(new ChatResponse()));
        assertEquals(1, chatService.getMyChats().size());
    }

    @Test
    void getMyChats_negative_unauthenticated() {
        assertThrows(AuthenticationException.class, () -> chatService.getMyChats());
    }

    @Test
    void getMyChats_npeSafety_wrongPrincipalType() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("x", "y"));
        assertThrows(AuthenticationException.class, () -> chatService.getMyChats());
    }

    @Test
    void getOrCreateChat_positive_existingChat() {
        authenticate(3L, "ROLE_USER");
        Chat existing = new Chat();
        ChatResponse response = new ChatResponse();
        when(chatDao.findByAdAndBuyer(9L, 3L)).thenReturn(Optional.of(existing));
        when(chatMapper.toResponse(existing)).thenReturn(response);
        assertSame(response, chatService.getOrCreateChat(9L));
        verify(chatDao, never()).save(any());
    }

    @Test
    void getOrCreateChat_negative_ownAd() {
        authenticate(3L, "ROLE_USER");
        Ad ad = new Ad();
        User owner = new User();
        owner.setId(3L);
        ad.setUser(owner);
        ad.setIsActive(true);
        when(chatDao.findByAdAndBuyer(9L, 3L)).thenReturn(Optional.empty());
        when(adDao.findById(9L)).thenReturn(Optional.of(ad));
        assertThrows(ForbiddenException.class, () -> chatService.getOrCreateChat(9L));
    }

    @Test
    void getOrCreateChat_npeSafety_nullAdIdWhenExistingChat() {
        authenticate(3L, "ROLE_USER");
        Chat existing = new Chat();
        when(chatDao.findByAdAndBuyer(null, 3L)).thenReturn(Optional.of(existing));
        when(chatMapper.toResponse(existing)).thenReturn(new ChatResponse());
        assertDoesNotThrow(() -> chatService.getOrCreateChat(null));
    }

}
