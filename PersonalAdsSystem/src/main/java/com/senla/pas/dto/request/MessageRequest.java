package com.senla.pas.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {

    @NotNull(message = "ID чата не может быть пустым")
    private Long chatId;

    @NotBlank(message = "Содержание сообщения не может быть пустым")
    private String content;
}
