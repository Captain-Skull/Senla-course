package com.senla.pas.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private Long id;
    private String senderUsername;
    private Long chatId;
    private String content;
    private LocalDateTime sendAt;
    private boolean isRead;
}
