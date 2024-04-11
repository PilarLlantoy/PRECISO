package com.inter.proyecto_intergrupo.model.ifrs9;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_riesgo_mercado")
public class MarketRisk {

    @Id
    @Column(name = "numero_papeleta")
    private String numeroPapeleta;

    @Column(name = "cod_nombre")
    private String codNombre;

    @Column(name = "cod_puc")
    private String codPuc;

    @Column(name = "fecha")
    private String fecha;

    @Column(name = "fecha_final")
    private String fechaFinal;

    @Column(name = "valor_total")
    private Long valorTotal;

    @Column(name = "intereses")
    private String intereses;

    @Column(name = "causacion_hoy")
    private Long causacionHoy;

    @Column(name = "moneda")
    private String moneda;

    @Column(name = "fecha_corte")
    private String fechaCorte;

    @Column(name = "exposicion")
    private Long exposicion;

    @Column(name = "dias_vto")
    private String diasVto;

    @Column(name = "tasa_descuento")
    private String tasaDescuento;

    @Column(name = "fd")
    private Double fd;

    @Column(name = "valor_presente")
    private Long valorPresente;

    @Column(name = "diferencia")
    private Long diferencia;
}
