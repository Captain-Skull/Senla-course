package com.senla.pas.mapper;

import com.senla.pas.dto.response.ChatResponse;
import com.senla.pas.entity.Chat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface ChatMapper {

    @Mapping(source = "ad.id", target = "adId")
    @Mapping(source = "ad.title", target = "adTitle")
    @Mapping(source = "buyer", target = "buyer")
    @Mapping(source = "seller", target = "seller")
    ChatResponse toResponse(Chat chat);

    List<ChatResponse> toResponseList(List<Chat> chats);
}
