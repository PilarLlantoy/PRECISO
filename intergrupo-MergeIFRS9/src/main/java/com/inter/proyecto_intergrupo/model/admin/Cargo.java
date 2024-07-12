package com.inter.proyecto_intergrupo.model.admin;

import com.google.gson.annotations.Expose;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_administracion_cargos")
public class Cargo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cargo")
    private int id;

    @Column(name = "nombre_cargo")
    @NotEmpty(message = "El nombre del ccargo no puede estar vacio")
    @NotBlank
    @Expose
    @NotNull
    private String nombre;

    @Builder.Default
    @Column(name = "estado_cargo", columnDefinition = "BIT DEFAULT 1")
    private boolean estado = true;

    @Builder.Default
    @Column(name = "activo_cargo", columnDefinition = "BIT DEFAULT 1")
    private boolean activo = true;

    @OneToMany(mappedBy = "cargo")
    private List<User> users;

}
