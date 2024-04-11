package com.inter.proyecto_intergrupo.model.eeffConsolidated;

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
@Table(name = "nexco_parametria_ajustes_mayores_eeff")

public class ParametricAjustesMayoresEEFF {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_parametro")
    private Long idTipoParametro;

    @Column(name = "l_4")
    private String l_4;

    @Column(name = "l_9")
    private String l_9;

    @Column(name = "entidad")
    private String entidad;

    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "concepto")
    private String concepto;

    @Column(name = "moneda")
    private String moneda;

    @Column(name = "saldo")
    private Double saldo;

    @Column(name = "debe")
    private Double debe;

    @Column(name = "haber")
    private Double haber;

    @Column(name = "periodo")
    private String periodo;

}
