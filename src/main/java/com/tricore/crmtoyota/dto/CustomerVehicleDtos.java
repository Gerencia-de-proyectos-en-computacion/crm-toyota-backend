package com.tricore.crmtoyota.dto;

import com.tricore.crmtoyota.domain.enums.DocumentType;
import com.tricore.crmtoyota.dto.CatalogDtos.MaintenanceKitResponse;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

public final class CustomerVehicleDtos {
    private CustomerVehicleDtos() {}

    public record CustomerRequest(
            @NotNull(message = "El tipo de documento es obligatorio")
            DocumentType documentType,

            @NotBlank(message = "El número de documento es obligatorio")
            String documentNumber,

            @NotBlank(message = "El nombre completo o razón social es obligatorio")
            @Size(max = 160)
            String fullName,

            @NotBlank(message = "El teléfono celular es obligatorio")
            @Size(min = 7, max = 25)
            String phone,

            @Email(message = "Formato de correo inválido")
            String email,

            @Size(max = 220)
            String address,

            @AssertTrue(message = "Debe contar con el consentimiento explícito del titular conforme a la Ley N.° 29733")
            Boolean dataConsentAccepted
    ) {}

    public record CustomerResponse(
            Long id,
            DocumentType documentType,
            String documentNumber,
            String fullName,
            String phone,
            String email,
            String address,
            boolean dataConsentAccepted,
            OffsetDateTime dataConsentAt,
            String consentVersion
    ) {}

    public record VehicleRequest(
            @NotNull(message = "El cliente propietario es obligatorio")
            Long customerId,

            @NotBlank(message = "La placa es obligatoria")
            @Size(max = 15)
            String licensePlate,

            boolean specialPlate,

            @Size(max = 30)
            String vin,

            @NotBlank(message = "El modelo Toyota es obligatorio")
            String model,

            @NotNull @Min(1990) @Max(2028)
            Integer manufactureYear,

            @NotBlank(message = "La versión/motorización es obligatoria")
            @Size(max = 60)
            String trimVersion,

            @NotNull @PositiveOrZero(message = "El kilometraje no puede ser negativo")
            Integer currentMileageKm,

            boolean allowMileageOverride
    ) {}

    public record VehicleResponse(
            Long id,
            Long customerId,
            String customerName,
            String customerDocumentType,
            String customerDocumentNumber,
            String customerPhone,
            String licensePlate,
            boolean specialPlate,
            String vin,
            String model,
            int manufactureYear,
            String trimVersion,
            int currentMileageKm,
            MaintenanceKitResponse suggestedPreventiveKit
    ) {}
}
