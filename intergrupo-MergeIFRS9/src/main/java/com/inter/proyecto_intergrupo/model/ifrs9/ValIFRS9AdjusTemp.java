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
@Table(name = "nexco_val_ifrs9_ajus_temp")
public class ValIFRS9AdjusTemp implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name="codicons")
    private String codicons;

    @Column(name="divisa")
    private String divisa;

    @Column(name="perimetro")
    private String perimetro;

    @Column(name="saldo")
    private BigDecimal saldo;

}