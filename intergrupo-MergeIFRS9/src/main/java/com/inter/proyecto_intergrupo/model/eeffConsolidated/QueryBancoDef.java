package com.inter.proyecto_intergrupo.model.eeffConsolidated;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_query_banco_def")

public class QueryBancoDef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empresa")
    Long id_empresa;

    @Column(name = "empresa")
    private String empresa;

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

    @Column(name = "nombre_cuenta")
    private String nombreCuenta;

    @Column(name = "moneda")
    private String moneda;

    @Column(name = "periodo")
    private String periodo;

    @Column(name = "indic")
    private String indic;

    @Column(name = "naturaleza")
    private String naturaleza;

}
