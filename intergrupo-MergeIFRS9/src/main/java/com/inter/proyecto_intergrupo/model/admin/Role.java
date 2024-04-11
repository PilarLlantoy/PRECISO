package com.inter.proyecto_intergrupo.model.admin;

import com.google.gson.annotations.Expose;
import lombok.*;
import org.springframework.boot.context.properties.bind.DefaultValue;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

import static javax.persistence.GenerationType.SEQUENCE;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_perfiles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perfil")
    private int id;

    @Column(name = "nombre_perfil")
    @NotEmpty(message = "El nombre del perfil no puede estar vacio")
    @NotBlank
    @Expose
    @NotNull
    private String nombre;

    // NUEVOOO
    @Builder.Default
    @Column(name = "estado_perfil", columnDefinition = "BIT DEFAULT 1")
    private boolean estado = true;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @ToString.Exclude
    @JoinTable(name="nexco_rol_vista", joinColumns = @JoinColumn(name="id_perfil"), inverseJoinColumns = @JoinColumn(name = "id_vista"))
    private List<View> vistas;

}
