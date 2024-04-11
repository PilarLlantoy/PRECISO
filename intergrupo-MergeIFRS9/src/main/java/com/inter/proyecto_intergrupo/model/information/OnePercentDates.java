package com.inter.proyecto_intergrupo.model.information;

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
@Table(name = "nexco_fechas_porc")
public class OnePercentDates {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "mes_contable")
    String mesContable;

    @Column(name = "fechas")
    String fecha;

    @Column(name = "fecha_corte")
    String fechaCorte;

    @Column(name = "version")
    String version;

}
