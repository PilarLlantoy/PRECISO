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
@Table(name = "nexco_reporte_rp21")
public class Rp21 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte")
    private Long idReporte;

    @Column(name = "tipo_operacion")
    private String tipoOperacion;

    @Column(name = "clase_futuros")
    private String claseFuturos;

    @Column(name = "tipo_riesgo")
    private String tipoRiesgo;

    @Column(name = "nit")
    private String nit;

    @Column(name = "contraparte")
    private String contraparte;

    @Column(name = "f_alta")
    private Date fAlta;

    @Column(name = "f_vencimiento")
    private Date fVenciemiento;

    @Column(name = "divisa")
    private String divisa;

    @Column(name = "vr_nominal_divisa")
    private double vrNominalDivisa;

    @Column(name = "mtm_cop")
    private double mtmCOP;

    @Column(name = "vr_nominal_cop")
    private double vrNominalCOP; // sumar este columna

    @Column(name = "vr_nominal_mtm")
    private double vrNominalMtm;

    @Column(name="intergrupo")
    private String intergrupo;

    @Column(name = "pais")
    private String pais;

    @Column(name = "neocon")
    private String neocon;

    @Column(name = "local_rp21")
    private String localRp21;//cuentas se agrupan y se saca la sumatoria 1

    @Column(name = "divRp21")
    private String divrp21;//divisa agrupar 2

    @Column(name = "neocon_")
    private String neocon2;

    @Column(name = "local_derec")
    private String localDerec;

    @Column(name = "local_obligacion")
    private String localObligacion;

    @Column(name = "pyg")
    private String pyg;

    @Column(name = "tipo_contraparte")
    private String tipoContraparte;

    @Column(name = "yintp")
    private String yintp;

    @Column(name = "operacion")
    private String operacion;

    @Column(name = "dvsaconciliacion1")
    private String dvsaconciliacion1;

    @Column(name = "dvsaconciliacion2")
    private String dvsaconciliacion2;

    @Column(name = "centro_cuenta")
    private String centroCuenta;

    @Column(name = "fecont")
    private Date fecont; // tomar parametro perido con el fecont registros perteneceientes al periodo

    @Column(name = "origen")
    private String origen;

}

