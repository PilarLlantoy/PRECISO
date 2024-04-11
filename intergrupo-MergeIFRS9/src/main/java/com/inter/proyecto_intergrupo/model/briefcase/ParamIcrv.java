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
@Table(name = "nexco_param_icrv")
public class ParamIcrv implements Serializable{

    @Id
    @Column(name = "id_param")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idParam;

    @Column(name = "categoria")
    private String categoria;

    @Column(name = "valor")
    private String valor;

    @Column(name = "valor2")
    private String valor2;

}
