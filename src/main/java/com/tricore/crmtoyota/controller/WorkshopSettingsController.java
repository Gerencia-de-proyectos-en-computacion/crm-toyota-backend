package com.tricore.crmtoyota.controller;

import com.tricore.crmtoyota.dto.WorkshopSettingsDtos.WorkshopSettingsResponse;
import com.tricore.crmtoyota.dto.WorkshopSettingsDtos.WorkshopSettingsUpdateRequest;
import com.tricore.crmtoyota.service.WorkshopSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workshop-settings")
@Tag(name = "02. Marca Blanca del Taller", description = "Configuración comercial dinámica para interfaz y membrete PDF")
public class WorkshopSettingsController {

    private final WorkshopSettingsService service;

    public WorkshopSettingsController(WorkshopSettingsService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Obtener configuración pública de marca blanca del taller")
    public WorkshopSettingsResponse getSettings() {
        return service.getSettings();
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar identidad comercial del taller (solo ADMIN)")
    public WorkshopSettingsResponse updateSettings(@Valid @RequestBody WorkshopSettingsUpdateRequest request) {
        return service.updateSettings(request);
    }
}
