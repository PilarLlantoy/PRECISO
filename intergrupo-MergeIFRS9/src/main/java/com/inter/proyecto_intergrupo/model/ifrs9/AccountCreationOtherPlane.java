package com.inter.proyecto_intergrupo.model.ifrs9;

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
@Table(name = "nexco_creacion_cuentas_otros_planos")
public class AccountCreationOtherPlane implements Serializable{

    @Id
    @Column(name = "id_plano")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPlano;

    @Column(name = "fecha_inicio")
    private Date fechaInicio;

    @Column(name = "fecha_fin")
    private Date fechaFin;

    @Column(name = "fecha_creacion")
    private Date fechaCreacion;

    @Column(name = "usuario_generador")
    private String usuarioGenerador;

    @Column(name = "nombre_archivo")
    private String nombreArchivo;

    @Lob
    @Column(name = "archivo", columnDefinition = "VARBINARY(MAX)")
    private byte[] archivo;
}
