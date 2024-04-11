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
@Table(name = "nexco_perdidaincurrida_temp")
public class IncurredLossTemporal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCarga")
    private Long idCarga;

    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "centro")
    private String centro;

    @Column(name = "contrato")
    private String contrato;

    @Column(name = "segmento")
    private String segmento;

    @Column(name = "stage")
    private String stage;

    @Column(name = "indicador_contrato")
    private String indicadorcontrato;

    @Column(name = "saldo")
    private Double saldo;

    @Column(name = "codigo_consolidacion")
    private String codigoconso;



}
