package com.inter.proyecto_intergrupo.model.parametric;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_query_anterior")
public class QueryOld implements Serializable {

    @Id
    @Column(name = "empresa")
    private String empresa;

    @Id
    @Column(name = "NUCTA")
    private String NUCTA;

    @Id
    @Column(name = "FECONT")
    private String FECONT;

    @Id
    @Column(name = "CODDIV")
    private String CODDIV;

    @Column(name = "SALMES")
    private BigDecimal SALMES;

    @Column(name = "SALMESD")
    private BigDecimal SALMESD;

    @Column(name = "SALMED")
    private BigDecimal SALMED;

    @Column(name = "SALMEDD")
    private BigDecimal SALMEDD;

    @Column(name = "CODIGEST")
    private String CODIGEST;

    @Column(name = "CODICONS")
    private String CODICONS;

    @Column(name = "FECHPROCE")
    private Date FECHPROCE;

    @Column(name = "SALDOQUERYDIVISA")
    private BigDecimal SALDOQUERYDIVISA;

    @Column(name = "SALDOQUERY")
    private BigDecimal SALDOQUERY;

    @Column(name = "DIVISA")
    private String DIVISA;

}
