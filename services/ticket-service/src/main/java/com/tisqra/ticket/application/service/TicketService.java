package com.tisqra.ticket.application.service;

import com.tisqra.common.enums.TicketStatus;
import com.tisqra.common.exception.BusinessException;
import com.tisqra.common.exception.ResourceNotFoundException;
import com.tisqra.kafka.config.KafkaTopics;
import com.tisqra.kafka.events.TicketGeneratedEvent;
import com.tisqra.kafka.events.TicketTransferredEvent;
import com.tisqra.kafka.events.TicketValidatedEvent;
import com.tisqra.ticket.application.dto.TicketDTO;
import com.tisqra.ticket.application.dto.TransferTicketRequest;
import com.tisqra.ticket.application.mapper.TicketMapper;
import com.tisqra.ticket.domain.model.Ticket;
import com.tisqra.ticket.domain.model.TicketTransfer;
import com.tisqra.ticket.domain.model.ValidationResult;
import com.tisqra.ticket.domain.repository.TicketRepository;
import com.tisqra.ticket.domain.repository.TicketTransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Ticket service - Application layer
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketTransferRepository ticketTransferRepository;
    private final TicketMapper ticketMapper;
    private final QRCodeService qrCodeService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RestTemplate restTemplate;

    @Transactional
    @KafkaListener(topics = "#{kafkaTopics.PAYMENT_COMPLETED}", groupId = "ticket-service")
    public void handlePaymentCompleted(Map<String, Object> event) {
        log.info("Payment completed event received, generating tickets for order: {}", event.get("orderId"));
        
        UUID orderId = UUID.fromString(event.get("orderId").toString());
        generateTicketsForOrder(orderId);
    }

    @Transactional
    public List<TicketDTO> generateTicketsForOrder(UUID orderId) {
        log.info("Generating tickets for order: {}", orderId);

        // Fetch order details
        Map<String, Object> orderData = fetchOrderDetails(orderId);
        UUID eventId = UUID.fromString(orderData.get("eventId").toString());
        UUID userId = UUID.fromString(orderData.get("userId").toString());
        List<Map<String, Object>> items = (List<Map<String, Object>>) orderData.get("items");

        // Fetch user details
        Map<String, Object> userData = fetchUserDetails(userId);
        String ownerEmail = userData.get("email").toString();
        String ownerName = userData.get("firstName") + " " + userData.get("lastName");

        List<Ticket> tickets = items.stream().flatMap(item -> {
            UUID categoryId = UUID.fromString(item.get("ticketCategoryId").toString());
            String categoryName = item.get("ticketCategoryName").toString();
            Integer quantity = Integer.parseInt(item.get("quantity").toString());

            return java.util.stream.IntStream.range(0, quantity).mapToObj(i -> {
                String ticketNumber = generateTicketNumber();
                String qrCodeContent = qrCodeService.generateQRCodeContent(ticketNumber);
                byte[] qrCodeImage = qrCodeService.generateQRCode(qrCodeContent);

                Ticket ticket = Ticket.builder()
                    .ticketNumber(ticketNumber)
                    .orderId(orderId)
                    .eventId(eventId)
                    .ticketCategoryId(categoryId)
                    .ticketCategoryName(categoryName)
                    .qrCode(qrCodeContent)
                    .qrCodeImage(qrCodeImage)
                    .ownerEmail(ownerEmail)
                    .ownerName(ownerName)
                    .status(TicketStatus.ACTIVE)
                    .isTransferable(true)
                    .build();

                ticket = ticketRepository.save(ticket);

                // Publish Kafka event
                TicketGeneratedEvent event = TicketGeneratedEvent.builder()
                    .ticketId(ticket.getId())
                    .ticketNumber(ticket.getTicketNumber())
                    .orderId(orderId)
                    .eventId(eventId)
                    .userId(userId)
                    .ownerEmail(ownerEmail)
                    .ownerName(ownerName)
                    .qrCode(qrCodeContent)
                    .generatedAt(ticket.getCreatedAt())
                    .eventId(UUID.randomUUID())
                    .build();
                kafkaTemplate.send(KafkaTopics.TICKET_GENERATED, event);

                return ticket;
            });
        }).collect(Collectors.toList());

        // Mark order as completed
        completeOrder(orderId);

        log.info("Generated {} tickets for order: {}", tickets.size(), orderId);
        return tickets.stream().map(ticketMapper::toDTO).collect(Collectors.toList());
    }

    @Cacheable(value = "tickets", key = "#id")
    public TicketDTO getTicketById(UUID id) {
        log.debug("Fetching ticket by ID: {}", id);
        Ticket ticket = ticketRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));
        return ticketMapper.toDTO(ticket);
    }

    public TicketDTO getTicketByNumber(String ticketNumber) {
        log.debug("Fetching ticket by number: {}", ticketNumber);
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket", "ticketNumber", ticketNumber));
        return ticketMapper.toDTO(ticket);
    }

    public List<TicketDTO> getOrderTickets(UUID orderId) {
        log.debug("Fetching tickets for order: {}", orderId);
        return ticketRepository.findByOrderId(orderId).stream()
            .map(ticketMapper::toDTO)
            .collect(Collectors.toList());
    }

    public Page<TicketDTO> getUserTickets(String email, Pageable pageable) {
        log.debug("Fetching tickets for user: {}", email);
        return ticketRepository.findByOwnerEmail(email, pageable)
            .map(ticketMapper::toDTO);
    }

    @Transactional
    @CacheEvict(value = "tickets", key = "#qrCode")
    public ValidationResult validateTicket(String qrCode, UUID scannerId, String scannerName) {
        log.info("Validating ticket with QR code");

        Ticket ticket = ticketRepository.findByQrCode(qrCode)
            .orElse(null);

        if (ticket == null) {
            return ValidationResult.builder()
                .valid(false)
                .reason("INVALID_QR_CODE")
                .message("Invalid QR code")
                .build();
        }

        if (ticket.getStatus() == TicketStatus.VALIDATED) {
            return ValidationResult.builder()
                .valid(false)
                .reason("ALREADY_VALIDATED")
                .ticketNumber(ticket.getTicketNumber())
                .message("Ticket has already been validated at " + ticket.getValidatedAt())
                .validatedAt(ticket.getValidatedAt())
                .validatedBy(ticket.getValidatedByName())
                .build();
        }

        if (ticket.getStatus() != TicketStatus.ACTIVE) {
            return ValidationResult.builder()
                .valid(false)
                .reason("INVALID_STATUS")
                .ticketNumber(ticket.getTicketNumber())
                .message("Ticket status is " + ticket.getStatus())
                .build();
        }

        // Validate ticket
        ticket.validate(scannerId, scannerName);
        ticketRepository.save(ticket);

        // Publish Kafka event
        TicketValidatedEvent event = TicketValidatedEvent.builder()
            .ticketId(ticket.getId())
            .ticketNumber(ticket.getTicketNumber())
            .eventId(ticket.getEventId())
            .validatedBy(scannerId)
            .validatedAt(ticket.getValidatedAt())
            .scannerDeviceId(null)
            .eventId(UUID.randomUUID())
            .build();
        kafkaTemplate.send(KafkaTopics.TICKET_VALIDATED, event);

        log.info("Ticket validated successfully: {}", ticket.getTicketNumber());

        return ValidationResult.builder()
            .valid(true)
            .reason("SUCCESS")
            .ticketNumber(ticket.getTicketNumber())
            .ownerName(ticket.getOwnerName())
            .categoryName(ticket.getTicketCategoryName())
            .message("Ticket validated successfully")
            .validatedAt(ticket.getValidatedAt())
            .validatedBy(scannerName)
            .build();
    }

    @Transactional
    @CacheEvict(value = "tickets", key = "#ticketId")
    public void transferTicket(UUID ticketId, TransferTicketRequest request) {
        log.info("Transferring ticket: {} to {}", ticketId, request.getToEmail());

        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        String fromEmail = ticket.getOwnerEmail();

        // Create transfer record
        TicketTransfer transfer = TicketTransfer.builder()
            .ticketId(ticketId)
            .fromEmail(fromEmail)
            .toEmail(request.getToEmail())
            .message(request.getMessage())
            .build();
        ticketTransferRepository.save(transfer);

        // Transfer ticket
        ticket.transfer(request.getToEmail(), request.getToName());
        ticketRepository.save(ticket);

        // Publish Kafka event
        TicketTransferredEvent event = TicketTransferredEvent.builder()
            .ticketId(ticketId)
            .ticketNumber(ticket.getTicketNumber())
            .eventId(ticket.getEventId())
            .fromEmail(fromEmail)
            .toEmail(request.getToEmail())
            .transferredAt(LocalDateTime.now())
            .eventId(UUID.randomUUID())
            .build();
        kafkaTemplate.send(KafkaTopics.TICKET_TRANSFERRED, event);

        log.info("Ticket transferred successfully");
    }

    @Transactional
    public void cancelTicket(UUID ticketId) {
        log.info("Cancelling ticket: {}", ticketId);

        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        ticket.cancel();
        ticketRepository.save(ticket);
    }

    public Long getEventTicketStats(UUID eventId, TicketStatus status) {
        return ticketRepository.countByEventIdAndStatus(eventId, status);
    }

    private String generateTicketNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "TKT-" + timestamp + "-" + random;
    }

    private Map<String, Object> fetchOrderDetails(UUID orderId) {
        try {
            String url = "http://order-service/api/orders/" + orderId;
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            log.error("Failed to fetch order details", e);
            throw new BusinessException("Failed to fetch order details");
        }
    }

    private Map<String, Object> fetchUserDetails(UUID userId) {
        try {
            String url = "http://user-service/api/users/" + userId;
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            log.error("Failed to fetch user details", e);
            throw new BusinessException("Failed to fetch user details");
        }
    }

    private void completeOrder(UUID orderId) {
        try {
            String url = "http://order-service/api/orders/" + orderId + "/complete";
            restTemplate.postForObject(url, null, Void.class);
        } catch (Exception e) {
            log.error("Failed to complete order", e);
        }
    }
}
