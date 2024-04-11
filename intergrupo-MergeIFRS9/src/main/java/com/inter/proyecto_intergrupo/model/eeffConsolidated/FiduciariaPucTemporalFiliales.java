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

@Table(name = "nexco_puc_fiduciaria_filiales_temporal")

public class FiduciariaPucTemporalFiliales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_puc")
    private Long idPuc;

    @Column(name = "tipo_de_puc")
    String tipoDePuc;

    @Column(name = "clase_cuenta")
    String claseCuenta;

    @Column(name = "cuenta")
    String cuenta;

    @Column(name = "nombre_cuenta")
    String nombreCuenta;

    @Column(name = "nivel")
    String nivel;

    @Column(name = "imputable")
    String imputable;

    @Column(name = "naturaleza")
    String naturaleza;

    @Column(name = "estado_cuenta")
    String estadoCuenta;

    @Column(name = "descripcion_tipo_puc")
    String descripcionTipoPuc;

    @Column(name = "periodo")
    String periodo;

    @Column(name = "moneda")
    String moneda;

    @Column(name = "empresa")
    String empresa;

    @Column(name = "cod_cons")
    String codCons;


}
