package com.inter.proyecto_intergrupo.model.admin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.Expose;
import com.inter.proyecto_intergrupo.model.admin.Role;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_vistas")
public class View {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Expose
    @Column(name = "id_vista")
    private int id;

    @Column(name="nombre")
    @NotEmpty
    @Expose
    private String viewName;

    @Column(name = "ruta")
    @NotEmpty
    @Expose
    private String path;

    @Column(name = "menu_principal")
    @NotEmpty
    @Expose
    private String menuPrincipal;

    @Column(name = "sub_menu_p1")
    @NotEmpty
    @Expose
    private String subMenuP1;


    @Column(name = "sub_menu_p2")
    @NotEmpty
    @Expose
    private String subMenuP2;

    @Column(name="unica")
    @NotEmpty
    @Expose
    private Boolean unique;


    @ManyToMany(mappedBy = "vistas", fetch = FetchType.EAGER)
    @ToString.Exclude
    @JsonIgnore // Excluye la serialización JSON de la relación roles en la entidad Role
    private Set<Role> roles;
}
