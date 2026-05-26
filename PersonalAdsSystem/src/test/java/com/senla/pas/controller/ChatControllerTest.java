package com.senla.pas.controller;

import com.senla.pas.dto.response.ChatResponse;
import com.senla.pas.exception.GlobalExceptionHandler;
import com.senla.pas.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ChatControllerTest extends AbstractControllerTest {

    @Mock
    private ChatService chatService;

    @InjectMocks
    private com.senla.pas.controller.ChatController chatController;

    @BeforeEach
    void init() {
        this.mockMvc = buildMockMvc(chatController, new GlobalExceptionHandler());
    }

    @Test
    void getMyChats_happy() throws Exception {
        ChatResponse r = new ChatResponse();
        r.setId(1L);
        when(chatService.getMyChats()).thenReturn(List.of(r));

        mockMvc.perform(get("/api/chats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getChatById_happy() throws Exception {
        ChatResponse r = new ChatResponse(); r.setId(2L);
        when(chatService.getChatById(2L)).thenReturn(r);

        mockMvc.perform(get("/api/chats/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void getOrCreateChat_happy() throws Exception {
        ChatResponse r = new ChatResponse(); r.setId(3L);
        when(chatService.getOrCreateChat(5L)).thenReturn(r);

        mockMvc.perform(post("/api/chats/ad/5").contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }
}
