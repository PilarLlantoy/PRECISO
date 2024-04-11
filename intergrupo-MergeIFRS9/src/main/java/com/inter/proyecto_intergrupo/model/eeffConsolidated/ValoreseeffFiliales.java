package com.inter.proyecto_intergrupo.model.eeffConsolidated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity

@Table(name = "nexco_eeff_valores_filiales")

public class ValoreseeffFiliales {

    @Id
    @Column(name = "cuenta")
    @Length(max = 255)
    String cuenta;

    @Column(name = "compañia")
    @Length(max = 1)
    String compania;

    @Column(name = "nombre_compañia")
    @Length(max = 255)
    String nombreCompania;

    @Column(name = "nombre_cuenta")
    @Length(max = 255)
    String nombreCuenta;

    @Column(name = "saldo_anterior")
    double saldoAnterior;

    @Column(name = "debitos")
    double debitos;

    @Column(name = "creditos")
    double creditos;

    @Column(name = "saldo_final")
    double saldoFinal;

    @Column(name = "codigo_categoria")
    @Length(max = 1)
    String codigoCategoria;

    @Column(name = "nombre_categoria")
    @Length(max = 255)
    String nombreCategoria;

    @Column(name = "codigo_oyd")
    @Length(max = 255)
    String codidoOyD;

    @Column(name = "periodo")
    @Length(max = 255)
    String periodo;

    @Column(name = "norma_contable")
    @Length(max = 255)
    String normaContable;

    @Column(name = "moneda")
    @Length(max = 255)
    String moneda;

    @Column(name = "empresa")
    @Length(max = 255)
    String empresa;

    @Column(name = "naturaleza")
    @Length(max = 255)
    String naturaleza;

    @Column(name = "cod_cons")
    String codCons;
}
