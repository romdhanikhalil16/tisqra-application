package com.tisqra.event.application.mapper;

import com.tisqra.event.application.dto.CreateEventRequest;
import com.tisqra.event.application.dto.EventDTO;
import com.tisqra.event.domain.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for Event entity
 */
@Mapper(
    componentModel = "spring",
    uses = {TicketCategoryMapper.class, EventScheduleMapper.class},
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface EventMapper {

    @Mapping(target = "availableTickets", expression = "java(event.getAvailableTickets())")
    EventDTO toDTO(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "schedule", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    Event toEntity(CreateEventRequest request);
}
