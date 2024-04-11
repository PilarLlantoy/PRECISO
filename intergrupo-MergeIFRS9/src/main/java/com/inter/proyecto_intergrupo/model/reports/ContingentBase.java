package com.inter.proyecto_intergrupo.model.reports;

import com.inter.proyecto_intergrupo.model.parametric.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_base_contingentes")
public class ContingentBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contingente")
    private Long idContingentes;

    @Column(name = "contrato")
    private String contrato;

    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "fecha_cierre")
    private Date fechaCierre;

    @Column(name = "divisa")
    private String divisa;

    @Column(name = "nit")
    private String nit;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "importe")
    private Double importe;

    @Column(name = "TP")
    private String tp;

    @Column(name = "DV")
    private String dv;

    @Column(name = "periodo")
    private String periodo;

    @Column(name = "origen")
    private String origen;

}

