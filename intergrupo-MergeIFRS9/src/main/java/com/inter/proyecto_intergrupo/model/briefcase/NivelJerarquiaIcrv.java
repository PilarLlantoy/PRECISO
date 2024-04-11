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
@Table(name = "nexco_nivel_jerarquia_icrv")
public class NivelJerarquiaIcrv implements Serializable{

    @Id
    @Column(name = "id_nivel")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNivel;

    @Column(name = "cuenta_contable_inversion")
    private String cuentaContableInversion;

    @Column(name = "cuenta_contable_valorizacion")
    private String cuentaContableValorizacion;

    @Column(name = "empresa")
    private String empresa;

    @Column(name = "fecha_de_adquision")
    private String fechaDeAdquision;

    @Column(name = "nit")
    private String nit;

    @Column(name = "isin")
    private String isin;

    @Column(name = "porcentaje_participacion")
    private Double porcentajeParticipacion;

    @Column(name = "acciones_en_circulacion")
    private Double accionesEnCirculacion;

    @Column(name = "acciones_que_posee_bbva")
    private Double accionesQuePoseeBbva;

    @Column(name = "capital")
    private Double capital;

    @Column(name = "val_patrimonial")
    private Double valPatrimonial;

    @Column(name = "val_nominal_accion")
    private Double valNominalAccion;

    @Column(name = "nominal")
    private Double nominal;

    @Column(name = "no_acciones")
    private Double noAcciones;

    @Column(name = "vr_mercado_inver")
    private Double vrMercadoInver;

    @Column(name = "saldo_inversion")
    private Double saldoInversion;

    @Column(name = "saldo_valoracion")
    private Double saldoValoracion;

    @Column(name = "vr_mercado_inver2")
    private Double vrMercadoInver2;

    @Column(name = "vr_intrinseco_de_la_accion")
    private Double vrIntrinsecoDeLaAccion;

    @Column(name = "corte")
    private String corte;

    @Column(name = "corte_de_eeff")
    private String corteDeEeff;

    @Column(name = "metodo_de_valoracion")
    private String metodoDeValoracion;

    @Column(name = "periodo")
    private String periodo;

}
