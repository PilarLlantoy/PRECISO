package com.inter.proyecto_intergrupo.model.reportNIC34;

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
@Table(name = "nexco_nic_fechas")
public class ParamFechas implements Serializable{

    @Id
    @Column(name = "id_fecha")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFecha;

    @Column(name = "ano")
    private String ano;

    @Column(name = "mes")
    private String mes;

    @Column(name = "fecont")
    private String fecont;

    @Column(name = "fechproce")
    private Date fechproce;

    @Column(name = "balance")
    private String balance;

    @Column(name = "pyg")
    private String pyg;

    @Column(name = "q_aplica")
    private String qaplica;

    @Column(name = "estado")
    private String estado;

    @Column(name = "estado_consol")
    private String estadoConsol;
}
