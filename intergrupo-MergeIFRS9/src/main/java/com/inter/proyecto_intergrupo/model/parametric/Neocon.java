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
@Table(name = "nexco_cuentas_neocon")
public class Neocon {

    @Id
    @Column(name = "cuenta")
    private Long cuenta;

    @Column(name = "plan_cuentas")
    private String planDeCuentas;

    @Column(name = "codigo_jerarquico")
    private String codigoJerarquico;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "entrada")
    private String entrada;

    @Column(name = "minimo")
    private String minimo;

    @Column(name="naturaleza")
    private int naturaleza;

    @Column(name = "intergupo")
    private String intergrupo;

    @Column(name = "grscing")
    private String grScIng;

    @Column(name = "epigraf")
    private String epigraf;

    @Column(name = "residencia")
    private String residencia;

    @Column(name = "bancaria")
    private String bancaria;

    @Column(name = "form")
    private String form;

    @Column(name = "tdes")
    private String tdes;

    @Column(name = "soporte_derivada")
    private String soporteDerivada;

    @Column(name = "unid")
    private String unid;

    @Column(name = "tipo_cambio")
    private String tipoCambio;

    @Column(name = "agregacion")
    private String agregacion;

    @Column(name = "tipo_divisa")
    private String tipoDivisa;

    @Column(name = "tipo_pais")
    private String tipoPais;

    @Column(name = "contrap")
    private String contrap;

    @Column(name = "timp")
    private String timp;

    @Column(name = "conciliacion")
    private String conciliacion;
}
