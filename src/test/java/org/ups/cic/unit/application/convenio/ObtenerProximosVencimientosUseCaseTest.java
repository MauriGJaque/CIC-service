package org.ups.cic.unit.application.convenio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ups.cic.application.convenio.ObtenerProximosVencimientosUseCase;
import org.ups.cic.domain.convenio.Convenio;
import org.ups.cic.domain.convenio.ConvenioRepository;
import org.ups.cic.domain.convenio.EstadoConvenio;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObtenerProximosVencimientosUseCaseTest {

    private static final LocalDate FECHA_FIJA = LocalDate.of(2026, 7, 3);

    @Mock
    private ConvenioRepository convenioRepository;

    private ObtenerProximosVencimientosUseCase useCase;

    @BeforeEach
    void setUp() {
        Supplier<LocalDate> fechaFija = () -> FECHA_FIJA;
        useCase = new ObtenerProximosVencimientosUseCase(convenioRepository, fechaFija);
    }

    @Test
    @DisplayName("Given convenios en rango ≤ 30 días, When ejecutar, Then retorna lista ordenada por fechaVencimiento ASC")
    void dadoConveniosEnRango_cuandoEjecutar_entoncesRetornaListaOrdenada() {
        LocalDate hoy = FECHA_FIJA;
        LocalDate limite = hoy.plusDays(30);

        Convenio convenio1 = convenio(1L, hoy.plusDays(5));
        Convenio convenio2 = convenio(2L, hoy.plusDays(20));
        when(convenioRepository.findByFechaVencimientoBetween(hoy, limite))
                .thenReturn(List.of(convenio1, convenio2));

        List<Convenio> resultado = useCase.ejecutar();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).fechaVencimiento()).isEqualTo(hoy.plusDays(5));
        assertThat(resultado.get(1).fechaVencimiento()).isEqualTo(hoy.plusDays(20));
        verify(convenioRepository).findByFechaVencimientoBetween(hoy, limite);
    }

    @Test
    @DisplayName("Given convenio con fechaVencimiento = hoy+30, When ejecutar, Then el convenio aparece — límite superior incluido")
    void dadoConvenioEnLimite30Dias_cuandoEjecutar_entoncesAparece() {
        LocalDate hoy = FECHA_FIJA;
        LocalDate limite = hoy.plusDays(30);

        Convenio convenioLimite = convenio(3L, limite);
        when(convenioRepository.findByFechaVencimientoBetween(hoy, limite))
                .thenReturn(List.of(convenioLimite));

        List<Convenio> resultado = useCase.ejecutar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).fechaVencimiento()).isEqualTo(limite);
    }

    @Test
    @DisplayName("Given convenio con fechaVencimiento = ayer, When ejecutar, Then el use case pasa 'hoy' como límite inferior al repositorio")
    void dadoConvenioVencido_cuandoEjecutar_entoncesRepoRecibeLimiteCorrecto() {
        LocalDate hoy = FECHA_FIJA;
        LocalDate limite = hoy.plusDays(30);

        when(convenioRepository.findByFechaVencimientoBetween(hoy, limite))
                .thenReturn(List.of());

        List<Convenio> resultado = useCase.ejecutar();

        assertThat(resultado).isEmpty();
        verify(convenioRepository).findByFechaVencimientoBetween(hoy, limite);
    }

    @Test
    @DisplayName("Given sin convenios en rango, When ejecutar, Then retorna lista vacía")
    void dadoSinConveniosEnRango_cuandoEjecutar_entoncesRetornaListaVacia() {
        LocalDate hoy = FECHA_FIJA;
        LocalDate limite = hoy.plusDays(30);

        when(convenioRepository.findByFechaVencimientoBetween(hoy, limite))
                .thenReturn(List.of());

        List<Convenio> resultado = useCase.ejecutar();

        assertThat(resultado).isEmpty();
    }

    private Convenio convenio(Long id, LocalDate fechaVencimiento) {
        return new Convenio(id, "Convenio Test " + id, "Entidad Test",
                LocalDate.of(2025, 1, 1), fechaVencimiento, EstadoConvenio.VIGENTE);
    }
}
