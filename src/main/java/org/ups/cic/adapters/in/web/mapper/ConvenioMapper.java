package org.ups.cic.adapters.in.web.mapper;

import org.ups.cic.adapters.in.web.generated.model.ProximoVencimientoDTO;
import org.ups.cic.adapters.in.web.generated.model.ProximosVencimientosResponse;
import org.ups.cic.domain.convenio.Convenio;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ConvenioMapper {

    private ConvenioMapper() {}

    public static ProximoVencimientoDTO toDto(Convenio convenio, LocalDate hoy) {
        int diasRestantes = (int) ChronoUnit.DAYS.between(hoy, convenio.fechaVencimiento());
        return new ProximoVencimientoDTO()
                .id(convenio.id())
                .nombre(convenio.nombre())
                .entidadSocio(convenio.entidadSocio())
                .fechaVencimiento(convenio.fechaVencimiento())
                .diasRestantes(diasRestantes);
    }

    public static ProximosVencimientosResponse toResponse(List<Convenio> convenios, LocalDate hoy) {
        List<ProximoVencimientoDTO> dtos = convenios.stream()
                .map(c -> toDto(c, hoy))
                .toList();

        boolean hayVencimientos = !convenios.isEmpty();
        String mensaje = hayVencimientos ? null : "Sin vencimientos próximos en los próximos 30 días";

        return new ProximosVencimientosResponse()
                .convenios(dtos)
                .totalConvenios(dtos.size())
                .hayVencimientos(hayVencimientos)
                .mensaje(mensaje);
    }
}
