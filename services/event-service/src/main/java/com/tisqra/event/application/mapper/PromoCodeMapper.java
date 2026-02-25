package com.tisqra.event.application.mapper;

import com.tisqra.event.application.dto.PromoCodeDTO;
import com.tisqra.event.domain.model.PromoCode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for PromoCode entity
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PromoCodeMapper {

    @Mapping(target = "isValid", expression = "java(promoCode.isValid())")
    PromoCodeDTO toDTO(PromoCode promoCode);
}
