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
@Table(name = "nexco_query_nic34_consol")
public class QueryNIC34Consol implements Serializable {

    @Id
    @Column(name = "ID_QUERY")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID_QUERY;

    @Column(name = "NUCTA")
    private String NUCTA;

    @Column(name = "FECONT")
    private String FECONT;

    @Column(name = "SALDOQUERY")
    private BigDecimal SALDOQUERY;

    @Column(name = "MONEDA")
    private String MONEDA;

}
