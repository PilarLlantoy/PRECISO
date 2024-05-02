package com.inter.proyecto_intergrupo.model.parametric;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_paises")
public class Country{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pais")
    private int id;

    @Column(name = "nombre_pais")
    @NotEmpty(message = "El nombre no puede estar vacio")
    private String nombre;

    @Column(name = "sigla_pais")
    private String sigla;

    @Builder.Default
    @Column(name = "estado_pais", columnDefinition = "BIT DEFAULT 1")
    private boolean estado = true;

    @Builder.Default
    @Column(name = "activo_pais", columnDefinition = "BIT DEFAULT 1")
    private boolean activo = true;

    @OneToMany(mappedBy = "pais", cascade = CascadeType.ALL)
    private List<YntpSociety> yntpSocietyList;

    @OneToMany(mappedBy = "paisContrato", cascade = CascadeType.ALL)
    private List<Contract> contractList;


}
