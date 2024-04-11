package com.inter.proyecto_intergrupo.model.ifrs9;

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
@Table(name = "nexco_porcentaje_calculado")
public class OnePercent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_calc")
    private Long idCalc;

    @Column(name = "cod_neocon")
    private String codNeocon;

    @Column(name = "cartera")
    private String cartera;

    @Column(name = "oficina")
    private String oficina;

    @Column(name = "calificacion")
    private String calificacion;

    @Column(name = "nucta")
    private String nucta;

    @Column(name = "epigrafe")
    private String epigrafe;

    @Column(name = "fecha_corte")
    private String fechaCorte;

    @Column(name = "fecha_creacion")
    private String fechaCreacion;

    @Column(name = "version_0")
    private Double version0;

    @Column(name = "version_1")
    private Double version1;

    @Column(name = "version_2")
    private Double version2;

    @Column(name = "version_3")
    private Double version3;

    @Column(name = "version_4")
    private Double version4;

    @Column(name = "version_5")
    private Double version5;

    @Column(name = "version_6")
    private Double version6;

    @Column(name = "version_7")
    private Double version7;

    @Column(name = "version_8")
    private Double version8;

    @Column(name = "variacion_1")
    private Double variacion1;

    @Column(name = "variacion_2")
    private Double variacion2;

    @Column(name = "variacion_3")
    private Double variacion3;

    @Column(name = "variacion_4")
    private Double variacion4;

    @Column(name = "variacion_5")
    private Double variacion5;

    @Column(name = "variacion_6")
    private Double variacion6;

    @Column(name = "variacion_7")
    private Double variacion7;

    @Column(name = "variacion_8")
    private Double variacion8;

    @Column(name = "calculo_1")
    private Double calculo1;

    @Column(name = "calculo_2")
    private Double calculo2;

    @Column(name = "calculo_3")
    private Double calculo3;

    @Column(name = "calculo_4")
    private Double calculo4;

    @Column(name = "calculo_5")
    private Double calculo5;

    @Column(name = "calculo_6")
    private Double calculo6;

    @Column(name = "calculo_7")
    private Double calculo7;

    @Column(name = "calculo_8")
    private Double calculo8;

    @Column(name = "porcentaje_calculado")
    private Double porcentajeCalc;

    @Column(name = "cuenta_balance")
    private String cuentaBalance;

    @Column(name = "cuenta_pyg")
    private String cuentaPyG;

    @Column(name = "fuente_info")
    private String fuenteInfo;

}
