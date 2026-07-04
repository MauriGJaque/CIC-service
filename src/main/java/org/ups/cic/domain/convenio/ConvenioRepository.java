package org.ups.cic.domain.convenio;

import java.time.LocalDate;
import java.util.List;

public interface ConvenioRepository {
    List<Convenio> findByFechaVencimientoBetween(LocalDate desde, LocalDate hasta);
}
