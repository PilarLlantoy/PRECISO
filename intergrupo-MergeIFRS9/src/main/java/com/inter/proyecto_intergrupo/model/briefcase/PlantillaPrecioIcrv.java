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
@Table(name = "nexco_plantilla_precio_icrv")
public class PlantillaPrecioIcrv implements Serializable{

    @Id
    @Column(name = "id_precio")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPrecio;

    @Column(name = "metodo")
    private String metodo;

    @Column(name = "empresa")
    private String empresa;

}
