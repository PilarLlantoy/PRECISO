package com.inter.proyecto_intergrupo.model.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_administracion_cuadro_mando_jobs")
public class ControlPanelJobs {

    @Id
    @Column(name = "id_job")
    private int idJob;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "fecha_ejecucion")
    private Date fechaEjecucion;

    @Column(name = "fecha_ejecucion_exitosa")
    private Date fechaEjecucionExitosa;

    @Column(name = "estado")
    private Boolean estado;

}
