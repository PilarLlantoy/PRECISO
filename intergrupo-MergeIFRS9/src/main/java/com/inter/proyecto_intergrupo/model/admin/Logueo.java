package com.inter.proyecto_intergrupo.model.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_logueo")
public class Logueo {

    @Id
    @Column(name = "usuario")
    private String usuario;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "fecha")
    private Date fecha;

}
