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
@Table(name = "nexco_balfiduciaria_icrv")
public class BalfiduciariaIcrv implements Serializable{

    @Id
    @Column(name = "id_bal")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idBal;

    @Column(name = "corte")
    private String corte;

    @Column(name = "codigo_contabilidad")
    private String codigoContabilidad;

    @Column(name = "nombre_fideicomiso")
    private String nombreFideicomiso;

    @Column(name = "ano")
    private String ano;

    @Column(name = "mes")
    private String mes;

    @Column(name = "codigo_puc")
    private String codigoPuc;

    @Column(name = "codigo_cuenta_niif")
    private String codigoCuentaNiif;

    @Column(name = "codigo_puc_local")
    private String codigoPucLocal;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "saldo_final")
    private Double saldoFinal;

    @Column(name = "nivel")
    private String nivel;

    @Column(name = "codicons")
    private String codicons;

    @Column(name = "codigest")
    private String codigest;

    @Column(name = "l4")
    private String l4;

    @Column(name = "periodo")
    private String periodo;

}
