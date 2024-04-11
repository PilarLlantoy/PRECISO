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

@Table(name = "nexco_eeff_fiduciaria_filiales")

public class FiduciariaeeffFiliales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_eeff")
    Long idEeff;

    @Column(name = "cuenta")
    @Length(max = 255)
    String cuenta;

    @Column(name = "nombre_Cuenta")
    @Length(max = 255)
    String nombreCuenta;

    @Column(name = "naturaleza")
    @Length(max = 255)
    String naturaleza;

    @Column(name = "saldo_anterior")
    double saldoAnterior;

    @Column(name = "debitos")
    double debitos;

    @Column(name = "creditos")
    double creditos;

    @Column(name = "saldo_final_export")
    double saldoFinalExport;

    @Column(name = "nivel")
    @Length(max = 255)
    String nivel;

    @Column(name = "periodo")
    @Length(max = 255)
    String periodo;

    @Column(name = "moneda")
    @Length(max = 255)
    String moneda;

    @Column(name = "empresa")
    @Length(max = 255)
    String empresa;

    @Column(name = "cod_cons")
    String codCons;
}
