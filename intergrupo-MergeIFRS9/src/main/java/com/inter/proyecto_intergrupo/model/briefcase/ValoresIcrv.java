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
@Table(name = "nexco_valores_icrv")
public class ValoresIcrv implements Serializable{

    @Id
    @Column(name = "id_valores")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idValores;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "capital_autorizado")
    private Double capitalAutorizado;

    @Column(name = "capital_por_suscribir")
    private Double capitalPorSuscribir;

    @Column(name = "reserva_legal")
    private Double reservaLegal;

    @Column(name = "reservas_ocasionales")
    private Double reservasOcasionales;

    @Column(name = "acciones_bvc_voluntarias")
    private Double accionesBvcVoluntarias;

    @Column(name = "pa_fab_asobolsa")
    private Double paFabAsobolsa;

    @Column(name = "acciones_bvc_obligatorias")
    private Double accionesBvcObligatorias;

    @Column(name = "impuesto_diferido")
    private Double impuestoDiferido;

    @Column(name = "provision_de_cartera")
    private Double provisionDeCartera;

    @Column(name = "revalorizacion_del_patrimonio")
    private Double revalorizacionDelPatrimonio;

    @Column(name = "utilidades_acumuladas_ea")
    private Double utilidadesAcumuladasEa;

    @Column(name = "perdidas_acumuladas_ea")
    private Double perdidasAcumuladasEa;

    @Column(name = "utilidad_del_ejercicio")
    private Double utilidadDelEjercicio;

    @Column(name = "perdida_del_ejercicio")
    private Double perdidaDelEjercicio;

    @Column(name = "patrimonio_total")
    private Double patrimonioTotal;

    @Column(name = "ori")
    private Double ori;

    @Column(name = "patrimonio_sin_ori")
    private Double patrimonioSinOri;

    @Column(name = "perdidas_acumuladas_ea_t")
    private Double perdidasAcumuladasEaT;

    @Column(name = "krl")
    private Double krl;

    @Column(name = "variacion_otras")
    private Double variacionOtras;

    @Column(name = "inversion")
    private Double inversion;

    @Column(name = "ori_calculado")
    private Double oriCalculado;

    @Column(name = "variacion_ori_calculdo")
    private Double variacionOriCalculdo;

    @Column(name = "pyg_calculado")
    private Double pygCalculado;

    @Column(name = "saldo_ori_contable")
    private Double saldoOriContable;

    @Column(name = "variacion_ori_registrado")
    private Double variacionOriRegistrado;

    @Column(name = "pyg")
    private Double pyg;

    @Column(name = "dif_pyg_ok")
    private Double difPygOk;

    @Column(name = "dif_ori_ok")
    private Double difOriOk;

    @Column(name = "borrar")
    private Double borrar;

    @Column(name = "nominal_002")
    private Double nominal002;

    @Column(name = "nominal_772")
    private Double nominal772;

    @Column(name = "variacion_inv")
    private Double variacionInv;

    @Column(name = "nominal_var_inv")
    private Double nominalVarInv;

    @Column(name = "patrimonio_total_porcentaje")
    private Double patrimonioTotalPorcentaje;

    @Column(name = "diferencia")
    private Double diferencia;

    @Column(name = "nominal_1315")
    private Double nominal1315;

    @Column(name = "ori_t")
    private Double oriT;

    @Column(name = "pyg_t")
    private Double pygT;

    @Column(name = "periodo")
    private String periodo;

}
