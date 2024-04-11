package com.inter.proyecto_intergrupo.model.ifrs9;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_val_ifrs9")
public class ValIFRS9 implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name="codicons")
    private String codicons;

    @Column(name="divisa")
    private String divisa;

    @Column(name="perimetro")
    private String perimetro;

    @Column(name="sdo_prov")
    private BigDecimal sdoProv;

    @Column(name="sdo_rec")
    private BigDecimal sdoRec;

    @Column(name="sdo_pri")
    private BigDecimal sdoPri;

    @Column(name="sdo_recla")
    private BigDecimal sdoRecla;

    @Column(name="sdo_aj")
    private BigDecimal sdoAj;

    @Column(name="sdo_total_plantilla")
    private BigDecimal sdoTotalPlantilla;

    @Column(name="naturaleza_total")
    private String naturalezaTotal;

    @Column(name="sdo_eeff_loc")
    private BigDecimal sdoEeffLoc;

    @Column(name="sdo_query_loc")
    private BigDecimal sdoQueryLoc;

    @Column(name="sdo_diff_loc")
    private BigDecimal sdoDiffLoc;

    @Column(name="sdo_nuevo")
    private BigDecimal sdoNuevo;

    @Column(name="sdo_eeff_ifrs9")
    private BigDecimal sdoEeffIfrs9;

    @Column(name="sdo_query_ifrs9")
    private BigDecimal sdoQueryIfrs9;

    @Column(name="sdo_diff_query")
    private BigDecimal sdoDiffQuery;

    @Column(name="sdo_diff_eeff")
    private BigDecimal sdoDiffEeff;

    @Column(name="periodo")
    private String periodo;
}