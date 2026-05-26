package com.senla.pas.controller;

import com.senla.pas.dto.request.MessageRequest;
import com.senla.pas.dto.response.MessageResponse;
import com.senla.pas.exception.GlobalExceptionHandler;
import com.senla.pas.exception.ResourceNotFoundException;
import com.senla.pas.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class MessageControllerTest extends AbstractControllerTest {

    @Mock
    private MessageService messageService;

    @InjectMocks
    private com.senla.pas.controller.MessageController messageController;

    @BeforeEach
    void init() {
        this.mockMvc = buildMockMvc(messageController, new GlobalExceptionHandler());
    }

    @Test
    void getMessages_happy() throws Exception {
        MessageResponse mr = new MessageResponse(1L, "john", 1L, "hi", LocalDateTime.now(), false);
        when(messageService.getMessagesByChat(1L)).thenReturn(List.of(mr));

        mockMvc.perform(get("/api/chats/1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getMessageById_notFound() throws Exception {
        when(messageService.getMessageById(99L)).thenThrow(new ResourceNotFoundException("no msg"));

        mockMvc.perform(get("/api/chats/1/messages/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("no msg"));
    }

    @Test
    void sendMessage_happy() throws Exception {
        MessageRequest req = new MessageRequest("hello");
        MessageResponse resp = new MessageResponse(); resp.setId(10L);
        when(messageService.sendMessage(1L, req)).thenReturn(resp);

        String json = objectMapper.writeValueAsString(req);
        mockMvc.perform(post("/api/chats/1/messages").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void updateMessage_happy() throws Exception {
        MessageRequest req = new MessageRequest("upd");
        MessageResponse resp = new MessageResponse(); resp.setId(11L);
        when(messageService.updateMessage(11L, req)).thenReturn(resp);

        String json = objectMapper.writeValueAsString(req);
        mockMvc.perform(put("/api/chats/1/messages/11").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11));
    }

    @Test
    void readMessage_happy() throws Exception {
        MessageResponse resp = new MessageResponse(); resp.setId(12L);
        when(messageService.readMessage(12L)).thenReturn(resp);

        mockMvc.perform(patch("/api/chats/1/messages/12/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12));
    }

    @Test
    void deleteMessage_happy() throws Exception {
        MessageResponse resp = new MessageResponse(); resp.setId(13L);
        when(messageService.deleteMessage(13L)).thenReturn(resp);

        mockMvc.perform(delete("/api/chats/1/messages/13"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(13));
    }
}
