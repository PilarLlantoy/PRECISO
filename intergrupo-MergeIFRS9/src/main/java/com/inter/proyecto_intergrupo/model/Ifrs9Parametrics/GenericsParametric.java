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
@Table(name = "nexco_parametrica_genericas")
public class GenericsParametric implements Serializable {

    @Id
    @Column(name = "fuente_info")
    String fuenteInfo;

    @Id
    @Column(name = "tp")
    String tp;

    @Id
    @Column(name = "indicador")
    String indicador;

    @Id
    @Column(name = "cartera")
    String cartera;

    @Id
    @Column(name = "clase")
    String clase;

    @Id
    @Column(name = "calificacion")
    String calificacion;

    @Column(name = "empresa")
    String empresa;

    @Column(name = "cuenta")
    String cuenta;

    @Column(name = "uno")
    String uno;

    @Column(name = "dos")
    String dos;

    @Column(name = "tres")
    String tres;

    @Column(name = "cuatro")
    String cuatro;

    @Column(name = "cinco")
    String cinco;

    @Column(name = "seis")
    String seis;

    @Column(name = "nombre_cuenta")
    String nombreCuenta;

    @Column(name = "porcentaje_calc")
    Double porcentajeCalc;

    @Id
    @Column(name = "codigo_ifrs9")
    String codigoIfrs9;
}
