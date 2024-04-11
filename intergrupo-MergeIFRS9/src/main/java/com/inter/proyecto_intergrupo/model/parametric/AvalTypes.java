package com.inter.proyecto_intergrupo.model.parametric;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_tipo_aval")
public class AvalTypes implements Serializable {

    @Id
    @Column(name = "aval_origen")
    private String avalOrigen;

    @Column(name = "id_tipo_aval")
    private int tipoAval;

    @Column(name = "tipo_archivo")
    @NotEmpty(message = "El tipo de archivo no puede estar vacio")
    private String tipoArchivo;

    @Id
    @Column(name = "cuenta_contable_13")
    private String cuentaContable13;

    @Column(name = "cuenta_contable_60")
    private String cuentaContable60;

    @Column(name = "contrapartida_generica")
    private String contraGenerica;

}
