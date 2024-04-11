package com.inter.proyecto_intergrupo.model.eeffConsolidated;

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

@Table(name = "nexco_valor_riesgo_total_verpt")

public class VerPT {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dato")
    private Long idDato;

    @Column(name = "valor_riesgo_total")
    Double valorRiesgoTotal;

    @Column(name = "periodo")
    String periodo;

    @Column(name = "moneda")
    String moneda;

}

