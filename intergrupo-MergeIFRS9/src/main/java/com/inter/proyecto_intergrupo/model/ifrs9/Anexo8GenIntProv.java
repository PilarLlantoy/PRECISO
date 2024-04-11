package com.inter.proyecto_intergrupo.model.ifrs9;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_anexo_8_prov_gen_int")
public class Anexo8GenIntProv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prov")
    private Long idProv;

    @Column(name = "centro")
    String centro;

    @Column(name = "cuenta")
    String cuenta;

    @Column(name = "divisa")
    String divisa;

    @Column(name = "importe")
    Double importe;

    @Column(name = "fecha_origen")
    String fechaOrigen;

    @Column(name = "fecha_cierre")
    String fechaCierre;

    @Column(name = "tp")
    String tp;

    @Column(name = "identificacion")
    String identificacion;

    @Column(name = "dv")
    String dv;

    @Column(name = "razon_social")
    String razonSocial;

    @Column(name = "contrato")
    String contrato;

    @Column(name = "observacion")
    String observacion;

    @Column(name = "cuenta_provision")
    String cuentaProvision;

    @Column(name = "valor_provision")
    String valorProvision;

    @Column(name = "importe_moneda_original")
    String importeMonedaOriginal;

    @Column(name = "prob_recup")
    String prob_recup;
}
