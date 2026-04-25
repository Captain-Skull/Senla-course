package com.senla.pas.mapper;

import com.senla.pas.dto.request.MessageRequest;
import com.senla.pas.dto.response.MessageResponse;
import com.senla.pas.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mapping(target = "id" , ignore = true)
    @Mapping(target = "sender" , ignore = true)
    @Mapping(target = "chat" , ignore = true)
    @Mapping(target = "sendAt" , expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "isRead" , constant = "false")
    Message toEntity(MessageRequest messageDto);

    @Mapping(target = "senderUsername", source = "sender.username")
    @Mapping(target = "chatId", source = "chat.id")
    MessageResponse toResponse(Message message);

    List<MessageResponse> toResponseList(List<Message> messages);
}
