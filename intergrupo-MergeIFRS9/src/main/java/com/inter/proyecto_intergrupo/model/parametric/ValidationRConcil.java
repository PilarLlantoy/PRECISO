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
@Table(name = "preciso_validaciones_rconcil")
public class ValidationRConcil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_validacion")
    private int id;

    @Column(name = "valor_validacion")
    private String valorValidacion;

    @Column(name = "operacion")
    private String operacion;

    @Column(name = "valor_operacion")
    private String valorOperacion;

    @Column(name = "formula", columnDefinition = "BIT DEFAULT 0")
    private boolean formula = false;

    @Column(name = "estado", columnDefinition = "BIT DEFAULT 1")
    private boolean estado = true;

    // Relación con ConciliationRoute
    @ManyToOne
    @JoinColumn(name = "id_rc", nullable = false)
    private ConciliationRoute rutaConciliacion;

    @ManyToOne
    @JoinColumn(name = "id_campo_referencia", nullable = false)
    private CampoRConcil campoRef;

    @ManyToOne
    @JoinColumn(name = "id_campo_validacion")
    private CampoRConcil campoVal;

}
