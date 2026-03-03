package com.tisqra.ticket.application.mapper;

import com.tisqra.ticket.application.dto.TicketDTO;
import com.tisqra.ticket.domain.model.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Base64;

/**
 * MapStruct mapper for Ticket entity
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TicketMapper {

    @Mapping(target = "qrCodeImageBase64", expression = "java(encodeQrCodeImage(ticket.getQrCodeImage()))")
    TicketDTO toDTO(Ticket ticket);

    default String encodeQrCodeImage(byte[] qrCodeImage) {
        if (qrCodeImage == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(qrCodeImage);
    }
}
