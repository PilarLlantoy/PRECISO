package com.inter.proyecto_intergrupo.model.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_inventario_descontabilizacion")
public class DescontabilizacionTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "centro")
    private String centro;

    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "divisa")
    private String divisa;

    @Column(name = "contrato")
    private String contrato;

    @Column(name = "concepto")
    private String concepto;

    @Column(name = "saldo")
    private BigDecimal saldo;

    @Column(name = "periodo")
    private String periodo;
}
