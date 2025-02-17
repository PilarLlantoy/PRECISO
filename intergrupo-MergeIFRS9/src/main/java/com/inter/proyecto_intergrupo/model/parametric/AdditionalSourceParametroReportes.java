package com.inter.proyecto_intergrupo.model.parametric;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_fuentes_adicionales_parametros_reportes")
public class AdditionalSourceParametroReportes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fuente")
    private int id;

    @Column(name = "estado", columnDefinition = "BIT DEFAULT 1")
    private boolean estado = true;

    @Column(name = "obliga_relacion", columnDefinition = "BIT DEFAULT 0")
    private boolean obligaRelacion = false;

    @Column(name = "tipo_fuente")
    private String tipoFuente;

    // Relación con AccountingRoute
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

}
