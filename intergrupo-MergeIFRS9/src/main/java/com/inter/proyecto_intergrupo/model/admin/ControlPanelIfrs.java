package com.inter.proyecto_intergrupo.model.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_administracion_cuadro_mando_ifrs")
public class ControlPanelIfrs implements Serializable {

    @Id
    @Column(name = "input")
    private String input;

    @Id
    @Column(name = "componente")
    private String componente;

    @Column(name = "fecha_cargue")
    private Date fechaCargue;

    @Column(name = "semaforo_input")
    private String semaforoInput;

    @Column(name = "empresa")
    private String empresa;

    @Column(name = "semaforo_componente")
    private String semaforoComponente;

    @Column(name = "usuario_carga")
    private String usuarioCarga;

    @Column(name = "estado")
    private Boolean estado;

    @Column(name = "orden")
    private int orden;
}
