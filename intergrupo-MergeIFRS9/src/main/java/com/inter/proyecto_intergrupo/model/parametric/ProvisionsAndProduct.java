package com.inter.proyecto_intergrupo.model.parametric;

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
@Table(name = "preciso_provisiones_producto")
public class ProvisionsAndProduct {

    @Id
    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "instrumento")
    private String instrumento;

    @Column(name = "jerarquia")
    private String jerarquia;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "minimo")
    private String minimo;

    @Column(name = "perimetro_ifrs9")
    private Boolean perimetroIFRS9;

    @Column(name = "stages_spain")
    private String stagesSpain;

    @Column(name = "producto_spain")
    private String productoSpain;

    @Column(name = "sector_spain")
    private String sectorSpain;

    @Column(name = "signo")
    private String signo;
}
