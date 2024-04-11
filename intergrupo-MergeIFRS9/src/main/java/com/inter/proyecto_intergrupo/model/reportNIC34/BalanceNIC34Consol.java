package com.inter.proyecto_intergrupo.model.reportNIC34;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_balance_nic34_consol")
public class BalanceNIC34Consol implements Serializable{

    @Id
    @Column(name = "id_nic34")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNic34;

    @Column(name = "id_grupo")
    private String idGrupo;

    @Column(name = "grupo")
    private String grupo;

    @Column(name = "saldo1")
    private Double saldo1;

    @Column(name = "saldo2")
    private Double saldo2;

    @Column(name = "variacion")
    private Double variacion;

    @Column(name = "q_aplica")
    private String qAplica;
}
