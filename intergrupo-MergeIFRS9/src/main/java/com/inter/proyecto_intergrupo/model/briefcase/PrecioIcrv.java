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
@Table(name = "nexco_precio_icrv")
public class PrecioIcrv implements Serializable{

    @Id
    @Column(name = "id_precio")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPrecio;

    @Column(name = "metodo")
    private String metodo;

    @Column(name = "fecha_contable")
    private String fechaContable;

    @Column(name = "empresa")
    private String empresa;

    @Column(name = "precio_valoracion")
    private Double precioValoracion;

    @Column(name = "patrimonio")
    private Double patrimonio;

    @Column(name = "acciones")
    private Double acciones;

    @Column(name = "vr_intrinseco")
    private Double vrIntrinseco;

    @Column(name = "ori")
    private Double ori;

    @Column(name = "fecha_recibo")
    private String fechaRecibo;

    @Column(name = "fecha_actualizacion")
    private Date fechaActualizacion;

    @Column(name = "resultado")
    private String resultado;

    @Column(name = "isin")
    private String isin;

    @Column(name = "periodo")
    private String periodo;

}
