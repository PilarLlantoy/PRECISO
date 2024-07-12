package com.inter.proyecto_intergrupo.model.parametric;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_filiales")
public class Subsidiaries implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Integer id;

    @Column(name = "yntp_empresa")
    private String yntpEmpresa;

    @Column(name = "cuenta_filial")
    private String cuentaFilial;

    @Column(name = "cuenta_local")
    private String cuentaLocal;

    @Column(name = "yntp_local")
    private String ytnpLocal;

    @Column(name = "divisa")
    private String divisa;

    @Column(name = "contrato_banco")
    private String contratoBanco;

    @Column(name = "contrato_filial")
    private String contratoFilial;

    @Column(name = "observacion_reportante")
    private String observacionReportante;

    @Column(name = "conceptos")
    private String conceptos;
}
