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
@Table(name = "preciso_parametros_generales")
public class GeneralParam {

    @Id
    @Column(name = "id_parametro")
    private Long id;

    @Column(name = "unidad_principal")
    @NotEmpty(message = "La unidad no puede estar vacia")
    private String unidadPrincipal;

    @Column(name = "unidad_secundaria")
    private String unidadSecundaria;

    @Column(name = "valor_unidad")
    private String valorUnidad;

}
