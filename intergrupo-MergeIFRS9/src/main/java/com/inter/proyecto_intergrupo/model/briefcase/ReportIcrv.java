package com.inter.proyecto_intergrupo.model.briefcase;

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
@Table(name = "nexco_report_icrv")
public class ReportIcrv implements Serializable{

    @Id
    @Column(name = "id_report")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReport;

    @Column(name = "entidad")
    private String entidad;

    @Column(name = "cod_periodo")
    private String codPeriodo;

    @Column(name = "cod_sociinfo")
    private String codSociinfo;

    @Column(name = "xti_cartera")
    private String xtiCartera;

    @Column(name = "cod_socipart")
    private String codSocipart;

    @Column(name = "cod_isin")
    private String codIsin;

    @Column(name = "coste_valor")
    private Double costeValor;

    @Column(name = "ajuste_valor_razonable")
    private Double ajusteValorRazonable;

    @Column(name = "microcoberturas")
    private Double microcoberturas;

    @Column(name = "correcciones_por_deterioro")
    private Double correccionesPorDeterioro;

    @Column(name = "valor_cotizado")
    private Double valorCotizado;

    @Column(name = "desembolso_pdte")
    private Double desembolsoPdte;

    @Column(name = "num_titulos")
    private Double numTitulos;

    @Column(name = "capital_social")
    private Double capitalSocial;

    @Column(name = "coste_adquisicion")
    private Double costeAdquisicion;

    @Column(name = "signo_valor_contable")
    private String signoValorContable;

    @Column(name = "signo_microcobertura")
    private String signoMicrocobertura;

    @Column(name = "periodo")
    private String periodo;

}
