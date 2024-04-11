package com.inter.proyecto_intergrupo.model.reports;

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
@Table(name = "nexco_intergrupo_conc_tm1")
public class ReconciliationTemp1 implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Integer id;

    @Column(name = "cuenta_local")
    String cuentaLocal;

    @Column(name = "contrato_banco")
    String contratoBanco;

    @Column(name = "contrato_filial")
    String contratoFilial;

    @Column(name = "yntp_empresa")
    String yntpEmpresa;

    @Column(name = "cuenta_filial")
    String cuentaFilial;

    @Column(name = "valor_banco")
    Double valorBanco;

    @Column(name = "valor_filial")
    Double valorFilial;

    @Column(name = "concepto")
    String concepto;
}
