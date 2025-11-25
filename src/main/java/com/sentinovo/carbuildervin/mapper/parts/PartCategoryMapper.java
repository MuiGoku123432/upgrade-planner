package com.sentinovo.carbuildervin.mapper.parts;

import com.sentinovo.carbuildervin.dto.common.PageResponseDto;
import com.sentinovo.carbuildervin.dto.parts.lookup.PartCategoryCreateDto;
import com.sentinovo.carbuildervin.dto.parts.lookup.PartCategoryDto;
import com.sentinovo.carbuildervin.dto.parts.lookup.PartCategoryUpdateDto;
import com.sentinovo.carbuildervin.entities.parts.PartCategory;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PartCategoryMapper {

    PartCategoryDto toDto(PartCategory entity);

    List<PartCategoryDto> toDtoList(List<PartCategory> entities);

    @Mapping(target = "parts", ignore = true)
    @Mapping(target = "subParts", ignore = true)
    PartCategory toEntity(PartCategoryCreateDto createDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "code", ignore = true) // Code is primary key, can't be updated
    @Mapping(target = "parts", ignore = true)
    @Mapping(target = "subParts", ignore = true)
    void updateEntity(@MappingTarget PartCategory entity, PartCategoryUpdateDto updateDto);

    default PageResponseDto<PartCategoryDto> toPageDto(Page<PartCategory> page) {
        List<PartCategoryDto> items = toDtoList(page.getContent());
        
        return PageResponseDto.<PartCategoryDto>builder()
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