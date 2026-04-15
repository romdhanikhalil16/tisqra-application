package com.tisqra.user.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for bulk cleanup of register-created users.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetRegisteredUsersResponse {
    private int deletedCount;
    private int skippedCount;
}
