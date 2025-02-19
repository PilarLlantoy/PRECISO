package com.inter.proyecto_intergrupo.model.parametric;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_plantillas_notas")
public class NoteTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "detalle")
    private String detalle;

    @ManyToOne
    @JoinColumn(name = "id_conciliacion")
    private Conciliation conciliacion;

    @ManyToOne
    @JoinColumn(name = "id_inventario_conciliacion")
    private ConciliationRoute inventarioConciliacion;

    @ManyToOne
    @JoinColumn(name = "id_matriz")
    private EventMatrix matriz;

    @ManyToOne
    @JoinColumn(name = "id_tipificacion")
    private Typification tipificacion;

    @ManyToOne
    @JoinColumn(name = "id_referencia_tercero")
    private CampoRConcil referenciaTercero;


    @Column(name = "evento")
    private String evento;

    @Column(name = "cuenta1")
    private String cuenta1;

    @Column(name = "cuenta2")
    private String cuenta2;


    @Column(name = "plantilla_libre", columnDefinition = "BIT DEFAULT 0")
    private boolean plantillaLibre = false;

    @Column(name = "estado", columnDefinition = "BIT DEFAULT 1")
    private boolean estado = true;

    /*
    @OneToMany(mappedBy = "conciliacion", cascade = CascadeType.ALL)
    private List<AccountConcil> arregloCuentas;
    */

}
