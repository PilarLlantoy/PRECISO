package com.inter.proyecto_intergrupo.model.eeffConsolidated;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_eliminaciones_version_inicial_cuadre_general")

public class EliminacionesVersionInicialCuadreGeneral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nombre")
    Long id_nombre;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "concepto")
    private String concepto;

    @Column(name = "plantilla_banco")
    private Double plantillaBanco;

    @Column(name = "plantilla_filial")
    private Double plantillaFilial;

    @Column(name = "ajuste")
    private Double ajuste;

    @Column(name = "total_general")
    private Double totalGeneral;

    @Column(name = "periodo")
    private String periodo;
}