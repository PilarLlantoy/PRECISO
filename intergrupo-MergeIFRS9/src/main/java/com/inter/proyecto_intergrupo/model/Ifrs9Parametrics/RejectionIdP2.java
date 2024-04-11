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
@Table(name = "nexco_identificacion_rechazos_p2")
public class RejectionIdP2 implements Serializable{

    @Id
    @Column(name = "linea_producto")
    private String lineaProducto;

    @Column(name = "segmentos")
    private String segmentos;

}
