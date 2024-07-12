package com.inter.proyecto_intergrupo.model.parametric;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_tipo_entidad")
public class TypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_entidad")
    private Long idTipoEntidad;

    @Column(name = "tipo_contraparte")
    private String tipoContraparte;

    @Column(name = "nit")
    private String nit;

    @Column(name = "contraparte")
    private String contraparte;

    @Column(name = "intergrupo")
    private Boolean intergrupo;

    @Column(name = "tipo_entidad")
    private String tipoEntidad;

    @Column(name = "eliminacion")
    private Boolean eliminacion;
}
