package org.ups.cic.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.ups.cic.adapters.out.persistence.ConvenioJpaRepository;
import org.ups.cic.adapters.out.persistence.ConvenioRepositoryAdapter;
import org.ups.cic.application.convenio.ObtenerProximosVencimientosUseCase;

import java.time.LocalDate;
import java.util.function.Supplier;

@Configuration
public class BeanConfiguration {

    @Bean
    public Supplier<LocalDate> currentDateSupplier() {
        return LocalDate::now;
    }

    @Bean
    public ConvenioRepositoryAdapter convenioRepositoryAdapter(ConvenioJpaRepository jpaRepository) {
        return new ConvenioRepositoryAdapter(jpaRepository);
    }

    @Bean
    public ObtenerProximosVencimientosUseCase obtenerProximosVencimientosUseCase(
            ConvenioRepositoryAdapter adapter,
            Supplier<LocalDate> currentDateSupplier) {
        return new ObtenerProximosVencimientosUseCase(adapter, currentDateSupplier);
    }
}
