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
@Table(name = "preciso_metodos_consolidacion")
public class ConsolidationMethod {

    @Id
    @Column(name = "id_metodo_ifrs")
    private String id;

    @Column(name = "nombre_metodo_ifrs")
    @NotEmpty(message = "El nombre no puede estar vacio")
    private String nombre;

    @OneToMany(mappedBy = "metodo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<YntpSociety> yntpSocietyList;

}
