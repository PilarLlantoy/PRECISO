package com.inter.proyecto_intergrupo.model.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_reporte_ifrs9")
public class Ifrs9Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte")
    private Long idReporte;

    @Column(name = "CENTRO")
    private String centro;

    @Column(name = "CVES_COD_CTACONT", columnDefinition = "nvarchar(20)")
    private String CVES_COD_CTACONT;

    @Column(name = "ORIGEN")
    private String origen;

    @Column(name = "CVES_IND_STAGE_FINAL", columnDefinition = "nvarchar(20)")
    private String CVES_IND_STAGE_FINAL;

    @Column(name = "CVES_COD_SEGM_FINREP", columnDefinition = "nvarchar(20)")
    private String CVES_COD_SEGM_FINREP;

    @Column(name = "CVES_COD_ENT_IUC")
    private String CVES_COD_ENT_IUC;

    @Column(name = "CVES_COD_CCONTR")
    private String CVES_COD_CCONTR;

    @Column(name = "SALDO")
    private Double SALDO;

    @Column(name = "LARGO6", columnDefinition = "nvarchar(6)")
    private String LARGO6;

    @Column(name = "CODICONS", columnDefinition = "nvarchar(10)")
    private String codicons;

    @Column(name = "SUBPRODUCTO")
    private String subproducto;

    @Column(name = "INSTRUMENTO")
    private String instrumento;

    @Column(name = "PERIODO")
    private String periodo;

    @Column(name = "Type")
    private String Type;

    @Column(name = "stage_espana")
    private String stageEspana;

    @Column(name = "producto_espana")
    private String productoEspana;

    @Column(name = "sector")
    private String sector;

    @Column(name = "signo")
    private String signo;

}
