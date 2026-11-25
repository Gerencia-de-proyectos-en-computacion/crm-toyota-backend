# Catálogo Oficial de Endpoints REST (`backend-crm`)

Base URL local: `http://localhost:8080` | Swagger UI: `/swagger-ui.html` | OpenAPI JSON: `/v3/api-docs`

## 1. Autenticación y Sesión de Turno (`AuthController`)
- `POST /api/v1/auth/login` *(Público)* — Valida credenciales BCrypt y emite JWT stateless de **14 horas** junto con `maxDiscountPercent` (`15` Admin, `5` Recepcionista, `0` Almacén).
- `POST /api/v1/auth/refresh` *(Autenticado)* — Renovación silenciosa (*sliding refresh*) para evitar cierres bruscos de sesión durante atención en mostrador.

## 2. Marca Blanca Configurable (`WorkshopSettingsController`)
- `GET /api/v1/workshop-settings` *(Público)* — Retorna nombre comercial (placeholder inicial `[Nombre del Taller Automotriz]`), lema, RUC, dirección, teléfono y días de vigencia.
- `PUT /api/v1/workshop-settings` *(`ADMIN`)* — Actualiza en caliente la identidad comercial reflejada en la UI y en el membrete de OpenPDF.

## 3. Clientes (Ley N.º 29733) y Vehículos Toyota (`CustomerVehicleController`)
- `GET /api/v1/customers?q=` *(`ADMIN`, `RECEPCIONISTA`)* — Búsqueda indexada por DNI/RUC o razón social.
- `POST /api/v1/customers` *(`ADMIN`, `RECEPCIONISTA`)* — Valida DNI (8 dígitos) / RUC (11 dígitos `10`/`20`), duplicados (`409`) y exige `dataConsentAccepted = true`.
- `PUT /api/v1/customers/{id}` *(`ADMIN`, `RECEPCIONISTA`)* — Actualización de datos de cliente.
- `GET /api/v1/vehicles?q=` *(`ADMIN`, `RECEPCIONISTA`)* — Buscador instantáneo POS por placa, DNI/RUC o titular; retorna ficha del auto **incluyendo `suggestedPreventiveKit`** según modelo y kilometraje.
- `POST /api/v1/vehicles` *(`ADMIN`, `RECEPCIONISTA`)* — Valida placa MTC (`ABC-123` o `specialPlate=true`).
- `PUT /api/v1/vehicles/{id}` *(`ADMIN`, `RECEPCIONISTA`)* — Protege contra retroceso accidental de kilometraje salvo confirmación explícita (`allowMileageOverride=true`).

## 4. Catálogo OEM/Equivalentes, Kits y Almacén (`CatalogInventoryController`)
- `GET /api/v1/parts?q=&category=&vehicleId=` *(Autenticado)* — Filtra repuestos compatibles por modelo/año/versión, calcula semáforo híbrido (`CRITICO`, `ALERTA`, `NORMAL`) y adjunta `availableEquivalents` agrupados por `oem_reference_code`.
- `POST /api/v1/parts` y `PUT /api/v1/parts/{id}` *(`ADMIN` exclusivo)* — Creación de repuestos y actualización de precios de venta (`salePriceIgv`) y stock mínimo; preserva compatibilidades vehiculares si no se reenvían.
- `GET /api/v1/labor-rates` *(Autenticado)*, `POST /api/v1/labor-rates` y `PUT /api/v1/labor-rates/{id}` *(`ADMIN` exclusivo)* — Tarifario estándar de mano de obra con IGV incluido.
- `GET /api/v1/maintenance-kits` y `GET /api/v1/maintenance-kits/suggest?model=&mileageKm=` *(Autenticado)* — Paquetes preventivos Toyota sugeridos por kilometraje (`EXT01`).
- `POST /api/v1/inventory/replenishments` y `GET /api/v1/inventory/movements` *(`ADMIN`, `ALMACEN`)* — Ingreso rápido de compras de stock (`EXT04`) y kardex auditable.

## 5. Cotizador Express POS y Proforma PDF (`QuoteController`)
- `GET /api/v1/quotes?status=&q=` y `GET /api/v1/quotes/{id}` *(`ADMIN`, `RECEPCIONISTA`)* — Listado y detalle incluyendo enlace prellenado `whatsappUrl` (`EXT02`).
- `POST /api/v1/quotes` y `PUT /api/v1/quotes/{id}` *(`ADMIN`, `RECEPCIONISTA`)* — Emisión y edición en estado `PENDIENTE` (vigencia 7 días, precios inc. IGV 18%, tope de descuento por rol).
- `POST /api/v1/quotes/{id}/approve` *(`ADMIN`, `RECEPCIONISTA`)* — Transacción `@Transactional` + `@Version`: si falta stock responde `409 Conflict` (`INSUFFICIENT_STOCK`) con equivalentes disponibles sin pasar a rechazada; si alcanza, descuenta stock y registra salida en kardex.
- `POST /api/v1/quotes/{id}/substitute-part` *(`ADMIN`, `RECEPCIONISTA`)* — Sustituye en 1 clic un repuesto agotado por su equivalente disponible y aprueba opcionalmente en el mismo paso.
- `POST /api/v1/quotes/{id}/reject` *(`ADMIN`, `RECEPCIONISTA`)* — Exige `rejectionReason` (`EXT03`); si venía de `APROBADA`, **reintegra atómicamente el stock** al almacén.
- `POST /api/v1/quotes/{id}/close` *(`ADMIN`, `RECEPCIONISTA`)* — Cierre comercial (`APROBADA -> CERRADA`) registrando `closedAt` (fecha y hora exacta de atención).
- `GET /api/v1/quotes/{id}/pdf` *(`ADMIN`, `RECEPCIONISTA`)* — Descarga proforma A4 en OpenPDF (`< 3s`) con marca blanca, fecha de atención/cierre cuando está `CERRADA` (zona horaria `America/Lima`) y aviso legal sin valor tributario SUNAT.

## 6. Dashboard de Pronóstico ML Batch (`ForecastDashboardController`)
- `GET /api/v1/forecasts/dashboard` *(`ADMIN`, `ALMACEN`)* — Lectura `< 500ms` desde `demand_forecasts` y `ml_model_runs`: métricas (`R2`, `WAPE`, `RMSE`, `MAPE>0`, `ADI`), KPIs de riesgo crítico/alerta, ventas perdidas por `FALTA_STOCK`, tasa de conversión y tabla priorizada de reorden.
- `POST /api/v1/forecasts/recalculate` *(`ADMIN`)* — Dispara lote hacia `MlForecastClient` (`RoutingMlForecastClient` con fallback automático a `MockMlForecastClient`) y persiste resultados en `demand_forecasts`.
