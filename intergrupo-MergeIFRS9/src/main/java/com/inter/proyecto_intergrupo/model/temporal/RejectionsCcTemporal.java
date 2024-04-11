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
@Table(name = "nexco_rechazos_cc_temporal")
public class RejectionsCcTemporal {

    @Id
    @Column(name = "id_rechazos")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRechazos;

    @Column(name = "fecha")
    private String fecha;

    @Column(name = "empresa")
    private String empresa;

    @Column(name = "centro")
    private String centro;

    @Column(name = "contrato")
    private String contrato;

    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "divisa")
    private String divisa;

    @Column(name = "stage")
    private String stage;

    @Column(name = "segmento")
    private String segmento;

    @Column(name = "importe_local")
    private Double importeLocal;

    @Column(name = "valor2")
    private String valor2;

    @Column(name = "tipo_rechazo")
    private String tipoRechazo;

    @Column(name = "importe_opc")
    private Double importeOpc;

    @Column(name = "origen")
    private String origen;
}
