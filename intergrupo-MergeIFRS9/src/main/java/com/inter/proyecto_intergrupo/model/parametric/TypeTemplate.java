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
@Table(name = "preciso_tipo_plantilla_esp")
public class TypeTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "nombre_archivo")
    private String nombreArchivo;

    @Column(name = "tipo_proceso")
    private String tipoProceso;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "tipo_asiento")
    private String tipoAsiento;

    @Column(name = "referencia")
    private String referencia;
}
