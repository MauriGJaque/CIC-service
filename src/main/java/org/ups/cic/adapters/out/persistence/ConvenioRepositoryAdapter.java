package org.ups.cic.adapters.out.persistence;

import org.ups.cic.domain.convenio.Convenio;
import org.ups.cic.domain.convenio.ConvenioRepository;

import java.time.LocalDate;
import java.util.List;

public class ConvenioRepositoryAdapter implements ConvenioRepository {

    private final ConvenioJpaRepository jpaRepository;

    public ConvenioRepositoryAdapter(ConvenioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Convenio> findByFechaVencimientoBetween(LocalDate desde, LocalDate hasta) {
        return jpaRepository.findProximosVencimientos(desde, hasta)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private Convenio toDomain(ConvenioJpaEntity entity) {
        return new Convenio(
                entity.getId(),
                entity.getNombre(),
                entity.getEntidadSocio(),
                entity.getFechaInicio(),
                entity.getFechaVencimiento(),
                entity.getEstado()
        );
    }
}
