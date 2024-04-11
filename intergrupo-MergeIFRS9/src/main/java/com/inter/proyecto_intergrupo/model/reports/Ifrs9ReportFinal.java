package com.inter.proyecto_intergrupo.model.reports;

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
@Table(name = "nexco_reporte_ifrs9_final")
public class Ifrs9ReportFinal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte")
    private Long idReporte;

    @Column(name = "consulta")
    private String consulta;

    @Column(name = "instrumento")
    private String instrumento;

    @Column(name = "subproducto")
    private String subproducto;

    @Column(name = "stage1_ant")
    private Double stage1Ant;

    @Column(name = "stage2_ant")
    private Double stage2Ant;

    @Column(name = "stage3_ant")
    private Double stage3Ant;

    @Column(name = "total_general_ant")
    private Double totalGeneralAnt;

    @Column(name = "stage1")
    private Double stage1;

    @Column(name = "stage2")
    private Double stage2;

    @Column(name = "stage3")
    private Double stage3;

    @Column(name = "total_general")
    private Double totalGeneral;

    @Column(name = "variacion")
    private Double variacion;

    @Column(name = "periodo")
    private String periodo;

    @Column(name = "type")
    private String type;

    @Column(name = "local")
    private String local;

    @Column(name = "tipo_registro")
    private String tipoRegistro;

}
