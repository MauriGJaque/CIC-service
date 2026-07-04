package org.ups.cic.adapters.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.ups.cic.adapters.in.web.generated.ApiApi;
import org.ups.cic.adapters.in.web.generated.model.ProximosVencimientosResponse;
import org.ups.cic.adapters.in.web.mapper.ConvenioMapper;
import org.ups.cic.application.convenio.ObtenerProximosVencimientosUseCase;
import org.ups.cic.domain.convenio.Convenio;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

@RestController
public class DashboardController implements ApiApi {

    private final ObtenerProximosVencimientosUseCase useCase;
    private final Supplier<LocalDate> currentDate;

    public DashboardController(ObtenerProximosVencimientosUseCase useCase,
                               Supplier<LocalDate> currentDate) {
        this.useCase = useCase;
        this.currentDate = currentDate;
    }

    @Override
    public ResponseEntity<ProximosVencimientosResponse> getProximosVencimientos() {
        List<Convenio> convenios = useCase.ejecutar();
        ProximosVencimientosResponse response = ConvenioMapper.toResponse(convenios, currentDate.get());
        return ResponseEntity.ok(response);
    }
}
