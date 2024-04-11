package com.inter.proyecto_intergrupo.model.parametric;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_counterparty_generic_contracts")
public class CounterpartyGenericContracts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "concepto")
    private String concepto;

    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "contrato")
    private String contrato;

    @Column(name = "fuente_informacion")
    private String fuenteInformacion;

    @Column(name = "saldo")
    private Integer saldo;

}
