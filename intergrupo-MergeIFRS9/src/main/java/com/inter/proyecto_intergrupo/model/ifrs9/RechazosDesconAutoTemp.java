package com.inter.proyecto_intergrupo.model.ifrs9;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "nexco_rechazos_descon_auto_temp_1")
public class RechazosDesconAutoTemp implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "temp_id")
    public Long tempId;

    @Column(name = "REC_COD_PROCESO")
    public String REC_COD_PROCESO;

    @Column(name = "REC_COD_CCONTR")
    private String REC_COD_CCONTR;

    @Column(name = "REC_COD_CTACONT")
    private String REC_COD_CTACONT;

    @Column(name = "REC_COD_SALDO")
    private String REC_COD_SALDO;

    @Column(name = "REC_IND_STAGE_FINAL")
    private String REC_IND_STAGE_FINAL;

    @Column(name = "REC_COD_SEGM_FINREP")
    private String REC_COD_SEGM_FINREP;

    @Column(name = "REC_RECHAZOS")
    private String REC_RECHAZOS;


}
