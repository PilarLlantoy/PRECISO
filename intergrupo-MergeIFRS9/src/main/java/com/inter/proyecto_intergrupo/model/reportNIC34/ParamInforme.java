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
@Table(name = "nexco_nic34_informes")
public class ParamInforme implements Serializable{

    @Id
    @Column(name = "id_nic34")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNic34;

    @Column(name = "agrupa_1")
    private String agrupa1;

    @Column(name = "aplica_query")
    private String aplicaQuery;

    @Column(name = "agrupa_2")
    private String agrupa2;

    @Column(name = "id_g")
    private String idG;

    @Column(name = "moneda")
    private String moneda;

    @Column(name = "signo")
    private Double signo;

    @Column(name = "concepto")
    private String concepto;

    @Column(name = "condicion")
    private String condicion;

    @Column(name = "notas")
    private String notas;

    @Column(name = "aplica")
    private String aplica;
}
