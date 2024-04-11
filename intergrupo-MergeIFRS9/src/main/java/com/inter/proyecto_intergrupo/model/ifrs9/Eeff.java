package com.inter.proyecto_intergrupo.model.ifrs9;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_eeff")
public class Eeff  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "")
    private Long idCarga;

    @Column(name = "sociedad_informante")
    private String codigosSocInformante;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "denominacion_cuenta")
    private String id;

    @Column(name = "tipo_cuenta")
    private String tipoCuenta;


    @Column(name = "cuenta")
    private String cuenta;


    @Column(name = "sociedad_IC")
    private String socIC;


    @Column(name = "descripcion_IC")
    private String descripcionIC;


    @Column(name = "desgloces")
    private String desgloces;


    @Column(name = "Divisa_espana")
    private String divisaespana;

    @Column(name = "saldo")
    private String saldo;

    @Column(name = "intergrupo")
    private String intergrupo;

    @Column(name = "entrada")
    private String entrada;

    @Column(name = "period")
    private String periodo;

    @Column(name = "tipo")
    private String tipo;

}
