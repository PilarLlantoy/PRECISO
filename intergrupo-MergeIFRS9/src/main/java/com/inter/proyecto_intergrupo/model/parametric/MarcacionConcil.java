package com.inter.proyecto_intergrupo.model.parametric;

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
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_h140_completa_ifrs9")
public class MarcacionConcil implements Serializable {

    @Id
    @Column(name = "id_h140")
    private Long id_h140;

    @Column(name = "aplicativo")
    private String aplicativo;

    @Column(name = "centro")
    private String centro;

    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "diferencia")
    private double diferencia;

    @Column(name = "divisa")
    private String divisa;

    @Column(name = "empresa")
    private String empresa;

    @Column(name = "fecha")
    private String fecha;

    @Column(name = "saldo_aplicativo")
    private double saldoAplicativo;

    @Column(name = "saldo_contable")
    private double saldoContable;

}
