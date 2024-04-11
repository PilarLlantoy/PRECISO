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
@Table(name = "nexco_reclasificacion_contingentes_com")
public class ReclassificationContingentCom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reclasificacion_contingentes_com")
    private Long idReclasificacion;

    @Column(name = "cuenta_contable")
    private String cuentaContable;

    @Column(name="divisa")
    private String divisa;

    @Column(name = "vr_div")
    private Double vrDiv;

    @Column(name = "saldo_divisa")
    private Double saldoDivisa;

    @Column(name = "saldo_pesos")
    private Double saldoPesos;

    @Column(name = "fecha_alta")
    private Date fechaAlta;

    @Column(name = "fecha_vencimiento")
    private Date fechaVenciemiento;

    @Column(name = "fecha_cierre")
    private Date fechaCierre;

    @Column(name = "nit")
    private String nit;

    @Column(name = "nit_banco")
    private String nitBanco;

    @Column(name = "nombre_cliente")
    private String nombreCliente;

    @Column(name = "contrato")
    private String contrato;

    @Column(name = "nombre_banco")
    private String nombreBanco;

    @Column(name = "pais_banco")
    private String paisBanco;

    @Column(name="intergrupo")
    private String intergrupo;

    @Column(name = "tipo_aval")
    private String tipoAval;

    @Column(name = "cta_contable_60")
    private String cuentaContable60;

    @Column(name = "tipo_moneda")
    private String tipoMoneda;

    @Column(name = "periodo_origen")
    private String periodoOrigen;

    @Column(name = "yntp")
    private String yntp;

    @Column(name = "codicons")
    private String codicons;

    @Column(name = "nombre_aval")
    private String nombreAval;

    @Column(name = "estado")
    private String estado;

    @Column(name = "centro")
    private String centro;

    @Column(name = "td")
    private String td;

    @Column(name = "dv")
    private String dv;

    @Column(name = "nombre_pais")
    private String nombrePais;
}

