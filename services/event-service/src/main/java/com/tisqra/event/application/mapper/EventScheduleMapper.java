package com.tisqra.event.application.mapper;

import com.tisqra.event.application.dto.CreateEventScheduleRequest;
import com.tisqra.event.application.dto.EventScheduleDTO;
import com.tisqra.event.domain.model.EventSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for EventSchedule entity
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface EventScheduleMapper {

    EventScheduleDTO toDTO(EventSchedule schedule);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "event", ignore = true)
    EventSchedule toEntity(CreateEventScheduleRequest request);
}
