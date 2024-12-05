package com.inter.proyecto_intergrupo.model.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_user_conciliacion")
public class UserConciliation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private User usuario;

    @ManyToOne
    @JoinColumn(name = "id_conciliacion", nullable = false)
    private Conciliation conciliacion;

    @Column(name = "rol")
    @Enumerated(EnumType.STRING)
    private RoleConciliation rol;

    public enum RoleConciliation {
        TITULAR, BACKUP
    }
}
