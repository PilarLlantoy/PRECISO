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
@Table(name = "nexco_inter_aux_ifrs9_temp")
public class ValInterIfrs9 implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name="codigo_consolidacion")
    private String codigoConsolidacion;

    @Column(name="divisa")
    private String divisa;

    @Column(name="sociedad_ic")
    private String sociedadIc;

    @Column(name="descripcion_ic")
    private String descripcionIc;

    @Column(name="saldo")
    private BigDecimal saldoquery;


}