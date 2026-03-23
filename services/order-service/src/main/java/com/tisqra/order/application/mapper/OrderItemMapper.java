package com.tisqra.order.application.mapper;

import com.tisqra.order.application.dto.OrderItemDTO;
import com.tisqra.order.domain.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for OrderItem entity
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface OrderItemMapper {

    OrderItemDTO toDTO(OrderItem orderItem);
}
