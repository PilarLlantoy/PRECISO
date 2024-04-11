package com.inter.proyecto_intergrupo.model.briefcase;

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
@Table(name = "nexco_balvalores_icrv")
public class BalvaloresIcrv implements Serializable{

    @Id
    @Column(name = "id_bal")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idBal;

    @Column(name = "mes")
    private String mes;

    @Column(name = "cuenta_niif")
    private String cuentaNiif;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "moneda_total")
    private Double monedaTotal;

    @Column(name = "periodo")
    private String periodo;

}
