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
@Table(name = "nexco_diferencias_automaticas")
public class DiferenciasConciliacionAutomaticas implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="cuenta")
    private String cuenta;

    @Column(name="centro")
    private String centro;

    @Column(name="contrato")
    private String contrato;

    @Column(name="valor_140")
    private BigDecimal valor140;

    @Column(name="valor_condeta")
    private BigDecimal valorCondeta;

    @Column(name="diferencia")
    private BigDecimal diferencia;

    @Column(name="periodo")
    private String periodo;
}