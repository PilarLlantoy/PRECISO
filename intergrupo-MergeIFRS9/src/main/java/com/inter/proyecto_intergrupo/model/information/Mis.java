package com.inter.proyecto_intergrupo.model.information;

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
@Table(name = "nexco_mis")
public class Mis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mis_inter")
    private Long idMisInter;

    @Column(name = "yntp_empresa_reportante")
    private String yntpEmpresa;

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
    private String nit;

    @Column(name = "valor")
    private Double valor;

    @Column(name = "cod_pais")
    private String codPais;

    @Column(name = "pais")
    private String pais;

    @Column(name = "cuenta_local")
    private String cuentaLocal;

    @Column(name = "observaciones")
    private String observaciones;

    @Column(name = "periodo_contable")
    private String periodoContable;
}
