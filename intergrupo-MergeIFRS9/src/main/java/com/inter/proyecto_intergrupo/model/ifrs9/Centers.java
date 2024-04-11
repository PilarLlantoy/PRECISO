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
@Table(name = "nexco_centers")
public class Centers implements Serializable {
    @Id
    @Column(name = "oficina")
    private String oficina;

    @Column(name = "tipo_unidad")
    private String tipo_unidad;

    @Column(name = "nomtip")
    private String nomtip;

    @Column(name = "clase_unidad")
    private String clase_unidad;

    @Column(name = "nombre_unidad")
    private String nombre_unidad;

    @Column(name = "dar")
    private String dar;

    @Column(name = "dug")
    private String dug;

    @Column(name="territorio")
    private String territorio;

    @Column(name = "direccion_regional")
    private String direccion_regional;

    @Column(name = "area_operativa")
    private String area_operativa;

    @Column(name = "suprarea")
    private String suprarea;

    @Column(name = "fecha_cierre")
    private String fecha_cierre;

    @Column(name="ofinegocio")
    private String ofinegocio;

    @Column(name = "fecha_apertura")
    private String fecha_apertura;

    @Column(name = "domicilio")
    private String domicilio;

    @Column(name="telefono")
    private String telefono;
}
