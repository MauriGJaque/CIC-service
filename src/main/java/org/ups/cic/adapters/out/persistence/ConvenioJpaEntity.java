package org.ups.cic.adapters.out.persistence;

import jakarta.persistence.*;
import lombok.*;
import org.ups.cic.domain.convenio.EstadoConvenio;

import java.time.LocalDate;

@Entity
@Table(name = "convenios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConvenioJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "entidad_socio", nullable = false)
    private String entidadSocio;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoConvenio estado;
}
