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
@Table(name = "nexco_plano_ifrs9_intergrupo")
public class PlainIFRS9Intergroup implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name="empresa")
    private String empresa;

    @Column(name="fecha_contable")
    private String fechaContable;

    @Column(name="fecha")
    private String fecha;

    @Column(name="divisa")
    private String divisa;

    @Column(name="cuenta")
    private String cuenta;

    @Column(name="importe_c")
    private Double importeC;

    @Column(name="importe_d")
    private Double importeD;

    @Column(name="importe_dd")
    private Double importeDD;

    @Column(name="importe_cd")
    private Double importeCD;

    @Column(name="observacion")
    private String observacion;

    @Column(name="contrato")
    private String contrato;

    @Column(name="origen")
    private String origen;

    @Column(name="periodo")
    private String periodo;

    @Column(name="importe_dd_exp")
    private Double importeDDExp;

    @Column(name="importe_cd_exp")
    private Double importeCDExp;

    @Column(name="importe_total")
    private Double importeTotal;

}