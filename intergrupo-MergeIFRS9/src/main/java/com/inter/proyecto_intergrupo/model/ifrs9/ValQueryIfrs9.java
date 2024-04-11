package com.inter.proyecto_intergrupo.model.ifrs9;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_query_aux_ifrs9_temp")
public class ValQueryIfrs9 implements Serializable {
    @Id
    @Column(name="codigo_consolidacion")
    private String codigoConsolidacion;

    @Id
    @Column(name="divisa")
    private String divisa;

    @Column(name="saldo")
    private BigDecimal saldoquery;

    @Column(name="fecha_proceso")
    private String fechaProceso;

}