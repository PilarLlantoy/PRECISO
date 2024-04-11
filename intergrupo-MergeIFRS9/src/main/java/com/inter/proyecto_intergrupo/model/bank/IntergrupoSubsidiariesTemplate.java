package com.inter.proyecto_intergrupo.model.bank;

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
@Table(name = "nexco_filiales_intergrupo")
public class IntergrupoSubsidiariesTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_filiales")
    private Long idFiliales;

    @Column(name = "yntp_reportante")
    String yntpReportante;

    @Column(name = "cod_neocon")
    String codNeocon;

    @Column(name = "divisa")
    String divisa;

    @Column(name = "yntp")
    String yntp;

    @Column(name = "sociedad_yntp")
    String sociedadYntp;

    @Column(name = "contrato")
    String contrato;

    @Column(name = "nit_contraparte")
    String nitContraparte;

    @Column(name = "valor")
    double valor;

    @Column(name = "cod_pais")
    String codPais;

    @Column(name = "pais")
    String pais;

    @Column(name = "cuenta_local")
    String cuentaLocal;

    @Column(name = "observaciones")
    String observaciones;

    @Column(name = "periodo")
    String periodo;

    @Column(name = "usuario")
    String usuario;
}
