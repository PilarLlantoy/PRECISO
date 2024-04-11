package com.inter.proyecto_intergrupo.model.admin;

import com.inter.proyecto_intergrupo.model.parametric.YntpSociety;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.SEQUENCE;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_auditoria")
public class Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private Long idAuditoria;

    @Column(name = "usuario")
    private String usuario;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "centro")
    private String centro;

    @Column(name = "componente")
    private String componente;

    @Column(name = "input")
    private String input;

    @Column(name = "accion")
    private String accion;

    @Column(name = "fecha")
    private Date fecha;

}
