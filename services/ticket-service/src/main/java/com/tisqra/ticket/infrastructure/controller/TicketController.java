package com.tisqra.ticket.infrastructure.controller;

import com.tisqra.common.ApiResponse;
import com.tisqra.ticket.application.dto.GenerateTicketsRequest;
import com.tisqra.ticket.application.dto.TicketDTO;
import com.tisqra.ticket.application.dto.TransferTicketRequest;
import com.tisqra.ticket.application.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets", description = "Ticket generation and validation APIs")
@SecurityRequirement(name = "bearer-jwt")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/generate")
    @Operation(summary = "Generate tickets for an order")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TicketDTO>>> generateTickets(
        @Valid @RequestBody GenerateTicketsRequest request) {
        List<TicketDTO> tickets = ticketService.generateTickets(request);
        return ResponseEntity.ok(ApiResponse.<List<TicketDTO>>builder().success(true).data(tickets).build());
    }

    @GetMapping("/{ticketId}")
    @Operation(summary = "Get ticket by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TicketDTO>> getTicketById(@PathVariable UUID ticketId) {
        TicketDTO dto = ticketService.getTicketById(ticketId);
        return ResponseEntity.ok(ApiResponse.<TicketDTO>builder().success(true).data(dto).build());
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get tickets for an order")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<TicketDTO>>> getTicketsByOrderId(
        @PathVariable UUID orderId,
        Pageable pageable) {
        Page<TicketDTO> tickets = ticketService.getTicketsByOrderId(orderId, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<TicketDTO>>builder().success(true).data(tickets).build());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get tickets for a user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<TicketDTO>>> getTicketsByUserId(
        @PathVariable UUID userId,
        Pageable pageable) {
        Page<TicketDTO> tickets = ticketService.getTicketsByUserId(userId, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<TicketDTO>>builder().success(true).data(tickets).build());
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate a ticket using QR code")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TicketDTO>> validateTicket(
        @RequestParam String qrCode,
        @RequestParam UUID scannerId,
        @RequestParam String scannerName) {
        TicketDTO dto = ticketService.validateTicket(qrCode, scannerId, scannerName);
        return ResponseEntity.ok(ApiResponse.<TicketDTO>builder().success(true).data(dto).build());
    }

    @PostMapping("/{ticketId}/transfer")
    @Operation(summary = "Transfer ticket to a new recipient email")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TicketDTO>> transferTicket(
        @PathVariable UUID ticketId,
        @Valid @RequestBody TransferTicketRequest request) {
        TicketDTO dto = ticketService.transferTicket(ticketId, request);
        return ResponseEntity.ok(ApiResponse.<TicketDTO>builder().success(true).data(dto).build());
    }

    @PostMapping("/{ticketId}/cancel")
    @Operation(summary = "Cancel a ticket")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> cancelTicket(@PathVariable UUID ticketId) {
        ticketService.cancelTicket(ticketId);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).data(null).build());
    }
}

