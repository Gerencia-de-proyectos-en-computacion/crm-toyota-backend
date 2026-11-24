# Arquitectura de `backend-crm` (Docs-as-Code)

## 1. Estilo Arquitectónico
Monolito modular limpio en **Spring Boot 3.x (Java 21)** organizado por paquetes de dominio (`auth`, `workshop`, `customer`, `catalog`, `quote`, `inventory`, `forecast`), comunicándose con PostgreSQL 16 (Supabase en nube con TDE AES-256) y consumiendo `ml-service` de forma asíncrona/batch vía `MlForecastClient`.

## 2. Decisiones Clave del Repositorio
- **Migraciones Determinísticas:** Flyway (`src/main/resources/db/migration/`).
- **Concurrencia en Mostrador:** Bloqueo optimista `@Version` en `Part` + transacciones `@Transactional` al aprobar o revertir cotizaciones.
- **Marca Blanca:** Configuración central `workshop_settings` consumida por el generador `OpenPdfQuoteGenerator` (`< 3s`).
- **Cobertura de Pruebas:** Meta `>= 95%` (JaCoCo) sobre servicios y reglas de negocio con JUnit 5 + Mockito + MockMvc.
