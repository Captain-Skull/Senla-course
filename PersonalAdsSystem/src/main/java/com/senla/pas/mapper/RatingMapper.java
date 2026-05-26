package com.senla.pas.mapper;

import com.senla.pas.dto.request.RatingRequest;
import com.senla.pas.dto.response.RatingResponse;
import com.senla.pas.entity.Rating;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface RatingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "recipient", ignore = true)
    @Mapping(target = "reviewer", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    Rating toEntity(RatingRequest ratingRequest);

    @Mapping(target = "reviewer", source = "reviewer")
    @Mapping(target = "recipient", source = "recipient")
    RatingResponse toResponse(Rating rating);

    List<RatingResponse> toResponseList(List<Rating> ratings);
}
