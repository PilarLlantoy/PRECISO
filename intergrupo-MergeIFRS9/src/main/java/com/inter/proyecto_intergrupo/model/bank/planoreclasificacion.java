package com.inter.proyecto_intergrupo.model.bank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_plano_reclasificacion")
@IdClass(planoreclasificacion.class)
public class planoreclasificacion implements Serializable {
    @Id
    @Column(name = "Contrato")
    private String contrato;

    @Id
    @Column(name = "Periodo")
    private String periodo;

    @Column(name = "Empresa")
    private String empresa;

    @Column(name = "Cta_anterior")
    private String ctaanterior;

    @Column(name = "Cta_nueva")
    private String ctanueva;
}
