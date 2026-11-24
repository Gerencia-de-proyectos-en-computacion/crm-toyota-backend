package com.tricore.crmtoyota.controller;

import com.tricore.crmtoyota.dto.ForecastDtos.ForecastDashboardResponse;
import com.tricore.crmtoyota.service.ForecastDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/forecasts")
@Tag(name = "06. Dashboard ML Random Forest", description = "Lectura < 500ms de alertas de reabastecimiento, ventas perdidas y ejecución batch ML")
public class ForecastDashboardController {

    private final ForecastDashboardService service;

    public ForecastDashboardController(ForecastDashboardService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACEN')")
    @Operation(summary = "Obtener KPIs de alertas críticas, métricas ML (R2, WAPE, MAPE>0), ventas perdidas y tabla ROP")
    public ForecastDashboardResponse getDashboard() {
        return service.getDashboard();
    }

    @PostMapping("/recalculate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ejecutar lote de pronóstico Random Forest (solo ADMIN) y persistir en demand_forecasts")
    public ForecastDashboardResponse recalculate() {
        return service.recalculateBatchForecast();
    }
}
