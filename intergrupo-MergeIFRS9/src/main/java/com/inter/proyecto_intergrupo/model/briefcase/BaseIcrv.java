package com.inter.proyecto_intergrupo.model.briefcase;

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
@Table(name = "nexco_base_icrv")
public class BaseIcrv implements Serializable{

    @Id
    @Column(name = "id_base")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idBase;

    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "empresa")
    private String empresa;

    @Column(name = "fecha_adquisicion")
    private String fechaAdquisicion;

    @Column(name = "naturaleza")
    private String naturaleza;

    @Column(name = "evento")
    private String evento;

    @Column(name = "concepto")
    private String concepto;

    @Column(name = "no_asignado")
    private String noAsignado;

    @Column(name = "codicons")
    private String codicons;

    @Column(name = "epigrafe")
    private String epigrafe;

    @Column(name = "descripcion_plano")
    private String descripcionPlano;

    @Column(name = "cta")
    private String cta;

    @Column(name = "descripcion_cta")
    private String descripcionCta;

}
