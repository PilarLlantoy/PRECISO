package com.inter.proyecto_intergrupo.model.dataquality;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_reglasdq")
public class RulesDQ implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_regla")
    private Long idRegla;

    @Column(name = "tipo_principio")
    private int tipoPrincipio;

    @Column(name = "tipo_regla")
    private int tipoRegla;

    @Column(name = "tabla")
    private String tabla;

    @Column(name = "columna")
    private String columna;

    @Column(name = "formato")
    private String formato;

    @Column(name = "longitud")
    private String longitud;

    @Column(name = "identificador")
    private String identificador;

    @Column(name = "fichero")
    private String fichero;

    @Column(name = "contraparte")
    private String contraparte;

    @Column(name = "campo")
    private String campo;

    @Column(name = "valor")
    private String valor;

    @Column(name = "umbral_minimo")
    private Double umbralMinimo;

    @Column(name = "umbral_objetivo")
    private Double umbralObjetivo;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "variacion_min")
    private Double variacionMin;

    @Column(name = "variacion_max")
    private Double variacionMax;
}
