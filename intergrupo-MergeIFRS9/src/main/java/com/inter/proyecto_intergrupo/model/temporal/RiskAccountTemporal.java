package com.inter.proyecto_intergrupo.model.temporal;

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
@Table(name = "nexco_contabilizacion_riesgos_temp")
public class RiskAccountTemporal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_criesgos")
    private Long idCriesgos;

    @Column(name = "contrato")
    private String contrato;

    @Column(name = "codigo_familia_inicial")
    private String codigoFamiliaInicial;

    @Column(name = "codigo_familia_final")
    private String codigoFamiliaFinal;

    @Column(name = "codigo_cliente")
    private String codigoCliente;

    @Column(name = "stage_inicial")
    private String stageInicial;

    @Column(name = "stage_final")
    private String stageFinal;

    @Column(name = "ead_inicial")
    private Double eadInicial;

    @Column(name = "ead_final")
    private Double eadFinal;

    @Column(name = "ead_y01_inicial")
    private Double eadY01Inicial;

    @Column(name = "ead_y01_final")
    private Double eadY01Final;

    @Column(name = "importe_inicial")
    private Double importeInicial;

    @Column(name = "importe_final")
    private Double importeFinal;

    @Column(name = "valor_ajuste_provisión")
    private Double valorAjusteProvisión;

    @Column(name = "numero_caso")
    private String numeroCaso;

    @Column(name = "imp_sdfuba")
    private Double impSdfuba;

    @Column(name = "imp_racreg")
    private Double impRacreg;

    @Column(name = "familia")
    private String familia;

    @Column(name = "cambia_provision")
    private Double cambiaProvision;

    @Column(name = "cambio_de_segmento")
    private String cambioDeSegmento;

    @Column(name = "valida")
    private String valida;

    @Column(name = "periodo")
    private String periodo;

}
