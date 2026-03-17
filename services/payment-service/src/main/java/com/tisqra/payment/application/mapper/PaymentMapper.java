package com.tisqra.payment.application.mapper;

import com.tisqra.payment.application.dto.PaymentDTO;
import com.tisqra.payment.domain.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for Payment entity
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PaymentMapper {

    PaymentDTO toDTO(Payment payment);
}
