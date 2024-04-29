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
@Table(name = "nexco_tipo_documento")
public class TipoDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_documento")
    private int id;

    @Column(name = "nombre_tipo_documento")
    @NotEmpty(message = "El nombre del tipo de documento no puede estar vacio")
    @NotBlank
    @Expose
    @NotNull
    private String nombre;

    @Column(name = "codigo_tipo_documento")
    @NotEmpty(message = "El codigo del tipo de documento no puede estar vacio")
    @NotBlank
    @Expose
    @NotNull
    private String codigo;

    @Builder.Default
    @Column(name = "estado_tipo_documento", columnDefinition = "BIT DEFAULT 1")
    private boolean estado = true;

    @OneToMany(mappedBy = "tipoDocumento")
    private List<User> users;

}
