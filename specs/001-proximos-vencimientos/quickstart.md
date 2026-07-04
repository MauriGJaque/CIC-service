# Quickstart: Validación de Panel de Próximos Vencimientos

**Feature**: 001-proximos-vencimientos
**Date**: 2026-07-02

## Prerrequisitos

- Java 17 instalado (`java -version`)
- Gradle disponible (o usar el wrapper `./gradlew`)
- Proyecto compilado y sin errores (`./gradlew build`)

> **Nota**: La aplicación carga automáticamente `src/main/resources/db/schema.sql` (DDL) y `src/main/resources/db/data.sql` (datos de demo) al arrancar. No se requiere configuración manual de la base de datos para validación en desarrollo.

## Escenario 1: Panel con convenios próximos a vencer

### Setup

Los datos de demo se cargan automáticamente desde `src/main/resources/db/data.sql` al iniciar la aplicación. Este archivo incluye convenios con distintas fechas:
- 1 convenio que vence en 1 día
- 1 convenio que vence en 8 días
- 1 convenio que vence en 20 días
- 1 convenio que vence en 60 días (no debe aparecer)
- 1 convenio ya vencido hace 5 días (no debe aparecer)

No se requiere insertar datos manualmente.

### Comando de prueba funcional

```bash
./gradlew test --tests "*.functional.*"
```

### Validación manual (servidor levantado)

```bash
# Iniciar la aplicación
./gradlew bootRun

# Consultar el endpoint
curl -X GET http://localhost:8080/api/v1/dashboard/proximos-vencimientos \
     -H "Accept: application/json" | python -m json.tool
```

### Resultado esperado

```json
{
  "convenios": [
    {
      "id": 1,
      "nombre": "Convenio UPS-ESPE",
      "entidadSocio": "ESPE",
      "fechaVencimiento": "<hoy+8>",
      "diasRestantes": 8
    },
    {
      "id": 2,
      "nombre": "Acuerdo UPS-Banco Pichincha",
      "entidadSocio": "Banco Pichincha",
      "fechaVencimiento": "<hoy+23>",
      "diasRestantes": 23
    }
  ],
  "totalConvenios": 2,
  "hayVencimientos": true,
  "mensaje": null
}
```

**Verificaciones clave**:
- [ ] El convenio vencido (id=3) NO aparece en la lista
- [ ] El convenio lejano (id=4, +60 días) NO aparece en la lista
- [ ] Los convenios están ordenados ascendentemente por fecha (id=1 antes que id=2)
- [ ] `hayVencimientos` es `true`
- [ ] `mensaje` es `null`

---

## Escenario 2: Panel sin convenios próximos a vencer

### Setup

Base de datos sin convenios en el rango ≤ 30 días (solo convenios lejanos o vencidos).

```sql
INSERT INTO convenios (id, nombre, entidad_socio, fecha_inicio, fecha_vencimiento, estado)
VALUES
  (10, 'Convenio futuro', 'ONG Z', '2025-06-01', CURRENT_DATE + 45, 'VIGENTE');
```

### Resultado esperado

```json
{
  "convenios": [],
  "totalConvenios": 0,
  "hayVencimientos": false,
  "mensaje": "Sin vencimientos próximos en los próximos 30 días"
}
```

**Verificaciones clave**:
- [ ] La lista `convenios` está vacía
- [ ] `totalConvenios` es `0`
- [ ] `hayVencimientos` es `false`
- [ ] `mensaje` contiene exactamente "Sin vencimientos próximos en los próximos 30 días"

---

## Escenario 3: Convenio con vencimiento en exactamente 30 días (límite incluido)

```sql
INSERT INTO convenios (id, nombre, entidad_socio, fecha_inicio, fecha_vencimiento, estado)
VALUES (20, 'Convenio límite', 'Instituto ABC', '2025-01-01', CURRENT_DATE + 30, 'VIGENTE');
```

**Verificación clave**:
- [ ] El convenio con `fechaVencimiento = hoy + 30` SÍ aparece en la lista

---

## Ejecución del suite completo de tests

```bash
# Todos los tests (unit + integration + functional)
./gradlew test

# Reporte de cobertura JaCoCo
./gradlew test jacocoTestReport

# Verificar que los thresholds de cobertura se cumplen
./gradlew test jacocoTestCoverageVerification
```

### Dónde ver el reporte JaCoCo

```
build/reports/jacoco/test/html/index.html
```

Abrir en el navegador para ver cobertura por clase y global.

---

## Contrato OpenAPI

El contrato completo está en:
```
specs/001-proximos-vencimientos/contracts/dashboard-api.yaml
```

Para visualizarlo en Swagger UI, copiar el contenido en [editor.swagger.io](https://editor.swagger.io) o usar la extensión Swagger Viewer del IDE.
