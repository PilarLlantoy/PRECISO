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
@Table(name = "nexco_plantilla_report_icrv")
public class PlantillaReportIcrv implements Serializable{

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

    @Column(name = "signo_valor_contable")
    private String signoValorContable;

    @Column(name = "signo_microcobertura")
    private String signoMicrocobertura;

    @Column(name = "periodo")
    private String periodo;

}
