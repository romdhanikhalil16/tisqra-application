package com.tisqra.ticket.application.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GenerateTicketsRequest(
    @NotNull UUID orderId
) {}

