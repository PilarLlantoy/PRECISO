package com.inter.proyecto_intergrupo.model.ifrs9;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_plano_ifrs9_neocon")
public class PlainIFRS9 implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name="sociedad")
    private String sociedad;

    @Column(name="tipocons")
    private String tipocons;

    @Column(name="tipo_asiento")
    private String tipoAsiento;

    @Column(name="descripcion")
    private String descripcion;

    @Column(name="movimiento")
    private String movimiento;

    @Column(name="referencia")
    private String referencia;

    @Column(name="modo_ejecucion")
    private String modoEjecucion;

    @Column(name="usuario")
    private String usuario;

    @Column(name="codicons")
    private String codicons;

    @Column(name="naturaleza_total")
    private String naturalezaTotal;

    @Column(name="saldo")
    private BigDecimal saldo;

    @Column(name="divisa")
    private String divisa;

    @Column(name="pais_negocio")
    private String paisNegocio;

    @Column(name="cod_desglose")
    private String codDesglose;

    @Column(name="intergrupo")
    private String intergrupo;

    @Column(name="periodo")
    private String periodo;

    @Column(name="tipo_registro")
    private String tipoRegistro;
}