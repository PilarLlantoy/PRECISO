package com.inter.proyecto_intergrupo.model.reports;

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
@Table(name = "nexco_intergrupo_v2")
public class IntergrupoV2 implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte")
    Integer idReporte;

    @Column(name = "yntp_empresa_reportante")
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

    @Column(name = "nit")
    String nit;

    @Column(name = "valor")
    Double valor;

    @Column(name = "cod_pais")
    String codPais;

    @Column(name = "pais")
    String pais;

    @Column(name = "cuenta_local")
    String cuentaLocal;

    @Column(name = "periodo")
    String periodo;

    @Column(name = "fuente")
    String fuente;

    @Column(name = "componente")
    String componente;

    @Column(name = "input")
    String input;
}