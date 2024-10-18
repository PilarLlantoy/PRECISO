package com.inter.proyecto_intergrupo.model.parametric;
import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_matriz_eventos")
public class EventMatrix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id; //Matriz

    @ManyToOne
    @JoinColumn(name = "id_conciliacion", nullable = false)
    private Conciliation conciliacion;

    @ManyToOne
    @JoinColumn(name = "id_inventario_conciliacion", nullable = false)
    private ConciliationRoute inventarioConciliacion;

    @ManyToOne
    @JoinColumn(name = "id_tipo_evento", nullable = false)
    private EventType tipoEvento;


    @ManyToOne
    @JoinColumn(name = "id_campo_centro_contable", nullable = false)
    private CampoRConcil campoCC;

    @ManyToOne
    @JoinColumn(name = "id_campo_operacion", nullable = false)
    private CampoRConcil campoOperacion;


    @Column(name = "centro_contable")
    private String centroContable;


    @Column(name = "aplica_PYG", columnDefinition = "BIT DEFAULT 1")
    private boolean PYG = true;

    @Column(name = "maneja_centro_contable", columnDefinition = "BIT DEFAULT 1")
    private boolean manejaCC = true;


    @Column(name = "hom_centros", columnDefinition = "BIT DEFAULT 1")
    private boolean homCntros = true;

    @Column(name = "estado", columnDefinition = "BIT DEFAULT 1")
    private boolean estado = true;


}
