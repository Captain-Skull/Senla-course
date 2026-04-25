package com.senla.pas.mapper;

import com.senla.pas.dto.request.ChatRequest;
import com.senla.pas.dto.response.ChatResponse;
import com.senla.pas.entity.Chat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface ChatMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "buyer", ignore = true)
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    Chat toEntity(ChatRequest chatRequest);

    @Mapping(source = "ad.id", target = "adId")
    @Mapping(source = "ad.title", target = "adTitle")
    @Mapping(source = "buyer", target = "buyer")
    @Mapping(source = "seller", target = "seller")
    ChatResponse toResponse(Chat chat);

    List<ChatResponse> toResponseList(List<Chat> chats);
}
