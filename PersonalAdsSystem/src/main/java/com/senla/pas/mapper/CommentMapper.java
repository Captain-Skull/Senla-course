package com.senla.pas.mapper;

import com.senla.pas.dto.request.CommentRequest;
import com.senla.pas.dto.response.CommentResponse;
import com.senla.pas.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "ad", ignore = true)
    @Mapping(target = "sendAt", expression = "java(java.time.LocalDateTime.now())")
    Comment toEntity(CommentRequest request);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "adId", source = "ad.id")
    CommentResponse toResponse(Comment comment);

    List<CommentResponse> toResponseList(List<Comment> comments);
}
