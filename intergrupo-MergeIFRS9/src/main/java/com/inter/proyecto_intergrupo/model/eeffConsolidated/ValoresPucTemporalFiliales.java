package com.inter.proyecto_intergrupo.model.eeffConsolidated;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity

@Table(name = "nexco_puc_valores_filiales_temporal")

public class ValoresPucTemporalFiliales {

    @Id
    @Column(name = "id_Cuenta")
    private String idcuenta;

    @Column(name = "nombre_cuenta")
    String nombreCuenta;

    @Column(name = "tipo_cuenta")
    @Pattern(regexp = "^[GNI]$")
    String tipoCuenta;

    @Column(name = "maneja_costos")
    String manejaCostos;

    @Column(name = "maneja_cierre")
    String manejaCierre;

    @Column(name = "maneja_movimientos")
    String manejaMovimientos;

    @Column(name = "maneja_moneda")
    String manejaMoneda;

    @Column(name = "maneja_ajustes")
    String manejaAjustes;

    @Column(name = "presupuesto")
    String presupuesto;

    @Column(name = "porcentaje_impuesto")
    String porcentajeImpuesto;

    @Column(name = "cta_puc")
    String ctaPuc;

    @Column(name = "flujo_efectivo")
    String flujoEfecivo;

    @Column(name = "codigo_flujo_efectivo")
    String codigoFlujoEfectivo;

    @Column(name = "naturaleza")
    String naturaleza;

    @Column(name = "cuenta_orden_super_valores")
    String cuentaOrdenSuperValores;

    @Column(name = "cuenta_orden_diferencia_en_cambio")
    String cuentaOrdenDiferenciaEnCambio;

    @Column(name = "maneja_segmento")
    String manejaSegmento;

    @Column(name = "codigo_norma_contable")
    String codigoNormaContable;

    @Column(name = "maneja_kardex")
    String manejaKardex;

    @Column(name = "moneda")
    String moneda;

    @Column(name = "usuario_actualizacion")
    String usuarioActualizacion;

    @Column(name = "fecha_actualizacion")
    Date fechaActualizacion;

    @Column(name = "usuario_creacion")
    String usuarioCreacion;

    @Column(name = "fecha_creacion")
    Date fechaCreacion;

    @Column(name = "periodo")
    String periodo;

    @Column(name = "empresa")
    String empresa;

    @Column(name = "cod_cons")
    String codCons;
}

