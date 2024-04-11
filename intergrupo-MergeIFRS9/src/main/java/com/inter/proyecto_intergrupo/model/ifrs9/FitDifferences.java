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
@Table(name = "nexco_cuadre_motor_diferencias")

public class FitDifferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cuadre_motor")
    private Long idCuadreMotor;

    @Column(name = "neocon")
    private String neocon;

    @Column(name = "fichero_saldos_ifrs9")
    private Double ficheroSaldosIFRS9;

    @Column(name = "ficha_saldos_inicial")
    private Double fichaSaldosInicial;

    @Column(name = "diferencias_ifrs9_pi")
    private Double diferenciasIFRS9PI;

    @Column(name = "saldo_eeff")
    private Double saldoEEFF;

    @Column(name = "diferencia_ifrs9_eeff")
    private Double diferenciaIFRS9EEFF;

    @Column(name = "procentaje_eeff")
    private Double porcentajeEEFF;

    @Column(name = "saldo_conciliacion")
    private Double saldoConciliacion;

    @Column(name = "diferencia_ifrs9_conciliacion")
    private Double diferenciaIFRS9Conciliacion;

    @Column(name = "procentaje_conciliacion")
    private Double porcentajeConciliacion;

    @Column(name = "diferencia_eeff_conciliacion")
    private Double diferenciaEEFFConciliacion;

    @Column(name = "periodo")
    private String periodo;

}
