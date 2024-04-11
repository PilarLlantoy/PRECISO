package com.inter.proyecto_intergrupo.model.parametric;

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
@Table(name = "nexco_reclasificacion_intergrupo_v2")
public class ReclassificationIntergroup implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Integer id;

    @Column(name = "concepto")
    private String concepto;

    @Column(name = "codicons")
    private String codicons;

    @Column(name = "segmento")
    private String segmento;

    @Column(name = "tipo_sociedad")
    private String tipoSociedad;

    @Column(name = "producto")
    private String producto;

    @Column(name = "tipo")
    private String tipo;

    @Column(name = "stage")
    private String stage;

    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "cuenta_contrapartida")
    private String cuentaContrapartida;
}
