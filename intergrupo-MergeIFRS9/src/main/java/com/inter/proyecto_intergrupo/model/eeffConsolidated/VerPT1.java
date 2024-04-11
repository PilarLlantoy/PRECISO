package com.inter.proyecto_intergrupo.model.eeffConsolidated;

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

@Table(name = "nexco_valor_patrimonio_tecnico_verpt")

public class VerPT1 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dato")
    private Long idDato1;

    @Column(name = "valor_patrimonio_tecnico")
    Double valorPatrimonioTecnico;

    @Column(name = "periodo")
    String periodo;

    @Column(name = "moneda")
    String moneda;

}

