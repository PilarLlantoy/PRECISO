package com.inter.proyecto_intergrupo.model.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_conciliacion_comer")
public class ConciliationComer implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comer")
    private Long idComer;

    @Column(name = "cuenta_banco")
    String cuentaBanco;

    @Column(name = "cuenta_comercializadora")
    String cuentaComercializadora;

    @Column(name = "nombre_cuenta_banco")
    String nombreCuentaBanco;

    @Column(name = "importe_comercializadora")
    Double importeComercializadora;

    @Column(name = "calculo_prorrata_iva")
    Double prorrataIva;

    @Column(name = "total_comercializadora")
    Double totalComer;

    @Column(name = "importe_real")
    Double importeReal;

    @Column(name = "importe_provisiones")
    Double importeProvisiones;

    @Column(name = "total_gps")
    Double totalGps;

    @Column(name = "diferencias_totales")
    Double diferenciasTot;

    @Column(name = "importe_base_fiscal")
    Double importeBaseFiscal;

    @Column(name = "diferencia_pagos_reales")
    Double diferenciaPagosReales;

    @Column(name = "importe_balance")
    Double importeBalance;

    @Column(name = "diferencia_total_gps_balance")
    Double diferenciaTotalGpsBalance;

    @Column(name = "periodo")
    String periodo;

}
