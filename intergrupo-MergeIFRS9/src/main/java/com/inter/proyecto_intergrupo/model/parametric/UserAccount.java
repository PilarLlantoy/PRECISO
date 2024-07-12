package com.inter.proyecto_intergrupo.model.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
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
@Table(name = "preciso_user_account")
public class UserAccount implements Serializable {

    @Id
    @ManyToOne()
    @JoinColumn(name = "id_usuario")
    private User idUsuario;

    @Id
    @ManyToOne()
    @JoinColumn(name = "cuenta_local")
    private ResponsibleAccount idCuentaLocal;

}
