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
@Table(name = "preciso_campos_resultantes_parametros_reportes")
public class ResultingFieldParametroReportes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_campo_resultante")
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
    @JoinColumn(name = "campo_fuente")
    private CampoRConcil campoFuente;

    @Column(name = "seleccion", columnDefinition = "BIT DEFAULT 0")
    private boolean seleccion = false;


}
