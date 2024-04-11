package com.inter.proyecto_intergrupo.model.Ifrs9Parametrics;

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
@Table(name = "nexco_rechazos_descontabilizacion")
public class RechazosDescontabilizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "centro")
    private String centro;

    @Column(name = "divisa")
    private String divisa;

    @Column(name = "contrato")
    private String contrato;

    @Column(name = "saldo")
    private Double saldo;

    @Column(name = "periodo")
    private String periodo;

    @Column(name = "descripcion")
    private String descripcion;

}