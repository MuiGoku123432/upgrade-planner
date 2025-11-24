package com.sentinovo.carbuildervin.mapper.parts;

import com.sentinovo.carbuildervin.dto.common.PageResponseDto;
import com.sentinovo.carbuildervin.dto.parts.lookup.PartTierCreateDto;
import com.sentinovo.carbuildervin.dto.parts.lookup.PartTierDto;
import com.sentinovo.carbuildervin.dto.parts.lookup.PartTierUpdateDto;
import com.sentinovo.carbuildervin.entities.parts.PartTier;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PartTierMapper {

    @Mapping(target = "id", source = "code") // PartTier uses code as primary key
    @Mapping(target = "name", source = "label") // Entity uses 'label' not 'name'
    @Mapping(target = "isActive", constant = "true") // PartTier doesn't have isActive field, default to true
    @Mapping(target = "createdAt", ignore = true) // PartTier doesn't extend BaseEntity
    @Mapping(target = "updatedAt", ignore = true)
    PartTierDto toDto(PartTier entity);

    List<PartTierDto> toDtoList(List<PartTier> entities);

    @Mapping(target = "label", source = "name") // Entity uses 'label' not 'name'
    @Mapping(target = "parts", ignore = true)
    @Mapping(target = "subParts", ignore = true)
    PartTier toEntity(PartTierCreateDto createDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "code", ignore = true) // Code is primary key, can't be updated
    @Mapping(target = "label", source = "name") // Entity uses 'label' not 'name'
    @Mapping(target = "parts", ignore = true)
    @Mapping(target = "subParts", ignore = true)
    void updateEntity(@MappingTarget PartTier entity, PartTierUpdateDto updateDto);

    default PageResponseDto<PartTierDto> toPageDto(Page<PartTier> page) {
        List<PartTierDto> items = toDtoList(page.getContent());
        
        return PageResponseDto.<PartTierDto>builder()
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