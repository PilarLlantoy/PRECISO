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
@Table(name = "nexco_calculo_plantilla_icrv")
public class PlantillaCalculoIcrv implements Serializable{

    @Id
    @Column(name = "id_calculo")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCalculo;

    @Column(name = "valoracion")
    private String valoracion;

    @Column(name = "empresa")
    private String empresa;

    @Column(name = "nit")
    private String nit;

    @Column(name = "dv")
    private String dv;

    @Column(name = "isin")
    private String isin;

    @Column(name = "participacion")
    private Double participacion;

    @Column(name = "vr_accion")
    private Double vrAccion;

    @Column(name = "no_acciones")
    private Double noAcciones;

}
