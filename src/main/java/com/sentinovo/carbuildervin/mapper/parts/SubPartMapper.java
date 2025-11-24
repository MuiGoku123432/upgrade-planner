package com.sentinovo.carbuildervin.mapper.parts;

import com.sentinovo.carbuildervin.dto.common.PageResponseDto;
import com.sentinovo.carbuildervin.dto.parts.SubPartCreateDto;
import com.sentinovo.carbuildervin.dto.parts.SubPartDto;
import com.sentinovo.carbuildervin.dto.parts.SubPartUpdateDto;
import com.sentinovo.carbuildervin.entities.parts.SubPart;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubPartMapper {

    @Mapping(target = "parentPartId", source = "parentPart.id")
    @Mapping(target = "categoryId", source = "partCategory.code") // PartCategory uses code as primary key
    @Mapping(target = "categoryName", source = "partCategory.label") // Entity uses 'label' not 'name'
    @Mapping(target = "tierId", source = "partTier.code") // PartTier uses code as primary key
    @Mapping(target = "tierName", source = "partTier.label") // Entity uses 'label' not 'name'
    @Mapping(target = "link", source = "productUrl") // Entity uses 'productUrl' not 'link'
    SubPartDto toDto(SubPart entity);

    List<SubPartDto> toDtoList(List<SubPart> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentPart", ignore = true)
    @Mapping(target = "partCategory", ignore = true)
    @Mapping(target = "partTier", ignore = true)
    @Mapping(target = "productUrl", source = "link") // Entity uses 'productUrl' not 'link'
    @Mapping(target = "quantity", defaultValue = "1")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SubPart toEntity(SubPartCreateDto createDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentPart", ignore = true)
    @Mapping(target = "partCategory", ignore = true)
    @Mapping(target = "partTier", ignore = true)
    @Mapping(target = "productUrl", source = "link") // Entity uses 'productUrl' not 'link'
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget SubPart entity, SubPartUpdateDto updateDto);

    default PageResponseDto<SubPartDto> toPageDto(Page<SubPart> page) {
        List<SubPartDto> items = toDtoList(page.getContent());
        
        return PageResponseDto.<SubPartDto>builder()
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