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
@Table(name = "nexco_eliminaciones_cuadre_general_detalle")

public class EliminacionesVersionInicialDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nombre")
    Long id_nombre;

    @Column(name = "id")
    private String id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "concepto")
    private String concepto;

    @Column(name = "cuenta_local")
    private String cuentaLocal;

    @Column(name = "valor")
    private Double valor;

    @Column(name = "yntp")
    private String yntp;

    @Column(name = "l")
    private String l;

    @Column(name = "abs")
    private Double abs;

    @Column(name = "nat")
    private String nat;

    @Column(name = "periodo")
    private String periodo;
}
