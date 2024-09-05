package com.inter.proyecto_intergrupo.model.parametric;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_campos_rc")
public class CampoRC {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_campo")
    private int id;

    @Column(name = "nombre")
    @NotEmpty(message = "El nombre no puede estar vacio")
    private String nombre;

    @Builder.Default
    @Column(name = "primario", columnDefinition = "BIT DEFAULT 1")
    private boolean primario = true;

    @Builder.Default
    @Column(name = "conciliacion", columnDefinition = "BIT DEFAULT 1")
    private boolean conciliacion = true;

    @Column(name = "tipo")
    private String tipo;

    @Column(name = "longitud")
    private String longitud;

    @Column(name = "formatoFecha")
    private String formatoFecha;

    @Column(name = "idioma")
    private String idioma;

    @Column(name = "separador")
    private String separador;

    @Column(name = "operacion")
    private String operacion;

    @Column(name = "valorOperacion")
    private String valorOperacion;

    @Column(name = "estado", columnDefinition = "BIT DEFAULT 1")
    private boolean estado = true;

    @Column(name = "visualizacion", columnDefinition = "BIT DEFAULT 1")
    private boolean visualizacion = true;

    @Column(name = "formula", columnDefinition = "BIT DEFAULT 1")
    private boolean formula = true;

    // Relación con AccountingRoute
    @ManyToOne
    @JoinColumn(name = "id_rc", nullable = false)
    private AccountingRoute rutaContable;

}
