package com.tisqra.ticket.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tisqra.common.ApiResponse;
import com.tisqra.common.enums.OrderStatus;
import com.tisqra.common.enums.TicketStatus;
import com.tisqra.common.exception.BusinessException;
import com.tisqra.common.exception.ResourceNotFoundException;
import com.tisqra.kafka.config.KafkaTopics;
import com.tisqra.kafka.events.TicketGeneratedEvent;
import com.tisqra.kafka.events.TicketTransferredEvent;
import com.tisqra.kafka.events.TicketValidatedEvent;
import com.tisqra.ticket.application.dto.GenerateTicketsRequest;
import com.tisqra.ticket.application.dto.TicketDTO;
import com.tisqra.ticket.application.dto.TransferTicketRequest;
import com.tisqra.ticket.application.dto.UserView;
import com.tisqra.ticket.application.dto.OrderItemView;
import com.tisqra.ticket.application.dto.OrderView;
import com.tisqra.ticket.domain.model.Ticket;
import com.tisqra.ticket.domain.model.TicketTransfer;
import com.tisqra.ticket.domain.repository.TicketRepository;
import com.tisqra.ticket.domain.repository.TicketTransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketTransferRepository ticketTransferRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public List<TicketDTO> generateTickets(GenerateTicketsRequest request) {
        UUID orderId = request.orderId();

        OrderView order = fetchOrder(orderId);
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BusinessException("Tickets can only be generated for CONFIRMED orders");
        }

        UserView user = fetchUser(order.getUserId());

        List<Ticket> tickets = new ArrayList<>();
        int index = 0;

        for (OrderItemView item : order.getItems()) {
            int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
            for (int i = 0; i < quantity; i++) {
                String ticketNumber = generateTicketNumber(orderId, index++);
                String qrCode = "QR-" + ticketNumber;

                Ticket ticket = Ticket.builder()
                    .ticketNumber(ticketNumber)
                    .qrCode(qrCode)
                    .orderId(order.getId())
                    .eventId(order.getEventId())
                    .ticketCategoryId(item.getTicketCategoryId())
                    .ownerEmail(user.getEmail())
                    .ownerName(user.getFirstName() + " " + user.getLastName())
                    .ownerUserId(user.getId())
                    .status(TicketStatus.ACTIVE)
                    .isTransferable(true)
                    .validatedAt(null)
                    .validatedBy(null)
                    .scannerDeviceId(null)
                    .build();

                tickets.add(ticket);
            }
        }

        tickets = ticketRepository.saveAll(tickets);

        // Publish ticket-generated events
        LocalDateTime generatedAt = LocalDateTime.now();
        for (Ticket ticket : tickets) {
            TicketGeneratedEvent event = TicketGeneratedEvent.builder()
                .ticketId(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .orderId(ticket.getOrderId())
                .eventId(ticket.getEventId())
                .userId(ticket.getOwnerUserId())
                .ownerEmail(ticket.getOwnerEmail())
                .ownerName(ticket.getOwnerName())
                .qrCode(ticket.getQrCode())
                .generatedAt(generatedAt)
                .build();

            kafkaTemplate.send(KafkaTopics.TICKET_GENERATED, event);
        }

        // Mark order completed (tickets generated)
        try {
            String url = "http://order-service/api/orders/" + orderId + "/complete";
            restTemplate.postForObject(url, null, ApiResponse.class);
        } catch (Exception e) {
            // Ticket generation succeeded; completion is best-effort
            log.warn("Failed to mark order COMPLETED after ticket generation", e);
        }

        return tickets.stream().map(this::toDTO).toList();
    }

    public TicketDTO getTicketById(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));
        return toDTO(ticket);
    }

    public Page<TicketDTO> getTicketsByOrderId(UUID orderId, Pageable pageable) {
        return ticketRepository.findByOrderId(orderId, pageable)
            .map(this::toDTO);
    }

    public Page<TicketDTO> getTicketsByUserId(UUID userId, Pageable pageable) {
        return ticketRepository.findByOwnerUserId(userId, pageable)
            .map(this::toDTO);
    }

    @Transactional
    public TicketDTO validateTicket(String qrCode, UUID scannerId, String scannerName) {
        Ticket ticket = ticketRepository.findByQrCode(qrCode)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket", "qrCode", qrCode));

        if (ticket.getStatus() != TicketStatus.ACTIVE) {
            throw new BusinessException("Ticket is not valid for use");
        }

        ticket.validate(scannerId, scannerName);
        Ticket saved = ticketRepository.save(ticket);

        TicketValidatedEvent event = TicketValidatedEvent.builder()
            .ticketId(saved.getId())
            .ticketNumber(saved.getTicketNumber())
            .eventId(saved.getEventId())
            .validatedBy(scannerId)
            .validatedAt(saved.getValidatedAt())
            .scannerDeviceId(scannerName)
            .build();

        kafkaTemplate.send(KafkaTopics.TICKET_VALIDATED, event);

        return toDTO(saved);
    }

    @Transactional
    public TicketDTO transferTicket(UUID ticketId, TransferTicketRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        if (!Boolean.TRUE.equals(ticket.getIsTransferable())) {
            throw new BusinessException("Ticket is not transferable");
        }

        String fromEmail = ticket.getOwnerEmail();
        ticket.transfer(request.recipientEmail(), request.recipientEmail());
        Ticket saved = ticketRepository.save(ticket);

        TicketTransfer transfer = TicketTransfer.builder()
            .ticketId(ticketId)
            .fromEmail(fromEmail)
            .toEmail(request.recipientEmail())
            .message(request.message())
            .accepted(true)
            .acceptedAt(LocalDateTime.now())
            .build();
        ticketTransferRepository.save(transfer);

        TicketTransferredEvent event = TicketTransferredEvent.builder()
            .ticketId(saved.getId())
            .ticketNumber(saved.getTicketNumber())
            .eventId(saved.getEventId())
            .fromEmail(fromEmail)
            .toEmail(request.recipientEmail())
            .transferredAt(LocalDateTime.now())
            .build();

        kafkaTemplate.send(KafkaTopics.TICKET_TRANSFERRED, event);

        return toDTO(saved);
    }

    @Transactional
    public void cancelTicket(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        ticket.cancel();
        ticketRepository.save(ticket);
    }

    private TicketDTO toDTO(Ticket ticket) {
        return TicketDTO.builder()
            .id(ticket.getId())
            .ticketNumber(ticket.getTicketNumber())
            .orderId(ticket.getOrderId())
            .eventId(ticket.getEventId())
            .ticketCategoryId(ticket.getTicketCategoryId())
            .qrCode(ticket.getQrCode())
            .ownerEmail(ticket.getOwnerEmail())
            .ownerName(ticket.getOwnerName())
            .ownerUserId(ticket.getOwnerUserId())
            .status(ticket.getStatus())
            .isTransferable(ticket.getIsTransferable())
            .validatedAt(ticket.getValidatedAt())
            .validatedBy(ticket.getValidatedBy())
            .scannerDeviceId(ticket.getScannerDeviceId())
            .build();
    }

    private OrderView fetchOrder(UUID orderId) {
        try {
            String url = "http://order-service/api/orders/" + orderId;
            ApiResponse<?> response = restTemplate.getForObject(url, ApiResponse.class);
            if (response == null || response.getData() == null) {
                throw new BusinessException("Missing order details");
            }
            return objectMapper.convertValue(response.getData(), OrderView.class);
        } catch (Exception e) {
            log.error("Failed to fetch order details", e);
            throw new BusinessException("Failed to fetch order details");
        }
    }

    private UserView fetchUser(UUID userId) {
        try {
            String url = "http://user-service/api/users/" + userId;
            ApiResponse<?> response = restTemplate.getForObject(url, ApiResponse.class);
            if (response == null || response.getData() == null) {
                throw new BusinessException("Missing user details");
            }

            return objectMapper.convertValue(response.getData(), UserView.class);
        } catch (Exception e) {
            log.error("Failed to fetch user details", e);
            throw new BusinessException("Failed to fetch user details");
        }
    }

    private String generateTicketNumber(UUID orderId, int index) {
        String base = orderId.toString().substring(0, 8).toUpperCase();
        return "TCK-" + base + "-" + index;
    }
}

