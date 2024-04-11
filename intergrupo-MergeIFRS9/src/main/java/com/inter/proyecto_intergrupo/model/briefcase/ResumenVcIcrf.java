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
@Table(name = "nexco_resumen_vc_icrf")
public class ResumenVcIcrf implements Serializable{

    @Id
    @Column(name = "id_report")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReport;

    @Column(name = "contabilidad")
    private String contabilidad;

    @Column(name = "isin_star")
    private String isinStar;

    @Column(name = "isin_new")
    private String isinNew;

    @Column(name = "vlr_libros")
    private Double vlrLibros;

    @Column(name = "conteo")
    private Integer conteo;

    @Column(name = "coste_excupon")
    private Double costeExcupon;

    @Column(name = "valor_nominal")
    private Double valorNominal;

    @Column(name = "valor_compra")
    private Double valorCompra;

    @Column(name = "valor_mercado")
    private Double valorMercado;

    @Column(name = "convertido_nominal")
    private Double convertidoNominal;

    @Column(name = "convertido_compra")
    private Double convertidoCompra;

    @Column(name = "reporte_nominal")
    private Double reporteNominal;

    @Column(name = "reporte_compra")
    private Double reporteCompra;

    @Column(name = "reporte_mercado")
    private Double reporteMercado;

    @Column(name = "periodo")
    private String periodo;

}
