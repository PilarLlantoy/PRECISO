package com.inter.proyecto_intergrupo.model.parametric;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_fuentes_parametros_reportes")
public class SourceParametroReportes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fuente")
    private int id;

    @Column(name = "estado", columnDefinition = "BIT DEFAULT 1")
    private boolean estado = true;

    // Relación con parametros
    @ManyToOne
    @JoinColumn(name = "id_parametro", nullable = false)
    private ParametrosReportes parametroReportes;

    @ManyToOne
    @JoinColumn(name = "id_conciliacion", nullable = false)
    private Conciliation fuente;

    @ManyToOne
    @JoinColumn(name = "id_inventario", nullable = false)
    private ConciliationRoute inventario;

    @ManyToOne
    @JoinColumn(name = "id_evento", nullable = false)
    private EventType evento;

    // Relación OneToMany con StructureParametroReportes
    @OneToMany(mappedBy = "fuente", cascade = CascadeType.ALL)
    private List<StructureParametroReportes> estructuras;

    // Relación OneToMany con ValidationParametroReportes
    @OneToMany(mappedBy = "fuente", cascade = CascadeType.ALL)
    private List<ValidationParametroReportes> validaciones;

    // Relación OneToMany con ResultingFieldParametroReportes
    @OneToMany(mappedBy = "fuente", cascade = CascadeType.ALL)
    private List<ResultingFieldParametroReportes> camposResultantes;


}
