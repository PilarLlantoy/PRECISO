package com.inter.proyecto_intergrupo.model.parametric;
import lombok.*;

import javax.persistence.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "preciso_cruces_ruta_conciliacion")
public class CrossesConcilRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cruce")
    private int id;

    @Column(name = "estado", columnDefinition = "BIT DEFAULT 1")
    private boolean estado = true;

    // Relación con ConciliationRoute
    @ManyToOne
    @JoinColumn(name = "id_RC_inventario", nullable = false)
    private ConciliationRoute inventario;

    //Campo de la ruta conciliacion asociada
    @ManyToOne
    @JoinColumn(name = "id_campo_actualiza", nullable = false)
    private CampoRConcil campoInvActualiza;

    //Campo de la ruta conciliacion asociada
    @ManyToOne
    @JoinColumn(name = "id_campo_inventario_valid", nullable = false)
    private CampoRConcil campoInvValid;

    // Relación con ConciliationRoute si es fichero
    @ManyToOne
    @JoinColumn(name = "id_RC_fichero", nullable = false)
    private ConciliationRoute fichero;

    //Campo del fichero asociado
    @ManyToOne
    @JoinColumn(name = "id_campo_fichero_validacion", nullable = false)
    private CampoRConcil campoFicValid;
    @ManyToOne

    //Campo del fichero asociado
    @JoinColumn(name = "id_campo_fichero_resultado", nullable = false)
    private CampoRConcil campoFicResul;

}
