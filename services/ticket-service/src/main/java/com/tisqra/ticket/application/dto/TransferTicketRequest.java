package com.tisqra.ticket.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for transferring a ticket
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferTicketRequest {

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Email must be valid")
    private String toEmail;

    @NotBlank(message = "Recipient name is required")
    private String toName;

    private String message;
}
