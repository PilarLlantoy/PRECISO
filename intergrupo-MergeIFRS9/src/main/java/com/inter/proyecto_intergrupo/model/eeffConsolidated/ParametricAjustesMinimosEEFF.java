package com.inter.proyecto_intergrupo.model.eeffConsolidated;

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
@Table(name = "nexco_parametria_ajustes_minimos_eeff")

public class ParametricAjustesMinimosEEFF {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_parametro")
    private Long idTipoParametro;

    @Column(name = "cuenta_origen")
    private String cuentaOrigen;

    @Column(name = "empresa_origen")
    private String empresaOrigen;

    @Column(name = "moneda_origen")
    private String monedaOrigen;

    @Column(name = "empresa_destino")
    private String empresaDestino;

    @Column(name = "cuenta_destino")
    private String cuentaDestino;

    @Column(name = "moneda_destino")
    private String monedaDestino;

    @Column(name = "periodo")
    private String periodo;
}
