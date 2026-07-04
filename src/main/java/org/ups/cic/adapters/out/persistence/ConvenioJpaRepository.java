package org.ups.cic.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ConvenioJpaRepository extends JpaRepository<ConvenioJpaEntity, Long> {

    @Query("SELECT c FROM ConvenioJpaEntity c " +
           "WHERE c.fechaVencimiento BETWEEN :desde AND :hasta " +
           "ORDER BY c.fechaVencimiento ASC")
    List<ConvenioJpaEntity> findProximosVencimientos(
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);
}
