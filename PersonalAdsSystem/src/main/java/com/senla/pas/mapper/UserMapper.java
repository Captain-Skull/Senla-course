package com.senla.pas.mapper;

import com.senla.pas.dto.request.RegisterRequest;
import com.senla.pas.dto.response.UserResponse;
import com.senla.pas.dto.response.UserShortResponse;
import com.senla.pas.entity.Role;
import com.senla.pas.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", qualifiedByName = "rolesToStringSet")
    UserResponse toResponse(User user);

    UserShortResponse toShortResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "averageRating", constant = "0.0")
    User toEntity(RegisterRequest userResponse);

    @Named("rolesToStringSet")
    default Set<String> rolesToStringSet(Set<Role> roles) {
        if (roles == null) return Set.of();
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    List<UserResponse> toResponseList(List<User> users);
}
