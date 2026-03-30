package com.tisqra.event.infrastructure.controller;

import com.tisqra.common.ApiResponse;
import com.tisqra.event.application.dto.TicketCategoryDTO;
import com.tisqra.event.application.service.TicketCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/ticket-categories")
@RequiredArgsConstructor
@Tag(name = "Ticket Categories", description = "Ticket category management APIs")
@SecurityRequirement(name = "bearer-jwt")
public class TicketCategoryController {

    private final TicketCategoryService ticketCategoryService;

    @GetMapping("/{id}")
    @Operation(summary = "Get ticket category by ID")
    public ResponseEntity<ApiResponse<TicketCategoryDTO>> getTicketCategory(@PathVariable UUID id) {
        TicketCategoryDTO dto = ticketCategoryService.getTicketCategory(id);
        return ResponseEntity.ok(ApiResponse.<TicketCategoryDTO>builder().success(true).data(dto).build());
    }

    @PostMapping("/{id}/reserve")
    @Operation(summary = "Reserve tickets for a ticket category")
    public ResponseEntity<ApiResponse<Void>> reserve(
            @PathVariable UUID id,
            @RequestParam Integer quantity) {
        ticketCategoryService.reserve(id, quantity);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).data(null).build());
    }

    @PostMapping("/{id}/release")
    @Operation(summary = "Release reserved tickets for a ticket category")
    public ResponseEntity<ApiResponse<Void>> release(
            @PathVariable UUID id,
            @RequestParam Integer quantity) {
        ticketCategoryService.release(id, quantity);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).data(null).build());
    }
}

