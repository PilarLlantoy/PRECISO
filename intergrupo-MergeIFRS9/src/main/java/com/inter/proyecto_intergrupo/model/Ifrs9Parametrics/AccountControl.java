package com.inter.proyecto_intergrupo.model.Ifrs9Parametrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_control_contable_ifrs")
public class AccountControl implements Serializable{

    @Id
    @Column(name = "CUENTA")
    private String CUENTA;

    @Column(name = "DESCRIPCION_CUENTA")
    private String DESCRIPCIONCUENTA;

    @Column(name = "CODIGO_DE_CONTROL")
    private String CODIGODECONTROL;

    @Column(name = "DIAS_DE_PLAZO")
    private String DIASDEPLAZO;

    @Column(name = "INDICADOR_DE_LA_CUENTA")
    private String INDICADORDELACUENTA;

    @Column(name = "TIPO_DE_APUNTE")
    private String TIPODEAPUNTE;

    @Column(name = "INVENTARIABLE")
    private String INVENTARIABLE;

}
