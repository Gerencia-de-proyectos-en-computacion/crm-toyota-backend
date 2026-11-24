package com.tricore.crmtoyota.dto;

import com.tricore.crmtoyota.domain.enums.Role;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public final class AuthDtos {
    private AuthDtos() {}

    public record LoginRequest(
            @NotBlank(message = "El usuario es obligatorio") String username,
            @NotBlank(message = "La contraseña es obligatoria") String password
    ) {}

    public record AuthResponse(
            String accessToken,
            Instant expiresAt,
            Long userId,
            String username,
            String fullName,
            String stationLabel,
            Role role,
            int maxDiscountPercent
    ) {}
}
