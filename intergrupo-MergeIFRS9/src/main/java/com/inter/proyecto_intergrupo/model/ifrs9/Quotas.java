package com.inter.proyecto_intergrupo.model.ifrs9;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;

import java.io.Serializable;

import static javax.persistence.GenerationType.SEQUENCE;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_cupos")
public class Quotas implements Serializable {

    @Id
    @Column(name = "cuentas_puc")
    private String cuentasPuc;

    @Id
    @Column(name = "contrato_origen")
    private String contratoOrigen;

    @Column(name = "contrato_ifrs9")
    private String contratoIfrs9;



}
