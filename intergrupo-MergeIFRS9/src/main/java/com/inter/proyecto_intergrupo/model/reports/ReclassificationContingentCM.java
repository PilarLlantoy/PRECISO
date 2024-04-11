package com.inter.proyecto_intergrupo.model.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_carga_masiva_contingentes")
public class ReclassificationContingentCM {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carga_masiva")
    private Long idCargaMasiva;

    @Column(name = "centro_costos")
    private String centroCostos;

    @Column(name="cuenta")
    private String cuenta;

    @Column(name = "divisa")
    private String divisa;

    @Column(name = "contrato")
    private String contrato;

    @Column(name = "referencia_cruce")
    private String referenciaCruce;

    @Column(name = "importe")
    private Double importe;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "fecha")
    private Date fecha;

    @Column(name = "TD")
    private String td;

    @Column(name = "numero_documento")
    private String numeroDocumento;

    @Column(name = "DV")
    private String dv;

    @Column(name = "tipo_perdida")
    private String tipoPerdida;

    @Column(name="clase_riesgo")
    private String claseRiesgo;

    @Column(name = "tipo_movimiento")
    private String tipoMovimiento;

    @Column(name = "producto")
    private String producto;

    @Column(name = "proceso")
    private String proceso;

    @Column(name = "linea_operativa")
    private String lineaOperativa;

    @Column(name = "periodo_origen")
    private String periodoOrigen;

    @Column(name = "tipo_info")
    private String tipoInfo;
}

