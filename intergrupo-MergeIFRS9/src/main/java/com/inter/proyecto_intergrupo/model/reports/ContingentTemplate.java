package com.inter.proyecto_intergrupo.model.reports;

import com.inter.proyecto_intergrupo.model.parametric.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_inventario_contingentes")
public class ContingentTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plantilla_contingente")
    private Long idPlantilla;

    @Column(name = "fecha_cierre")
    private Date fechaCierre;

    @Column(name = "cuenta_contable")
    private String cuentaContable;

    @Column(name = "saldo_divisa")
    private Double saldoDivisa;

    @Column(name = "fecha_alta")
    private Date fechaAlta;

    @Column(name = "fecha_vencimiento")
    private Date fechaVenciemiento;

    @Column(name = "nit")
    private String nit;

    @Column(name = "nombre_cliente")
    private String nombreCliente;

    @Column(name = "contrato")
    private String contrato;

    @Column(name = "nombre_banco")
    private String nombreBanco;

    @Column(name = "pais_banco")
    private String paisBanco;

    @Column(name="intergrupo")
    private String intergrupo;

    @Column(name = "tasa")
    private Double tasa;

    @Column(name = "saldo_pesos")
    private Double saldoPesos;

    @Column(name = "prefijo")
    private String prefijo;

    @Column(name = "numero")
    private String numero;

    @Column(name = "tipo_moneda")
    private String tipoMoneda;

    @ManyToOne()
    @JoinColumn(name="id_divisa")
    private Currency divisa;

    @Column(name = "periodo")
    private String periodo;

    @Column(name = "origen")
    private String origen;

    @Column(name = "nit_banco")
    private String nitBanco;

    @Column(name = "centro")
    private String centro;

}

