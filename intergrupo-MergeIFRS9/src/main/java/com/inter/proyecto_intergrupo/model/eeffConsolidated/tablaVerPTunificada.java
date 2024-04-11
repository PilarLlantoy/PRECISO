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

@Table(name = "nexco_ver_pt_tabla_unificada")

public class tablaVerPTunificada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_column")
    Long id_column;

    @Column(name = "concepto")
    String concepto;

    @Column(name = "cuenta")
    String cuenta;

    @Column(name = "saldo_local")
    Double saldoLocal;

    @Column(name = "requerido")
    Double requerido;

    @Column(name = "ajuste")
    Double ajuste;

    @Column(name = "fuente")
    String fuente;

    @Column(name = "periodo")
    String periodo;

    @Column(name = "moneda")
    String moneda;

}
