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
@Table(name = "nexco_plantilla_local_icrf")
public class PlantillaLocalIcrf implements Serializable {

    @Id
    @Column(name = "id_report")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReport;

    @Column(name = "periodo")
    private String periodo;

    @Column(name = "sociedad_informante")
    private String sociedadInformante;

    @Column(name = "numero_desglose")
    private String numeroDesglose;

    @Column(name = "tipo_de_registro")
    private String tipodeRegistro;

    @Column(name = "codigo_emisor")
    private String codigoEmisor;

    @Column(name = "codigo_isin")
    private String codigoISIN;

    @Column(name = "tipo_cartera")
    private String tipoCartera;

    @Column(name = "dudoso")
    private String dudoso;

    @Column(name = "codigo_emisor_valor_cubierto")
    private String codigoEmisorValorCubierto;

    @Column(name = "codigo_isin_valor_cubierto")
    private String codigoISINValorCubierto;

    @Column(name = "tipo_cartera_valor_cubierto")
    private String tipoCarteraValorCubierto;

    @Column(name = "numtitulos")
    private Integer numTitulos;

    @Column(name = "nominal")
    private Double nominal;

    @Column(name = "coste_excupon")
    private Double costeExcupon;

    @Column(name = "cupon_corrido")
    private Double cuponCorrido;

    @Column(name = "periodica_y_pac")
    private Double periodicayPAC;

    @Column(name = "plusvalias_bruto")
    private Double plusvaliasBruto;

    @Column(name = "minusvalias_bruto")
    private Double minusvaliasBruto;

    @Column(name = "microcoberturas_bruto")
    private Double microcoberturasBruto;

    @Column(name = "correcciones_de_valor_especifica")
    private Double correccionesdeValorEspecifica;

    @Column(name = "correcciones_de_valor_riesgo_pais")
    private Double correccionesdeValorRiesgoPais;

    @Column(name = "valor_contable")
    private Double valorContable;

    @Column(name = "plusvalias_neto")
    private Double plusvaliasNeto;

    @Column(name = "minusvalias_neto")
    private Double minusvaliasNeto;

    @Column(name = "microcoberturas_neto")
    private Double microcoberturasNeto;

    @Column(name = "provision_generica")
    private Double provisionGenerica;

    @Column(name = "valor_de_compra")
    private Double valordeCompra;

    @Column(name = "valor_de_mercado")
    private Double valordeMercado;

    @Column(name = "aux_3")
    private Double aux3;

    @Column(name = "aux_4")
    private Double aux4;

    @Column(name = "aux_5")
    private Double aux5;

    @Column(name = "proceso")
    private String proceso;

    @Column(name = "origen")
    private String origen;
}
