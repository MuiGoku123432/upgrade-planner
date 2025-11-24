package com.sentinovo.carbuildervin.mapper.user;

import com.sentinovo.carbuildervin.dto.auth.RoleDto;
import com.sentinovo.carbuildervin.entities.user.Role;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleDto toDto(Role entity);

    List<RoleDto> toDtoList(List<Role> entities);
}