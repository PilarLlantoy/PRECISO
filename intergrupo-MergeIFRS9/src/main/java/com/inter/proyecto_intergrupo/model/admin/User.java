package com.inter.proyecto_intergrupo.model.admin;

import com.inter.proyecto_intergrupo.model.parametric.UserAccount;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.hibernate.validator.constraints.Length;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_usuarios")
public class User {

    @Id
    @Column(name = "usuario")
    @NotEmpty(message = "Ingrese un usuario")
    private String usuario;

    @Column(name = "correo", unique = true)
    @Email(message = "El email no es valido")
    @NotEmpty(message = "Ingrese un Email")
    private String correo;

    @Column(name = "contra")
    private String contra;

    @Column(name = "nombre")
    @NotEmpty(message = "Ingrese un nombre")
    private String nombre;

    @Column(name = "centro")
    @NotEmpty(message = "Ingrese el centro")
    private String centro;

    @Column(name = "estado")
    private Boolean estado;

    @Column(name = "empresa")
    @NotEmpty(message = "Ingrese una empresa")
    private String empresa;

    @Column(name = "creacion")
    private Date creacion;

    @Column(name = "passwordToken")
    private String resetPasswordToken;

    @ManyToMany(cascade = CascadeType.MERGE)
    @ToString.Exclude
    @JoinTable(name="nexco_user_rol", joinColumns = @JoinColumn(name="usuario"), inverseJoinColumns = @JoinColumn(name = "id_perfil"))
    private Set<Role> roles;

    @OneToMany(mappedBy = "idUsuario", cascade = CascadeType.ALL)
    private List<UserAccount> userAccountList;
}
