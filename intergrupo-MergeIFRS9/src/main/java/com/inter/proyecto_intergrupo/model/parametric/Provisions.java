package com.inter.proyecto_intergrupo.model.parametric;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_provisiones")
public class Provisions {

    @Id
    @Column(name = "cuenta_neocon")
    private String cuentaNeocon;

    @Column(name = "instrumento")
    private String instrumento;

    @Column(name = "jerarquia")
    private String jerarquia;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "minimo")
    private String minimo;

    @Column(name = "ifrs9")
    private String ifrs9;

    @Column(name = "stage_espana")
    private String stageEspana;

    @Column(name = "producto_espana")
    private String productoEspana;

    @Column(name = "sector")
    private String sector;

    @Column(name = "signo")
    private String signo;
}
