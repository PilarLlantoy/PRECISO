package com.inter.proyecto_intergrupo.model.ifrs9;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.sql.rowset.serial.SerialArray;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_repo")
public class Repo implements Serializable {

    @Column(name = "cod")
    private String cod;

    @Column(name = "negociador")
    private String negociador;

    @Id
    @Column(name = "cod_cliente")
    private String codCliente;

    @Column(name = "fecha")
    private String fecha;

    @Column(name = "fecha_final")
    private String fechaFinal;

    @Column(name = "valor_total")
    private BigDecimal valorTotal;

    @Column(name = "intereses")
    private String intereses;

    @Column(name = "tasa")
    private String tasa;

    @Column(name = "negocio")
    private String negocio;

    @Column(name = "estado")
    private String estado;

    @Column(name = "tipo_mov")
    private String tipoMov;

    @Id
    @Column(name = "numero_papeleta")
    private String numeroPapeleta;

    @Column(name = "tipo_op_mercado")
    private BigDecimal tipoOpMercado;

    @Column(name = "causacion_ayer")
    private BigDecimal causacionAyer;

    @Column(name = "portafolio")
    private String portafolio;

    @Column(name = "contabilidad")
    private String contabilidad;

    @Column(name = "tasa_mesa")
    private String tasa_mesa;

    @Column(name = "causacion_neta")
    private String causacionNeta;

    @Column(name = "nombre")
    private BigDecimal nombre;

    @Column(name = "numero_ident")
    private BigDecimal numeroIdentificacion;

    @Column(name = "duracion_modificada_anual")
    private String duracionModificadaAnual;

    @Column(name = "moneda")
    private String moneda;

    @Column(name = "centro_contable_alt")
    private BigDecimal centroContableAlt;

    @Column(name = "tipo_entidad")
    private String tipoEntidad;

    @Column(name = "valor_libros")
    private String valorLibros;

    @Column(name = "valor_libros_interes_hoy")
    private BigDecimal valorLibrosHoy;

    @Column(name = "valor_mdo_gtia_activa")
    private BigDecimal valorMdoGtiaActiva;

    @Column(name = "valor_mdo_gtia_pasiva")
    private String valorMdoGtiaPasiva;

    @Column(name = "pap_reemplaza")
    private String papReemplaza;

    @Column(name = "calif_contraparte")
    private BigDecimal califContraparte;

    @Column(name = "cod_puc")
    private BigDecimal codPuc;

    @Column(name = "cod_puc_incumplimiento")
    private String codPucIncumplimiento;

    @Column(name = "monto_prov")
    private String montoProv;

    @Column(name = "monto_ext")
    private BigDecimal montoExt;

    @Column(name = "dias_vto")
    private String diasVto;

    @Column(name = "tasa_descuento")
    private String tasaDescuento;

    @Column(name = "fd")
    private Double fd;

    @Column(name = "valor_presente")
    private BigDecimal valorPresente;

    @Column(name = "diferencia")
    private BigDecimal diferencia;
}
