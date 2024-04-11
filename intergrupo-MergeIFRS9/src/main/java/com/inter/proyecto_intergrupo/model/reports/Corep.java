package com.inter.proyecto_intergrupo.model.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_corep_obligatorios")
public class Corep{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_corep")
    private Long idCorep;

    @Column(name = "CUENTA")
    private String cuenta;

    @Column(name = "CTA_CONTA_MONEDA")
    private String ctaContaMoneda;

    @Column(name = "DIVISA")
    private String divisa;

    @Column(name = "SALDODIVISA")
    private Double saldoDivisa;

    @Column(name = "SALDOPESOS")
    private Double saldoPesos;

    @Column(name = "FEC_ALTA")
    private Date fechaAlta;

    @Column(name = "FEC_VENCI")
    private Date fechaVencimiento;

    @Column(name = "FEC_CIERRE")
    private Date fechaCierre;

    @Column(name = "NIT")
    private String nit;

    @Column(name = "CONTRATO")
    private String contrato;

    @Column(name = "NOMBRE_CLIENTE")
    private String nombreCliente;

    @Column(name = "NOMBRE_BANCO")
    private String nombreBanco;

    @Column(name = "COD_PAIS")
    private String codPais;

    @Column(name = "PAIS_BANCO")
    private String paisBanco;

    @Column(name = "INTERGRUPO")
    private String intergrupo;

    @Column(name = "TCLIENTE")
    private String tCliente;

    @Column(name = "Vencimiento_Rel")
    private String vencimientoRel;

    @Column(name = "Vencimiento_Residul")
    private String vencimientoResidul;

    @Column(name = "Vto_Rel")
    private String vtoRel;

    @Column(name = "Vto_Residul")
    private String vto_Residul;

    @Column(name = "CCF")
    private String ccf;

    @Column(name = "RW")
    private String rw;

    @Column(name = "CODCONSOL")
    private String codconsol;

    @Column(name = "Tipo_Avl")
    private String tipoAvl;

    @Column(name = "NUEVO_CODIGO")
    private String nuevoCodigo;

    @Column(name = "Provision")
    private Double provision;

    @Column(name = "CUENTAPROVISION")
    private String cuentaProvision;

    @Column(name = "ISO")
    private String iso;

    @Column(name = "ISO_GARANTIA")
    private String isoGarantia;

    @Column(name = "SEGMENTO_FINREP")
    private String segmentoFinrep;

}
