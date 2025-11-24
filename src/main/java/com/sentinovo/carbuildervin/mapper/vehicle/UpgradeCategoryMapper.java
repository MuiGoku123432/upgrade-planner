package com.sentinovo.carbuildervin.mapper.vehicle;

import com.sentinovo.carbuildervin.dto.common.PageResponseDto;
import com.sentinovo.carbuildervin.dto.upgrade.UpgradeCategoryCreateDto;
import com.sentinovo.carbuildervin.dto.upgrade.UpgradeCategoryDto;
import com.sentinovo.carbuildervin.dto.upgrade.UpgradeCategoryUpdateDto;
import com.sentinovo.carbuildervin.entities.vehicle.UpgradeCategory;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UpgradeCategoryMapper {

    @Mapping(target = "priorityValue", source = "sortOrder")
    @Mapping(target = "createdAt", ignore = true) // UpgradeCategory doesn't extend BaseEntity
    @Mapping(target = "updatedAt", ignore = true)
    UpgradeCategoryDto toDto(UpgradeCategory entity);

    List<UpgradeCategoryDto> toDtoList(List<UpgradeCategory> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sortOrder", source = "priorityValue")
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "vehicleUpgrades", ignore = true)
    UpgradeCategory toEntity(UpgradeCategoryCreateDto createDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "key", ignore = true)
    @Mapping(target = "sortOrder", source = "priorityValue")
    @Mapping(target = "vehicleUpgrades", ignore = true)
    void updateEntity(@MappingTarget UpgradeCategory entity, UpgradeCategoryUpdateDto updateDto);

    default PageResponseDto<UpgradeCategoryDto> toPageDto(Page<UpgradeCategory> page) {
        List<UpgradeCategoryDto> items = toDtoList(page.getContent());
        
        return PageResponseDto.<UpgradeCategoryDto>builder()
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