package com.inter.proyecto_intergrupo.model.process;
import com.inter.proyecto_intergrupo.model.parametric.*;
import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_ajuste_libre")
public class FreeAdj {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "aplica_conciliacion")
    private boolean aplicaConciliacion;

    @ManyToOne
    @JoinColumn(name = "id_conciliacion")
    private Conciliation conciliacion;

    @Column(name = "fecha_conciliacion")
    private Date fechaConciliacion;

    @Column(name = "fecha_contabilidad")
    private Date fechaContabilidad;

    @ManyToOne
    @JoinColumn(name = "id_plantilla")
    private NoteTemplate plantilla;

    @Column(name = "estado", columnDefinition = "BIT DEFAULT 0")
    private boolean estado = false;

    @Column(name = "usuario")
    private String usuario;

    @Column(name = "ajuste")
    private double ajuste;

    @Column(name = "semaforo", columnDefinition = "BIT DEFAULT 0")
    private boolean semaforo = false;

}
