package com.inter.proyecto_intergrupo.model.briefcase;

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
@Table(name = "nexco_calculo_icrv")
public class CalculoIcrv implements Serializable{

    @Id
    @Column(name = "id_calculo")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCalculo;

    @Column(name = "valoracion")
    private String valoracion;

    @Column(name = "empresa")
    private String empresa;

    @Column(name = "nit")
    private String nit;

    @Column(name = "dv")
    private String dv;

    @Column(name = "isin")
    private String isin;

    @Column(name = "participacion")
    private Double participacion;

    @Column(name = "vr_accion")
    private Double vrAccion;

    @Column(name = "no_acciones")
    private Double noAcciones;

    @Column(name = "valor_nominal")
    private Double valorNominal;

    @Column(name = "precio")
    private Double precio;

    @Column(name = "vr_patrimonio")
    private Double vrPatrimonio;

    @Column(name = "vr_mercado")
    private Double vrMercado;

    @Column(name = "saldo_libros_valoracion")
    private Double saldoLibrosValoracion;

    @Column(name = "ajuste")
    private Double ajuste;

    @Column(name = "dividendos_pagados_no_acciones")
    private Double dividendosPagadosNoAcciones;

    @Column(name = "dividendos_pagados_acciones")
    private Double dividendosPagadosAcciones;

    @Column(name = "capital")
    private Double capital;

    @Column(name = "periodo")
    private String periodo;

}
