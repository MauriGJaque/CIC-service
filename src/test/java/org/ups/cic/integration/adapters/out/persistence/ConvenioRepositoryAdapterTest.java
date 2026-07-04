package org.ups.cic.integration.adapters.out.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.ups.cic.adapters.out.persistence.ConvenioJpaRepository;
import org.ups.cic.adapters.out.persistence.ConvenioRepositoryAdapter;
import org.ups.cic.domain.convenio.Convenio;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ConvenioRepositoryAdapterTest {

    @Autowired
    private ConvenioJpaRepository jpaRepository;

    @Test
    @DisplayName("Given convenios con fechas variadas en H2, When findByFechaVencimientoBetween(hoy, hoy+30), Then retorna solo los del rango ordenados ASC")
    @Sql(statements = {
        "INSERT INTO convenios (nombre, entidad_socio, fecha_inicio, fecha_vencimiento, estado) VALUES ('Convenio A', 'Entidad A', '2025-01-01', DATEADD('DAY', 5, CURRENT_DATE), 'VIGENTE')",
        "INSERT INTO convenios (nombre, entidad_socio, fecha_inicio, fecha_vencimiento, estado) VALUES ('Convenio B', 'Entidad B', '2025-01-01', DATEADD('DAY', 15, CURRENT_DATE), 'VIGENTE')",
        "INSERT INTO convenios (nombre, entidad_socio, fecha_inicio, fecha_vencimiento, estado) VALUES ('Convenio Vencido', 'Entidad C', '2024-01-01', DATEADD('DAY', -3, CURRENT_DATE), 'VENCIDO')",
        "INSERT INTO convenios (nombre, entidad_socio, fecha_inicio, fecha_vencimiento, estado) VALUES ('Convenio Lejano', 'Entidad D', '2025-01-01', DATEADD('DAY', 60, CURRENT_DATE), 'VIGENTE')"
    })
    void dadoConveniosVariados_cuandoBuscarEnRango_entoncesRetornaSoloRango() {
        ConvenioRepositoryAdapter adapter = new ConvenioRepositoryAdapter(jpaRepository);
        LocalDate hoy = LocalDate.now();

        List<Convenio> resultado = adapter.findByFechaVencimientoBetween(hoy, hoy.plusDays(30));

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).fechaVencimiento()).isBefore(resultado.get(1).fechaVencimiento());
        assertThat(resultado).allMatch(c ->
                !c.fechaVencimiento().isBefore(hoy) && !c.fechaVencimiento().isAfter(hoy.plusDays(30)));
    }

    @Test
    @DisplayName("Given sin convenios en rango, When findByFechaVencimientoBetween, Then retorna lista vacía")
    @Sql(statements = {
        "INSERT INTO convenios (nombre, entidad_socio, fecha_inicio, fecha_vencimiento, estado) VALUES ('Convenio Lejano', 'Entidad Z', '2025-06-01', DATEADD('DAY', 45, CURRENT_DATE), 'VIGENTE')"
    })
    void dadoSinConveniosEnRango_cuandoBuscar_entoncesListaVacia() {
        ConvenioRepositoryAdapter adapter = new ConvenioRepositoryAdapter(jpaRepository);
        LocalDate hoy = LocalDate.now();

        List<Convenio> resultado = adapter.findByFechaVencimientoBetween(hoy, hoy.plusDays(30));

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Given convenio con fechaVencimiento = exactamente hoy+30, When findByFechaVencimientoBetween(hoy, hoy+30), Then el convenio aparece — límite superior incluido")
    @Sql(statements = {
        "INSERT INTO convenios (nombre, entidad_socio, fecha_inicio, fecha_vencimiento, estado) VALUES ('Convenio Limite', 'Entidad L', '2025-01-01', DATEADD('DAY', 30, CURRENT_DATE), 'VIGENTE')"
    })
    void dadoConvenioEnLimite30_cuandoBuscar_entoncesAparece() {
        ConvenioRepositoryAdapter adapter = new ConvenioRepositoryAdapter(jpaRepository);
        LocalDate hoy = LocalDate.now();

        List<Convenio> resultado = adapter.findByFechaVencimientoBetween(hoy, hoy.plusDays(30));

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).fechaVencimiento()).isEqualTo(hoy.plusDays(30));
    }
}
