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

    @Column(name = "conciliacion")
    private String conciliacion;

    @Column(name = "inventario_conciliacion")
    private String inventarioConciliacion;

    @ManyToOne
    @JoinColumn(name = "id_tipo_evento", nullable = false)
    private EventType tipoEvento;

    @Column(name = "operacion")
    private String operacion;

    @Column(name = "aplica_PYG", columnDefinition = "BIT DEFAULT 1")
    private boolean PYG = true;

    @Column(name = "maneja_centro_contable", columnDefinition = "BIT DEFAULT 1")
    private boolean manejaCC = true;

    @Column(name = "campo_centro_contable")
    private String campoCC;

    @Column(name = "centro_contable")
    private String centroContable;

    @Column(name = "hom_centros", columnDefinition = "BIT DEFAULT 1")
    private boolean homCntros = true;

    @Column(name = "activo", columnDefinition = "BIT DEFAULT 1")
    private boolean activo = true;

    @Column(name = "estado", columnDefinition = "BIT DEFAULT 1")
    private boolean estado = true;


}
