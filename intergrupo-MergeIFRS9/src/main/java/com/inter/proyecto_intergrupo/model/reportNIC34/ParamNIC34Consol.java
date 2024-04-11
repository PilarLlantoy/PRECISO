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
@Table(name = "nexco_param_nic34_consol")
public class ParamNIC34Consol implements Serializable{

    @Id
    @Column(name = "id_nic34")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNic34;

    @Column(name = "l6")
    private String l6;

    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "id_grupo")
    private String idGrupo;

    @Column(name = "grupo")
    private String grupo;

    @Column(name = "aplica")
    private String aplica;

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

    @Column(name = "signo")
    private Double signo;

    @Column(name = "responsable")
    private String responsable;
}
