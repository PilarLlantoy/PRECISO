package com.inter.proyecto_intergrupo.model.parametric;
import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_param_homologacion_centros")
public class ParamOfficeMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "id_rc_origen")
    private Long idRcOrigen;

    @Column(name = "campo_rc_centro_origen")
    private Long campoRcCentroOrigen;

    @Column(name = "campo_rc_centro_origen_n")
    private String campoRcCentroOrigenN;

    @Column(name = "campo_rc_1_detalle_origen")
    private Long campoRc1DetalleOrigen;

    @Column(name = "campo_rc_1_detalle_origen_n")
    private String campoRc1DetalleOrigenN;

    @Column(name = "campo_rc_centro_resultado")
    private Long campoRcCentroResultado;

    @Column(name = "campo_rc_centro_resultado_n")
    private String campoRcCentroResultadoN;

}
