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
@Table(name = "nexco_descon_nv15")
public class Desconnv15 implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carga")
    private Long idCarga;

    @Column(name="cod_neocon")
    private String codNeocon;

    @Column(name = "val_neocon")
    private String valNeocon;

    @Column(name="cuenta")
    private String cuenta;

    @Column(name="divisa")
    private String divisa;

    @Column(name="sald")
    private Double sald;

    @Column(name="salc")
    private Double salc;

    @Column(name="sald_div")
    private Double saldDiv;

    @Column(name="salc_div")
    private Double salcDiv;

    @Column(name="diferencia")
    private Double diferencia;

    @Column(name="observaciones")
    private String observaciones;

    @Column(name="tp")
    private String tp;

    @Column(name="tp1")
    private String tp1;

    @Column(name="fuente_informacion")
    private String fuenteInformacion;

    @Column(name="periodo")
    private String periodo;
}
