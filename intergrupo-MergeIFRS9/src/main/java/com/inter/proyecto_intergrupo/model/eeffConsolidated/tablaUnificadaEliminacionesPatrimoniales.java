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

@Table(name = "nexco_eliminaciones_tabla_unificada")

public class tablaUnificadaEliminacionesPatrimoniales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_column")
    Long id_column;

    @Column(name = "concepto")
    String concepto;

    @Column(name = "cuenta")
    String cuenta;

    @Column(name = "saldo")
    Double saldo;

    @Column(name = "fuente")
    String fuente;

    @Column(name = "periodo")
    String periodo;

    @Column(name = "fiduciaria")
    Double fiduciaria;

    @Column(name = "valores")
    Double valores;
}
