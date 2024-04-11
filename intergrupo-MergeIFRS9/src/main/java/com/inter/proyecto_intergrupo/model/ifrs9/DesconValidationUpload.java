package com.inter.proyecto_intergrupo.model.ifrs9;

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
@Table(name = "nexco_validacion_descon_carga")
public class DesconValidationUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "cod_neocon")
    private String codNeocon;

    @Column(name = "div")
    private String div;

    @Column(name = "sald")
    private Double sald;

    @Column(name = "salc")
    private Double salc;

    @Column(name = "sald_div")
    private Double saldDiv;

    @Column(name = "salc_div")
    private Double salcDiv;

    @Column(name = "observacion")
    private String observacion;

    @Column(name = "vr_ml")
    private Double vrMl;

    @Column(name = "vr_me")
    private Double vrMe;

    @Column(name = "vr_total")
    private Double vrTotal;

    @Column(name = "vr_miles")
    private Double vrMiles;

    @Column(name = "periodo")
    private String periodo;

    @Column(name = "tasa_conversion")
    private Double tasaConversion;

    @Column(name = "nombre_real")
    private String nombreReal;

}
