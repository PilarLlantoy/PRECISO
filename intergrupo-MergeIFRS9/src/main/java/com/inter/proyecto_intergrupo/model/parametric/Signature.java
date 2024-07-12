package com.inter.proyecto_intergrupo.model.parametric;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Blob;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_firmas")
public class Signature implements Serializable{

    @Id
    @Column(name = "id_firma")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFirma;

    @Column(name = "cargo")
    private String cargo;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "correo")
    private String correo;

    @Column(name = "direccion")
    private String direccion;

    @Lob
    @Column(name = "firma", columnDefinition = "VARBINARY(MAX)")
    private byte[] firma;

}
