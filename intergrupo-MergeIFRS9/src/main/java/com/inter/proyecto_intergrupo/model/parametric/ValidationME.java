package com.inter.proyecto_intergrupo.model.parametric;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_validaciones_matriz_evento")
public class ValidationME {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_validacion_me")
    private int id;

    @ManyToOne
    @JoinColumn(name = "id_campo_validacion", nullable = false)
    private CampoRConcil campoVal;

    @Column(name = "valor_validacion")
    private String valorValidacion;

    @Column(name = "aplica_formula", columnDefinition = "BIT DEFAULT 0")
    private boolean aplicaFormula = false;

    @ManyToOne
    @JoinColumn(name = "id_campo_referencia")
    private CampoRConcil campoRef;

    @Column(name = "operacion")
    private String operacion;

    @Column(name = "valor_operacion")
    private String valorOperacion;

    @Column(name = "adiciona_campo_afecta", columnDefinition = "BIT DEFAULT 1")
    private boolean adCampoAfecta = true;

    @ManyToOne
    @JoinColumn(name = "id_campo_afecta")
    private CampoRConcil campoAfecta;


    @Column(name = "estado", columnDefinition = "BIT DEFAULT 1")
    private boolean estado = true;

    // Relación con AccountingRoute
    @ManyToOne
    @JoinColumn(name = "id_me", nullable = false)
    private EventMatrix matrizEvento;

}
