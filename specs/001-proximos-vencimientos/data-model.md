# Data Model: Panel de Próximos Vencimientos

**Feature**: 001-proximos-vencimientos
**Date**: 2026-07-02

## Entidades de Dominio

### Convenio (Domain Entity)

Representa un acuerdo formal entre la institución y una entidad socia.

| Campo | Tipo | Descripción | Restricciones |
|-------|------|-------------|---------------|
| `id` | Long | Identificador único del convenio | NOT NULL, auto-generado |
| `nombre` | String | Nombre descriptivo del convenio | NOT NULL, max 255 chars |
| `entidadSocio` | String | Nombre de la organización contraparte | NOT NULL, max 255 chars |
| `fechaInicio` | LocalDate | Fecha de entrada en vigor | NOT NULL |
| `fechaVencimiento` | LocalDate | Fecha límite de vigencia | NOT NULL |
| `estado` | EstadoConvenio | Estado actual del convenio | NOT NULL |

**Estado del convenio** (`EstadoConvenio` enum):
- `VIGENTE` — activo y dentro de fecha
- `POR_VENCER` — vigente pero con vencimiento ≤ 30 días
- `VENCIDO` — fecha de vencimiento anterior a hoy
- `RENOVADO` — ya fue renovado/extendido
- `SUSPENDIDO` — pausado administrativamente

**Reglas de negocio**:
- Un convenio aparece en "Próximos vencimientos" si: `hoy ≤ fechaVencimiento ≤ hoy + 30 días`
- Los convenios con `fechaVencimiento < hoy` son "vencidos" y NO aparecen en la sección
- El ordenamiento es por `fechaVencimiento` ascendente (más urgente primero)

## DTOs de Transferencia

### ProximoVencimientoDTO (Response)

DTO devuelto por el endpoint de próximos vencimientos. Solo contiene los datos necesarios para el panel.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | Long | Identificador del convenio |
| `nombre` | String | Nombre del convenio |
| `entidadSocio` | String | Nombre de la entidad socia |
| `fechaVencimiento` | LocalDate (ISO-8601) | Fecha de vencimiento |
| `diasRestantes` | int | Días calendarios hasta el vencimiento (calculado) |

### ProximosVencimientosResponse (Response wrapper)

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `convenios` | List\<ProximoVencimientoDTO\> | Lista ordenada ascendentemente por fecha |
| `totalConvenios` | int | Cantidad total de convenios en la lista |
| `hayVencimientos` | boolean | `true` si la lista no está vacía |
| `mensaje` | String | "Sin vencimientos próximos en los próximos 30 días" cuando lista vacía; `null` cuando hay convenios |

## Relaciones

```text
Convenio (domain entity)
    │
    ├── consultado por ──→ ObtenerProximosVencimientosUseCase
    │                         (filtra y ordena, calcula diasRestantes)
    │
    └── mapeado a ──→ ProximoVencimientoDTO
                         (en la capa de adaptadores)
```

## Persistencia (JPA)

### ConvenioJpaEntity

Mapeo de la entidad de dominio `Convenio` a tabla relacional.

| Columna BD | Campo Java | Tipo SQL |
|------------|-----------|----------|
| `id` | `id` | BIGINT PK AUTO_INCREMENT |
| `nombre` | `nombre` | VARCHAR(255) NOT NULL |
| `entidad_socio` | `entidadSocio` | VARCHAR(255) NOT NULL |
| `fecha_inicio` | `fechaInicio` | DATE NOT NULL |
| `fecha_vencimiento` | `fechaVencimiento` | DATE NOT NULL |
| `estado` | `estado` | VARCHAR(50) NOT NULL |

**Tabla**: `convenios`

**Índice recomendado**: `idx_convenios_fecha_vencimiento` sobre `fecha_vencimiento` para optimizar la consulta de rango de fechas.

### Query JPQL para próximos vencimientos

```sql
SELECT c FROM ConvenioJpaEntity c
WHERE c.fechaVencimiento BETWEEN :hoy AND :limite
ORDER BY c.fechaVencimiento ASC
```

Donde:
- `:hoy` = `LocalDate.now()`
- `:limite` = `LocalDate.now().plusDays(30)`
