package com.sentinovo.carbuildervin.mapper.vehicle;

import com.sentinovo.carbuildervin.dto.common.PageResponseDto;
import com.sentinovo.carbuildervin.dto.vehicle.VehicleCreateDto;
import com.sentinovo.carbuildervin.dto.vehicle.VehicleDto;
import com.sentinovo.carbuildervin.dto.vehicle.VehicleUpdateDto;
import com.sentinovo.carbuildervin.entities.vehicle.Vehicle;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    @Mapping(target = "ownerId", source = "owner.id")
    VehicleDto toDto(Vehicle entity);

    List<VehicleDto> toDtoList(List<Vehicle> entities);

    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "isArchived", constant = "false")
    @Mapping(target = "vehicleUpgrades", ignore = true)
    Vehicle toEntity(VehicleCreateDto createDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "isArchived", ignore = true)
    @Mapping(target = "vehicleUpgrades", ignore = true)
    void updateEntity(@MappingTarget Vehicle entity, VehicleUpdateDto updateDto);

    default PageResponseDto<VehicleDto> toPageDto(Page<Vehicle> page) {
        List<VehicleDto> items = toDtoList(page.getContent());
        
        return PageResponseDto.<VehicleDto>builder()
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