package com.inter.proyecto_intergrupo.model.reportNIC34;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_query_nic34")
public class QueryNIC34 implements Serializable {

    @Id
    @Column(name = "ID_QUERY")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID_QUERY;

    @Column(name = "EMPRESA")
    private String EMPRESA;

    @Column(name = "NUCTA")
    private String NUCTA;

    @Column(name = "FECONT")
    private String FECONT;

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

    @Column(name = "MONEDA")
    private String MONEDA;

}
