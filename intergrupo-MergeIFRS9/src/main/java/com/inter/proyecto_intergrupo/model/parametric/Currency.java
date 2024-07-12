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
@Table(name = "preciso_divisas")
public class Currency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_divisa")
    private int id;

    @Column(name = "nombre_divisa")
    @NotEmpty(message = "El nombre no puede estar vacio")
    private String nombre;

    @Column(name = "sigla_divisa")
    @NotEmpty(message = "El nombre no puede estar vacio")
    private String sigla;

    @Builder.Default
    @Column(name = "estado_divisa", columnDefinition = "BIT DEFAULT 1")
    private boolean estado = true;

    @Builder.Default
    @Column(name = "activo_divisa", columnDefinition = "BIT DEFAULT 1")
    private boolean activo = true;


    //ELIMINAR

    @Column(name = "divisa_neocon")
    private String divisaNeocon;

    @OneToMany(mappedBy = "divisa",cascade = CascadeType.ALL)
    private List<YntpSociety> yntpSocietyList;




}
