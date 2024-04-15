package com.inter.proyecto_intergrupo.model.admin;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Embeddable
public class RoleViewId implements Serializable {
    @Column(name = "id_perfil")
    private int roleId;

    @Column(name = "id_vista")
    private int viewId;

}
