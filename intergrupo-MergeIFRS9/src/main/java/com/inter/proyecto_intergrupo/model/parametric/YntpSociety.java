package com.inter.proyecto_intergrupo.model.parametric;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_sociedades_yntp")
public class YntpSociety {

    @Id
    @Column(name = "yntp")
    private String yntp;

    @Column(name = "sociedad_larga")
    @Size(max = 255,message = "*La sociedad debe tener maximo 254 caracteres")
    private String sociedadDescripcionLarga;

    @Column(name = "sociedad_corta")
    @Size(max = 255,message = "*La sociedad debe tener maximo 254 caracteres")
    private String sociedadDescripcionCorta;

    @ManyToOne()
    @JoinColumn(name = "id_divisa")
    private Currency divisa;

    @ManyToOne()
    @JoinColumn(name = "id_metodo_ifrs")
    private ConsolidationMethod metodo;

    @ManyToOne()
    @JoinColumn(name = "id_grupo_ifrs")
    private ConsolidationGroup grupo;

    @ManyToOne()
    @JoinColumn(name="id_pais")
    private Country pais;

    @Column(name = "tipo_entidad")
    private String tipoEntidad;

}
