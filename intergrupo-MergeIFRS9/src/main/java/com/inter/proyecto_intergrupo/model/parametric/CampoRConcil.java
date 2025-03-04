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
@Table(name = "preciso_campos_rconcil")
public class CampoRConcil implements Serializable {

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

    @Column(name = "separador")
    private String separador;

    @Column(name = "formatoFecha")
    private String formatoFecha;

    @Column(name = "idioma")
    private String idioma;

    @Column(name = "estado", columnDefinition = "BIT DEFAULT 1")
    private boolean estado = true;

    @Column(name = "nulo_moneda", columnDefinition = "BIT DEFAULT 1")
    private boolean nuloMoneda = true;


    // Relación con ConciliationRoute
    @ManyToOne
    @JoinColumn(name = "id_rconcil", nullable = false)
    private ConciliationRoute rutaConciliacion;

}
