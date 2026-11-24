# `crm-toyota-backend` — Reglas Locales del Repositorio

1. **Stack:** Java 21 + Spring Boot 3.x + Spring Data JPA + Flyway + PostgreSQL 16 (Supabase) + Spring Security (JWT 14h + BCrypt) + OpenPDF (`< 3s`) + Springdoc OpenAPI.
2. **Negocio Crítico:** Precios de catálogo incluyen IGV 18%; topes de descuento (`RECEPCIONISTA <= 5%`, `ADMIN <= 15%`); máquina `PENDIENTE -> APROBADA -> CERRADA | RECHAZADA` con `@Transactional` y `@Version` (bloqueo `409` sin rechazo automático al faltar stock; reintegro atómico al pasar de `APROBADA` a `RECHAZADA`); marca blanca desde `workshop_settings`.
3. **Docs-as-Code & Calidad:** Mantener `docs/` sincronizado en cada cambio y sostener cobertura JaCoCo `>= 95%` en servicios/dominio.
