package com.inter.proyecto_intergrupo.model.parametric;

import com.inter.proyecto_intergrupo.model.reports.ContingentTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_divisas")
public class Currency {

    @Id
    @Column(name = "id_divisa")
    private String id;

    @Column(name = "nombre_divisa")
    @NotEmpty(message = "El nombre no puede estar vacio")
    private String nombre;

    @Column(name = "divisa_neocon")
    private String divisaNeocon;

    @OneToMany(mappedBy = "divisa",cascade = CascadeType.ALL)
    private List<YntpSociety> yntpSocietyList;

    @OneToMany(mappedBy = "divisa",cascade = CascadeType.ALL)
    private List<ContingentTemplate> contingentTemplateList;
}
