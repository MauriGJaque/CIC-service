---
description: "Task list for Panel de Próximos Vencimientos"
---

# Tasks: Panel de Próximos Vencimientos

**Input**: Design documents from `specs/001-proximos-vencimientos/`

**Prerequisites**: plan.md ✅ | spec.md ✅ | research.md ✅ | data-model.md ✅ | contracts/dashboard-api.yaml ✅

**Tests**: Incluidos — la constitución exige BDD con tests unitarios, de integración y funcionales escritos ANTES de la implementación (TDD red-green-refactor).

**Organization**: Tareas agrupadas por historia de usuario para habilitar implementación y prueba independiente de cada historia.

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Puede ejecutarse en paralelo (archivos diferentes, sin dependencias incompletas)
- **[Story]**: Historia de usuario a la que pertenece la tarea (US1, US2)
- Incluye rutas de archivo exactas en las descripciones

## Path Conventions

- **Single Spring Boot project**: `src/main/`, `src/test/` en la raíz del repositorio
- Paquete base: `org.ups.cic`
- Código generado por openapi-generator: `build/generated/openapi/` (nunca editar)

---

## Phase 1: Setup (Build, recursos DB y config)

**Purpose**: Preparar el build (openapi-generator + JaCoCo), los archivos SQL de inicialización y la config de la app antes de escribir código Java.

- [X] T001 Copiar contrato OpenAPI desde `specs/001-proximos-vencimientos/contracts/dashboard-api.yaml` a `src/main/resources/openapi/dashboard-api.yaml`
- [X] T002 Agregar plugin `org.openapi.generator` v7.x en `build.gradle`; configurar tarea `openApiGenerate` con `inputSpec = "$projectDir/src/main/resources/openapi/dashboard-api.yaml"`, `generatorName = "spring"`, `outputDir = "$buildDir/generated/openapi"`, `apiPackage = "org.ups.cic.adapters.in.web.generated"`, `modelPackage = "org.ups.cic.adapters.in.web.generated.model"`, `configOptions = [interfaceOnly: "true", useSpringBoot3: "true"]`; agregar `"$buildDir/generated/openapi/src/main/java"` a `sourceSets.main.java.srcDirs`; hacer que `compileJava` dependa de `openApiGenerate`
- [X] T003 [P] Agregar plugin `jacoco` en `build.gradle`; configurar `jacocoTestReport` con reportes HTML y XML; configurar `jacocoTestCoverageVerification` con reglas: `element = CLASS`, `counter = LINE`, `minimum = 0.80` y `counter = BRANCH`, `minimum = 0.80`; excluir `**/CicServiceApplication*`, `**/generated/**`, `**/*JpaEntity*`, `**/config/**`; hacer que `check` dependa de `jacocoTestCoverageVerification`
- [X] T004 [P] Actualizar `src/main/resources/application.yaml` con: datasource H2 `jdbc:h2:mem:cicdb;DB_CLOSE_DELAY=-1`, driver `org.h2.Driver`, username `sa`, password vacía; `spring.jpa.hibernate.ddl-auto: none`; `spring.jpa.defer-datasource-initialization: true`; `spring.sql.init.mode: always`; `spring.sql.init.schema-locations: classpath:db/schema.sql`; `spring.sql.init.data-locations: classpath:db/data.sql`; `spring.h2.console.enabled: true` en path `/h2-console`; `spring.jpa.show-sql: true`
- [X] T005 [P] Crear `src/main/resources/db/schema.sql` con: `CREATE TABLE IF NOT EXISTS convenios (id BIGINT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(255) NOT NULL, entidad_socio VARCHAR(255) NOT NULL, fecha_inicio DATE NOT NULL, fecha_vencimiento DATE NOT NULL, estado VARCHAR(50) NOT NULL);` y `CREATE INDEX IF NOT EXISTS idx_convenios_fecha_vencimiento ON convenios (fecha_vencimiento);`
- [X] T006 [P] Crear `src/main/resources/db/data.sql` con 5 INSERTs de convenios de demo: (1) nombre `'Convenio UPS-ESPE'` entidad `'ESPE'` `fecha_vencimiento = DATEADD('DAY', 1, CURRENT_DATE)` estado `'VIGENTE'`; (2) `'Acuerdo UPS-ESPOCH'` `'ESPOCH'` `DATEADD('DAY', 8, CURRENT_DATE)` `'VIGENTE'`; (3) `'Convenio UPS-Banco Pichincha'` `'Banco Pichincha'` `DATEADD('DAY', 20, CURRENT_DATE)` `'VIGENTE'`; (4) `'Convenio UPS-ONG Amazonía'` `'ONG Amazonía'` `DATEADD('DAY', 60, CURRENT_DATE)` `'VIGENTE'`; (5) `'Acuerdo UPS-IESS'` `'IESS'` `DATEADD('DAY', -5, CURRENT_DATE)` `'VENCIDO'`
- [X] T007 [P] Crear `src/test/resources/application-test.yaml` con `spring.sql.init.mode: never` para que los tests no ejecuten `data.sql` principal y controlen sus propios datos con `@Sql`

**Checkpoint**: `./gradlew openApiGenerate` genera interfaces en `build/generated/openapi/`; `./gradlew bootRun` levanta la app con datos pre-cargados en `http://localhost:8080/h2-console` (tabla `convenios` con 5 filas)

---

## Phase 2: Foundational (Capa de dominio y persistencia — bloquea todas las historias)

**Purpose**: Entidad de dominio, puerto de repositorio, entidad JPA, adaptador de persistencia y wiring de beans. Sin lógica de negocio ni controladores.

**⚠️ CRÍTICO**: Ninguna historia de usuario puede comenzar hasta que esta fase esté completa.

- [X] T008 Crear enum `EstadoConvenio.java` en `src/main/java/org/ups/cic/domain/convenio/EstadoConvenio.java` con valores `VIGENTE`, `POR_VENCER`, `VENCIDO`, `RENOVADO`, `SUSPENDIDO`; sin anotaciones Spring
- [X] T009 [P] Crear entidad de dominio `Convenio.java` en `src/main/java/org/ups/cic/domain/convenio/Convenio.java` como Java record con campos: `Long id`, `String nombre`, `String entidadSocio`, `LocalDate fechaInicio`, `LocalDate fechaVencimiento`, `EstadoConvenio estado`; sin anotaciones Spring ni JPA (depende de T008)
- [X] T010 [P] Crear interfaz de puerto `ConvenioRepository.java` en `src/main/java/org/ups/cic/domain/convenio/ConvenioRepository.java` con método `List<Convenio> findByFechaVencimientoBetween(LocalDate desde, LocalDate hasta)`; sin anotaciones Spring
- [X] T011 Crear entidad JPA `ConvenioJpaEntity.java` en `src/main/java/org/ups/cic/adapters/out/persistence/ConvenioJpaEntity.java` con `@Entity @Table(name="convenios")`; campos: `@Id @GeneratedValue id`, `nombre`, `entidadSocio` (`@Column(name="entidad_socio")`), `fechaInicio` (`@Column(name="fecha_inicio")`), `fechaVencimiento` (`@Column(name="fecha_vencimiento")`), `estado` (`@Enumerated(EnumType.STRING)`); anotaciones Lombok `@Data @NoArgsConstructor @AllArgsConstructor @Builder`
- [X] T012 Crear `ConvenioJpaRepository.java` en `src/main/java/org/ups/cic/adapters/out/persistence/ConvenioJpaRepository.java` que extienda `JpaRepository<ConvenioJpaEntity, Long>`; agregar método `@Query("SELECT c FROM ConvenioJpaEntity c WHERE c.fechaVencimiento BETWEEN :desde AND :hasta ORDER BY c.fechaVencimiento ASC") List<ConvenioJpaEntity> findProximosVencimientos(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta)`
- [X] T013 Crear `ConvenioRepositoryAdapter.java` en `src/main/java/org/ups/cic/adapters/out/persistence/ConvenioRepositoryAdapter.java` que implemente `ConvenioRepository` (puerto de dominio); inyectar `ConvenioJpaRepository` por constructor; método `findByFechaVencimientoBetween` llama a `jpaRepository.findProximosVencimientos(desde, hasta)` y convierte cada `ConvenioJpaEntity` a `Convenio` de dominio mediante método privado `toDomain`; sin `@Service` (se registra como `@Bean`)
- [X] T014 Crear `BeanConfiguration.java` en `src/main/java/org/ups/cic/infrastructure/config/BeanConfiguration.java` con `@Configuration`; definir `@Bean Supplier<LocalDate> currentDateSupplier()` que retorna `() -> LocalDate.now()`; definir `@Bean ConvenioRepositoryAdapter convenioRepositoryAdapter(ConvenioJpaRepository jpaRepo)`; el bean `ObtenerProximosVencimientosUseCase` se definirá en T019 tras implementar el use case

**Checkpoint**: `./gradlew compileJava` compila sin errores — capas de dominio y persistencia presentes

---

## Phase 3: User Story 1 — Ver convenios próximos a vencer (Priority: P1) 🎯 MVP

**Goal**: La directora carga el panel y ve todos los convenios con vencimiento ≤ 30 días ordenados por fecha ascendente.

**Independent Test**: Sembrar convenios con fechas variadas en H2 (vía `@Sql`) → `GET /api/v1/dashboard/proximos-vencimientos` → solo aparecen los de ≤ 30 días en orden ascendente; el vencido y el lejano quedan excluidos.

### Tests para User Story 1 ⚠️ — Escribir PRIMERO (fase RED)

- [X] T015 [P] [US1] Crear `ObtenerProximosVencimientosUseCaseTest.java` en `src/test/java/org/ups/cic/unit/application/convenio/ObtenerProximosVencimientosUseCaseTest.java`; usar JUnit 5 + Mockito; declarar `@Mock ConvenioRepository` y `Supplier<LocalDate>` con fecha fija `2026-07-02`; 4 métodos con `@DisplayName`: (1) `"Given convenios en rango ≤ 30 días, When ejecutar, Then retorna lista ordenada por fechaVencimiento ASC"` — stubear repo para retornar 2 convenios ordenados, verificar que el use case los retorna en el mismo orden; (2) `"Given convenio con fechaVencimiento = hoy+30, When ejecutar, Then aparece en el resultado"` — verificar límite superior incluido; (3) `"Given convenio con fechaVencimiento = ayer, When ejecutar, Then no aparece en resultado"` — el adaptador ya filtra en la query, pero verificar que el use case pasa los parámetros correctos (hoy y hoy+30); (4) `"Given sin convenios en rango, When ejecutar, Then retorna lista vacía"`
- [X] T016 [P] [US1] Crear `ConvenioRepositoryAdapterTest.java` en `src/test/java/org/ups/cic/integration/adapters/out/persistence/ConvenioRepositoryAdapterTest.java` con `@DataJpaTest` y `@ActiveProfiles("test")`; usar `@Sql` para sembrar datos específicos; 3 escenarios: (1) `@DisplayName("Given convenios con fechas variadas en H2, When findByFechaVencimientoBetween(hoy, hoy+30), Then retorna solo los del rango ordenados ASC")` — insertar 4 convenios (2 en rango, 1 vencido, 1 lejano); verificar que retorna exactamente 2, ordenados; (2) `@DisplayName("Given sin convenios en rango, When findByFechaVencimientoBetween, Then retorna lista vacía")`; (3) `@DisplayName("Given convenio con fechaVencimiento = exactamente hoy+30, When findByFechaVencimientoBetween(hoy, hoy+30), Then el convenio aparece en el resultado — límite superior incluido")` — verificar que el BETWEEN incluye el límite derecho
- [X] T017 [P] [US1] Crear `DashboardControllerFunctionalTest.java` en `src/test/java/org/ups/cic/functional/adapters/in/web/DashboardControllerFunctionalTest.java` con `@SpringBootTest(webEnvironment = RANDOM_PORT)`, `@AutoConfigureMockMvc` y `@ActiveProfiles("test")`; anotar la clase con `@Sql(scripts = "classpath:data-us1.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)` y `@Sql(statements = "DELETE FROM convenios", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)`; método US1: `@DisplayName("Given convenios próximos sembrados, When GET /api/v1/dashboard/proximos-vencimientos, Then 200 con lista no vacía, hayVencimientos=true, mensaje=null, orden ASC por fechaVencimiento")`; verificar con `MockMvc`: status 200, `$.hayVencimientos == true`, `$.mensaje == null`, `$.totalConvenios >= 1`, `$.convenios[0].diasRestantes <= $.convenios[1].diasRestantes`
- [X] T018 [P] [US1] Crear `src/test/resources/data-us1.sql` con 5 INSERTs: (1) `DATEADD('DAY', 1, CURRENT_DATE)` estado `'VIGENTE'` (en rango); (2) `DATEADD('DAY', 15, CURRENT_DATE)` `'VIGENTE'` (en rango); (3) `DATEADD('DAY', 30, CURRENT_DATE)` `'VIGENTE'` (límite exacto hoy+30 — debe incluirse); (4) `DATEADD('DAY', -3, CURRENT_DATE)` `'VENCIDO'` (excluido — ya venció); (5) `DATEADD('DAY', 45, CURRENT_DATE)` `'VIGENTE'` (excluido — demasiado lejano)
- [X] T019 [US1] Verificar que T015, T016 y T017 FALLAN ejecutando `./gradlew test` (fase RED del ciclo TDD) — los errores esperados son de compilación o `ClassNotFoundException` porque el use case y el controller aún no existen

### Implementation para User Story 1

- [X] T020 [US1] Implementar `ObtenerProximosVencimientosUseCase.java` en `src/main/java/org/ups/cic/application/convenio/ObtenerProximosVencimientosUseCase.java`; constructor recibe `ConvenioRepository convenioRepository` y `Supplier<LocalDate> currentDate`; método `public List<Convenio> ejecutar()` calcula `LocalDate hoy = currentDate.get()`, `LocalDate limite = hoy.plusDays(30)`, retorna `convenioRepository.findByFechaVencimientoBetween(hoy, limite)`; sin anotaciones Spring
- [X] T021 [P] [US1] Crear `ConvenioMapper.java` en `src/main/java/org/ups/cic/adapters/in/web/mapper/ConvenioMapper.java`; método estático `ProximoVencimientoDTO toDto(Convenio convenio, LocalDate hoy)` que construye el DTO con `diasRestantes = (int) ChronoUnit.DAYS.between(hoy, convenio.fechaVencimiento())`; método estático `ProximosVencimientosResponse toResponse(List<Convenio> convenios, LocalDate hoy)` que mapea la lista con `toDto`, calcula `totalConvenios`, `hayVencimientos = !convenios.isEmpty()`, `mensaje = convenios.isEmpty() ? "Sin vencimientos próximos en los próximos 30 días" : null`
- [X] T022 [US1] Crear `DashboardController.java` en `src/main/java/org/ups/cic/adapters/in/web/DashboardController.java` que implemente la interfaz `DashboardApi` generada por openapi-generator; inyectar `ObtenerProximosVencimientosUseCase useCase` y `Supplier<LocalDate> currentDate` por constructor; implementar `getProximosVencimientos()`: llama `useCase.ejecutar()`, mapea con `ConvenioMapper.toResponse(convenios, currentDate.get())`, retorna `ResponseEntity.ok(response)`
- [X] T023 [US1] Actualizar `BeanConfiguration.java` en `src/main/java/org/ups/cic/infrastructure/config/BeanConfiguration.java` para agregar `@Bean ObtenerProximosVencimientosUseCase obtenerProximosVencimientosUseCase(ConvenioRepositoryAdapter adapter, Supplier<LocalDate> currentDateSupplier)`
- [X] T024 [US1] Ejecutar `./gradlew test --tests "*.unit.*" --tests "*.integration.*"` y verificar que T015 y T016 pasan (GREEN); corregir hasta que pasen
- [X] T025 [US1] Ejecutar `./gradlew test --tests "*.functional.*"` y verificar que T017 pasa (GREEN); corregir hasta que pase

**Checkpoint**: US1 completamente funcional — `./gradlew test` pasa; `curl http://localhost:8080/api/v1/dashboard/proximos-vencimientos` retorna los 3 convenios demo (1, 8 y 20 días), excluye el vencido y el de 60 días

---

## Phase 4: User Story 2 — Panel sin convenios próximos a vencer (Priority: P2)

**Goal**: Cuando no existen convenios con vencimiento en ≤ 30 días, el panel muestra el mensaje "Sin vencimientos próximos en los próximos 30 días" en lugar de una lista vacía sin contexto.

**Independent Test**: Sembrar solo convenios con fechas > 30 días o ya vencidos → `GET /api/v1/dashboard/proximos-vencimientos` → `convenios=[]`, `totalConvenios=0`, `hayVencimientos=false`, `mensaje="Sin vencimientos próximos en los próximos 30 días"`.

### Tests para User Story 2

- [X] T026 [P] [US2] Crear `src/test/resources/data-us2.sql` con 2 INSERTs: (1) `DATEADD('DAY', 45, CURRENT_DATE)` `'VIGENTE'` (fuera del rango); (2) `DATEADD('DAY', -10, CURRENT_DATE)` `'VENCIDO'` (vencido)
- [X] T027 [US2] Agregar método de test US2 en `DashboardControllerFunctionalTest.java` en `src/test/java/org/ups/cic/functional/adapters/in/web/DashboardControllerFunctionalTest.java`; anotar el método con `@Sql(scripts = "classpath:data-us2.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)` y `@Sql(statements = "DELETE FROM convenios", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)`; `@DisplayName("Given sin convenios próximos, When GET /api/v1/dashboard/proximos-vencimientos, Then 200 con convenios=[], totalConvenios=0, hayVencimientos=false, mensaje='Sin vencimientos próximos en los próximos 30 días'")`

### Implementation para User Story 2

- [X] T028 [US2] Verificar que `ConvenioMapper.toResponse()` ya produce el mensaje correcto cuando la lista es vacía (implementado en T021); si falta ajuste, corregir `ConvenioMapper.java` en `src/main/java/org/ups/cic/adapters/in/web/mapper/ConvenioMapper.java`
- [X] T029 [US2] Ejecutar `./gradlew test --tests "*.functional.*"` y verificar que T027 pasa (GREEN)

**Checkpoint**: US1 y US2 completamente funcionales — `./gradlew test` completo pasa al 100%

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Validación de cobertura, calidad de código y verificación final del pipeline completo.

- [X] T030 [P] Ejecutar `./gradlew test jacocoTestReport`; abrir `build/reports/jacoco/test/html/index.html`; verificar que cobertura global de líneas ≥ 80% y cobertura por clase > 80%; documentar cualquier clase con cobertura baja e identificar tests faltantes
- [X] T031 [P] Ejecutar `./gradlew test jacocoTestCoverageVerification`; confirmar que el build pasa; eliminar temporalmente un test para verificar que el build FALLA (gate funciona); restaurar el test
- [X] T032 Agregar Javadoc al método `ejecutar()` de `ObtenerProximosVencimientosUseCase.java` en `src/main/java/org/ups/cic/application/convenio/ObtenerProximosVencimientosUseCase.java` explicando: rango `[hoy, hoy+30]` ambos extremos incluidos; convenios con `fechaVencimiento < hoy` excluidos por la query; lista retornada siempre ordenada por `fechaVencimiento ASC`
- [X] T033 [P] Verificar que `ConvenioJpaEntity.java` en `src/main/java/org/ups/cic/adapters/out/persistence/ConvenioJpaEntity.java` tiene `@Column(name="fecha_vencimiento")` explícito y que `schema.sql` incluye el índice `idx_convenios_fecha_vencimiento`
- [X] T034 [P] Agregar tarea de lint del contrato OpenAPI con Spectral en `build.gradle`: configurar tarea `lintOpenApi` de tipo `Exec` que ejecute `npx @stoplight/spectral-cli lint src/main/resources/openapi/dashboard-api.yaml --ruleset https://unpkg.com/@stoplight/spectral-owasp-ruleset`; alternativamente, agregar script `lint-api.sh` en `.specify/scripts/` que ejecute Spectral y retorne exit code no-cero si hay errores; hacer que `check` dependa de `lintOpenApi`
- [X] T035 [P] Ejecutar `./gradlew build` completo y confirmar que el pipeline pasa de inicio a fin: `openApiGenerate` → `lintOpenApi` → `compileJava` → `test` → `jacocoTestReport` → `jacocoTestCoverageVerification`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sin dependencias — comenzar de inmediato; T002, T003, T004, T005, T006, T007 pueden ejecutarse en paralelo entre sí
- **Foundational (Phase 2)**: Requiere Phase 1 completa (necesita `build.gradle` y `application.yaml` configurados); T009 y T010 requieren T008 (enum); T011–T014 requieren T009–T010
- **User Story 1 (Phase 3)**: Requiere Phase 2 completa; T015–T018 (tests) pueden escribirse en paralelo entre sí; implementación T020–T023 en secuencia tras tests
- **User Story 2 (Phase 4)**: Requiere Phase 3 completa; T026 (data-us2.sql) puede escribirse en paralelo con T027 (test)
- **Polish (Phase 5)**: Requiere Phase 4 — todas las historias completas

### Within Each User Story (BDD TDD cycle)

1. Tests escritos → ejecutados → deben FALLAR (RED)
2. Implementación mínima para hacer pasar los tests (GREEN)
3. Refactorización sin romper tests (REFACTOR)
4. Checkpoint de historia completado antes de avanzar

### Parallel Opportunities (13 oportunidades)

- Phase 1: T002, T003, T004, T005, T006, T007 — todos en paralelo (archivos distintos)
- Phase 2: T009 y T010 en paralelo (tras T008); T011, T012, T013 en paralelo (tras T009, T010)
- Phase 3 tests: T015, T016, T017, T018 — todos en paralelo
- Phase 3 impl: T021 en paralelo con T020
- Phase 4: T026 en paralelo con T027
- Phase 5: T030, T031, T033, T034 — en paralelo

---

## Parallel Example: User Story 1 — Fase de Tests

```bash
# Escribir todos los tests de US1 simultáneamente (archivos distintos):
Task T015: ObtenerProximosVencimientosUseCaseTest.java
Task T016: ConvenioRepositoryAdapterTest.java
Task T017: DashboardControllerFunctionalTest.java (escenario US1)
Task T018: src/test/resources/data-us1.sql
```

---

## Implementation Strategy

### MVP First (User Story 1 — T001 a T025)

1. Phase 1 Setup (T001–T007): build + DB files
2. Phase 2 Foundational (T008–T014): capas domain + persistence
3. Phase 3 US1 Tests (T015–T019): escribir y verificar RED
4. Phase 3 US1 Impl (T020–T025): implementar y verificar GREEN
5. **DETENER Y VALIDAR**: `./gradlew test` + demo del endpoint en `bootRun`

### Incremental Delivery

1. Setup + Foundation → base lista para cualquier historia
2. US1 → endpoint funcional con datos demo → demo a directora (MVP)
3. US2 → estado vacío validado → feature completa
4. Polish → JaCoCo gates verificados → listo para code review

### Parallel Team Strategy

Con múltiples desarrolladores después de Phase 2:
- Dev A: US1 tests (T015–T018) + implementación (T020–T023)
- Dev B: Puede adelantar data-us2.sql (T026) mientras espera

---

## Notes

- `[P]` = archivos distintos, sin dependencias incompletas en la misma fase
- `[US1]`, `[US2]` mapean directamente a las historias de usuario de `spec.md`
- Los tests de US1 (T015 escenario 4) cubren implícitamente el caso vacío a nivel unitario; T027 lo valida end-to-end
- `schema.sql` usa `CREATE TABLE IF NOT EXISTS` para soportar reinicios del contexto H2 en tests
- `data.sql` usa `DATEADD('DAY', N, CURRENT_DATE)` — sintaxis nativa H2, determinista en cualquier fecha de ejecución
- El código generado por openapi-generator en `build/generated/openapi/` NUNCA se edita manualmente; cambios van al contrato YAML
