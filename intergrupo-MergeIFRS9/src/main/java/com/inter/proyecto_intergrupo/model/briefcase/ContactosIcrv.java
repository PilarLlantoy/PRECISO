package com.inter.proyecto_intergrupo.model.briefcase;

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
@Table(name = "nexco_contactos_icrv")
public class ContactosIcrv implements Serializable{

    @Id
    @Column(name = "id_contacto")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idContacto;

    @Column(name = "proceso")
    private String proceso;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "empresa")
    private String empresa;

    @Column(name = "correo_principal")
    private String correoPrincipal;

    @Column(name = "correo_secundario")
    private String correoSecundario;

    @Column(name = "superior")
    private String superior;

    @Column(name = "superior1")
    private String superior1;

    @Column(name = "celular")
    private Long celular;

    @Column(name = "telefono")
    private Long telefono;

    @Column(name = "extension")
    private String extension;

    @Column(name = "pagina")
    private String pagina;

}
