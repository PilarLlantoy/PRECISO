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
@Table(name = "nexco_parametros_pyg")
public class ParametersPYG {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parametros_pyg")
    private Long idParametrosPyg;

    @Column(name = "aplica_reversion")
    private boolean aplicaReversion;

    @Column(name = "centro")
    private String centro;

    @Column(name = "tp")
    private String tp;

    @Column(name = "tercero")
    private String tercero;

    @Column(name = "dv")
    private String dv;

    @Column(name = "cuenta_espana_pasivo")
    private String cuentaEspanaPasivo;

    @Column(name="cuenta_local_pasivo")
    private String cuentaLocalPasivo;

    @Column(name = "cuenta_espana_pyg")
    private String cuentaEspanaPyg;

    @Column(name="cuenta_local_pyg")
    private String cuentaLocalPyg;

}
