package com.sentinovo.carbuildervin.mapper.parts;

import com.sentinovo.carbuildervin.dto.common.PageResponseDto;
import com.sentinovo.carbuildervin.dto.parts.PartCreateDto;
import com.sentinovo.carbuildervin.dto.parts.PartDto;
import com.sentinovo.carbuildervin.dto.parts.PartSummaryDto;
import com.sentinovo.carbuildervin.dto.parts.PartUpdateDto;
import com.sentinovo.carbuildervin.entities.parts.Part;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PartMapper {

    @Mapping(target = "vehicleUpgradeId", source = "vehicleUpgrade.id")
    @Mapping(target = "categoryCode", source = "partCategory.code")
    @Mapping(target = "categoryName", source = "partCategory.label")
    @Mapping(target = "tierCode", source = "partTier.code")
    @Mapping(target = "tierName", source = "partTier.label")
    @Mapping(target = "productUrl", source = "productUrl")
    PartDto toDto(Part entity);

    List<PartDto> toDtoList(List<Part> entities);

    @Mapping(target = "vehicleUpgrade", ignore = true)
    @Mapping(target = "partCategory", ignore = true)
    @Mapping(target = "partTier", ignore = true)
    @Mapping(target = "productUrl", source = "productUrl")
    @Mapping(target = "subParts", ignore = true)
    Part toEntity(PartCreateDto createDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "vehicleUpgrade", ignore = true)
    @Mapping(target = "partCategory", ignore = true)
    @Mapping(target = "partTier", ignore = true)
    @Mapping(target = "productUrl", source = "productUrl")
    @Mapping(target = "subParts", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Part entity, PartUpdateDto updateDto);

    @Mapping(target = "vehicleUpgradeId", source = "vehicleUpgrade.id")
    @Mapping(target = "categoryName", source = "partCategory.label")
    @Mapping(target = "tierName", source = "partTier.label")
    @Mapping(target = "partNumber", ignore = true)
    @Mapping(target = "quantity", constant = "1")
    @Mapping(target = "totalPrice", expression = "java(entity.getPrice() != null ? entity.getPrice() : null)")
    PartSummaryDto toSummaryDto(Part entity);

    default PageResponseDto<PartDto> toPageDto(Page<Part> page) {
        List<PartDto> items = toDtoList(page.getContent());
        
        return PageResponseDto.<PartDto>builder()
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