package com.inter.proyecto_intergrupo.model.parametric;

import lombok.*;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_log_cargues_contables")
public class LogAccountingLoad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lcc")
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
    @JoinColumn(name = "idRc", nullable = false)
    private AccountingRoute idRc;

}
