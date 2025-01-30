package com.inter.proyecto_intergrupo.model.parametric;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;


@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_campos_param_reportes")
public class CampoParamReportes implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_campo")
    private int id;

    @Column(name = "detalle")
    @NotEmpty(message = "El detalle no puede estar vacio")
    private String detalle;

    @Builder.Default
    @Column(name = "filtrado", columnDefinition = "BIT DEFAULT 0")
    private boolean filtrado = false;

    @Builder.Default
    @Column(name = "primario", columnDefinition = "BIT DEFAULT 0")
    private boolean primario = false;

    @Builder.Default
    @Column(name = "adicion", columnDefinition = "BIT DEFAULT 0")
    private boolean adicion = false;

    @Column(name = "tipo")
    private String tipo;

    @Builder.Default
    @Column(name = "fuente_adicion", columnDefinition = "BIT DEFAULT 0")
    private boolean fuenteAdicion = false;

    @Column(name = "longitud")
    private String longitud;

    @Column(name = "tipo_filtro")
    private String tipoFiltro;

    @Column(name = "orden")
    private String orden;

    @Column(name = "estado", columnDefinition = "BIT DEFAULT 1")
    private boolean estado = true;


    // Relación con ConciliationRoute
    @ManyToOne
    @JoinColumn(name = "id_parametro", nullable = false)
    private ParametrosReportes parametroReportes;

}
