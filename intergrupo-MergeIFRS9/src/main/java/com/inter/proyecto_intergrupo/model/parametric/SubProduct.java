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
@Table(name = "nexco_subproducto")
public class SubProduct {

    @Id
    @Column(name = "CTA")
    private String cta;

    @Column(name = "SUBPRODUCTO")
    private String subproducto;

}
