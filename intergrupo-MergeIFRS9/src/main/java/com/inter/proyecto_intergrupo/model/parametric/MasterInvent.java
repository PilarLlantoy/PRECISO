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
@Table(name = "preciso_maestro_inventarios")
public class MasterInvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "codigo_conciliacion")
    private Conciliation codigoConciliacion;

    @Column(name = "fecha_conciliacion")
    private Date fechaConciliacion;

    @ManyToOne
    @JoinColumn(name = "codigo_cargue_contable")
    private AccountingRoute codigoCargueContable;

    @Column(name = "fecha_cargue_contable")
    private Date fechaCargueContable;

    @Column(name = "estado_cargue_conciliacion", columnDefinition = "BIT DEFAULT 0")
    private boolean estadoCargueConciliacion = false;

    @Column(name = "estado_cargue_cargue_contable", columnDefinition = "BIT DEFAULT 0")
    private boolean estadoCargueCargueContable = false;

    @Column(name = "aplica_semana", columnDefinition = "BIT DEFAULT 0")
    private boolean aplica_semana = false;
}
