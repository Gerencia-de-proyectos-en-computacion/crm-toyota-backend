package com.tricore.crmtoyota.dto;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

public final class WorkshopSettingsDtos {
    private WorkshopSettingsDtos() {}

    public record WorkshopSettingsUpdateRequest(
            @NotBlank(message = "El nombre comercial del taller es obligatorio")
            @Size(max = 150)
            String businessName,

            @NotBlank(message = "El lema/especialidad es obligatorio")
            @Size(max = 200)
            String slogan,

            @NotBlank(message = "El RUC es obligatorio")
            @Pattern(regexp = "\\d{11}", message = "El RUC debe contener exactamente 11 dígitos")
            String ruc,

            @NotBlank(message = "La dirección es obligatoria")
            @Size(max = 250)
            String address,

            @NotBlank(message = "El teléfono es obligatorio")
            @Size(max = 30)
            String phone,

            @Email(message = "Correo inválido")
            String email,

            @Min(1) @Max(30)
            Integer quoteValidityDays
    ) {}

    public record WorkshopSettingsResponse(
            String businessName,
            String slogan,
            String ruc,
            String address,
            String phone,
            String email,
            String currencySymbol,
            int quoteValidityDays,
            OffsetDateTime updatedAt
    ) {}
}
