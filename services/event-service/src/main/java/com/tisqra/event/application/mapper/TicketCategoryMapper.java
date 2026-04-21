package com.tisqra.event.application.mapper;

import com.tisqra.event.application.dto.CreateTicketCategoryRequest;
import com.tisqra.event.application.dto.TicketCategoryDTO;
import com.tisqra.event.domain.model.TicketCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for TicketCategory entity
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TicketCategoryMapper {

    @Mapping(target = "availableCount", expression = "java(category.getAvailableCount())")
    @Mapping(target = "isAvailable", expression = "java(category.isAvailable())")
    TicketCategoryDTO toDTO(TicketCategory category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "soldCount", ignore = true)
    @Mapping(target = "reservedCount", ignore = true)
    @Mapping(target = "crea tedAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TicketCategory toEntity(CreateTicketCategoryRequest request);
}