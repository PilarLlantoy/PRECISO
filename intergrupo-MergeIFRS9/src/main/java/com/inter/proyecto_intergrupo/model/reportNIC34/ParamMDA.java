package com.inter.proyecto_intergrupo.model.reportNIC34;

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
@Table(name = "nexco_mda")
public class ParamMDA implements Serializable{

    @Id
    @Column(name = "id_mda")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMda;

    @Column(name = "fecha")
    private String fecha;

    @Column(name = "divisa")
    private String divisa;

    @Column(name = "moneda")
    private String moneda;
}
