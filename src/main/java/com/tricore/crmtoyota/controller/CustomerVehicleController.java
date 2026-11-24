package com.tricore.crmtoyota.controller;

import com.tricore.crmtoyota.dto.CustomerVehicleDtos.*;
import com.tricore.crmtoyota.service.CustomerVehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "03. Clientes y Flota Toyota", description = "Gestión de clientes (Ley N.° 29733) y vehículos Toyota con sugerencia preventiva")
public class CustomerVehicleController {

    private final CustomerVehicleService service;

    public CustomerVehicleController(CustomerVehicleService service) {
        this.service = service;
    }

    @GetMapping("/customers")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    @Operation(summary = "Listar o buscar clientes por DNI/RUC o nombre")
    public List<CustomerResponse> listCustomers(@RequestParam(required = false) String q) {
        return service.listCustomers(q);
    }

    @PostMapping("/customers")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    @Operation(summary = "Registrar cliente validando DNI/RUC y consentimiento Ley N.° 29733")
    public CustomerResponse createCustomer(@Valid @RequestBody CustomerRequest request) {
        return service.createCustomer(request);
    }

    @PutMapping("/customers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    @Operation(summary = "Actualizar datos de cliente")
    public CustomerResponse updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return service.updateCustomer(id, request);
    }

    @GetMapping("/vehicles")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    @Operation(summary = "Búsqueda instantánea POS por placa, DNI/RUC o cliente incluyendo Kit Preventivo sugerido")
    public List<VehicleResponse> searchVehicles(@RequestParam(required = false) String q) {
        return service.searchVehiclesForPos(q);
    }

    @PostMapping("/vehicles")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    @Operation(summary = "Registrar vehículo Toyota asociado a un cliente")
    public VehicleResponse createVehicle(@Valid @RequestBody VehicleRequest request) {
        return service.createVehicle(request);
    }

    @PutMapping("/vehicles/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    @Operation(summary = "Actualizar vehículo Toyota y kilometraje preventivo")
    public VehicleResponse updateVehicle(@PathVariable Long id, @Valid @RequestBody VehicleRequest request) {
        return service.updateVehicle(id, request);
    }
}
