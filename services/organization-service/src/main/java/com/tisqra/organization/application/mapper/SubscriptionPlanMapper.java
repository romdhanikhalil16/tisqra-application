package com.tisqra.organization.application.mapper;

import com.tisqra.organization.application.dto.SubscriptionPlanDTO;
import com.tisqra.organization.domain.model.SubscriptionPlan;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for SubscriptionPlan entity
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SubscriptionPlanMapper {

    SubscriptionPlanDTO toDTO(SubscriptionPlan plan);
}
