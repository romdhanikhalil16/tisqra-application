package com.tisqra.ticket.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record TransferTicketRequest(
    @Email @NotBlank String recipientEmail,
    String message
) {}

