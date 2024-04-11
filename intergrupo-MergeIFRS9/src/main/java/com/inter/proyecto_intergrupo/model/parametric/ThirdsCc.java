package com.inter.proyecto_intergrupo.model.parametric;

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
@Table(name = "nexco_terceros_cc")
public class ThirdsCc implements Serializable{

    @Id
    @Column(name = "nit")
    private String nit;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "correo")
    private String correo;

    @Column(name = "impuesto")
    private String impuesto;

    @Column(name = "correo_alterno")
    private String correoAlterno;

    @Column(name = "correo_alterno2")
    private String correoAlterno2;

    @Column(name = "direccion")
    private String direccion;

    @Column(name = "correo_copia1")
    private String correoCopia1;

    @Column(name = "correo_copia2")
    private String correoCopia2;

}
