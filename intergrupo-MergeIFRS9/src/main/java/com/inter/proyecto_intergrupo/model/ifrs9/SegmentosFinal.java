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
@Table(name = "nexco_segmentos_final")
public class SegmentosFinal implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "identificacion")
    private String identificacion;

    @Column(name = "numero_cliente")
    private String numeroCliente;

    @Column(name = "nombre_cliente")
    private String nombreCliente;

    @Column(name = "tipo_persona")
    private String tipoPersona;

    @Column(name = "segmento_finrep_old")
    private String segmentoFinrepOld;

    @Column(name = "segmento_finrep_new")
    private String segmentoFinrepNew;

    @Column(name = "corasu")
    private String corasu;

    @Column(name = "subcorasu")
    private String subcorasu;

    @Column(name = "ciiu")
    private String ciiu;

    @Column(name = "numero_empleados")
    private Double numeroEmpleados;

    @Column(name = "total_activos")
    private Double totalActivos;

    @Column(name = "total_ventas")
    private Double totalVentas;

    @Column(name = "tipo_institucion")
    private String tipoInstitucion;

    @Column(name = "periodo")
    private String periodo;

    @Column(name = "observaciones")
    private String observaciones;
}
