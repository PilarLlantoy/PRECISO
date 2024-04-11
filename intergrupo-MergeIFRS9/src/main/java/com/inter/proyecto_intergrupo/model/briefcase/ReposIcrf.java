package com.inter.proyecto_intergrupo.model.briefcase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_repos_e_inter_icrf")
public class ReposIcrf implements Serializable{

    @Id
    @Column(name = "id_report")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReport;

    @Column(name = "codigo")
    private String codigo;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "negociador")
    private String negociador;

    @Column(name = "cod_cli")
    private String codCli;

    @Column(name = "fecha")
    private Date fecha;

    @Column(name = "fecha_final")
    private Date fechaFinal;

    @Column(name = "valor_total")
    private Double valorTotal;

    @Column(name = "intereses")
    private String intereses;

    @Column(name = "tasa")
    private String tasa;

    @Column(name = "negocio")
    private String negocio;

    @Column(name = "estado")
    private String estado;

    @Column(name = "tipo_mov")
    private String tipoMov;

    @Column(name = "nro_papeleta")
    private String nroPapeleta;

    @Column(name = "tipo_op_mdo")
    private String tipoOpMdo;

    @Column(name = "causacion_hoy")
    private Double causacionHoy;

    @Column(name = "causacion_ayer")
    private Double causacionAyer;

    @Column(name = "portafolio")
    private String portafolio;

    @Column(name = "contabilidad")
    private String contabilidad;

    @Column(name = "tasa_mesa")
    private String tasaMesa;

    @Column(name = "causacion_neta")
    private String causacionNeta;

    @Column(name = "nombre1")
    private String nombre1;

    @Column(name = "nro_identificacion")
    private String nroIdentificacion;

    @Column(name = "duracion_modificada_anual")
    private String duracionModificadaAnual;

    @Column(name = "moneda")
    private String moneda;

    @Column(name = "ctro_contable_alt")
    private String ctroContableAlt;

    @Column(name = "tipo_entidad")
    private String tipoEntidad;

    @Column(name = "vlr_libros")
    private Double vlrLibros;

    @Column(name = "vlr_libros_intereses_hoy")
    private String vlrLibrosInteresesHoy;

    @Column(name = "vlr_mdo_gtia_act")
    private String vlrMdoGtiaAct;

    @Column(name = "vlr_mdo_gtia_pas")
    private String vlrMdoGtiaPas;

    @Column(name = "pap_reemplaza")
    private String papReemplaza;

    @Column(name = "calif_contraparte")
    private String califContraparte;

    @Column(name = "cod_puc")
    private String codPuc;

    @Column(name = "codpuc_incumpl")
    private String codpucIncumpl;

    @Column(name = "monto_prov")
    private Double montoProv;

    @Column(name = "monto_ext")
    private Double montoExt;

    @Column(name = "monto_fin_ext")
    private Double montoFinExt;

    @Column(name = "monto_legal_f")
    private Double montoLegalF;

    @Column(name = "puc_rend")
    private String pucRend;

    @Column(name = "monto_rend")
    private String montoRend;

    @Column(name = "cod_puccupon_ar")
    private String codPuccuponAr;

    @Column(name = "monto_cupon_ar")
    private String montoCuponAr;

    @Column(name = "codpuc_cupon")
    private String codpucCupon;

    @Column(name = "monto_cupon")
    private String montoCupon;

    @Column(name = "llamado_margen")
    private String llamadoMargen;

    @Column(name = "tp_llamado_margen")
    private String tpLlamadoMargen;

    @Column(name = "codpuc_llamado_margend")
    private String codpucLlamadoMargend;

    @Column(name = "codpuc_llamado_margenv")
    private String codpucLlamadoMargenv;

    @Column(name = "mnto_acum_llamado_margen")
    private String mntoAcumLlamadoMargen;

    @Column(name = "exposicon_neta")
    private String exposiconNeta;

    @Column(name = "tipo_vinculacion")
    private String tipoVinculacion;

    @Column(name = "codigo_normalizado")
    private String codigoNormalizado;

    @Column(name = "portafolio_front")
    private String portafolioFront;

    @Column(name = "cta_balance")
    private String ctaBalance;

    @Column(name = "cta_balance_interes")
    private String ctaBalanceInteres;

    @Column(name = "cta_pyg")
    private String ctaPyg;

    @Column(name = "isin_cdd")
    private String isinCdd;

    @Column(name = "cod_emisor")
    private String codEmisor;

    @Column(name = "nom_emisor")
    private String nomEmisor;

    @Column(name = "isin_new")
    private String isinNew;

    @Column(name = "periodo")
    private String periodo;

}
