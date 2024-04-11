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
@Table(name = "nexco_conciliacion_scope_ifrs9_centro")
public class IFRS9ConcilAccountCenter implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carga")
    private Long idCarga;

    @Column(name="empresa")
    private String empresa;

    @Column(name="cuenta")
    private String cuenta;

    @Column(name="descripcion")
    private String descripcion;

    @Column(name="centro")
    private String centro;

    @Column(name="codicons")
    private String codicons;

    @Column(name="saldo_s2")
    private Double saldoS2;

    @Column(name="saldo_per_inc")
    private Double saldoPerInc;

    @Column(name="saldo_primera_vez")
    private Double saldoPrimeraVez;

    @Column(name="saldo_dif_conc")
    private Double saldoDifConc;

    @Column(name="saldo_manuales")
    private Double saldoManuales;

    @Column(name="saldo_prov_gen_int")
    private Double saldoProvGenint;

    @Column(name="saldo_porc_calc")
    private Double saldoPorcCal;

    @Column(name="saldo_rechazos_aut")
    private Double saldoRechazosAut;

    @Column(name="diferencias")
    private Double diferencias;

    @Column(name="validacion")
    private String validacion;

    @Column(name="observacion")
    private String observacion;

    @Column(name="periodo")
    private String periodo;

}
