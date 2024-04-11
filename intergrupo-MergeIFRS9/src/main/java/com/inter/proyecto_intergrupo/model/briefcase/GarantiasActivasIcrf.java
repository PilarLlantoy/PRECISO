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
@Table(name = "nexco_garantias_activas_icrf")
public class GarantiasActivasIcrf implements Serializable{

    @Id
    @Column(name = "id_report")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReport;

    @Column(name = "cod_emp")
    private String codEmp;

    @Column(name = "portafolio")
    private String portafolio;

    @Column(name = "negocio")
    private String negocio;

    @Column(name = "fecha")
    private Date fecha;

    @Column(name = "fecha_final")
    private Date fechaFinal;

    @Column(name = "estado")
    private String estado;

    @Column(name = "operacion")
    private String operacion;

    @Column(name = "nro_papeleta")
    private String nroPapeleta;

    @Column(name = "seguimiento")
    private String seguimiento;

    @Column(name = "acceso")
    private String acceso;

    @Column(name = "origen")
    private String origen;

    @Column(name = "moneda")
    private String moneda;

    @Column(name = "emisor")
    private String emisor;

    @Column(name = "tipo")
    private String tipo;

    @Column(name = "nro_titulo")
    private String nroTitulo;

    @Column(name = "valor_nominal")
    private Double valorNominal;

    @Column(name = "tasa_nominal")
    private Double tasaNominal;

    @Column(name = "pago_interes")
    private String pagoInteres;

    @Column(name = "modalidad")
    private String modalidad;

    @Column(name = "tipo_interes")
    private String tipoInteres;

    @Column(name = "pago_capital")
    private String pagoCapital;

    @Column(name = "pago_dia")
    private String pagoDia;

    @Column(name = "fecha_emision")
    private Date fechaEmision;

    @Column(name = "fecha_vcto")
    private Date fechaVcto;

    @Column(name = "fecha_compra")
    private Date fechaCompra;

    @Column(name = "fecha_val")
    private Date fechaVal;

    @Column(name = "sw_val_tir")
    private String swValTir;

    @Column(name = "sw_360_comp")
    private String sw360Comp;

    @Column(name = "sw_360_vlr")
    private String sw360Vlr;

    @Column(name = "sw_met_lin")
    private String swMetLin;

    @Column(name = "sw_met_mar")
    private String swMetMar;

    @Column(name = "tasa_basica")
    private String tasaBasica;

    @Column(name = "tipo_emisor")
    private String tipoEmisor;

    @Column(name = "sw_margen")
    private String swMargen;

    @Column(name = "nemo_bvc")
    private String nemoBvc;

    @Column(name = "valor_compra")
    private Double valorCompra;

    @Column(name = "valor_dia")
    private Double valorDia;

    @Column(name = "real_360")
    private Double real360;

    @Column(name = "real_365")
    private Double real365;

    @Column(name = "tir_360")
    private Double tir360;

    @Column(name = "tir_365")
    private Double tir365;

    @Column(name = "margen")
    private Double margen;

    @Column(name = "t360_tre")
    private Double t360Tre;

    @Column(name = "t365_tre")
    private Double t365Tre;

    @Column(name = "valor_mercado")
    private Double valorMercado;

    @Column(name = "valor_vcto")
    private Double valorVcto;

    @Column(name = "plazo")
    private String plazo;

    @Column(name = "dias_vcto")
    private String diasVcto;

    @Column(name = "dias_int")
    private String diasInt;

    @Column(name = "intereses_cobrar")
    private String interesesCobrar;

    @Column(name = "plaza_expedicion")
    private String plazaExpedicion;

    @Column(name = "plazo_rango")
    private String plazoRango;

    @Column(name = "cod_cli")
    private String codCli;

    @Column(name = "aleatorio")
    private String aleatorio;

    @Column(name = "tir_margen")
    private String tirMargen;

    @Column(name = "tasa_basica_marg")
    private String tasaBasicaMarg;

    @Column(name = "estado_anterior")
    private String estadoAnterior;

    @Column(name = "autorizacion")
    private String autorizacion;

    @Column(name = "pap_ant")
    private String papAnt;

    @Column(name = "cod_titulo")
    private String codTitulo;

    @Column(name = "nro_derecho")
    private String nroDerecho;

    @Column(name = "tipo_derecho")
    private String tipoDerecho;

    @Column(name = "llave_titulo")
    private String llaveTitulo;

    @Column(name = "mercado_rtefte")
    private String mercadoRtefte;

    @Column(name = "inicio_flujo")
    private Date inicioFlujo;

    @Column(name = "fin_flujo")
    private Date finFlujo;

    @Column(name = "valor_flujo")
    private Double valorFlujo;

    @Column(name = "valor_autoretencion")
    private String valorAutoretencion;

    @Column(name = "valor_ret_trasladada")
    private String valorRetTrasladada;

    @Column(name = "valor_ret_por_pagar")
    private String valorRetPorPagar;

    @Column(name = "valor_ret_comision")
    private String valorRetComision;

    @Column(name = "sw_genera_plano")
    private String swGeneraPlano;

    @Column(name = "moneda_compra")
    private Double monedaCompra;

    @Column(name = "moneda_emision")
    private Double monedaEmision;

    @Column(name = "fec_cumplimiento")
    private Date fecCumplimiento;

    @Column(name = "vlr_futuro")
    private Double vlrFuturo;

    @Column(name = "intereses_futuros")
    private String interesesFuturos;

    @Column(name = "vlr_compromiso")
    private String vlrCompromiso;

    @Column(name = "oper_cubierta")
    private String operCubierta;

    @Column(name = "rend_papel_fut")
    private Double rendPapelFut;

    @Column(name = "vlr_riesgo_ayer")
    private Double vlrRiesgoAyer;

    @Column(name = "vlr_riesgo_hoy")
    private Double vlrRiesgoHoy;

    @Column(name = "tir_papel")
    private Double tirPapel;

    @Column(name = "fec_val_riesgo")
    private Date fecValRiesgo;

    @Column(name = "dias_futuros")
    private String diasFuturos;

    @Column(name = "ajuste_indice")
    private Double ajusteIndice;

    @Column(name = "vp_titulo")
    private Double vpTitulo;

    @Column(name = "vp_compromiso")
    private Double vpCompromiso;

    @Column(name = "vp_titulo_ayer")
    private Double vpTituloAyer;

    @Column(name = "vp_compromiso_ayer")
    private Double vpCompromisoAyer;

    @Column(name = "pap_cruzada")
    private String papCruzada;

    @Column(name = "fecha1")
    private Date fecha1;

    @Column(name = "negocio1")
    private String negocio1;

    @Column(name = "carrusel")
    private String carrusel;

    @Column(name = "usr_actualiza_remate")
    private String usrActualizaRemate;

    @Column(name = "valor_total")
    private Double valorTotal;

    @Column(name = "causacion_neta")
    private Double causacionNeta;

    @Column(name = "tipo_op_mdo")
    private String tipoOpMdo;

    @Column(name = "fec_val_riesgo1")
    private Date fecValRiesgo1;

    @Column(name = "isin_star")
    private String isinStar;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "nombre_portafolio")
    private String nombrePortafolio;

    @Column(name = "nro_identificacion")
    private String nroIdentificacion;

    @Column(name = "ctro_contable_alt")
    private String ctroContableAlt;

    @Column(name = "tipo_entidad")
    private String tipoEntidad;

    @Column(name = "nro_ope_origen")
    private String nroOpeOrigen;

    @Column(name = "c")
    private String c;

    @Column(name = "n")
    private String n;

    @Column(name = "isin_star1")
    private String isinStar1;

    @Column(name = "nom_emisor")
    private String nomEmisor;

    @Column(name = "cta_orden")
    private String ctaOrden;

    @Column(name = "valor_cupon")
    private String valorCupon;

    @Column(name = "periodo")
    private String periodo;

}
