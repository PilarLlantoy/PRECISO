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
@Table(name = "nexco_portafolio_diario_icrf")
public class PortafolioDiarioIcrf implements Serializable{

    @Id
    @Column(name = "id_report")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReport;

    @Column(name = "origen")
    private String origen;

    @Column(name = "portafolio")
    private String portafolio;

    @Column(name = "nombre_portafolio")
    private String nombrePortafolio;

    @Column(name = "fecha_val")
    private Date fechaVal;

    @Column(name = "nro_titulo")
    private String nroTitulo;

    @Column(name = "isin_star")
    private String isinStar;

    @Column(name = "emisor")
    private String emisor;

    @Column(name = "nombre_emisor")
    private String nombreEmisor;

    @Column(name = "fecha_vcto")
    private Date fechaVcto;

    @Column(name = "inicio_flujo")
    private Date inicioFlujo;

    @Column(name = "fin_flujo")
    private Date finFlujo;

    @Column(name = "tasa_cupon")
    private Double tasaCupon;

    @Column(name = "tipo_papel")
    private String tipoPapel;

    @Column(name = "tipo")
    private String tipo;

    @Column(name = "valor_nominal")
    private Double valorNominal;

    @Column(name = "saldo_capital")
    private Double saldoCapital;

    @Column(name = "moneda")
    private String moneda;

    @Column(name = "causados")
    private String causados;

    @Column(name = "precio_limpio")
    private Double precioLimpio;

    @Column(name = "valor_compra")
    private Double valorCompra;

    @Column(name = "interes_lineal_compra")
    private Double interesLinealCompra;

    @Column(name = "intereses_lineal_hoy")
    private Double interesesLinealHoy;

    @Column(name = "valor_mercado2")
    private Double valorMercado2;

    @Column(name = "fecha_compra")
    private Date fechaCompra;

    @Column(name = "tir_compra")
    private String tirCompra;

    @Column(name = "tir_365")
    private String tir365;

    @Column(name = "tir_365_1")
    private String tir3651;

    @Column(name = "precio_sucio_asset")
    private String precioSucioAsset;

    @Column(name = "estado_")
    private String estado;

    @Column(name = "tvariable")
    private String tvariable;

    @Column(name = "tvariable1")
    private String tvariable1;

    @Column(name = "tasa_nominal")
    private String tasaNominal;

    @Column(name = "spread")
    private String spread;

    @Column(name = "dn03por1_duracion_br")
    private String dn03por1DuracionBr;

    @Column(name = "prima_descto_total")
    private String primaDesctoTotal;

    @Column(name = "fecha_emision")
    private Date fechaEmision;

    @Column(name = "dias_vcto")
    private String diasVcto;

    @Column(name = "amortiza")
    private String amortiza;

    @Column(name = "mod_interes")
    private String modInteres;

    @Column(name = "pago_interes")
    private String pagoInteres;

    @Column(name = "tipo_custodio")
    private String tipoCustodio;

    @Column(name = "custodio")
    private String custodio;

    @Column(name = "base_interes")
    private String baseInteres;

    @Column(name = "caculint")
    private String caculint;

    @Column(name = "cod_tipo_interes")
    private String codTipoInteres;

    @Column(name = "tipo_interes")
    private String tipoInteres;

    @Column(name = "puntos_adicionales_spead")
    private String puntosAdicionalesSpead;

    @Column(name = "nro_total_dias_cupon")
    private String nroTotalDiasCupon;

    @Column(name = "intereses_cobrar")
    private Double interesesCobrar;

    @Column(name = "cta_contab_concil")
    private String ctaContabConcil;

    @Column(name = "cod_emp")
    private String codEmp;

    @Column(name = "nemo_bvc")
    private String nemoBvc;

    @Column(name = "valor_mercado2_a")
    private Double valorMercado2A;

    @Column(name = "valor_causacion_hoy")
    private Double valorCausacionHoy;

    @Column(name = "valor_causacion_ayer")
    private Double valorCausacionAyer;

    @Column(name = "valor_en_mda_hoy")
    private Double valorEnMdaHoy;

    @Column(name = "valor_en_mda_ayer")
    private Double valorEnMdaAyer;

    @Column(name = "prima_descto_hoy")
    private Double primaDesctoHoy;

    @Column(name = "prima_descto_ayer")
    private Double primaDesctoAyer;

    @Column(name = "intereses_lineal_ayer")
    private Double interesesLinealAyer;

    @Column(name = "dn03por1_valor_infoval")
    private String dn03por1ValorInfoval;

    @Column(name = "nit_emisor")
    private String nitEmisor;

    @Column(name = "nominal_residual")
    private String nominalResidual;

    @Column(name = "duracion_modificada")
    private String duracionModificada;

    @Column(name = "estado_garantia")
    private String estadoGarantia;

    @Column(name = "ctro_contable_alt")
    private String ctroContableAlt;

    @Column(name = "vr_patrimonio_hoy")
    private Double vrPatrimonioHoy;

    @Column(name = "vr_patrimonio_ayer")
    private Double vrPatrimonioAyer;

    @Column(name = "dn03por1_isin_bvc")
    private String dn03por1IsinBvc;

    @Column(name = "num_acc")
    private String numAcc;

    @Column(name = "clas_acc")
    private String clasAcc;

    @Column(name = "vlr_cmp_pesos")
    private Double vlrCmpPesos;

    @Column(name = "modalidad")
    private String modalidad;

    @Column(name = "tasvar_1erflujo")
    private String tasvar1erflujo;

    @Column(name = "fec_ult_rep")
    private Date fecUltRep;

    @Column(name = "vlr_presult_rep")
    private Double vlrPresultRep;

    @Column(name = "calificacion")
    private String calificacion;

    @Column(name = "ent_calificadora")
    private String entCalificadora;

    @Column(name = "cal_riesgo_emi")
    private String calRiesgoEmi;

    @Column(name = "cal_avalista")
    private String calAvalista;

    @Column(name = "nro_ident")
    private String nroIdent;

    @Column(name = "codcat_unicausa_val")
    private String codcatUnicausaVal;

    @Column(name = "caus_val")
    private Double causVal;

    @Column(name = "codcat_unicausa_pat")
    private String codcatUnicausaPat;

    @Column(name = "causa_pat")
    private String causaPat;

    @Column(name = "vlr_raz_hv")
    private Double vlrRazHv;

    @Column(name = "nro_asignado")
    private String nroAsignado;

    @Column(name = "tip_id_emisor")
    private String tipIdEmisor;

    @Column(name = "id_emisor")
    private String idEmisor;

    @Column(name = "nat_jur_emi")
    private String natJurEmi;

    @Column(name = "ciiu_emi")
    private String ciiuEmi;

    @Column(name = "vinc_emisor")
    private String vincEmisor;

    @Column(name = "prov_precios")
    private String provPrecios;

    @Column(name = "mod_orig")
    private String modOrig;

    @Column(name = "codigo_normalizado")
    private String codigoNormalizado;

    @Column(name = "cod_cli")
    private String codCli;

    @Column(name = "portafolio_front")
    private String portafolioFront;

    @Column(name = "cta_pyg")
    private String ctaPyg;

    @Column(name = "cta_pyg_prima")
    private String ctaPygPrima;

    @Column(name = "cta_pyg_rof")
    private String ctaPygRof;

    @Column(name = "correccion_monetaria")
    private String correccionMonetaria;

    @Column(name = "cta_orden_pp")
    private String ctaOrdenPp;

    @Column(name = "titulo_fraccionado_padre")
    private String tituloFraccionadoPadre;

    @Column(name = "titulo_fraccionado_original")
    private String tituloFraccionadoOriginal;

    @Column(name = "periodo")
    private String periodo;

}
