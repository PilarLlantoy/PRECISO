package com.inter.proyecto_intergrupo.model.accountsReceivable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_cuentas_cc")
public class AccountCc implements Serializable{

    @Id
    @Column(name = "id_cuentas")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCuentas;

    @Column(name = "concepto")
    private String concepto;

    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "centro")
    private String centro;

    @Column(name = "naturaleza")
    private String naturaleza;

    @Column(name = "impuesto")
    private String impuesto;

    @Column(name = "evento")
    private String evento;

}
