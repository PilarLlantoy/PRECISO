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
@Table(name = "nexco_condetari")
public class CondetaRI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="empresa")
    private String empresa;

    @Column(name="aplicativo")
    private String aplicativo;

    @Column(name="fecha1")
    private String fecha1;

    @Column(name="fecha2")
    private String fecha2;

    @Column(name="cuenta")
    private String cuenta;

    @Column(name="divisa")
    private String divisa;

    @Column(name="centro")
    private String centro;

    @Column(name="contrato")
    private String contrato;

    @Column(name="tipo_identificacion")
    private String tipo_identificacion;

    @Column(name="identificacion")
    private String identificacion;

    @Column(name="digito_verificacion")
    private String digito_verificacion;

    @Column(name="valor_aplicativo")
    private Double valor_aplicativo;

    @Column(name="valor_contable")
    private Double valor_contable;

    @Column(name="valor_diferencia")
    private Double valor_diferencia;

    @Column(name="valor_variacion")
    private Double valor_variacion;

}
