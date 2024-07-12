package com.inter.proyecto_intergrupo.model.admin;

import com.inter.proyecto_intergrupo.model.parametric.UserAccount;
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
@Table(name = "preciso_administracion_cuadro_mando")
public class ControlPanel implements Serializable {

    @Id
    @Column(name = "responsable")
    private String responsable; // tiene que ser el centro del usuario

    @Id
    @Column(name = "input")
    private String input;  //R&S,FORWARD,SWAP,OPCIONES ->

    @Id
    @Column(name = "componente")//DERIVADOS _> PENDING?
    private String componente;

    @Column(name = "semaforo_input")
    private String semaforoInput;

    @Column(name = "empresa")
    private String empresa;

    @Column(name = "semaforo_componente")
    private String semaforoComponente; // ejecuta igual a PENDING?

    @Column(name = "usuario_carga")
    private String usuarioCarga;

    @Column(name = "estado")
    private Boolean estado;

    @Column(name = "fecha_carga")
    private Date fechaCarga;

    @Id
    @Column(name = "fecha_reporte") //periodo
    private String fechaReporte;
}
