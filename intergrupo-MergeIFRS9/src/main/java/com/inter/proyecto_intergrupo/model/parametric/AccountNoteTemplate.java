package com.inter.proyecto_intergrupo.model.parametric;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_cuentas_plantilla_nota")
public class AccountNoteTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cuenta_pn") // Cambiado de "id-cuenta_pn"
    private int id;

    @Column(name = "aplica_a")
    private String aplicaA;

    @Column(name = "divisa")
    private String divisa;

    @Column(name = "cuenta_ganancia")
    private String cuentaGanancia;

    @Column(name = "cuenta_perdida")
    private String cuentaPerdida;

    @Column(name = "maneja_formula", columnDefinition = "BIT DEFAULT 0")
    private boolean manejaFormula = false;

    @Column(name = "operacion")
    private String operacion;

    @Column(name = "valor_operacion")
    private String valorOp;

    @ManyToOne
    @JoinColumn(name = "id_campo_inv1", referencedColumnName = "id_campo")
    private CampoRC campoInv1;

    @Column(name = "operacion_inv")
    private String operacionInv;

    @ManyToOne
    @JoinColumn(name = "id_campo_inv2", referencedColumnName = "id_campo")
    private CampoRC campoInv2;

    @ManyToOne
    @JoinColumn(name = "id_campo_centro")
    private CampoRC campoCentro;

    @Column(name = "numero_refencia")
    private String referencia;

    @ManyToOne
    @JoinColumn(name = "id_plantilla_nota", nullable = false)
    private NoteTemplate plantillaNota;

    @Column(name = "estado", columnDefinition = "BIT DEFAULT 1")
    private boolean estado = true;
}
