# Estrategia de Pruebas y Cobertura JaCoCo (`backend-crm`)

## 1. Regla de Calidad Automatizada (`>= 95%`)
El archivo `pom.xml` incluye `jacoco-maven-plugin` con compuerta obligatoria en la fase `verify` exigiendo **`>= 95%` de cobertura de instrucciones** sobre el paquete de lógica de negocio `com.tricore.crmtoyota.service.*`.

## 2. Suites Incluidas
1. `CrmToyotaFullFlowIntegrationTest`: Prueba integral E2E con `@SpringBootTest` + `MockMvc` + Flyway (`V1` y `V2`) validando login de los 3 roles, marca blanca, alta de clientes/autos con Ley N.º 29733, sugerencia de kit Yaris 40k km, conflicto `409` por quiebre de stock, sustitución en 1 clic por equivalente Denso, generación real de PDF `< 3s`, reversión `APROBADA -> RECHAZADA` reintegrando stock, reposición de almacén y recálculo batch ML.
2. `ServiceEdgeCasesCoverageTest`: Pruebas exhaustivas de ramas y casos borde (DNI/RUC inválidos, regresión de kilometraje, topes de descuento, transiciones prohibidas de estados y umbrales del semáforo híbrido).

## 3. Comando de Ejecución (Windows PowerShell)
```powershell
cd crm-toyota\backend-crm
.\mvnw.cmd verify -B
```
Reporte HTML generado en: `target/site/jacoco/index.html`.
