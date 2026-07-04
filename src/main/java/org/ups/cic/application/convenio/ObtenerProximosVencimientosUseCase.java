package org.ups.cic.application.convenio;

import org.ups.cic.domain.convenio.Convenio;
import org.ups.cic.domain.convenio.ConvenioRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

public class ObtenerProximosVencimientosUseCase {

    private final ConvenioRepository convenioRepository;
    private final Supplier<LocalDate> currentDate;

    public ObtenerProximosVencimientosUseCase(ConvenioRepository convenioRepository,
                                              Supplier<LocalDate> currentDate) {
        this.convenioRepository = convenioRepository;
        this.currentDate = currentDate;
    }

    /**
     * Retorna convenios con fechaVencimiento en el rango [hoy, hoy+30], ambos extremos incluidos.
     * Convenios con fechaVencimiento anterior a hoy quedan excluidos por la consulta al repositorio.
     * La lista retornada está ordenada por fechaVencimiento ASC (garantizado por el repositorio).
     */
    public List<Convenio> ejecutar() {
        LocalDate hoy = currentDate.get();
        LocalDate limite = hoy.plusDays(30);
        return convenioRepository.findByFechaVencimientoBetween(hoy, limite);
    }
}
