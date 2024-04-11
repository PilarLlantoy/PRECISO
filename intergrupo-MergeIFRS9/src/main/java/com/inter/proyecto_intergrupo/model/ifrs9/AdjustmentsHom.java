package com.inter.proyecto_intergrupo.model.ifrs9;

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
@Table(name = "nexco_ajustes_hom")
public class AdjustmentsHom implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "tipo_asiento")
    private String tipoAsiento;

    @Column(name = "descripcion_asiento")
    private String descripcionAsiento;

    @Column(name = "codicons")
    private String codicons;

    @Column(name = "divisa")
    private String divisa;

    @Column(name = "sociedad_ic")
    private String sociedadIc;

    @Column(name = "descripcion_ic")
    private String descripcionIc;

    @Column(name = "saldo_debe1")
    private Double saldoDebe1;

    @Column(name = "saldo_haber1")
    private Double saldoHaber1;

    @Column(name = "saldo_debe2")
    private Double saldoDebe2;

    @Column(name = "saldo_haber2")
    private Double saldoHaber2;

    @Column(name = "periodo")
    private String periodo;

}
