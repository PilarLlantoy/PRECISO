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
@Table(name = "preciso_arbol_decision_segmento")
public class SegmentDecisionTree {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "codigo_ifrs9")
    private String codigoIFRS9;

    @Column(name = "descripcion_sectorizacion")
    private String descripcionSectorizacion;

    @Column(name = "corasu")
    private String corasu;

    @Column(name = "corasu_op")
    private String corasuOp;

    @Column(name = "sub_corasu")
    private String subCorasu;

    @Column(name = "sub_corasu_op")
    private String subCorasuOp;

    @Column(name = "ciiu")
    private String ciiu;

    @Column(name = "ciiuOp")
    private String ciiuOp;

    @Column(name = "numero_empleados")
    private String numeroEmpleados;

    @Column(name = "numero_empleados_op")
    private String numeroEmpleadosOp;

    @Column(name = "total_activos")
    private String totalActivos;

    @Column(name = "total_activos_op")
    private String totalActivosOp;

    @Column(name = "total_ventas")
    private String totalVentas;

    @Column(name = "total_ventas_op")
    private String totalVentasOp;

    @Column(name = "verificacion_contratos")
    private String verificacionContratos;

    @Column(name = "otros_criterios")
    private String otrosCriterios;

}
