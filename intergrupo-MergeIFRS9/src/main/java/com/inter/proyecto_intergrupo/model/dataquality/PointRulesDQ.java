package com.inter.proyecto_intergrupo.model.dataquality;

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
@Table(name = "nexco_puntuacion_validacion_dq")
public class PointRulesDQ implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_puntuacion")
    private Long idPuntuacion;

    @Column(name = "fecha_cierre")
    private Date fechaCierre;

    @Column(name = "fecha_ejecucion")
    private Date fechaEjecucion;

    @Column(name = "identificador_pais")
    private String identificadorPais;

    @Column(name = "identificador_uuaa")
    private String identificadorUuaa;

    @Column(name = "nombre_data_system")
    private String nombreDataSystem;

    @Column(name = "descripcion_desglose")
    private String descripcionDesglose;

    @Column(name = "identificador_secuencial_legacy")
    private String identificadorSecuencialLegacy;

    @Column(name = "tipo_principio")
    private String tipoPrincipio;

    @Column(name = "tipo_regla")
    private String tipoRegla;

    @Column(name = "nombre_fisico_objeto")
    private String nombreFisicoObjeto;

    @Column(name = "nombre_fisico_campo")
    private String nombreFisicoCampo;

    @Column(name = "porcentaje_cumplimiento")
    private Double porcentajeCumplimiento;

    @Column(name = "numerador_cumplimiento")
    private Double numeradorCumplimiento;

    @Column(name = "denominador_cumplimiento")
    private Double denominadorCumplimiento;

    @Column(name = "tipo_frecuencia_ejecucion")
    private String tipoFrecuenciaEjecucion;

    @Column(name = "porcentaje_umbral_minimo")
    private Double porcentajeUmbralMinimo;

    @Column(name = "porcentaje_umbral_objetivo")
    private Double porcentajeUmbralObjetivo;

    @Column(name = "nombre_campo_importe")
    private String nombreCampoImporte;

    @Column(name = "porcentaje_cumplimiento_saldo")
    private Double porcentajeCumplimientoSaldo;

    @Column(name = "importe_numerador")
    private Double importeNumerador;

    @Column(name = "importe_denominador")
    private Double importeDenominador;

    @Column(name = "periodo")
    private String periodo;
}
