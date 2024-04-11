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
@Table(name = "nexco_val_ifrs9_provrec_temp")
public class ValIFRS9ProvRecTemp implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name="codicons")
    private String codicons;

    @Column(name="divisa")
    private String divisa;

    @Column(name="sdo_prov")
    private BigDecimal sdoProv;

    @Column(name="sdo_rec")
    private BigDecimal sdoRec;
}