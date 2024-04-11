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
@Table(name = "nexco_pdu_icrv")
public class PduIcrv implements Serializable{

    @Id
    @Column(name = "id_pdu")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPdu;

    @Column(name = "noisin")
    private String noisin;

    @Column(name = "grupo")
    private String grupo;

    @Column(name = "entidad")
    private String entidad;

    @Column(name = "porcentaje")
    private Double porcentaje;

    @Column(name = "fechaAsamblea")
    private String fechaAsamblea;

    @Column(name = "fechaCausacion")
    private String fechaCausacion;

    @Column(name = "utilidadDelEjercicio")
    private Double utilidadDelEjercicio;

    @Column(name = "reservaNoDistribuida")
    private Double reservaNoDistribuida;

    @Column(name = "utilidadDistribuir")
    private Double utilidadDistribuir;

    @Column(name = "dividendosRecibidos")
    private Double dividendosRecibidos;

    @Column(name = "porcentajeEfectivo")
    private Double porcentajeEfectivo;

    @Column(name = "efectivo")
    private Double efectivo;

    @Column(name = "porcentajeAccion")
    private Double porcentajeAccion;

    @Column(name = "accion")
    private Double accion;

    @Column(name = "total ")
    private Double total ;

    @Column(name = "validacion")
    private Double validacion;

    @Column(name = "aplica_retfuente")
    private String aplicaRetfuente;

    @Column(name = "retencion_en_fuente")
    private Double retencionEnFuente;

    @Column(name = "valorRecibir")
    private Double valorRecibir;

    @Column(name = "fechas_de_pago1")
    private String fechasDePago1;

    @Column(name = "fechas_de_pago2")
    private String fechasDePago2;

    @Column(name = "fechas_de_pago3")
    private String fechasDePago3;

    @Column(name = "valor_dividendos_pago1")
    private Double valorDividendosPago1;

    @Column(name = "valor_dividendos_pago2")
    private Double valorDividendosPago2;

    @Column(name = "valor_dividendos_pago3")
    private Double valorDividendosPago3;

    @Column(name = "correo")
    private Double correo;

    @Column(name = "periodo")
    private String periodo;

}
