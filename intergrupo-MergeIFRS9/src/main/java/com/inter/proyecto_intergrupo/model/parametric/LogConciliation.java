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
@Table(name = "preciso_log_conciliacion")
public class LogConciliation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lc")
    private Long id;

    @Column(name = "fechaInventario")
    private Date fechaInventario;

    @Column(name = "fechaProceso")
    private Date fechaProceso;

    @Column(name = "novedad")
    private String novedad;

    @Column(name = "fechaPreciso")
    private Date fechaPreciso;

    @Column(name = "usuario")
    private String usuario;

    @Column(name = "estadoProceso")
    private String estadoProceso;

    @ManyToOne
    @JoinColumn(name = "idConciliacion", nullable = false)
    private Conciliation idConciliacion;

}