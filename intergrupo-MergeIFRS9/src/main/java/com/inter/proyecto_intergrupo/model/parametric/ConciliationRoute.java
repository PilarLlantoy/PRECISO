package com.inter.proyecto_intergrupo.model.parametric;
import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_rutas_conciliaciones")
public class ConciliationRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "conciliacion")
    private String conciliacion;

    @Column(name = "detalle")
    private String detalle;

    @Column(name = "nombreArchivo")
    private String nombreArchivo;

    @Column(name = "tipoArchivo")
    private String tipoArchivo;

    @Column(name = "delimitador")
    private String delimitador;

    @Column(name = "ruta")
    private String ruta;

    @Column(name = "formatoFecha")
    private String formatoFecha;

    @Column(name = "idiomaFecha")
    private String idiomaFecha;

    @Column(name = "siglasFechas", columnDefinition = "BIT DEFAULT 0")
    private boolean siglasFechas = false;

    @Column(name = "activo", columnDefinition = "BIT DEFAULT 1")
    private boolean activo = true;

    @Column(name = "esFichero", columnDefinition = "BIT DEFAULT 1")
    private boolean esFichero = false;

    @Column(name = "estado", columnDefinition = "BIT DEFAULT 1")
    private boolean estado = true;

    @Column(name = "filasOmitidas")
    private Integer filasOmitidas;


}
