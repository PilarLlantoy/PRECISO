package com.inter.proyecto_intergrupo.model.ifrs9;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_manuales_anexo")
public class Manuals implements Serializable {

    @Id
    @Column(name="id_manuales")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idManuales;

    @Column(name="centro")
    private String centro;

    @Column(name="descripcion_centro")
    private String descripcionCentro;

    @Column(name="cuenta_puc")
    private String cuentaPuc;

    @Column(name="descripcion_cuenta_puc")
    private String descripcionCuentaPuc;

    @Column(name="divisa")
    private String divisa;

    @Column(name="importe")
    private Double importe;

    @Column(name="fecha_origen")
    private String fechaOrigen;

    @Column(name="fecha_cierre")
    private String fechaCierre;

    @Column(name="tp")
    private String tp;

    @Column(name="identificacion")
    private String identificacion;

    @Column(name="dv")
    private String dv;

    @Column(name="nombre")
    private String nombre;

    @Column(name="contrato")
    private String contrato;

    @Column(name="observacion",length = 300)
    private String observacion;

    @Column(name="cuenta_prov")
    private String cuentaProv;

    @Column(name="importe_prov")
    private String importeProv;

    @Column(name="importe_original")
    private String importeOriginal;

    @Column(name="probabilidad_recuperacion")
    private String probabilidadRecuperacion;

    @Column(name="altura")
    private String altura;

    @Column(name="fuente_informacion")
    private String fuenteInformacion;

    @Column(name="periodo")
    private String periodo;

    @Column(name="descripcion_provisiones")
    private String descripcionProvisiones;

}
