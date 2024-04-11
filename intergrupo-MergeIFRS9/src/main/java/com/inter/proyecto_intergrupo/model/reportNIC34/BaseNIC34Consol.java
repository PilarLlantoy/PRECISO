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
@Table(name = "nexco_base_nic34_consol")
public class BaseNIC34Consol implements Serializable{

    @Id
    @Column(name = "id_base")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idBase;

    @Column(name = "fecont")
    private String fecont;

    @Column(name = "l6")
    private String l6;

    @Column(name = "nucta")
    private String nucta;

    @Column(name = "id_grupo")
    private String idGrupo;

    @Column(name = "grupo")
    private String grupo;

    @Column(name = "aplica")
    private String aplica;

    @Column(name = "signo")
    private Double signo;

    @Column(name = "id_nota")
    private String idNota;

    @Column(name = "nota")
    private String nota;

    @Column(name = "id_subnota")
    private String idSubnota;

    @Column(name = "subnota")
    private String subnota;

    @Column(name = "id_campo")
    private String idCampo;

    @Column(name = "campo")
    private String campo;

    @Column(name = "moneda")
    private String moneda;

    @Column(name = "saldoquery")
    private Double saldoquery;

    @Column(name = "balance")
    private String balance;

    @Column(name = "pyg")
    private String pyg;

    @Column(name = "q_aplica")
    private String qAplica;
}
