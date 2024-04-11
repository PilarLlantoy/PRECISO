package com.inter.proyecto_intergrupo.model.ifrs9;

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
@Table(name = "nexco_validacion_descon_dif")
public class DesconValDif {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cod_neocon")
    private String codNeocon;

    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "divisa")
    private String divisa;

    @Column(name = "concepto")
    private String concepto;

    @Column(name = "saldo_plano")
    private Double saldoPlano;

    @Column(name = "saldo_concil")
    private Double saldoConcil;

    @Column(name = "diferencia")
    private Double diferencia;

    @Column(name = "periodo")
    private String periodo;
}
