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
@Table(name = "preciso_filtros_parametros_reportes")
public class FilterParametroReportes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_filtro")
    private int id;

    @Column(name = "valorCondicion")
    private String valorCondicion;

    @Column(name = "estado", columnDefinition = "BIT DEFAULT 1")
    private boolean estado = true;

    // Relación con AccountingRoute
    @ManyToOne
    @JoinColumn(name = "id_parametro", nullable = false)
    private ParametrosReportes parametroReportes;

    @ManyToOne
    @JoinColumn(name = "id_campo", nullable = false)
    private CampoParamReportes campo;

}
