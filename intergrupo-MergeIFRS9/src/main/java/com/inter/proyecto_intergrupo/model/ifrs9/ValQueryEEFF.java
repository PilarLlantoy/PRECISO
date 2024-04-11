package com.inter.proyecto_intergrupo.model.ifrs9;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_query_eeff")
public class ValQueryEEFF implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name="fecha_proceso_local")
    private String fechaProcesoLocal;

    @Column(name="fecha_proceso_ifrs9")
    private String fechaProcesoIfrs9;

    @Column(name="codigo_consolidacion")
    private String codigoConsolidacion;

    @Column(name="descripcion")
    private String descripcion;

    @Column(name="tipo_cuenta")
    private String tipoCuenta;

    @Column(name="perimetro")
    private String perimetro;

    @Column(name="intergrupo")
    private String intergrupo;

    @Column(name="sociedad_ic")
    private String sociedadIc;

    @Column(name="descripcion_ic")
    private String descripcionIc;

    @Column(name="divisa")
    private String divisa;

    @Column(name="saldo_eeff_local")
    private BigDecimal saldoEeffLocal;

    @Column(name="saldo_eeff_ifrs9")
    private BigDecimal saldoEeffIfrs9;

    @Column(name="saldo_query_local")
    private BigDecimal saldoQueryLocal;

    @Column(name="saldo_query_ifrs9")
    private BigDecimal saldoQueryIfrs9;

    @Column(name="saldo_inter_local")
    private BigDecimal saldoInterLocal;

    @Column(name="saldo_inter_ifrs9")
    private BigDecimal saldoInterIfrs9;

    @Column(name="saldo_dif_local")
    private BigDecimal saldoDifLocal;

    @Column(name="saldo_dif_ifrs9")
    private BigDecimal saldoDifIfrs9;

    @Column(name="saldo_dif_eeff_inter_local")
    private BigDecimal saldoDifEeffLocal;

    @Column(name="saldo_dif_eeff_inter_ifrs9")
    private BigDecimal saldoDifEeffIfrs9;

    @Column(name="saldo_ajuste_hom")
    private BigDecimal saldoAjusteHom;

    @Column(name="saldo_ajuste_man")
    private BigDecimal saldoAjusteMan;

    @Column(name="periodo")
    private String periodo;

}