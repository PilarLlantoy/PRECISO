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
@Table(name = "preciso_estructuras_parametros_reportes")
public class StructureParametroReportes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estructura")
    private int id;

    @Column(name = "estado", columnDefinition = "BIT DEFAULT 1")
    private boolean estado = true;

    // Relación con ParametrosReportes
    @ManyToOne
    @JoinColumn(name = "id_parametro", nullable = false)
    private ParametrosReportes parametroReportes;

    // Relación con SourceParametroReportes
    @ManyToOne
    @JoinColumn(name = "id_fuente", nullable = false)
    private SourceParametroReportes fuente;

    // Relación con CampoParamReportes (suponiendo que CampoParamReportes sea una entidad)
    @ManyToOne
    @JoinColumn(name = "campo_reporte_id")
    private CampoParamReportes campoReporte;

    // Relación con CampoRConcil (suponiendo que CampoRConcil sea una entidad)
    @ManyToOne
    @JoinColumn(name = "campo_1_id")
    private CampoRConcil campo1;

    @ManyToOne
    @JoinColumn(name = "campo_1rc_id")
    private CampoRC campo1rc;

    @Column(name = "operacion")
    private String operacion;

    @ManyToOne
    @JoinColumn(name = "campo_2_id")
    private CampoRConcil campo2;

    @ManyToOne
    @JoinColumn(name = "campo_2rc_id")
    private CampoRC campo2rc;

    @Column(name = "aplica_formula", columnDefinition = "BIT DEFAULT 0")
    private boolean aplicaFormula = false;

    @Column(name = "valor_formula")
    private String valorFormula;

}
