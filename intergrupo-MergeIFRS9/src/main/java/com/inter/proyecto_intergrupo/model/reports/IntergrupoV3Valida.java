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
@Table(name = "nexco_intergrupo_v3_valida")
public class IntergrupoV3Valida implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_intergrupo")
    private Long idReporte;

    @Column(name = "cod_neocon")
    String codNeocon;

    @Column(name = "cod_pais")
    String codPais;

    @Column(name = "componente")
    String componente;

    @Column(name = "contrato")
    String contrato;

    @Column(name = "cuenta_local")
    String cuentaLocal;

    @Column(name = "cuenta_plano")
    String cuentaPlano;

    @Column(name = "divisa")
    String divisa;

    @Column(name = "elimina")
    String elimina;

    @Column(name = "fuente")
    String fuente;

    @Column(name = "input")
    String input;

    @Column(name = "intergrupo")
    String intergrupo;

    @Column(name = "nit")
    String nit;

    @Column(name = "pais")
    String pais;

    @Column(name = "perimetro")
    String perimetro;

    @Column(name = "periodo")
    String periodo;

    @Column(name = "sociedad_yntp")
    String sociedadYntp;

    @Column(name = "valor")
    Double valor;

    @Column(name = "valor_prov")
    Double valorProv;

    @Column(name = "valor_rec")
    Double valorRec;

    @Column(name = "yntp")
    String yntp;

    @Column(name = "yntp_empresa_reportante")
    String yntpEmpresaReportante;

}

