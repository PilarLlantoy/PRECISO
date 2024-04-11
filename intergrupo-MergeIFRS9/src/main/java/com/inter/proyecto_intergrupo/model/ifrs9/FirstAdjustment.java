package com.inter.proyecto_intergrupo.model.ifrs9;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_ajuste_primera_vez")

public class FirstAdjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carga")
    private Long idCarga;

    @Column(name = "empresa")
    private String empresa;

    @Column(name = "aplicativo")
    private String aplicativo;

    @Column(name = "fecha_contable")
    private String fechacontable;

    @Column(name = "fecha_proceso")
    private String fechaproceso;

    @Column(name = "ristra")
    private String ristra;

    @Column(name = "centro_operante")
    private String centrooperante;

    @Column(name = "centro_origen")
    private String centroorigen;

    @Column(name = "centro_destino")
    private String centrodestino;

    @Column(name = "numero_mov_debe")
    private String numeromovdebe;

    @Column(name = "numero_mov_haber")
    private String numeromovhaber;

    @Column(name = "importe_en_pesos_debe")
    private Double importepesosdebe;

    @Column(name = "importe_en_pesos_haber")
    private Double importepesoshaber;

    @Column(name = "importe_en_divisa_debe")
    private Double importedivisadebe;

    @Column(name = "importe_en_divisa_haber")
    private Double importedivisahaber;

    @Column(name = "diferencia_pesos")
    private Double diferenciaPesos;

    @Column(name = "correctora")
    private String correctora;

    @Column(name = "referencia")
    private String referencia;

    @Column(name = "clave_de_interfaz")
    private String claveinterfaz;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "contrato")
    private String contrato;

    @Column(name = "stage")
    private String stage;

    @Column(name = "segmento")
    private String segmento;

    @Column(name = "ceros")
    private String ceros;

    @Column(name = "cuenta_ingreso")
    private String cuentaingreso;

    @Column(name = "cuenta_gasto")
    private String cuentagasto;

    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "codigo_de_consolidacion")
    private String codigoconsolidacion;

}
