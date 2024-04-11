package com.inter.proyecto_intergrupo.model.temporal;

import com.inter.proyecto_intergrupo.model.parametric.ConsolidationGroup;
import com.inter.proyecto_intergrupo.model.parametric.ConsolidationMethod;
import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.parametric.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SelectBeforeUpdate;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_sociedades_yntp_temporal")
public class YntpSocietyTemporal {

    @Id
    @Column(name = "yntp")
    private String yntp;

    @Column(name = "sociedad_larga")
    private String sociedadDescripcionLarga;

    @Column(name = "sociedad_corta")
    private String sociedadDescripcionCorta;

    @Column(name = "id_divisa")
    private String divisa;

    @Column(name = "id_metodo_ifrs")
    private String metodo;

    @Column(name = "id_grupo_ifrs")
    private String grupo;

    @Column(name="id_pais")
    private String pais;

    @Column(name = "tipo_entidad")
    private String tipoEntidad;

    @Column(name = "nombre_pais")
    private String nombrePais;

    @Column(name = "nombre_divisa")
    private String nombreDivisa;

}
