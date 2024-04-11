package com.inter.proyecto_intergrupo.model.ifrs9;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_h140")
public class H140 implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_h140")
    private Long idCarga;

    @Column(name = "empresa")
    private String empresa;

    @Column(name = "fecha")
    private String fecha;

    @Column(name = "centro")
    private String centro;

    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "divisa")
    private String divisa;

    @Column(name = "aplicativo")
    private String aplicativo;

    @Column(name = "saldo_aplicativo")
    private double saldo_aplicativo;

    @Column(name = "saldo_contable")
    private double saldo_contable;

    @Column(name = "diferencia")
    private double diferencia;
}
