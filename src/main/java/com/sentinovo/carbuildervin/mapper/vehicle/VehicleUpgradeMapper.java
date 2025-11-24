package com.sentinovo.carbuildervin.mapper.vehicle;

import com.sentinovo.carbuildervin.dto.build.VehicleUpgradeCreateDto;
import com.sentinovo.carbuildervin.dto.build.VehicleUpgradeDto;
import com.sentinovo.carbuildervin.dto.build.VehicleUpgradeSummaryDto;
import com.sentinovo.carbuildervin.dto.build.VehicleUpgradeUpdateDto;
import com.sentinovo.carbuildervin.dto.common.PageResponseDto;
import com.sentinovo.carbuildervin.entities.vehicle.VehicleUpgrade;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface VehicleUpgradeMapper {

    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "upgradeCategoryId", source = "upgradeCategory.id")
    @Mapping(target = "upgradeCategoryName", source = "upgradeCategory.name")
    VehicleUpgradeDto toDto(VehicleUpgrade entity);

    List<VehicleUpgradeDto> toDtoList(List<VehicleUpgrade> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicle", ignore = true)
    @Mapping(target = "upgradeCategory", ignore = true)
    @Mapping(target = "parts", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    VehicleUpgrade toEntity(VehicleUpgradeCreateDto createDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicle", ignore = true)
    @Mapping(target = "upgradeCategory", ignore = true)
    @Mapping(target = "parts", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget VehicleUpgrade entity, VehicleUpgradeUpdateDto updateDto);

    @Mapping(target = "vehicleId", source = "entity.vehicle.id")
    @Mapping(target = "categoryName", source = "entity.upgradeCategory.name")
    @Mapping(target = "partCount", source = "partCount")
    @Mapping(target = "estimatedCost", source = "estimatedCost")
    @Mapping(target = "currencyCode", constant = "USD")
    VehicleUpgradeSummaryDto toSummaryDto(VehicleUpgrade entity, Long partCount, BigDecimal estimatedCost);

    default PageResponseDto<VehicleUpgradeDto> toPageDto(Page<VehicleUpgrade> page) {
        List<VehicleUpgradeDto> items = toDtoList(page.getContent());
        
        return PageResponseDto.<VehicleUpgradeDto>builder()
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