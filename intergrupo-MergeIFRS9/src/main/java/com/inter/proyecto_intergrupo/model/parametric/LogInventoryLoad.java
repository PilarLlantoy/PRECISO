package com.inter.proyecto_intergrupo.model.parametric;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_log_cargues_inventarios")
public class LogInventoryLoad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lci")
    private Long id;

    @Column(name = "fechaCargue")
    private Date fechaCargue;

    @Column(name = "cantidadRegistros")
    private Long cantidadRegistros;

    @Column(name = "novedad")
    private String novedad;

    @Column(name = "fechaPreciso")
    private Date fechaPreciso;

    @Column(name = "usuario")
    private String usuario;

    @Column(name = "tipoProceso")
    private String tipoProceso;

    @Column(name = "estadoProceso")
    private String estadoProceso;

    @ManyToOne
    @JoinColumn(name = "idCR", nullable = false)
    private ConciliationRoute idCR;

}
