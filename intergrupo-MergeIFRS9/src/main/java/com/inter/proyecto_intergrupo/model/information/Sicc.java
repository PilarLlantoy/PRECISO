package com.inter.proyecto_intergrupo.model.information;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_sicc")
public class Sicc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sicc")
    private int idSicc;

    @Column(name = "cod_neocon")
    private String codNeocon;

    @Column(name = "divisa")
    private String divisa;

    @Column(name = "yntp")
    private String yntp;

    @Column(name = "sociedad_yntp")
    private String sociedadYntp;

    @Column(name = "contrato")
    private String contrato;

    @Column(name = "nit_contraparte")
    private String nitContraparte;

    @Column(name = "valor")
    private String valor;

    @Column(name = "cod_pais")
    private String codPais;

    @Column(name = "pais")
    private String pais;

    @Column(name = "cuenta_local")
    private String cuentaLocal;

    @Column(name = "periodo_contable")
    private String periodoContable;
}
