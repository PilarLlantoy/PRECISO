package com.inter.proyecto_intergrupo.model.admin;

import com.inter.proyecto_intergrupo.model.parametric.UserAccount;
import com.inter.proyecto_intergrupo.model.parametric.UserConciliation;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_administracion_usuarios")
public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private int id;

    @Column(name = "codigo_usuario")
    @NotEmpty(message = "Ingrese un usuario")
    private String usuario;

    @ManyToOne
    @JoinColumn(name = "id_tipo_documento")
    private TipoDocumento tipoDocumento;

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
    @JoinTable(name="preciso_administracion_user_rol", joinColumns = @JoinColumn(name="id_usuario"), inverseJoinColumns = @JoinColumn(name = "id_perfil"))
    private Set<Role> roles;

    @ManyToOne
    @JoinColumn(name = "id_cargo")
    private Cargo cargo;

    @Column(name = "activo", columnDefinition = "BIT DEFAULT 1")
    private boolean activo = true;

    @Column(name = "estado", columnDefinition = "BIT DEFAULT 1")
    private boolean estado = true;

    @Column(name = "correo", unique = true)
    @Email(message = "El email no es valido")
    //@NotEmpty(message = "Ingrese un Email")
    private String correo;

    @Column(name = "contra")
    private String contra;

    @Column(name = "creacion")
    private Date creacion;

    @Column(name = "passwordToken")
    private String resetPasswordToken;

    @OneToMany(mappedBy = "idUsuario", cascade = CascadeType.ALL)
    private List<UserAccount> userAccountList;

    @Column(name = "empresa")
    //@NotEmpty(message = "Ingrese una empresa")
    private String empresa;

    @Column(name = "centro")
    //@NotEmpty(message = "Ingrese el centro")
    private String centro;


    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserConciliation> userConciliations = new ArrayList<>();



    // Otros métodos y campos

    public String getUsername() {
        return primerNombre + " " +
                (segundoNombre != null && !segundoNombre.isEmpty() ? segundoNombre + " " : "") +
                primerApellido + " " + segundoApellido;
    }

    public String getFechaCreacion(){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(creacion);
    }
}
