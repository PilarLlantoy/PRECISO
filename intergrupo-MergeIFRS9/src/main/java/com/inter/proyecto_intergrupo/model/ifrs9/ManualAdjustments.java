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
@Table(name = "nexco_ajustes_manuales")
public class ManualAdjustments implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "codicons")
    private String codicons;

    @Column(name = "divisa_espana")
    private String divisaEspana;

    @Column(name = "saldo")
    private Double saldo;

    @Column(name = "fuente")
    private String fuente;

    @Column(name = "periodo")
    private String periodo;

    @Column(name = "observacion")
    private String observacion;

}
