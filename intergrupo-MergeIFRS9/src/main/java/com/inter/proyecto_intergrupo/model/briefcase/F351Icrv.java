package com.inter.proyecto_intergrupo.model.briefcase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_f351_icrv")
public class F351Icrv implements Serializable{

    @Id
    @Column(name = "id_f")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idF;

    @Column(name = "fecha_proceso")
    private String fechaProceso;

    @Column(name = "nro_asignado")
    private String nroAsignado;

    @Column(name = "codigo_puc")
    private String codigoPuc;

    @Column(name = "nit")
    private String nit;

    @Column(name = "documento_emisor")
    private String documentoEmisor;

    @Column(name = "razon_social_emisor")
    private String razonSocialEmisor;

    @Column(name = "vinculado")
    private String vinculado;

    @Column(name = "aval")
    private String aval;

    @Column(name = "tipo_identificacion_aval")
    private String tipoIdentificacionAval;

    @Column(name = "identificacion_aval")
    private String identificacionAval;

    @Column(name = "razon_social_aval")
    private String razonSocialAval;

    @Column(name = "identifacion_administrador")
    private String identifacionAdministrador;

    @Column(name = "razon_social_administrador")
    private String razonSocialAdministrador;

    @Column(name = "clase_inversion")
    private String claseInversion;

    @Column(name = "nemotecnico")
    private String nemotecnico;

    @Column(name = "cupon_principal")
    private String cuponPrincipal;

    @Column(name = "fecha_emite")
    private String fechaEmite;

    @Column(name = "fecha_vcto")
    private String fechaVcto;

    @Column(name = "fecha_vcto_cupon")
    private String fechaVctoCupon;

    @Column(name = "fecha_compra")
    private String fechaCompra;

    @Column(name = "cod_moneda")
    private String codMoneda;

    @Column(name = "valor_nominal")
    private Double valorNominal;

    @Column(name = "amortizaciones")
    private String amortizaciones;

    @Column(name = "valor_nominal_capitalizado")
    private Double valorNominalCapitalizado;

    @Column(name = "numero_acciones")
    private Double numeroAcciones;

    @Column(name = "clase_accion")
    private String claseAccion;

    @Column(name = "valor_compra")
    private Double valorCompra;

    @Column(name = "valor_compra_pesos")
    private Double valorCompraPesos;

    @Column(name = "tasa_facial")
    private String tasaFacial;

    @Column(name = "valor_tasa")
    private Double valorTasa;

    @Column(name = "calculo_interes")
    private String calculoInteres;

    @Column(name = "periodicidad_pago")
    private String periodicidadPago;

    @Column(name = "modalidad")
    private String modalidad;

    @Column(name = "ind_tasa_referencia")
    private String indTasaReferencia;

    @Column(name = "valor_mercado_1316")
    private Double valorMercado1316;

    @Column(name = "valor_presente_pesos")
    private Double valorPresentePesos;

    @Column(name = "valor_mercado_dif_peso")
    private Double valorMercadoDifPeso;

    @Column(name = "tasa_negociacion")
    private String tasaNegociacion;

    @Column(name = "dias_vcto")
    private String diasVcto;

    @Column(name = "tasa_referencia")
    private String tasaReferencia;

    @Column(name = "valor_tasa_referencia")
    private Double valorTasaReferencia;

    @Column(name = "valor_tasa_primer_flujo")
    private Double valorTasaPrimerFlujo;

    @Column(name = "margen_valora")
    private String margenValora;

    @Column(name = "tasa_descuento")
    private String tasaDescuento;

    @Column(name = "precio")
    private String precio;

    @Column(name = "metodo_valora")
    private Double metodoValora;

    @Column(name = "fecha_ultimo_reprecio")
    private String fechaUltimoReprecio;

    @Column(name = "valor_presente_reprecio")
    private Double valorPresenteReprecio;

    @Column(name = "ind_bursatibilidad")
    private String indBursatibilidad;

    @Column(name = "interes_vencidos")
    private String interesVencidos;

    @Column(name = "puc_provision")
    private String pucProvision;

    @Column(name = "base_provision")
    private String baseProvision;

    @Column(name = "valor_provision")
    private Double valorProvision;

    @Column(name = "calificacion")
    private String calificacion;

    @Column(name = "entidad_calificadora")
    private String entidadCalificadora;

    @Column(name = "calificacion_riesgo")
    private String calificacionRiesgo;

    @Column(name = "calificacion_avalista")
    private String calificacionAvalista;

    @Column(name = "calificacion_soberania")
    private String calificacionSoberania;

    @Column(name = "entidad_calificadora_soberania")
    private String entidadCalificadoraSoberania;

    @Column(name = "custodio")
    private String custodio;

    @Column(name = "numero_identificacion")
    private String numeroIdentificacion;

    @Column(name = "fungible")
    private String fungible;

    @Column(name = "monto_emision")
    private String montoEmision;

    @Column(name = "porcentaje_participacion")
    private String porcentajeParticipacion;

    @Column(name = "ramo")
    private String ramo;

    @Column(name = "relacion_matrix")
    private Double relacionMatrix;

    @Column(name = "concentracion_propiedad")
    private String concentracionPropiedad;

    @Column(name = "relacion_vinculacion")
    private String relacionVinculacion;

    @Column(name = "codigo_puc_causacion")
    private String codigoPucCausacion;

    @Column(name = "causacion_valoracion")
    private String causacionValoracion;

    @Column(name = "codigo_puc_causa_pat")
    private String codigoPucCausaPat;

    @Column(name = "causa_valoracion_pat")
    private String causaValoracionPat;

    @Column(name = "fecha_corte")
    private String fechaCorte;

    @Column(name = "unidad_captura")
    private String unidadCaptura;

    @Column(name = "cod_emp")
    private String codEmp;

    @Column(name = "portafolio")
    private String portafolio;

    @Column(name = "tipo_evaluacion")
    private String tipoEvaluacion;

    @Column(name = "tipo_fideicomiso")
    private String tipoFideicomiso;

    @Column(name = "cod_fideicomiso")
    private String codFideicomiso;

    @Column(name = "tipo_entidad_vig")
    private String tipoEntidadVig;

    @Column(name = "cod_entidad_vig")
    private String codEntidadVig;

    @Column(name = "valor_valorizacion")
    private String valorValorizacion;

    @Column(name = "valor_desvaloriza")
    private String valorDesvaloriza;

    @Column(name = "fecha_tasa_ref")
    private String fechaTasaRef;

    @Column(name = "numero_asignado")
    private String numeroAsignado;

    @Column(name = "vlr_mercado_inv")
    private String vlrMercadoInv;

    @Column(name = "negocio_dn02")
    private String negocioDn02;

    @Column(name = "operación_futuro")
    private String operacionFuturo;

    @Column(name = "valoracion_portafolio")
    private String valoracionPortafolio;

    @Column(name = "tipo_titulo")
    private String tipoTitulo;

    @Column(name = "isin_star")
    private String isinStar;

    @Column(name = "registro_manual")
    private String registroManual;

    @Column(name = "ciiu")
    private String ciiu;

    @Column(name = "naturaleza_jurídica")
    private String naturalezaJuridica;

    @Column(name = "vinculación")
    private String vinculacion;

    @Column(name = "proveedor_de_precios")
    private String proveedorDePrecios;


    @Column(name = "periodo")
    private String periodo;

}
