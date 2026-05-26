package com.senla.pas.service;

import com.senla.pas.dao.ChatDao;
import com.senla.pas.dao.MessageDao;
import com.senla.pas.dao.UserDao;
import com.senla.pas.dto.request.MessageRequest;
import com.senla.pas.dto.response.MessageResponse;
import com.senla.pas.entity.Chat;
import com.senla.pas.entity.Message;
import com.senla.pas.entity.User;
import com.senla.pas.exception.ForbiddenException;
import com.senla.pas.exception.ResourceNotFoundException;
import com.senla.pas.mapper.MessageMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest extends AbstractServiceTest {

    @Mock
    private MessageDao messageDao;
    @Mock
    private ChatDao chatDao;
    @Mock
    private UserDao userDao;
    @Mock
    private MessageMapper messageMapper;
    @InjectMocks
    private MessageService messageService;


    @Test
    void getMessageById_positive() {
        authenticate(2L, "ROLE_USER");
        Chat chat = new Chat();
        chat.setId(9L);
        Message message = new Message();
        message.setChat(chat);
        when(messageDao.findById(1L)).thenReturn(Optional.of(message));
        when(chatDao.isChatAvailableForUser(9L, 2L)).thenReturn(true);
        when(messageMapper.toResponse(message)).thenReturn(new MessageResponse());
        assertDoesNotThrow(() -> messageService.getMessageById(1L));
    }

    @Test
    void getMessageById_negative_forbidden() {
        authenticate(2L, "ROLE_USER");
        Chat chat = new Chat();
        chat.setId(9L);
        Message message = new Message();
        message.setChat(chat);
        when(messageDao.findById(1L)).thenReturn(Optional.of(message));
        when(chatDao.isChatAvailableForUser(9L, 2L)).thenReturn(false);
        assertThrows(ForbiddenException.class, () -> messageService.getMessageById(1L));
    }

    @Test
    void getMessageById_npeSafety_nullId() {
        authenticate(2L, "ROLE_USER");
        when(messageDao.findById(null)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> messageService.getMessageById(null));
    }

    @Test
    void getMessagesByChat_positive() {
        authenticate(2L, "ROLE_USER");
        when(chatDao.findById(4L)).thenReturn(Optional.of(new Chat()));
        when(chatDao.isChatAvailableForUser(4L, 2L)).thenReturn(true);
        when(messageDao.findByChatId(4L)).thenReturn(List.of(new Message()));
        when(messageMapper.toResponseList(anyList())).thenReturn(List.of(new MessageResponse()));
        assertEquals(1, messageService.getMessagesByChat(4L).size());
    }

    @Test
    void getMessagesByChat_negative_forbidden() {
        authenticate(2L, "ROLE_USER");
        when(chatDao.findById(4L)).thenReturn(Optional.of(new Chat()));
        when(chatDao.isChatAvailableForUser(4L, 2L)).thenReturn(false);
        assertThrows(ForbiddenException.class, () -> messageService.getMessagesByChat(4L));
    }

    @Test
    void getMessagesByChat_npeSafety_nullChatId() {
        authenticate(2L, "ROLE_USER");
        when(chatDao.findById(null)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> messageService.getMessagesByChat(null));
    }

    @Test
    void sendMessage_positive() {
        authenticate(2L, "ROLE_USER");
        Chat chat = new Chat();
        User user = new User();
        Message message = new Message();
        MessageRequest request = new MessageRequest("hello");
        when(chatDao.findById(4L)).thenReturn(Optional.of(chat));
        when(chatDao.isChatAvailableForUser(4L, 2L)).thenReturn(true);
        when(userDao.findById(2L)).thenReturn(Optional.of(user));
        when(messageMapper.toEntity(request)).thenReturn(message);
        when(messageMapper.toResponse(message)).thenReturn(new MessageResponse());
        assertDoesNotThrow(() -> messageService.sendMessage(4L, request));
        verify(messageDao).save(message);
    }

    @Test
    void sendMessage_negative_forbidden() {
        authenticate(2L, "ROLE_USER");
        when(chatDao.findById(4L)).thenReturn(Optional.of(new Chat()));
        when(chatDao.isChatAvailableForUser(4L, 2L)).thenReturn(false);
        assertThrows(ForbiddenException.class, () -> messageService.sendMessage(4L, new MessageRequest("x")));
    }

    @Test
    void sendMessage_npeSafety_nullRequest() {
        authenticate(2L, "ROLE_USER");
        when(chatDao.findById(4L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> messageService.sendMessage(4L, null));
    }

    @Test
    void updateMessage_positive() {
        authenticate(2L, "ROLE_USER");
        User sender = new User();
        sender.setId(2L);
        Chat chat = new Chat();
        chat.setId(10L);
        Message message = new Message();
        message.setSender(sender);
        message.setChat(chat);
        when(messageDao.findById(1L)).thenReturn(Optional.of(message));
        when(messageMapper.toResponse(message)).thenReturn(new MessageResponse());
        assertDoesNotThrow(() -> messageService.updateMessage(1L, new MessageRequest("upd")));
        assertEquals("upd", message.getContent());
        verify(messageDao).update(message);
    }

    @Test
    void updateMessage_negative_forbidden() {
        authenticate(2L, "ROLE_USER");
        User sender = new User();
        sender.setId(5L);
        Message message = new Message();
        message.setSender(sender);
        when(messageDao.findById(1L)).thenReturn(Optional.of(message));
        assertThrows(ForbiddenException.class, () -> messageService.updateMessage(1L, new MessageRequest("upd")));
    }

    @Test
    void updateMessage_npeSafety_nullRequest() {
        authenticate(2L, "ROLE_USER");
        when(messageDao.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> messageService.updateMessage(1L, null));
    }

    @Test
    void readMessage_positive() {
        authenticate(3L, "ROLE_USER");
        User sender = new User();
        sender.setId(2L);
        Chat chat = new Chat();
        chat.setId(8L);
        Message message = new Message();
        message.setSender(sender);
        message.setChat(chat);
        when(messageDao.findById(7L)).thenReturn(Optional.of(message));
        when(chatDao.isChatAvailableForUser(8L, 3L)).thenReturn(true);
        when(messageMapper.toResponse(message)).thenReturn(new MessageResponse());
        messageService.readMessage(7L);
        assertTrue(message.isRead());
        verify(messageDao).update(message);
    }

    @Test
    void readMessage_negative_cannotReadOwn() {
        authenticate(2L, "ROLE_USER");
        User sender = new User();
        sender.setId(2L);
        Message message = new Message();
        message.setSender(sender);
        message.setChat(new Chat());
        when(messageDao.findById(7L)).thenReturn(Optional.of(message));
        assertThrows(ForbiddenException.class, () -> messageService.readMessage(7L));
    }

    @Test
    void readMessage_npeSafety_nullId() {
        authenticate(1L, "ROLE_USER");
        when(messageDao.findById(null)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> messageService.readMessage(null));
    }

    @Test
    void deleteMessage_positive() {
        authenticate(2L, "ROLE_USER");
        User sender = new User();
        sender.setId(2L);
        Chat chat = new Chat();
        chat.setId(11L);
        Message message = new Message();
        message.setSender(sender);
        message.setChat(chat);
        when(messageDao.findById(6L)).thenReturn(Optional.of(message));
        when(messageMapper.toResponse(message)).thenReturn(new MessageResponse());
        assertDoesNotThrow(() -> messageService.deleteMessage(6L));
        verify(messageDao).delete(6L);
    }

    @Test
    void deleteMessage_negative_forbidden() {
        authenticate(2L, "ROLE_USER");
        User sender = new User();
        sender.setId(9L);
        Message message = new Message();
        message.setSender(sender);
        when(messageDao.findById(6L)).thenReturn(Optional.of(message));
        assertThrows(ForbiddenException.class, () -> messageService.deleteMessage(6L));
    }

    @Test
    void deleteMessage_npeSafety_nullId() {
        when(messageDao.findById(null)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> messageService.deleteMessage(null));
    }

}
