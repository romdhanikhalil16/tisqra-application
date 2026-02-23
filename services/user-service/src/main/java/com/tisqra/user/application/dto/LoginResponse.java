package com.tisqra.user.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for successful login
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserDTO user;
}
