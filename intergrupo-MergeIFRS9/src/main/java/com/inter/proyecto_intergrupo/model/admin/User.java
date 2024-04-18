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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private int id;

    @Column(name = "codigo_usuario")
    @NotEmpty(message = "Ingrese un usuario")
    private String usuario;

    @Column(name = "tipo_documento")
    //@NotEmpty(message = "Ingrese un tipo de documento")
    private String tipoDocumento;

    @Column(name = "numero_documento")
    //@NotEmpty(message = "Ingrese un número de documento")
    private String numeroDocumento;

    @Column(name = "primer_nombre")
    //@NotEmpty(message = "Ingrese un nombre")
    private String primerNombre;

    @Column(name = "segundo_nombre")
    private String segundoNombre;

    @Column(name = "primer_apellido")
    //@NotEmpty(message = "Ingrese un primer apellido")
    private String primerApellido;

    @Column(name = "segundo_apellido")
    //@NotEmpty(message = "Ingrese un segundo apellido")
    private String segundoApellido;

    @ManyToMany(cascade = CascadeType.MERGE)
    @ToString.Exclude
    @JoinTable(name="nexco_user_rol", joinColumns = @JoinColumn(name="id_usuario"), inverseJoinColumns = @JoinColumn(name = "id_perfil"))
    private Set<Role> roles;

    @ManyToOne
    @JoinColumn(name = "id_cargo")
    private Cargo cargo;

    @Column(name = "activo")
    private Boolean activo;

    @Column(name = "fecha_nacimiento")
    //@NotNull(message = "Ingrese una fecha de nacimiento")
    private Date fechaNacimiento;

    @Column(name = "inicio_inactividad")
    private Date inicioInactividad;

    @Column(name = "fin_inactividad")
    private Date finInactividad;

    @Column(name = "correo", unique = true)
    @Email(message = "El email no es valido")
    @NotEmpty(message = "Ingrese un Email")
    private String correo;

    @Column(name = "contra")
    private String contra;

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



    @OneToMany(mappedBy = "idUsuario", cascade = CascadeType.ALL)
    private List<UserAccount> userAccountList;
}
