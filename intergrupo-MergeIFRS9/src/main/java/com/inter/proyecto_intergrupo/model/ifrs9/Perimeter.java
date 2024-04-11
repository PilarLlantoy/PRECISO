package com.inter.proyecto_intergrupo.model.ifrs9;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.math.BigDecimal;

import static javax.persistence.GenerationType.SEQUENCE;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_perimetereeff")

public class Perimeter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "")
    private Long idCarga;

    @Column(name = "segmento")
    private String segmento;

    @Column(name = "subproducto")
    private String subproducto;

    @Column(name = "codigo_consolidacion")
    private String codConso;

    @Column(name = "divisa")
    private String divisa;

    @Column(name = "saldo_query")
    private BigDecimal saldoQuery;

    @Column(name = "saldo_eeff")
    private BigDecimal saldoEEFF;

    @Column(name = "diferencia_saldos")
    private BigDecimal difSaldos;

    @Column(name = "fecha_proceso")
    private String fechaProceso;

    @Column(name = "periodo")
    private String periodo;
}
