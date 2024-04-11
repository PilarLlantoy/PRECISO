package com.inter.proyecto_intergrupo.model.Ifrs9Parametrics;

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
@Table(name = "nexco_identificacion_rechazos_p1")
public class RejectionIdP1 implements Serializable{

    @Id
    @Column(name = "inicial_cuenta")
    private String inicialCuenta;

    @Column(name = "asignacion")
    private String asignacion;

    @Column(name = "tipo_cuenta")
    private String tipoCuenta;

    @Column(name = "linea_inicial")
    private int lineaInicial;

    @Column(name = "linea_cantidad")
    private int lineaCantidad;

    @Column(name = "segmento_inicial")
    private int segmentoInicial;

    @Column(name = "segmento_cantidad")
    private int segmentoCantidad;

    @Column(name = "stage_inicial")
    private int stageInicial;

    @Column(name = "stage_cantidad")
    private int stageCantidad;

}
