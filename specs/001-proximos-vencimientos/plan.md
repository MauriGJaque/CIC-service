# Implementation Plan: Panel de Próximos Vencimientos

**Branch**: `001-proximos-vencimientos` | **Date**: 2026-07-02 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/001-proximos-vencimientos/spec.md`

## Summary

Implementar el endpoint REST `GET /api/v1/dashboard/proximos-vencimientos` que retorna todos los convenios con fecha de vencimiento en los próximos 30 días calendario (inclusive), ordenados ascendentemente por urgencia. Cuando no hay convenios en el rango, retorna un mensaje "Sin vencimientos próximos en los próximos 30 días". La implementación sigue Clean Architecture con cuatro capas (dominio, use cases, adapters, infrastructure), BDD testing con JUnit 5, diseño API First con OpenAPI 3.1 y openapi-generator, y JaCoCo para cobertura ≥ 80%.

## Technical Context

**Language/Version**: Java 17

**Primary Dependencies**: Spring Boot 4.1.0, Spring Data JPA, Spring Web MVC, Lombok, openapi-generator-gradle-plugin 7.x, JaCoCo

**Storage**: H2 in-memory (desarrollo y tests) — tabla `convenios`

**Testing**: JUnit 5 (unit + integration + functional), Given/When/Then via `@DisplayName`

**Target Platform**: JVM / servidor Linux (Spring Boot embedded Tomcat)

**Project Type**: Web service REST (microservicio)

**Performance Goals**: Respuesta percibida como inmediata por la directora al cargar el panel (< 500ms p95 bajo carga normal)

**Constraints**: Cobertura JaCoCo > 80% por clase, ≥ 80% global; build falla si no se cumple

**Scale/Scope**: Consulta de convenios de una institución (volumen bajo-medio, < 10k registros)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| # | Principio | Verificación | Estado |
|---|-----------|-------------|--------|
| I | Clean Architecture | Capas: `domain/` → `application/` → `adapters/` → `infrastructure/`. Sin anotaciones Spring en domain/application. | ✅ Cumple |
| II | BDD Testing | Tests unitarios (use case), integración (`@DataJpaTest`), funcional (`@SpringBootTest`). Nombres Given/When/Then. | ✅ Cumple |
| III | SOLID + YAGNI + DRY | Puerto `ConvenioRepository` (interfaz), adaptador de persistencia separado. Solo lo que la historia requiere. | ✅ Cumple |
| IV | API First | Contrato `contracts/dashboard-api.yaml` creado ANTES de la implementación. openapi-generator genera stubs. | ✅ Cumple |
| V | JaCoCo ≥ 80% | Configuración en `build.gradle` con `jacocoTestCoverageVerification`. Build falla si no se cumple. | ✅ Cumple |

**No hay violaciones. Se puede proceder a la implementación.**

## Project Structure

### Documentation (this feature)

```text
specs/001-proximos-vencimientos/
├── plan.md              # Este archivo
├── research.md          # Decisiones tecnológicas resueltas
├── data-model.md        # Modelo de datos (Convenio, DTOs)
├── quickstart.md        # Guía de validación manual y automatizada
├── contracts/
│   └── dashboard-api.yaml   # Contrato OpenAPI 3.1
└── tasks.md             # Generado por /speckit-tasks
```

### Source Code (repository root)

```text
src/
├── main/
│   ├── java/org/ups/cic/
│   │   ├── domain/
│   │   │   └── convenio/
│   │   │       ├── Convenio.java                        # Entidad de dominio
│   │   │       └── ConvenioRepository.java              # Puerto (interfaz)
│   │   ├── application/
│   │   │   └── convenio/
│   │   │       └── ObtenerProximosVencimientosUseCase.java
│   │   ├── adapters/
│   │   │   ├── in/web/
│   │   │   │   ├── DashboardController.java             # Implementa interfaz generada
│   │   │   │   └── mapper/ConvenioMapper.java           # Domain → DTO
│   │   │   └── out/persistence/
│   │   │       ├── ConvenioJpaEntity.java               # JPA entity
│   │   │       ├── ConvenioJpaRepository.java           # Spring Data JPA interface
│   │   │       └── ConvenioRepositoryAdapter.java       # Implementa ConvenioRepository
│   │   └── infrastructure/
│   │       └── config/
│   │           └── BeanConfiguration.java               # Wiring de dependencias
│   └── resources/
│       ├── db/
│       │   ├── schema.sql                               # DDL: CREATE TABLE convenios
│       │   └── data.sql                                 # DML: datos pre-cargados de demo
│       ├── openapi/
│       │   └── dashboard-api.yaml                       # Contrato OpenAPI (fuente de verdad)
│       └── application.yaml
└── test/
    ├── java/org/ups/cic/
    │   ├── unit/
    │   │   └── application/convenio/
    │   │       └── ObtenerProximosVencimientosUseCaseTest.java
    │   ├── integration/
    │   │   └── adapters/out/persistence/
    │   │       └── ConvenioRepositoryAdapterTest.java
    │   └── functional/
    │       └── adapters/in/web/
    │           └── DashboardControllerFunctionalTest.java
    └── resources/
        ├── application-test.yaml                        # Config H2 para tests (init-mode: never)
        ├── data-us1.sql                                 # Datos de prueba: escenario US1 (con vencimientos)
        └── data-us2.sql                                 # Datos de prueba: escenario US2 (sin vencimientos)
```

**Structure Decision**: Single Spring Boot project (Option 1). Clean Architecture con subpaquetes `domain/`, `application/`, `adapters/in`, `adapters/out`, `infrastructure/`. Código generado por openapi-generator va a `build/generated/openapi/` y se agrega a sourceSets — no se edita manualmente.

**DB Initialization Strategy**:
- `src/main/resources/db/schema.sql` — DDL ejecutado al iniciar la aplicación (Spring Boot `spring.sql.init`). Define la tabla `convenios` con sus columnas y el índice sobre `fecha_vencimiento`.
- `src/main/resources/db/data.sql` — DML con datos de demo/referencia pre-cargados al arrancar la aplicación en perfil `dev`. Incluye al menos 5 convenios con diferentes fechas (próximos, vigentes lejanos, vencidos) para ejercer el panel sin configuración manual.
- `src/test/resources/application-test.yaml` — sobrescribe `spring.sql.init.mode: never` para que los tests controlen sus propios datos con `@Sql` por clase.
- `src/test/resources/data-us1.sql` y `data-us2.sql` — datasets específicos para cada escenario funcional, cargados con `@Sql(scripts = "...")` en `DashboardControllerFunctionalTest`.

## Complexity Tracking

> No hay violaciones a la constitución que justificar.

---

## Notas de Implementación por Fase

### Build Configuration (pre-implementación)

Antes de escribir cualquier clase, actualizar `build.gradle`:

1. Agregar plugin `openapi-generator` y tarea `openApiGenerate` apuntando a `src/main/resources/openapi/dashboard-api.yaml`
2. Copiar el contrato `contracts/dashboard-api.yaml` a `src/main/resources/openapi/dashboard-api.yaml`
3. Agregar plugin `jacoco` con thresholds (líneas ≥ 80%, ramas ≥ 80%)
4. Excluir de JaCoCo: clases generadas, `CicServiceApplication`, `*JpaEntity`

### DB Initialization (resources/db/)

Actualizar `src/main/resources/application.yaml` con:

```yaml
spring:
  application:
    name: CIC-service
  datasource:
    url: jdbc:h2:mem:cicdb;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: none          # Schema gestionado por schema.sql, no por Hibernate
    defer-datasource-initialization: true   # Ejecuta data.sql DESPUÉS del schema JPA
    show-sql: true
  sql:
    init:
      mode: always
      schema-locations: classpath:db/schema.sql
      data-locations: classpath:db/data.sql
  h2:
    console:
      enabled: true
      path: /h2-console
```

**schema.sql** (`src/main/resources/db/schema.sql`) — creado en T004-DB:

```sql
CREATE TABLE IF NOT EXISTS convenios (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre           VARCHAR(255) NOT NULL,
    entidad_socio    VARCHAR(255) NOT NULL,
    fecha_inicio     DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    estado           VARCHAR(50) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_convenios_fecha_vencimiento
    ON convenios (fecha_vencimiento);
```

**data.sql** (`src/main/resources/db/data.sql`) — datos de demo pre-cargados:
- Incluye convenios con diferentes estados: próximos a vencer (1, 8, 20 días), vigentes lejanos (60, 120 días), y vencidos (para verificar que el filtro los excluye en el panel).

**Configuración de tests** (`src/test/resources/application-test.yaml`):

```yaml
spring:
  sql:
    init:
      mode: never    # Tests controlan sus propios datos con @Sql por clase
```

Cada clase de test funcional usa `@Sql(scripts = "classpath:data-us1.sql", executionPhase = BEFORE_TEST_CLASS)` + `@Sql(scripts = "classpath:cleanup.sql", executionPhase = AFTER_TEST_CLASS)`.

### Domain Layer

- `Convenio.java`: record o clase inmutable con los campos del data-model. Sin anotaciones Spring/JPA.
- `ConvenioRepository.java`: interfaz con método `List<Convenio> findProximosAVencer(LocalDate desde, LocalDate hasta)`

### Application Layer

- `ObtenerProximosVencimientosUseCase.java`: recibe `Supplier<LocalDate>` como dependencia. Calcula rango `[hoy, hoy+30]`. Llama al repositorio. Retorna `List<Convenio>` ya ordenada.

### Adapters — Out (Persistencia)

- `ConvenioJpaEntity.java`: `@Entity @Table(name="convenios")` con los campos del data-model
- `ConvenioJpaRepository.java`: `extends JpaRepository<ConvenioJpaEntity, Long>` con query JPQL `BETWEEN :desde AND :hasta ORDER BY fechaVencimiento ASC`
- `ConvenioRepositoryAdapter.java`: implementa `ConvenioRepository`, convierte `ConvenioJpaEntity` ↔ `Convenio` (dominio)

### Adapters — In (Web)

- `DashboardController.java`: implementa la interfaz `DashboardApi` generada por openapi-generator. Llama al use case. Mapea `List<Convenio>` → `ProximosVencimientosResponse`. Calcula `diasRestantes` y `mensaje`.

### Infrastructure

- `BeanConfiguration.java`: `@Configuration` que provee el `Supplier<LocalDate>` y el `ObtenerProximosVencimientosUseCase` como beans.

### Tests BDD

**Unit** — `ObtenerProximosVencimientosUseCaseTest`:
- `@DisplayName("Given convenios en rango <= 30 días, When se consultan próximos vencimientos, Then se retornan ordenados por fecha")`
- `@DisplayName("Given sin convenios en rango, When se consultan próximos vencimientos, Then se retorna lista vacía")`
- `@DisplayName("Given convenio vencido, When se consultan próximos vencimientos, Then no aparece en resultado")`

**Integration** — `ConvenioRepositoryAdapterTest` con `@DataJpaTest`:
- `@DisplayName("Given convenios sembrados en H2, When se buscan entre dos fechas, Then solo retorna los del rango")`

**Functional** — `DashboardControllerFunctionalTest` con `@SpringBootTest + MockMvc`:
- `@DisplayName("Given convenios próximos, When GET /api/v1/dashboard/proximos-vencimientos, Then retorna 200 con lista ordenada")`
- `@DisplayName("Given sin convenios próximos, When GET /api/v1/dashboard/proximos-vencimientos, Then retorna 200 con mensaje vacío")`
