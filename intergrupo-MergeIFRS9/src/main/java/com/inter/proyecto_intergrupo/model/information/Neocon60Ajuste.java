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
@Table(name = "nexco_neocon60_ajuste")
public class Neocon60Ajuste implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Neocon60")
    private Long idNeocon60;

    @Column(name = "ano")
    private String ano;

    @Column(name = "mes")
    private String mes;

    @Column(name = "codicons")
    private String codicons;

    @Column(name = "divisa")
    private String divisa;

    @Column(name = "divisa_espana")
    private String divisaEspana;

    @Column(name = "saldo")
    private double saldo;

    @Column(name = "observacion")
    private String observacion;

    @Column(name = "periodo")
    private String periodo;

    @Column(name = "aplica")
    private String aplica;

}
