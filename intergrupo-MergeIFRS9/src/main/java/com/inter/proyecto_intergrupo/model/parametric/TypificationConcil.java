package com.inter.proyecto_intergrupo.model.parametric;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/*@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity*/

@Table(name = "preciso_tipificacion_concil")
public class TypificationConcil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    private List<Conciliation> conciliaciones;
    private Typification tipificaciones;

}
