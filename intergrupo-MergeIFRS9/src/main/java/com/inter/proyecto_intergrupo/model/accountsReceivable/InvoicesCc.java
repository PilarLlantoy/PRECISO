package com.inter.proyecto_intergrupo.model.accountsReceivable;

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
@Table(name = "preciso_facturas_cc")
public class InvoicesCc implements Serializable{

    @Id
    @Column(name = "id_factura")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFactura;

    @Column(name = "tercero")
    private String tercero;

    @Column(name = "fecha")
    private String fecha;

    @Column(name = "concepto")
    private String concepto;

    @Column(name = "persona")
    private String persona;

    @Column(name = "valor")
    private Double valor;

    @Column(name = "estado")
    private String estado;

    @Column(name = "periodo")
    private String periodo;

    @Column(name = "lote")
    private String lote;

    @Column(name = "carga_masiva")
    private String cargaMasiva;

    @Column(name = "firma")
    private Long firma;

    @Column(name = "pago")
    private Boolean pago;

    @Lob
    @Column(name = "docx", columnDefinition = "VARBINARY(MAX)")
    private byte[] docx;

}
