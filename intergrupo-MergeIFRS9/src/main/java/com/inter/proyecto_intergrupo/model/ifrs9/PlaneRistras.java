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
@Table(name = "nexco_plano_ristras")
public class PlaneRistras {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plano")
    private Long idPlano;

    @Column(name = "banco")
    private String banco;

    @Column(name = "interfaz")
    private String interfaz;

    @Column(name = "cuenta_definitiva")
    private String cuentaDefinitiva;

    @Column(name = "producto")
    private String producto;

    @Column(name = "tipo_de_cartera")
    private String tipoDeCartera;

    @Column(name = "campo12")
    private String campo12;

    @Column(name = "calificacion")
    private String calificacion;

    @Column(name="campo14")
    private String campo14;

    @Column(name = "codigo_sector")
    private String codigoSector;

    @Column(name = "codigo_subsector")
    private String codigoSubsector;

    @Column(name = "forma_de_pago")
    private String formaDePago;

    @Column(name = "linea_de_credito")
    private String lineaDeCredito;

    @Column(name="entid_redescuento")
    private String entidRedescuento;

    @Column(name = "morosidad")
    private String morosidad;

    @Column(name = "tipo_inversion")
    private String tipoInversion;

    @Column(name="tipo_de_gasto")
    private String tipoDeGasto;

    @Column(name = "concepto_contable")
    private String conceptoContable;

    @Column(name = "divisa")
    private String divisa;

    @Column(name = "tipo_moneda")
    private String tipoMoneda;

    @Column(name = "filler")
    private String filler;

    @Column(name = "varios")
    private String varios;

    @Column(name = "valor")
    private String valor;

    @Column(name = "sagrupas")
    private String sagrupas;

}
