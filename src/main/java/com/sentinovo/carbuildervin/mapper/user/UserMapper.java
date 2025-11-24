package com.sentinovo.carbuildervin.mapper.user;

import com.sentinovo.carbuildervin.dto.auth.RegisterRequestDto;
import com.sentinovo.carbuildervin.dto.auth.UserDto;
import com.sentinovo.carbuildervin.dto.auth.UserProfileDto;
import com.sentinovo.carbuildervin.dto.auth.UserUpdateDto;
import com.sentinovo.carbuildervin.dto.common.PageResponseDto;
import com.sentinovo.carbuildervin.entities.user.User;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User entity);

    List<UserDto> toDtoList(List<User> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true) 
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "vehicles", ignore = true)
    User toEntity(RegisterRequestDto createDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "vehicles", ignore = true)
    void updateEntity(@MappingTarget User entity, UserUpdateDto updateDto);

    @Mapping(target = "vehicleCount", expression = "java(entity.getVehicles() != null ? (long) entity.getVehicles().size() : 0L)")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    UserProfileDto toProfileDto(User entity);

    default PageResponseDto<UserDto> toPageDto(Page<User> page) {
        List<UserDto> items = toDtoList(page.getContent());
        
        return PageResponseDto.<UserDto>builder()
                .items(items)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}