package com.tisqra.organization.application.mapper;

import com.tisqra.organization.application.dto.SubscriptionDTO;
import com.tisqra.organization.domain.model.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for Subscription entity
 */
@Mapper(
    componentModel = "spring",
    uses = {SubscriptionPlanMapper.class},
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SubscriptionMapper {

    @Mapping(target = "canCreateEvent", expression = "java(subscription.canCreateEvent())")
    SubscriptionDTO toDTO(Subscription subscription);
}
