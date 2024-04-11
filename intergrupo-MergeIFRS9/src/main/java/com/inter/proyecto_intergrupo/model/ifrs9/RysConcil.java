package com.inter.proyecto_intergrupo.model.ifrs9;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_rys_conciliacion")
public class RysConcil implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_concil_pape")
    private Long idConcilPape;

    @Column(name = "numero_papeleta")
    private String numeroPapeleta;

    @Column(name = "cnta_cntble_1_conciliacion")
    private String cntaCntble1Conciliacion;

    @Column(name = "tipo_entidad")
    private String tipoEntidad;

    @Column(name = "cntro_cntble_1_conciliacion")
    private String cntroCntble1Conciliacion;

    @Column(name = "dvsa_cntble_1_conciliacion")
    private String dvsaCntble1conciliacion;

    @Column(name = "nro_identificacion")
    private String nroIdentificacion;

    @Column(name = "cnta_cntble_1_pyg")
    private String cntaCntble1Pyg;

    @Column(name="total_rys")
    private Double totalRyS;

    @Column(name="diferencia")
    private Double diferencia;

    @Column(name = "total_riesgos")
    private Double totalRiesgos;

    @Column(name = "ajuste")
    private Double ajuste;

    @Column(name = "cta_pyg")
    private String ctaPyg;

    @Column(name = "ajuste_pyg")
    private Double ajustePyg;

    @Column(name="valida")
    private Double valida;

    @Column(name = "cta_neocon")
    private String ctaNeocon;

    @Column(name = "cta_neocon_pyg")
    private String ctaNeoconPyg;

    @Column(name = "periodo")
    private String periodo;
}
