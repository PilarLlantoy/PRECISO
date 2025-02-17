package com.inter.proyecto_intergrupo.model.parametric;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_parametros_reportes")
public class ParametrosReportes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parametro")
    private int id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "detalle")
    private String detalle;

    @Column(name = "tipo_insumo")
    private String tipoInsumo;

    @ManyToOne
    @JoinColumn(name = "id_pais")
    private Country pais;

    @Column(name = "activo", columnDefinition = "BIT DEFAULT 1")
    private boolean activo = true;

    @Column(name = "aplica_festivo", columnDefinition = "BIT DEFAULT 0")
    private boolean festivo = false;

    @Column(name = "excluye_sab_dom", columnDefinition = "BIT DEFAULT 0")
    private boolean excluyeSabDom = false;

    // Relación con CampoParamReportes
    @OneToMany(mappedBy = "parametroReportes", cascade = CascadeType.ALL)
    private List<CampoParamReportes> campos;

    @OneToMany(mappedBy = "parametroReportes", cascade = CascadeType.ALL)
    private List<FilterParametroReportes> filtros;

    @OneToMany(mappedBy = "parametroReportes", cascade = CascadeType.ALL)
    private List<SourceParametroReportes> fuentes;

    @OneToMany(mappedBy = "parametroReportes", cascade = CascadeType.ALL)
    private List<AdditionalSourceParametroReportes> fuentesAdicionales;

}
