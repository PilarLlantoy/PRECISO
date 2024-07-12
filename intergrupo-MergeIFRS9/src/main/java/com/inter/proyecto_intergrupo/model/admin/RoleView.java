package com.inter.proyecto_intergrupo.model.admin;

import com.google.gson.annotations.Expose;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_administracion_rol_vista")
public class RoleView {
    @EmbeddedId
    private RoleViewId idRolView;

    @ManyToOne
    @MapsId("roleId")
    @JoinColumn(name = "id_perfil")
    private Role role;

    @ManyToOne
    @MapsId("viewId")
    @JoinColumn(name = "id_vista")
    private View view;

    // NUEVOOO
    @Builder.Default
    @Column(name = "p_visualizar", columnDefinition = "BIT DEFAULT 0")
    private boolean pVisualizar = false;

    // NUEVOOO
    @Builder.Default
    @Column(name = "p_modificar", columnDefinition = "BIT DEFAULT 0")
    private boolean pModificar = false;

}
