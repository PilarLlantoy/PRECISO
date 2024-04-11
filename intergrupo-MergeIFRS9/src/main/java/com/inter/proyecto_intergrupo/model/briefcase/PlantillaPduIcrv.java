package com.inter.proyecto_intergrupo.model.briefcase;

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
@Table(name = "nexco_pdu_plantilla_icrv")
public class PlantillaPduIcrv implements Serializable{

    @Id
    @Column(name = "id_pdu")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPdu;

    @Column(name = "noisin")
    private String noisin;

    @Column(name = "grupo")
    private String grupo;

    @Column(name = "entidad")
    private String entidad;

}
