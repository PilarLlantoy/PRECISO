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
@Table(name = "nexco_descontabilizacion_report")
public class DescontabilizacionReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "fuente_informacion")
    private String fuenteInformacion;

    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "centro")
    private String centro;

    @Column(name = "divisa")
    private String divisa;

    @Column(name = "contrato")
    private String contrato;

    @Column(name = "concepto")
    private String concepto;

    @Column(name = "saldo")
    private Double saldo;

    @Column(name = "contrapartida")
    private String contrapartida;

    @Column(name = "contrato_generico")
    private String contratoGenerico;

    @Column(name = "periodo")
    private String periodo;

}
