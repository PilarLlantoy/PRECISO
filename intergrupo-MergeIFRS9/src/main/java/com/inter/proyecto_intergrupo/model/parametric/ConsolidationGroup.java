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
@Table(name = "nexco_grupos_consolidacion")
public class ConsolidationGroup {

    @Id
    @Column(name = "id_grupo_ifrs")
    private String id;

    @Column(name = "nombre_grupo_ifrs")
    @NotEmpty(message = "El nombre no puede estar vacio")
    private String nombre;

    @OneToMany(mappedBy = "grupo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<YntpSociety> yntpSocietyList;

}
