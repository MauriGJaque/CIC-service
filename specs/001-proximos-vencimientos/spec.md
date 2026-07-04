# Feature Specification: Panel de Próximos Vencimientos

**Feature Branch**: `001-proximos-vencimientos`

**Created**: 2026-07-02

**Status**: Draft

**Input**: User description: "HU-E1-02 · Panel de próximos vencimientos en la dirección · E1 · 2 pts"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Ver convenios próximos a vencer (Priority: P1)

La directora de alianzas accede a su panel de dirección y visualiza la sección "Próximos vencimientos", la cual lista todos los convenios cuya fecha de vencimiento es dentro de los próximos 30 días calendario, ordenados de forma ascendente por fecha (los más urgentes primero).

**Why this priority**: Es el flujo principal de la historia de usuario. Sin esta capacidad, la directora no puede detectar convenios en riesgo de vencer sin revisar registro por registro, lo que representa el valor de negocio central.

**Independent Test**: Se puede probar de forma independiente cargando el panel con al menos un convenio cuya fecha de vencimiento caiga dentro del rango ≤ 30 días y verificando que aparece listado en la sección correcta.

**Acceptance Scenarios**:

1. **Given** la directora tiene sesión iniciada y existen convenios con vencimiento en ≤ 30 días, **When** carga el panel de dirección, **Then** ve la sección "Próximos vencimientos" con todos esos convenios listados en orden ascendente por fecha de vencimiento.
2. **Given** existen convenios con vencimiento en 1 día, 15 días y 28 días, **When** la directora carga el panel, **Then** los convenios aparecen en ese orden (1 día primero, 28 días al final) bajo la sección "Próximos vencimientos".
3. **Given** existen convenios con vencimiento exactamente hoy (0 días restantes), **When** la directora carga el panel, **Then** esos convenios también aparecen en la sección "Próximos vencimientos" como los más urgentes.

---

### User Story 2 - Panel sin convenios próximos a vencer (Priority: P2)

La directora accede al panel en un momento donde ningún convenio vence en los próximos 30 días, y el sistema le comunica claramente que no hay elementos que requieran atención.

**Why this priority**: Es un estado válido del sistema que debe manejarse con un mensaje explícito para evitar confusión (la directora no debe pensar que la sección está rota o que hay un error).

**Independent Test**: Se puede probar de forma independiente configurando el sistema sin convenios próximos a vencer y verificando que el mensaje vacío aparece correctamente.

**Acceptance Scenarios**:

1. **Given** no existen convenios con vencimiento en ≤ 30 días, **When** la directora abre el panel, **Then** la sección "Próximos vencimientos" muestra el mensaje "Sin vencimientos próximos en los próximos 30 días" y no muestra ningún convenio.
2. **Given** existen convenios pero todos vencen en más de 30 días, **When** la directora carga el panel, **Then** la sección muestra el mismo mensaje de estado vacío.

---

### Edge Cases

- ¿Qué sucede cuando un convenio vence exactamente en 30 días? Debe incluirse en la lista (límite incluido).
- ¿Qué sucede cuando un convenio ya venció (fecha de vencimiento pasada)? No debe aparecer en "Próximos vencimientos" (es un convenio vencido, no próximo a vencer).
- ¿Qué sucede cuando hay un gran número de convenios próximos a vencer (ej. 100+)? En v1 el sistema retorna la lista completa sin paginación; el ordenamiento ascendente se mantiene independientemente del volumen. La paginación se considerará en versiones futuras si el volumen lo justifica.
- ¿Qué sucede si la sesión de la directora no es válida o ha expirado al llamar al endpoint? El sistema retorna HTTP 401 sin incluir datos de convenios en la respuesta. La redirección al login es responsabilidad del cliente (frontend), no del servicio REST.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema DEBE mostrar una sección denominada "Próximos vencimientos" dentro del panel de la directora de alianzas.
- **FR-002**: El sistema DEBE incluir en la sección todos los convenios cuya fecha de vencimiento sea igual o menor a 30 días calendario desde la fecha actual (inclusive ambos extremos: hoy y hoy+30).
- **FR-003**: El sistema DEBE ordenar los convenios listados por fecha de vencimiento de forma ascendente (más próximo primero).
- **FR-004**: El sistema DEBE mostrar el mensaje "Sin vencimientos próximos en los próximos 30 días" cuando no existan convenios con vencimiento en el rango ≤ 30 días.
- **FR-005**: El sistema NO DEBE incluir convenios ya vencidos (fecha de vencimiento anterior a hoy) en la sección "Próximos vencimientos".
- **FR-006**: El sistema DEBE actualizar la sección con los datos vigentes cada vez que la directora cargue o recargue el panel.
- **FR-007**: El sistema DEBE mostrar para cada convenio listado al menos: nombre o identificador del convenio y fecha de vencimiento.

### Key Entities

- **Convenio**: Acuerdo formal registrado en el sistema con al menos un identificador, nombre, y fecha de vencimiento. Es la entidad central que se consulta para determinar los próximos vencimientos.
- **Panel de Dirección**: Vista principal de la directora de alianzas que agrupa métricas e información estratégica, incluyendo la sección "Próximos vencimientos".
- **Vencimiento**: Fecha límite de vigencia de un convenio. Determina si el convenio entra al filtro de ≤ 30 días.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: La directora puede identificar todos los convenios que requieren atención inmediata en menos de 10 segundos desde que carga el panel, sin necesidad de navegación adicional.
- **SC-002**: El 100% de los convenios con vencimiento en ≤ 30 días aparecen listados en la sección, sin omisiones ni duplicados.
- **SC-003**: Los convenios siempre aparecen ordenados correctamente por urgencia; ninguna prueba de ordenación falla.
- **SC-004**: El mensaje de estado vacío aparece en el 100% de los casos en que no hay convenios próximos a vencer, eliminando ambigüedad para la directora.
- **SC-005**: El endpoint de próximos vencimientos responde en menos de 500 ms en el percentil 95 bajo carga normal de operación (hasta 50 solicitudes concurrentes).

## Assumptions

- La directora de alianzas cuenta con un rol autenticado en el sistema que le da acceso al panel de dirección; la autenticación está fuera del alcance de esta historia.
- El sistema ya cuenta con un registro de convenios con fecha de vencimiento almacenada; esta historia no cubre la creación ni edición de convenios.
- El cálculo de los "30 días" se realiza en base a la fecha del servidor (timezone del sistema), no la fecha local del navegador de la directora.
- Los convenios sin fecha de vencimiento registrada no se incluyen en la sección "Próximos vencimientos".
- El panel de dirección donde se integra esta sección ya existe como punto de entrada para la directora; esta historia agrega la sección dentro de dicho panel.
- En v1 el endpoint retorna la lista completa de convenios próximos sin paginación. Paginación fuera del alcance de esta historia.
- La autenticación produce HTTP 401/403 como respuesta REST; la redirección al login es responsabilidad del cliente que consume la API.
