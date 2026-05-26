package com.senla.pas.mapper;

import com.senla.pas.dto.request.CreateAdRequest;
import com.senla.pas.dto.request.UpdateAdRequest;
import com.senla.pas.dto.response.AdResponse;
import com.senla.pas.entity.Ad;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface AdMapper {

    @Mapping(target = "user", source = "user")
    AdResponse toResponse(Ad ad);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "isPremium", constant = "false")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    Ad toEntity(CreateAdRequest adRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateAdRequest adRequest, @MappingTarget Ad ad);

    List<AdResponse> toResponseList(List<Ad> ads);
}
