package com.inter.proyecto_intergrupo.model.temporal;

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
@Table(name = "nexco_base_fiscal_temporal")
public class TaxBaseTemporal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_base_fiscal")
    private Long idBaseFiscal;

    @Column(name = "cod_neocon")
    private String codNeocon;

    @Column(name = "divisa")
    private String divisa;

    @Column(name = "contrato")
    private String contrato;

    @Column(name = "nit_contraparte")
    private String nitContraparte;

    @Column(name = "valor")
    private double valor;

    @Column(name = "cuenta_local")
    private String cuentaLocal;

    @Column(name = "periodo")
    private String periodo;

    @Column(name = "peticion")
    private String peticion;
}
