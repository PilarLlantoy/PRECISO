package com.inter.proyecto_intergrupo.model.ifrs9;

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
@Table(name = "nexco_riesgos_mercados")
public class RiskRys implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_riesgos_mercados")
    private Long idRiesgosMercados;

    @Column(name = "numero_papeleta")
    private String numeroPapeleta;

    @Column(name = "cod_nombre")
    private String codNombre;

    @Column(name = "cod_puc")
    private String codPuc;

    @Column(name = "fecha")
    private Date fecha;

    @Column(name = "fecha_final")
    private Date fechaFinal;

    @Column(name = "valor_total")
    private Double valorTotal;

    @Column(name = "intereses")
    private Double intereses;

    @Column(name="causacion_hoy")
    private Double causacionHoy;

    @Column(name = "moneda")
    private String moneda;

    @Column(name = "fecha_corte")
    private Date fechaCorte;

    @Column(name = "exposicion")
    private String exposicion;

    @Column(name = "dias_al_vto")
    private String diasAlVto;

    @Column(name="tasa_descuento")
    private Double tasaDescuento;

    @Column(name = "fd")
    private String fd;

    @Column(name = "valor_presente")
    private Double valorPresente;

    @Column(name = "diferencia")
    private Double diferencia;

    @Column(name = "periodo")
    private String periodo;
}
