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
    @Mapping(target = "categoryId", source = "partCategory.code") // PartCategory uses code as primary key
    @Mapping(target = "categoryName", source = "partCategory.label") // Entity uses 'label' not 'name'
    @Mapping(target = "tierId", source = "partTier.code") // PartTier uses code as primary key
    @Mapping(target = "tierName", source = "partTier.label") // Entity uses 'label' not 'name'
    @Mapping(target = "link", source = "productUrl") // Entity uses 'productUrl' not 'link'
    PartDto toDto(Part entity);

    List<PartDto> toDtoList(List<Part> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicleUpgrade", ignore = true)
    @Mapping(target = "partCategory", ignore = true)
    @Mapping(target = "partTier", ignore = true)
    @Mapping(target = "productUrl", source = "link") // Entity uses 'productUrl' not 'link'
    @Mapping(target = "quantity", defaultValue = "1")
    @Mapping(target = "subParts", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Part toEntity(PartCreateDto createDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicleUpgrade", ignore = true)
    @Mapping(target = "partCategory", ignore = true)
    @Mapping(target = "partTier", ignore = true)
    @Mapping(target = "productUrl", source = "link") // Entity uses 'productUrl' not 'link'
    @Mapping(target = "subParts", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Part entity, PartUpdateDto updateDto);

    @Mapping(target = "vehicleUpgradeId", source = "vehicleUpgrade.id")
    @Mapping(target = "categoryName", source = "partCategory.label") // Entity uses 'label' not 'name'
    @Mapping(target = "tierName", source = "partTier.label") // Entity uses 'label' not 'name'
    @Mapping(target = "totalPrice", expression = "java(entity.getPrice() != null ? entity.getPrice().multiply(java.math.BigDecimal.valueOf(entity.getQuantity())) : null)")
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