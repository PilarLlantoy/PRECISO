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
@Table(name = "nexco_pyg_nic34_consol")
public class PygNIC34Consol implements Serializable{

    @Id
    @Column(name = "id_nic34")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNic34;

    @Column(name = "id_grupo")
    private String idGrupo;

    @Column(name = "moneda")
    private String moneda;

    @Column(name = "signo")
    private Double signo;

    @Column(name = "grupo")
    private String grupo;

    @Column(name = "saldo1")
    private Double saldo1;

    @Column(name = "saldo2")
    private Double saldo2;

    @Column(name = "variacion1")
    private Double variacion1;

    @Column(name = "variacion2")
    private Double variacion2;

    @Column(name = "saldo3")
    private Double saldo3;

    @Column(name = "saldo4")
    private Double saldo4;

    @Column(name = "q_aplica")
    private String qAplica;
}