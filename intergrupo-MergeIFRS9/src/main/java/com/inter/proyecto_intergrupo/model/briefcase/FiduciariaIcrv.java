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
@Table(name = "nexco_fiduciaria_icrv")
public class FiduciariaIcrv implements Serializable{

    @Id
    @Column(name = "id_Fiduciaria")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFiduciaria;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "capital_autorizado")
    private Double capitalAutorizado;

    @Column(name = "capital_por_suscribir")
    private Double capitalPorSuscribir;

    @Column(name = "reserva_legal")
    private Double reservaLegal;

    @Column(name = "apropiacion_de_utilidades")
    private Double apropiacionDeUtilidades;

    @Column(name = "readquisicion_de_acciones")
    private Double readquisicionDeAcciones;

    @Column(name = "acciones_propias_readquiridas")
    private Double accionesPropiasReadquiridas;

    @Column(name = "instrumentos_financieros_valor_razonable")
    private Double instrumentosFinancierosValorRazonable;

    @Column(name = "instrumentos_financieros_valor_razonable_cambios_ori")
    private Double instrumentosFinancierosValorRazonableCambiosOri;

    @Column(name = "titulos_de_tesoreria")
    private Double titulosDeTesoreria;

    @Column(name = "impto_diferido_valor_inv_disponible_vta")
    private Double imptoDiferidoValorInvDisponibleVta;

    @Column(name = "resultado_ejercicios_anteriores")
    private Double resultadoEjerciciosAnteriores;

    @Column(name = "resultado_ejercicios_anteriores2")
    private Double resultadoEjerciciosAnteriores2;

    @Column(name = "resultados_del_ejercicio")
    private Double resultadosDelEjercicio;

    @Column(name = "patrimonio_total")
    private Double patrimonioTotal;

    @Column(name = "ori")
    private Double ori;

    @Column(name = "patrimonio_sin_ori")
    private Double patrimonioSinOri;

    @Column(name = "k_reservas")
    private Double kReservas;

    @Column(name = "otras")
    private Double otras;

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

    @Column(name = "nominal_001")
    private Double nominal001;

    @Column(name = "nominal_771")
    private Double nominal771;

    @Column(name = "nominal_1315")
    private Double nominal1315;

    @Column(name = "ori_t")
    private Double oriT;

    @Column(name = "pyg_t")
    private Double pygT;

    @Column(name = "periodo")
    private String periodo;

}
