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
@Table(name = "preciso_tipificacion")
public class Typification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipificacion")
    private Long id;

    @Column(name = "detalle_tipificacion")
    private String detalle;

    @Builder.Default
    @Column(name = "estado_tipificacion", columnDefinition = "BIT DEFAULT 1")
    private boolean estado = true;

    @Column(name = "aplica_concil")
    private boolean aplicaConcil;



}
