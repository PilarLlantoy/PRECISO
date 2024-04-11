package com.inter.proyecto_intergrupo.model.bank;

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
@Table(name = "nexco_plantilla_carga")
public class TemplateBank implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carga")
    private Long idCargaBanco;

    @Column(name = "cod_neocon")
    private String Neocon;

    @Column(name = "yntp_empresa")
    private  String yntpEmpresa;

    @Column(name = "divisa")
    private String divisa;

    @Column(name = "yntp")
    private String yntp;

    @Column(name = "sociedad_yntp")
    private String Sociedad_yntp;

    @Column(name = "contrato")
    private String CONTRATO;

    @Column(name = "nit")
    private String nit;

    @Column(name = "valor")
    private Double valor;

    @Column(name = "cod_pais")
    private String COD_PAIS;

    @Column(name = "pais")
    private String PAIS;

    @Column(name = "cuenta_local")
    private String cuentaLocal;

    @Column(name = "observaciones")
    private String observaciones;

    @Column(name = "periodo")
    private String periodo;

    @Column(name = "usuario")
    private String usuario;

    @Column(name = "centro")
    private String centro;

}
