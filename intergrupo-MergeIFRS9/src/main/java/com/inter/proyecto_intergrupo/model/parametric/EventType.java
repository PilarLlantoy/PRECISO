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
@Table(name = "preciso_tipo_evento")
public class EventType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_evento")
    private int id;

    @Column(name = "nombre_tipo_evento")
    @NotEmpty(message = "El nombre no puede estar vacio")
    private String nombre;

    @Builder.Default
    @Column(name = "estado_tipo_evento", columnDefinition = "BIT DEFAULT 1")
    private boolean estado = true;

    @Builder.Default
    @Column(name = "activo_tipo_evento", columnDefinition = "BIT DEFAULT 1")
    private boolean activo = true;


}
