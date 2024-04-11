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
    @Column(name = "id_pais")
    private String id;

    @Column(name = "nombre_pais")
    @NotEmpty(message = "El nombre no puede estar vacio")
    private String nombre;

    @OneToMany(mappedBy = "pais", cascade = CascadeType.ALL)
    private List<YntpSociety> yntpSocietyList;

    @OneToMany(mappedBy = "paisContrato", cascade = CascadeType.ALL)
    private List<Contract> contractList;


}
