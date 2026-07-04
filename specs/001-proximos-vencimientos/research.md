# Research: Panel de Próximos Vencimientos

**Feature**: 001-proximos-vencimientos
**Date**: 2026-07-02

## 1. Stack tecnológico del proyecto

**Decision**: Java 17 + Spring Boot 4.1.0 + Spring Data JPA + H2 (dev/test) + Gradle  
**Rationale**: Ya establecido en `build.gradle`. No se introduce tecnología nueva para esta historia.  
**Alternatives considered**: N/A — stack ya definido.

## 2. Arquitectura Clean Architecture en Spring Boot

**Decision**: Estructura de paquetes por capa limpia bajo `org.ups.cic`:
```
org.ups.cic/
├── domain/
│   └── convenio/
│       ├── Convenio.java          (entidad de dominio)
│       └── ConvenioRepository.java (puerto/interfaz)
├── application/
│   └── convenio/
│       └── ObtenerProximosVencimientosUseCase.java
├── adapters/
│   ├── in/
│   │   └── web/
│   │       ├── DashboardController.java
│   │       └── dto/ProximoVencimientoResponse.java
│   └── out/
│       └── persistence/
│           ├── ConvenioJpaEntity.java
│           ├── ConvenioJpaRepository.java (extends JpaRepository)
│           └── ConvenioRepositoryAdapter.java (implements domain port)
└── infrastructure/
    └── config/
        └── BeanConfiguration.java
```
**Rationale**: Cumple Principio de Dependencia (Clean Architecture). La lógica de negocio (30 días) vive exclusivamente en el use case, sin depender de Spring.  
**Alternatives considered**: Estructura por feature (feature-package) — descartada porque dificulta separar capas sin framework.

## 3. Estrategia de testing BDD con JUnit 5

**Decision**: Usar JUnit 5 con nombres `@DisplayName` tipo Given/When/Then. Sin framework BDD externo (no Cucumber) dado el tamaño de la historia.  
**Rationale**: Spring Boot 4.x incluye JUnit 5 nativo. Cucumber añadiría dependencia sin valor proporcional para 2 escenarios. El convenio es naming convention + estructura de test.  
**Alternatives considered**: Cucumber — descartado por overhead de configuración para historias pequeñas.

Tipos de test requeridos:
- **Unit**: `ObtenerProximosVencimientosUseCaseTest` — prueba la lógica de filtrado/ordenación con mock del repositorio
- **Integration**: `ConvenioRepositoryAdapterTest` — prueba la capa de persistencia con H2 real (`@DataJpaTest`)
- **Functional**: `DashboardControllerFunctionalTest` — prueba el endpoint completo HTTP→DB→response con `@SpringBootTest` + `MockMvc`

## 4. API First con OpenAPI Generator

**Decision**: Contrato OpenAPI 3.1 en `src/main/resources/openapi/dashboard-api.yaml`. Usar `openapi-generator-gradle-plugin` para generar interfaces de controlador (server stubs) y DTOs.  
**Rationale**: Cumple el principio IV de la constitución. El controlador implementa la interfaz generada; nunca se modifica el código generado.  
**Alternatives considered**: Springdoc (code-first) — descartado porque la constitución exige API First (contrato primero).

Configuración gradle:
```groovy
id 'org.openapi.generator' version '7.x'
// task openApiGenerate → genera en build/generated/openapi/
// sourceSets incluye build/generated/openapi/src/main/java
```

## 5. JaCoCo — Coverage Gates

**Decision**: Configurar JaCoCo en `build.gradle` con thresholds:
- Coverage por clase: > 80% líneas + ramas
- Coverage global: ≥ 80% líneas + ramas
- Build falla si no se cumplen
- Exclusiones: clases generadas por openapi-generator, entidades JPA (`*JpaEntity`), clase principal (`CicServiceApplication`)

**Rationale**: Principio V de la constitución — non-negotiable.  
**Alternatives considered**: Jacoco defaults sin threshold — descartado porque no fuerza la cobertura.

## 6. Cálculo de "próximos 30 días"

**Decision**: Calcular en el use case con `LocalDate.now()` inyectado como `Supplier<LocalDate>` para facilitar testing. El rango es `[hoy, hoy+30]` ambos extremos incluidos. Convenios vencidos (fecha < hoy) excluidos.  
**Rationale**: La lógica de negocio no debe depender del reloj del sistema directamente (testabilidad). Inyectar el proveedor de fecha permite tests deterministas.  
**Alternatives considered**: `LocalDate.now()` hardcodeado — descartado por no ser testeable.

## 7. Persistencia — H2 para dev/test

**Decision**: H2 in-memory para desarrollo y tests. Sin cambio de base de datos por esta historia.  
**Rationale**: Ya configurado en el proyecto. Esta historia no justifica migrar a PostgreSQL.  
**Alternatives considered**: PostgreSQL con TestContainers — reservado para cuando el proyecto madure a producción real.

## Resumen de decisiones clave

| Área | Decisión |
|------|----------|
| Lenguaje/Runtime | Java 17 + Spring Boot 4.1.0 |
| Arquitectura | Clean Architecture (4 capas) |
| Testing | JUnit 5 BDD (unit + integration + functional) |
| API | OpenAPI 3.1 + openapi-generator-gradle-plugin |
| Coverage | JaCoCo ≥ 80% global, > 80% por clase |
| DB (dev/test) | H2 in-memory |
| Cálculo fechas | `Supplier<LocalDate>` inyectado |
