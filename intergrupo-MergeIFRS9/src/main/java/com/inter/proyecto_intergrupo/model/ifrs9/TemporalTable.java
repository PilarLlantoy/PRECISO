package com.inter.proyecto_intergrupo.model.ifrs9;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

import static javax.persistence.GenerationType.SEQUENCE;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_temporal")
public class TemporalTable implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="cuenta")
    private String cuenta;

    @Column(name="divisa")
    private String divisa;

    @Column(name="centro")
    private String centro;

    @Column(name="contrato")
    private String contrato;

    @Column(name="valor_aplicativo")
    private BigDecimal valor_aplicativo;

    @Column(name="valor_contable")
    private BigDecimal valor_contable;

    @Column(name="valor_diferencial")
    private BigDecimal valor_diferencial;

    @Column(name="valor_variacion")
    private BigDecimal valor_variacion;
}
