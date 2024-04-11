package com.inter.proyecto_intergrupo.model.reports;

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
@Table(name = "nexco_intergrupo_contingentes")
public class ContingentIntergroup
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_intergrupo_contingentes")
    private Long idContingentesInter;

    @Column(name = "cod_neocon")
    private String codNeocon;

    @Column(name = "divisa")
    private String divisa;

    @Column(name = "yntp")
    private String yntp;

    @Column(name = "sociedad")
    private String sociedad;

    @Column(name = "contrato")
    private String contrato;

    @Column(name = "nit")
    private String nit;

    @Column(name = "valor")
    private Double valor;

    @Column(name = "cod_pais")
    private String codPais;

    @Column(name = "pais")
    private String pais;

    @Column(name = "cuenta_local")
    private String cuentaLocal;

    @Column(name = "periodo")
    private String periodo;

}

