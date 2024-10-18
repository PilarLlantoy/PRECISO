package com.inter.proyecto_intergrupo.model.parametric;
import lombok.*;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_parametros_construccion")
public class ConstructionParameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "id_campo_conciliacion")
    private CampoRConcil campoConciliacion;

    @ManyToOne
    @JoinColumn(name = "id_ruta_contable")
    private AccountingRoute rutaContable;

    @ManyToOne
    @JoinColumn(name = "id_campo_cont_validar")
    private CampoRC campoContValidar;

    @ManyToOne
    @JoinColumn(name = "id_campo_cont_resultante")
    private CampoRC campoContResultante;

    @Column(name = "estado", columnDefinition = "BIT DEFAULT 1")
    private boolean estado = true;


    @ManyToOne
    @JoinColumn(name = "id_cuenta_me", nullable = false)
    private AccountEventMatrix account;

}
