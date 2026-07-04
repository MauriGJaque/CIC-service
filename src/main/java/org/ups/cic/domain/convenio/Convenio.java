package org.ups.cic.domain.convenio;

import java.time.LocalDate;

public record Convenio(
        Long id,
        String nombre,
        String entidadSocio,
        LocalDate fechaInicio,
        LocalDate fechaVencimiento,
        EstadoConvenio estado
) {}
