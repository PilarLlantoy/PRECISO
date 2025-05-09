package com.inter.proyecto_intergrupo.model.parametric;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_homologacion_centros")
public class OfficeMapping {

    /*@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;*/

    @Id
    @Column(name = "centro_origen")
    private String centroOrigen;

    @Column(name = "nombre_centro")
    private String nombreCentro;

    @Column(name = "centro_destino")
    private String centroDestino;

}
