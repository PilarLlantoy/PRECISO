package com.inter.proyecto_intergrupo.model.temporal;

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
@Table(name = "nexco_corep_temporal")
public class CorepTeporalH {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_corep_temp")
    private Long idCorepTemp;

    @Column(name = "VALOR")
    private Double valor;

    @Column(name = "NUCTA")
    private String nucta;

    @Column(name = "CONTRATO")
    private String contrato;

}

