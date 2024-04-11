package com.inter.proyecto_intergrupo.model.ifrs9;

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
@Table(name = "nexco_contabilizacion_riesgos_final")
public class RiskAccountFinal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_criesgos")
    private Long idCriesgos;

    @Column(name = "contrato")
    private String contrato;

    @Column(name = "stage")
    private String stage;

    @Column(name = "segmento")
    private String segmento;

    @Column(name = "provision")
    private Double provision;

    @Column(name = "valida")
    private String valida;

    @Column(name = "periodo")
    private String periodo;

}
