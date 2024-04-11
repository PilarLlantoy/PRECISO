package com.inter.proyecto_intergrupo.model.ifrs9;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_risk")
public class Risk implements Serializable {

    @Id
    @Column(name = "CMCO_COD_CCONTR")
    private String CMCO_COD_CCONTR;

    @Column(name = "CMCO_COD_FAMILIA_INICIAL")
    private String CMCO_COD_FAMILIA_INICIAL;

    @Column(name = "CMCO_COD_FAMILIA_FINAL")
    private String CMCO_COD_FAMILIA_FINAL;

    @Column(name = "CMCO_COD_CLIENT")
    private String CMCO_COD_CLIENT;

    @Column(name = "CMCO_IND_STAGE_INICIAL")
    private String CMCO_IND_STAGE_INICIAL;

    @Column(name = "CMCO_IND_STAGE_FINAL")
    private String CMCO_IND_STAGE_FINAL;

    @Column(name = "EAD_INICIAL")
    private String EAD_INICIAL;

    @Column(name = "EAD_FINAL")
    private String EAD_FINAL;

    @Column(name = "EAD_Y01_INICIAL")
    private String EAD_Y01_INICIAL;

    @Column(name = "EAD_Y01_FINAL")
    private String EAD_Y01_FINAL;

    @Column(name = "CMCO_IMP_PROV_INICIAL")
    private String CMCO_IMP_PROV_INICIAL;

    @Column(name = "CMCO_IMP_PROV_FINAL")
    private String CMCO_IMP_PROV_FINAL;

    @Column(name = "VALOR_AJUSTE_PROVISION")
    private String VALOR_AJUSTE_PROVISION;

    @Column(name = "Numero_caso")
    private String Numero_caso;

    @Column(name = "CMCO_IMP_SDFUBA_CON")
    private String CMCO_IMP_SDFUBA_CON;

    @Column(name = "CMCO_IMP_RACREG_CON")
    private String CMCO_IMP_RACREG_CON;

    @Column(name = "FAMILIA")
    private String FAMILIA;

    @Column(name = "CAMBIA_PROVISION")
    private String CAMBIA_PROVISION;

    @Column(name = "VALIDA")
    private String VALIDA;

    @Column(name = "CAMBIO_DE_SEGMENTO")
    private String CAMBIO_DE_SEGMENTO;
}
