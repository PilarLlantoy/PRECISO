package com.inter.proyecto_intergrupo.model.eeffConsolidated;

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
@Table(name = "nexco_parametria_eeff")

public class ParametricEEFF {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_parametro")
    private Long idTipoParametro;

    @Column(name = "parametro")
    private String parametro;

    @Column(name = "concepto")
    private String concepto;

    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "cuenta2")
    private String cuenta2;

    @Column(name = "porcentaje")
    private Double porcentaje;


}
