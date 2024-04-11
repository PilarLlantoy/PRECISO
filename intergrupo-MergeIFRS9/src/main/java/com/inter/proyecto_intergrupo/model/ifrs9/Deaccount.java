package com.inter.proyecto_intergrupo.model.ifrs9;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

import static javax.persistence.GenerationType.SEQUENCE;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_diferencias")
public class Deaccount implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Integer id;

    @Column(name="centro")
    private String centro;

    @Column(name="cuenta")
    private String cuenta;

    @Column(name="contrato")
    private String contrato;

    @Column(name="valor_contable")
    private Double valorContable;

    @Column(name="valor_aplicativo")
    private Double valorAplicativo;

    @Column(name="valor_diferencia")
    private Double valorDiferencia;

    @Column(name="periodo")
    private String periodo;

    @Column(name="origen")
    private String origen;

}
