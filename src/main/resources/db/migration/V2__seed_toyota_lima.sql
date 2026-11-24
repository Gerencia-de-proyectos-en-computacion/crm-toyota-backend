-- =============================================================================
-- V2__seed_toyota_lima.sql — Datos Semilla Realistas (Lima, Perú — Parque Toyota)
-- Alineado 1:1 con los prototipos de Google Stitch (Mostrador POS & Dashboard ML)
-- =============================================================================

-- 1. Marca Blanca Configurable (Placeholder por defecto editable por ADMIN)
INSERT INTO workshop_settings (id, business_name, slogan, ruc, address, phone, email, currency_symbol, quote_validity_days, updated_at)
VALUES (
    1,
    '[Nombre del Taller Automotriz]',
    'Servicio Técnico y Repuestos Especializados Toyota',
    '20000000001',
    'Av. Angamos Este 1840, Surquillo — Lima, Perú',
    '+51 999 888 777',
    'mostrador@tallertecnicotoyota.pe',
    'S/',
    7,
    CURRENT_TIMESTAMP
);

-- 2. Usuarios Iniciales (las contraseñas se aseguran con BCrypt al arrancar si usan marcador SEED, o hash estándar)
INSERT INTO users (id, username, password_hash, full_name, station_label, role, active, created_at) VALUES
(1, 'admin', 'SEED:Toyota2026!', 'Farid Briceño (Admin)', 'Dirección Técnica', 'ADMIN', TRUE, CURRENT_TIMESTAMP),
(2, 'recepcion', 'SEED:Toyota2026!', 'Carlos Mendoza', 'Caja 02', 'RECEPCIONISTA', TRUE, CURRENT_TIMESTAMP),
(3, 'almacen', 'SEED:Toyota2026!', ' Miguel Huamán', 'Almacén Central', 'ALMACEN', TRUE, CURRENT_TIMESTAMP);

-- 3. Clientes Reales Simulados (DNI 8 dígitos y RUC 11 dígitos con Consentimiento Ley N.° 29733)
INSERT INTO customers (id, document_type, document_number, full_name, phone, email, address, data_consent_accepted, data_consent_at, consent_version, created_at) VALUES
(1, 'DNI', '44821903', 'Luis Paredes', '987654321', 'lparedes@correo.pe', 'Jr. Domingo Orué 420, Surquillo', TRUE, CURRENT_TIMESTAMP, 'LEY-29733-v1.0', CURRENT_TIMESTAMP),
(2, 'RUC', '20512345678', 'Transportes del Sur SAC', '998112233', 'flota@transpsur.com.pe', 'Av. Nicolás Arriola 1420, La Victoria', TRUE, CURRENT_TIMESTAMP, 'LEY-29733-v1.0', CURRENT_TIMESTAMP),
(3, 'DNI', '70921844', 'María Fernanda Quispe', '991223344', 'mf.quispe@gmail.com', 'Calle Las Camelias 310, San Isidro', TRUE, CURRENT_TIMESTAMP, 'LEY-29733-v1.0', CURRENT_TIMESTAMP),
(4, 'DNI', '09841233', 'Jorge Chávez R.', '945667788', 'jchavez@outlook.pe', 'Av. La Marina 2110, San Miguel', TRUE, CURRENT_TIMESTAMP, 'LEY-29733-v1.0', CURRENT_TIMESTAMP),
(5, 'RUC', '20488912301', 'Inversiones Andinas EIRL', '956778899', 'logistica@invandinas.pe', 'Av. Alfredo Mendiola 3890, Los Olivos', TRUE, CURRENT_TIMESTAMP, 'LEY-29733-v1.0', CURRENT_TIMESTAMP),
(6, 'DNI', '41209381', 'Ricardo Mendoza', '964332211', 'rmendoza@yahoo.com', 'Av. Aviación 2815, San Borja', TRUE, CURRENT_TIMESTAMP, 'LEY-29733-v1.0', CURRENT_TIMESTAMP);

-- 4. Vehículos Toyota Asociados
INSERT INTO vehicles (id, customer_id, license_plate, special_plate, vin, model, manufacture_year, trim_version, current_mileage_km, created_at) VALUES
(1, 1, 'ABC-123', FALSE, 'MR2B29F30M1049281', 'Yaris', 2021, '1.5 CVT', 39850, CURRENT_TIMESTAMP),
(2, 2, 'F4T-891', FALSE, '8AJBA3CD5P0219834', 'Hilux', 2023, '2.8 4x4 Diesel', 58900, CURRENT_TIMESTAMP),
(3, 3, 'B8K-312', FALSE, 'JTDBB3FE9N3019283', 'Corolla', 2022, '1.8 HEV', 19600, CURRENT_TIMESTAMP),
(4, 4, 'D3R-504', FALSE, 'JTMW13FV2M5091822', 'RAV4', 2021, '2.5 AWD', 38400, CURRENT_TIMESTAMP),
(5, 5, 'C9L-118', FALSE, 'MHFBA8GS4L0091821', 'Avanza', 2020, '1.5 MT', 79200, CURRENT_TIMESTAMP),
(6, 6, 'A7P-440', FALSE, '9BRB29BT1K2019384', 'Yaris', 2019, '1.3 Dual VVT-i', 62100, CURRENT_TIMESTAMP);

-- 5. Catálogo de Repuestos OEM y Equivalentes (Denso, Aisin, Alternativo)
-- Agrupados por oem_reference_code para sustitución inmediata en 1 clic
INSERT INTO parts (id, sku, oem_reference_code, brand, name, category, compatible_summary, sale_price_igv, current_stock, min_stock, active, version) VALUES
(1, 'TOY-FIL-041', '04152-YZZA6', 'TOYOTA_OEM', 'Filtro de Aceite Cartucho', 'Filtros', 'Yaris, Corolla, RAV4', 45.00, 18, 6, TRUE, 0),
(2, 'DEN-FIL-041', '04152-YZZA6', 'DENSO', 'Filtro de Aceite Alta Eficiencia', 'Filtros', 'Yaris, Corolla, RAV4', 36.00, 22, 6, TRUE, 0),
(3, 'DEN-AIR-178', '17801-21060', 'DENSO', 'Filtro de Aire Motor Yaris 1.5', 'Filtros', 'Yaris, Avanza (16-24)', 85.00, 4, 3, TRUE, 0),
(4, 'TOY-AIR-178', '17801-21060', 'TOYOTA_OEM', 'Filtro de Aire Motor Original', 'Filtros', 'Yaris, Avanza (16-24)', 110.00, 11, 4, TRUE, 0),
(5, 'TOY-OIL-530', '08880-83265', 'TOYOTA_OEM', 'Aceite Sintético 5W-30 Galón 4L', 'Aceites', 'Toda la línea gasolina', 165.00, 24, 8, TRUE, 0),
(6, 'AIS-PAD-0D1', '04465-0D150', 'AISIN', 'Pastillas de Freno Delanteras', 'Frenos', 'Yaris (14-24)', 155.00, 0, 4, TRUE, 0),
(7, 'TOY-PAD-523', '04465-0D150', 'TOYOTA_OEM', 'Pastillas de Freno Delanteras OEM', 'Frenos', 'Yaris (14-24)', 190.00, 9, 4, TRUE, 0),
(8, 'DEN-PAD-523', '04465-0D150', 'DENSO', 'Pastillas de Freno Cerámicas', 'Frenos', 'Yaris (14-24)', 175.00, 6, 4, TRUE, 0),
(9, 'DEN-CAB-871', '87139-0N010', 'DENSO', 'Filtro de Cabina / Polen Carbón', 'Filtros', 'Yaris, Corolla, RAV4, Hilux', 65.00, 14, 5, TRUE, 0),
(10, 'TOY-BUJ-909', '90919-01275', 'TOYOTA_OEM', 'Bujías Iridium (Set x4)', 'Encendido', 'Yaris, Corolla, RAV4', 220.00, 2, 4, TRUE, 0),
(11, 'DEN-BUJ-909', '90919-01275', 'DENSO', 'Bujías Iridium TT (Set x4)', 'Encendido', 'Yaris, Corolla, RAV4', 185.00, 10, 4, TRUE, 0),
(12, 'TOY-FIL-0L0', '23390-0L070', 'TOYOTA_OEM', 'Filtro Combustible Diésel Hilux', 'Filtros', 'Hilux 2.4 / 2.8 GD', 125.00, 1, 5, TRUE, 0),
(13, 'DEN-FIL-0L0', '23390-0L070', 'DENSO', 'Filtro Combustible Diésel Separador', 'Filtros', 'Hilux 2.4 / 2.8 GD', 98.00, 12, 5, TRUE, 0),
(14, 'AIS-CLU-312', '31250-0K204', 'AISIN', 'Kit de Embrague Reforzado 275mm', 'Embrague', 'Hilux 2.4 / 2.8 (16-25)', 890.00, 3, 3, TRUE, 0),
(15, 'TOY-OIL-154', '08883-81015', 'TOYOTA_OEM', 'Aceite Diésel 15W-40 CI-4 (Balde 8L)', 'Aceites', 'Hilux 2.4 / 2.8 Diesel', 240.00, 15, 5, TRUE, 0),
(16, 'TOY-LIQ-DOT', '08823-80004', 'TOYOTA_OEM', 'Líquido de Frenos DOT-4 Toyota 500ml', 'Frenos', 'Toda la línea Toyota', 42.00, 20, 6, TRUE, 0);

-- 6. Compatibilidades por Modelo, Rango de Años y Versión
INSERT INTO part_compatibilities (part_id, model, year_from, year_to, trim_version) VALUES
(1, 'Yaris', 2014, 2026, 'ALL'), (1, 'Corolla', 2014, 2026, 'ALL'), (1, 'RAV4', 2016, 2026, 'ALL'),
(2, 'Yaris', 2014, 2026, 'ALL'), (2, 'Corolla', 2014, 2026, 'ALL'), (2, 'RAV4', 2016, 2026, 'ALL'),
(3, 'Yaris', 2016, 2026, 'ALL'), (3, 'Avanza', 2016, 2026, 'ALL'),
(4, 'Yaris', 2016, 2026, 'ALL'), (4, 'Avanza', 2016, 2026, 'ALL'),
(5, 'Yaris', 2010, 2026, 'ALL'), (5, 'Corolla', 2010, 2026, 'ALL'), (5, 'RAV4', 2010, 2026, 'ALL'), (5, 'Avanza', 2012, 2026, 'ALL'),
(6, 'Yaris', 2014, 2026, 'ALL'),
(7, 'Yaris', 2014, 2026, 'ALL'),
(8, 'Yaris', 2014, 2026, 'ALL'),
(9, 'ALL', 2012, 2026, 'ALL'),
(10, 'Yaris', 2016, 2026, 'ALL'), (10, 'Corolla', 2016, 2026, 'ALL'), (10, 'RAV4', 2016, 2026, 'ALL'),
(11, 'Yaris', 2016, 2026, 'ALL'), (11, 'Corolla', 2016, 2026, 'ALL'), (11, 'RAV4', 2016, 2026, 'ALL'),
(12, 'Hilux', 2016, 2026, 'ALL'),
(13, 'Hilux', 2016, 2026, 'ALL'),
(14, 'Hilux', 2016, 2026, 'ALL'),
(15, 'Hilux', 2016, 2026, 'ALL'),
(16, 'ALL', 2010, 2026, 'ALL');

-- 7. Tarifas Estándar de Mano de Obra (con IGV 18% incluido)
INSERT INTO labor_rates (id, code, service_name, estimated_hours, interval_km, price_igv, active) VALUES
(1, 'MO-MANT-10K', 'Mantenimiento Preventivo 10,000 km Toyota', 1.00, 10000, 65.00, TRUE),
(2, 'MO-MANT-20K', 'Mantenimiento Preventivo 20,000 km Toyota', 1.25, 20000, 75.00, TRUE),
(3, 'MO-MANT-40K', 'Mantenimiento Preventivo 40,000 km Mayor', 1.50, 40000, 80.00, TRUE),
(4, 'MO-MANT-60K', 'Mantenimiento Preventivo 60,000 km Flota/Diésel', 2.00, 60000, 110.00, TRUE),
(5, 'MO-FREN-DEL', 'Cambio de Pastillas y Rectificado Delantero', 1.00, NULL, 65.00, TRUE),
(6, 'MO-ESCAN-OBD', 'Diagnóstico Electrónico Techstream Toyota', 0.50, NULL, 55.00, TRUE),
(7, 'MO-CLU-HIL', 'Desmontaje e Instalación Kit Embrague Hilux', 4.50, NULL, 280.00, TRUE);

-- 8. Kits Preventivos Inteligentes por Modelo y Kilometraje (EXT01)
INSERT INTO maintenance_kits (id, code, name, model, interval_km, labor_rate_id, active) VALUES
(1, 'KIT-YARIS-40K', 'Kit Mantenimiento 40,000 km Yaris', 'Yaris', 40000, 3, TRUE),
(2, 'KIT-COROLLA-20K', 'Kit Mantenimiento 20,000 km Corolla', 'Corolla', 20000, 2, TRUE),
(3, 'KIT-HILUX-60K', 'Kit Mantenimiento 60,000 km Hilux Diésel', 'Hilux', 60000, 4, TRUE),
(4, 'KIT-RAV4-40K', 'Kit Mantenimiento 40,000 km RAV4', 'RAV4', 40000, 3, TRUE);

INSERT INTO maintenance_kit_items (kit_id, part_id, quantity) VALUES
-- Yaris 40k: Filtro aceite OEM + Filtro aire Denso + Aceite 5W-30 + Filtro Cabina + Líquido frenos
(1, 1, 1), (1, 3, 1), (1, 5, 1), (1, 9, 1), (1, 16, 1),
-- Corolla 20k: Filtro aceite OEM + Aceite 5W-30 + Filtro Cabina
(2, 1, 1), (2, 5, 1), (2, 9, 1),
-- Hilux 60k: Filtro Diésel + Aceite Diésel 15W-40 + Filtro Cabina
(3, 13, 1), (3, 15, 1), (3, 9, 1),
-- RAV4 40k: Filtro aceite OEM + Aceite 5W-30 + Filtro Cabina + Líquido frenos
(4, 1, 1), (4, 5, 1), (4, 9, 1), (4, 16, 1);

-- 9. Cotizaciones Históricas Semilla (Alineadas con Stitch Gestión de Cotizaciones)
INSERT INTO quotes (id, quote_number, customer_id, vehicle_id, created_by_user_id, mileage_at_quote, status, rejection_reason, rejection_notes, gross_total_igv, discount_percent, discount_amount, taxable_subtotal, igv_amount, final_total_igv, valid_until, notes, created_at, updated_at) VALUES
(1, 'COT-2026-00037', 6, 6, 2, 62100, 'RECHAZADA', 'FALTA_STOCK', 'Cliente requería pastillas AISIN el mismo día y estaban agotadas.', 375.00, 0.00, 0.00, 317.80, 57.20, 375.00, CURRENT_DATE + 3, 'Atención mostrador', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'COT-2026-00038', 5, 5, 2, 79200, 'CERRADA', NULL, NULL, 355.00, 0.00, 0.00, 300.85, 54.15, 355.00, CURRENT_DATE + 4, 'Servicio preventivo cerrado', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'COT-2026-00039', 4, 4, 2, 38400, 'APROBADA', NULL, NULL, 397.00, 0.00, 0.00, 336.44, 60.56, 397.00, CURRENT_DATE + 5, 'Mantenimiento RAV4 aprobado', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'COT-2026-00040', 3, 3, 2, 19600, 'APROBADA', NULL, NULL, 350.00, 0.00, 0.00, 296.61, 53.39, 350.00, CURRENT_DATE + 6, 'Corolla 20k aprobado', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'COT-2026-00041', 2, 2, 2, 58900, 'PENDIENTE', NULL, NULL, 1170.00, 0.00, 0.00, 991.53, 178.47, 1170.00, CURRENT_DATE + 7, 'Embrague Hilux flota', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'COT-2026-00042', 1, 1, 2, 39850, 'PENDIENTE', NULL, NULL, 480.00, 0.00, 0.00, 406.78, 73.22, 480.00, CURRENT_DATE + 7, 'Proforma con pastillas AISIN sin stock para demo conflicto 409', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO quote_items (quote_id, item_type, part_id, labor_rate_id, item_code, description, quantity, unit_price_igv, line_total_igv) VALUES
(1, 'PART', 6, NULL, 'AIS-PAD-0D1', 'Pastillas de Freno Delanteras', 2, 155.00, 310.00),
(1, 'LABOR', NULL, 5, 'MO-FREN-DEL', 'Cambio de Pastillas y Rectificado Delantero', 1, 65.00, 65.00),
(2, 'PART', 5, NULL, 'TOY-OIL-530', 'Aceite Sintético 5W-30 Galón 4L', 1, 165.00, 165.00),
(2, 'PART', 4, NULL, 'TOY-AIR-178', 'Filtro de Aire Motor Original', 1, 110.00, 110.00),
(2, 'LABOR', NULL, 3, 'MO-MANT-40K', 'Mantenimiento Preventivo 40,000 km Mayor', 1, 80.00, 80.00),
(3, 'PART', 1, NULL, 'TOY-FIL-041', 'Filtro de Aceite Cartucho', 1, 45.00, 45.00),
(3, 'PART', 5, NULL, 'TOY-OIL-530', 'Aceite Sintético 5W-30 Galón 4L', 1, 165.00, 165.00),
(3, 'PART', 9, NULL, 'DEN-CAB-871', 'Filtro de Cabina / Polen Carbón', 1, 65.00, 65.00),
(3, 'PART', 16, NULL, 'TOY-LIQ-DOT', 'Líquido de Frenos DOT-4 Toyota 500ml', 1, 42.00, 42.00),
(3, 'LABOR', NULL, 3, 'MO-MANT-40K', 'Mantenimiento Preventivo 40,000 km Mayor', 1, 80.00, 80.00),
(4, 'PART', 1, NULL, 'TOY-FIL-041', 'Filtro de Aceite Cartucho', 1, 45.00, 45.00),
(4, 'PART', 5, NULL, 'TOY-OIL-530', 'Aceite Sintético 5W-30 Galón 4L', 1, 165.00, 165.00),
(4, 'PART', 9, NULL, 'DEN-CAB-871', 'Filtro de Cabina / Polen Carbón', 1, 65.00, 65.00),
(4, 'LABOR', NULL, 2, 'MO-MANT-20K', 'Mantenimiento Preventivo 20,000 km Toyota', 1, 75.00, 75.00),
(5, 'PART', 14, NULL, 'AIS-CLU-312', 'Kit de Embrague Reforzado 275mm', 1, 890.00, 890.00),
(5, 'LABOR', NULL, 7, 'MO-CLU-HIL', 'Desmontaje e Instalación Kit Embrague Hilux', 1, 280.00, 280.00),
(6, 'PART', 6, NULL, 'AIS-PAD-0D1', 'Pastillas de Freno Delanteras', 2, 155.00, 310.00),
(6, 'PART', 1, NULL, 'TOY-FIL-041', 'Filtro de Aceite Cartucho', 2, 45.00, 90.00),
(6, 'LABOR', NULL, 3, 'MO-MANT-40K', 'Mantenimiento Preventivo 40,000 km Mayor', 1, 80.00, 80.00);

-- 10. Ejecución Inicial de Random Forest Batch (Métricas Calibradas Demanda Intermitente ADI >= 1.32)
INSERT INTO ml_model_runs (id, model_version, horizon_label, r2, wape, rmse, mape_nonzero, adi, execution_source, calculated_at)
VALUES (1, 'rf-toyota-lima-v1.0', 'Próximos 30 días', 0.8600, 0.1140, 1.4200, 0.1210, 1.58, 'SEED_BASELINE', CURRENT_TIMESTAMP);

INSERT INTO demand_forecasts (part_id, run_id, predicted_demand, reorder_point, suggested_order_qty, adi, calculated_at) VALUES
(6, 1, 24.00, 10, 30, 1.45, CURRENT_TIMESTAMP),
(10, 1, 18.00, 8, 20, 1.62, CURRENT_TIMESTAMP),
(12, 1, 32.00, 14, 35, 1.38, CURRENT_TIMESTAMP),
(3, 1, 19.00, 7, 15, 1.51, CURRENT_TIMESTAMP),
(14, 1, 9.00, 4, 8, 1.84, CURRENT_TIMESTAMP),
(1, 1, 44.00, 16, 0, 1.33, CURRENT_TIMESTAMP),
(5, 1, 28.00, 12, 0, 1.36, CURRENT_TIMESTAMP),
(7, 1, 15.00, 6, 0, 1.49, CURRENT_TIMESTAMP),
(9, 1, 22.00, 9, 0, 1.41, CURRENT_TIMESTAMP);

-- Ajustar contadores de identidad tras inserts con ID explícito
ALTER TABLE users ALTER COLUMN id RESTART WITH 10;
ALTER TABLE customers ALTER COLUMN id RESTART WITH 10;
ALTER TABLE vehicles ALTER COLUMN id RESTART WITH 10;
ALTER TABLE parts ALTER COLUMN id RESTART WITH 25;
ALTER TABLE labor_rates ALTER COLUMN id RESTART WITH 15;
ALTER TABLE maintenance_kits ALTER COLUMN id RESTART WITH 10;
ALTER TABLE quotes ALTER COLUMN id RESTART WITH 10;
ALTER TABLE ml_model_runs ALTER COLUMN id RESTART WITH 5;
