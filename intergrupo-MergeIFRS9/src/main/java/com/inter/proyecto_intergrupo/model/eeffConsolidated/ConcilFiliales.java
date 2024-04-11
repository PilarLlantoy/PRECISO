package com.inter.proyecto_intergrupo.model.eeffConsolidated;

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

@Table(name = "nexco_concil_filiales")

public class ConcilFiliales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_column")
    Long id_column;

    @Column(name = "L_1")
    String L_1;

    @Column(name = "L_2")
    String L_2;

    @Column(name = "L_4")
    String L_4;

    @Column(name = "L_6")
    String L_6;

    @Column(name = "L_9")
    String L_9;

    @Column(name = "cuenta")
    String cuenta;

    @Column(name = "nombre_cuenta")
    String nombreCuenta;

    @Column(name = "empresa")
    String empresa;

    @Column(name = "moneda")
    String moneda;

    @Column(name = "banco")
    Double banco;

    @Column(name = "fiduciaria")
    Double fiduciaria;

    @Column(name = "valores")
    Double valores;

    @Column(name = "total")
    Double total;

    @Column(name = "periodo")
    String periodo;

    @Column(name = "debe")
    Double debe;

    @Column(name = "haber")
    Double haber;

    @Column(name = "eliminacion")
    Double eliminacion;

    @Column(name = "debe_patrimonio")
    Double debePatrimonio;

    @Column(name = "haber_patrimonio")
    Double haberPatrimonio;

    @Column(name = "eliminacion_patrimonio")
    Double eliminacionPatrimonio;

    @Column(name = "codicons")
    String codicons;

    @Column(name = "debe_ajustes_minimos")
    Double debeAjustesMinimos;

    @Column(name = "haber_ajustes_minimos")
    Double haberAjustesMinimos;

    @Column(name = "debe_ajustes_mayores")
    Double debeAjustesMayores;

    @Column(name = "haber_ajustes_mayores")
    Double haberAjustesMayores;

    @Column(name = "debe_ver_pt")
    Double debeVerPt;

    @Column(name = "haber_ver_pt")
    Double haberVerPt;

    @Column(name = "debe_total")
    Double debeTotal;

    @Column(name = "haber_total")
    Double haberTotal;

    @Column(name = "total_ifrs")
    Double totalIfrs;

    @Column(name = "total_ifrs_2")
    Double totalIfrs2;

    @Column(name = "total_consol_2")
    Double totalConsol2;

}
