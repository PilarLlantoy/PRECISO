package com.inter.proyecto_intergrupo.model.parametric;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_repos_simultaneas")
public class ReposYSimultaneas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "codigo")
    private String codigo;

    @Column(name = "cod_nombre")
    private String codNombre;

    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "cuenta_pyg")
    private String cuentaPyG;

    @Column(name = "descripcion_cuenta_pyg")
    private String descripcionCuentaPyG;

}

