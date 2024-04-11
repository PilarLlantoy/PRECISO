package com.inter.proyecto_intergrupo.model.bank;

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
@Table(name = "nexco_gpsreport")
public class GpsReport  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_gps")
    private Long idCarga;

    @Column(name = "nombre1")
    private String nombre1;

    @Column(name = "razon_social")
    private String razon_social;

    @Column(name = "ident_fis")
    private String ident_fis;

    @Column(name = "nif")
    private String nif;

    @Column(name = "soc")
    private String soc;

    @Column(name = "clase")
    private String clase;

    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "cuenta_local")
    private String cuenta_local;

    @Column(name = "importe_md")
    private String importe_md;

    @Column(name = "tipo_cambio")
    private String tipo_cambio;

    @Column(name = "fecont")
    private String fecont;

    @Column(name = "ejercicioMes")
    private String ejercicioMes;

    @Column(name = "ce_coste")
    private String ce_coste;

    @Column(name = "fecha_doc")
    private String fecha_doc;

    @Column(name = "texto")
    private String texto;

    @Column(name = "texto_camb")
    private String texto_camb;

    @Column(name = "mon1")
    private String mon1;

    @Column(name = "divisa")
    private String divisa;

    @Column(name = "importe_ml")
    private String importe_ml;

    @Column(name = "referencia")
    private String referencia;

    @Column(name = "numero_doc")
    private String numero_doc;

    @Column(name = "clave_3")
    private String clave_3;

    @Column(name = "doc_comp")
    private String doc_comp;

    @Column(name = "archv_fijo")
    private String archv_fijo;

    @Column(name = "elemento_pep")
    private String elemento_pep;

    @Column(name = "usuario_em")
    private String usuario_em;

    @Column(name = "prorrata_iva")
    private String prorrata_iva;

    @Column(name = "tipo_importe")
    private String tipo_importe;

    @Column(name = "perimetro_comer")
    private String perimetroComer;
}
