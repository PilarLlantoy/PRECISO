package com.inter.proyecto_intergrupo.model.information;

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
@Table(name = "nexco_neocon60_base")
public class Neocon60Base implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Neocon60")
    private Long idNeocon60;

    @Column(name = "ano")
    private String ano;

    @Column(name = "mes")
    private String mes;

    @Column(name = "yntp_reportante")
    private String yntpReportante;

    @Column(name = "codicons")
    private String codicons;

    @Column(name = "divisa")
    private String divisa;

    @Column(name = "pais")
    private String pais;

    @Column(name="yntp_intergrupo")
    private String yntpIntergrupo;

    @Column(name = "saldo")
    private double saldo;

    @Column(name = "naturaleza")
    private String naturaleza;

    @Column(name = "pais_contraparte")
    private String paisContraparte;

    @Column(name = "periodo")
    private String periodo;

}
