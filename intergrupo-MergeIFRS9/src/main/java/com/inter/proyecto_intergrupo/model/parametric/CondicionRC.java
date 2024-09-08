package com.inter.proyecto_intergrupo.model.parametric;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_condiciones_rc")
public class CondicionRC {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_condicion")
    private int id;

    @Column(name = "valorCondicion")
    private String valorCondicion;

    @Column(name = "estado", columnDefinition = "BIT DEFAULT 1")
    private boolean estado = true;

    // Relación con AccountingRoute
    @ManyToOne
    @JoinColumn(name = "id_rc", nullable = false)
    private AccountingRoute rutaContable;

    @ManyToOne
    @JoinColumn(name = "id_campo", nullable = false)
    private CampoRC campo;

}
