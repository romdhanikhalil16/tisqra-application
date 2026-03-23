package com.tisqra.order.application.mapper;

import com.tisqra.order.application.dto.OrderDTO;
import com.tisqra.order.domain.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for Order entity
 */
@Mapper(
    componentModel = "spring",
    uses = {OrderItemMapper.class},
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface OrderMapper {

    @Mapping(target = "totalTickets", expression = "java(order.getTotalTickets())")
    OrderDTO toDTO(Order order);
}
