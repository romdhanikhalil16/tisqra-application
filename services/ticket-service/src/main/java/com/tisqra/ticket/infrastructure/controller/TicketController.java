package com.tisqra.ticket.infrastructure.controller;

import com.tisqra.common.enums.TicketStatus;
import com.tisqra.ticket.application.dto.GenerateTicketsRequest;
import com.tisqra.ticket.application.dto.TicketDTO;
import com.tisqra.ticket.application.dto.TransferTicketRequest;
import com.tisqra.ticket.application.service.TicketService;
import com.tisqra.ticket.domain.model.ValidationResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Ticket REST Controller
 */
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets", description = "Ticket management APIs")
@SecurityRequirement(name = "bearer-jwt")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/generate")
    @Operation(summary = "Generate tickets for an order")
    public ResponseEntity<List<TicketDTO>> generateTickets(@Valid @RequestBody GenerateTicketsRequest request) {
        List<TicketDTO> tickets = ticketService.generateTicketsForOrder(request.getOrderId());
        return ResponseEntity.status(HttpStatus.CREATED).body(tickets);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ticket by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketDTO> getTicketById(@PathVariable UUID id) {
        TicketDTO ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/number/{ticketNumber}")
    @Operation(summary = "Get ticket by ticket number")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketDTO> getTicketByNumber(@PathVariable String ticketNumber) {
        TicketDTO ticket = ticketService.getTicketByNumber(ticketNumber);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get tickets for an order")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TicketDTO>> getOrderTickets(@PathVariable UUID orderId) {
        List<TicketDTO> tickets = ticketService.getOrderTickets(orderId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/user/{email}")
    @Operation(summary = "Get user tickets")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<TicketDTO>> getUserTickets(
            @PathVariable String email,
            Pageable pageable) {
        Page<TicketDTO> tickets = ticketService.getUserTickets(email, pageable);
        return ResponseEntity.ok(tickets);
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate ticket QR code")
    @PreAuthorize("hasAnyRole('SCANNER', 'ADMIN_ORG', 'SUPER_ADMIN')")
    public ResponseEntity<ValidationResult> validateTicket(
            @RequestParam String qrCode,
            @RequestParam UUID scannerId,
            @RequestParam String scannerName) {
        ValidationResult result = ticketService.validateTicket(qrCode, scannerId, scannerName);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/transfer")
    @Operation(summary = "Transfer ticket to another user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> transferTicket(
            @PathVariable UUID id,
            @Valid @RequestBody TransferTicketRequest request) {
        ticketService.transferTicket(id, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel ticket")
    @PreAuthorize("hasAnyRole('ADMIN_ORG', 'SUPER_ADMIN')")
    public ResponseEntity<Void> cancelTicket(@PathVariable UUID id) {
        ticketService.cancelTicket(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/event/{eventId}/stats")
    @Operation(summary = "Get event ticket statistics")
    @PreAuthorize("hasAnyRole('ADMIN_ORG', 'SUPER_ADMIN')")
    public ResponseEntity<Long> getEventTicketStats(
            @PathVariable UUID eventId,
            @RequestParam TicketStatus status) {
        Long count = ticketService.getEventTicketStats(eventId, status);
        return ResponseEntity.ok(count);
    }
}
