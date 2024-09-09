package com.inter.proyecto_intergrupo.model.parametric;
import lombok.*;
import java.util.List;

import javax.persistence.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_rutas_contables")
public class AccountingRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rc")
    private int id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "nombreArchivo")
    private String nombreArchivo;

    @Column(name = "tipoArchivo")
    private String tipoArchivo;

    @Column(name = "delimitador")
    private String delimitador;

    @Column(name = "complementoArchivo")
    private String complementoArchivo;

    @Column(name = "contable", columnDefinition = "BIT DEFAULT 0")
    private boolean contable = false;

    @Column(name = "ruta")
    private String ruta;

    @Column(name = "cargueDiario", columnDefinition = "BIT DEFAULT 0")
    private boolean cargueDiario = false;

    @Column(name = "siglasFechas", columnDefinition = "BIT DEFAULT 0")
    private boolean siglasFechas = false;

    @Column(name = "formatoFecha")
    private String formatoFecha;

    @Column(name = "idiomaFecha")
    private String idiomaFecha;

    @Column(name = "activo", columnDefinition = "BIT DEFAULT 1")
    private boolean activo = true;

    @Column(name = "estado", columnDefinition = "BIT DEFAULT 1")
    private boolean estado = true;


    @ManyToOne
    @JoinColumn(name = "id_sf", nullable = false)
    private SourceSystem sfrc;

    @Column(name = "filasOmitidas")
    private Integer filasOmitidas;

    // Relación con CampoAC
    @OneToMany(mappedBy = "rutaContable", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CampoRC> campos;

    @OneToMany(mappedBy = "rutaContable", cascade = CascadeType.ALL)
    private List<CondicionRC> condiciones;

    @OneToMany(mappedBy = "rutaContable", cascade = CascadeType.ALL)
    private List<ValidationRC> validaciones;

}
