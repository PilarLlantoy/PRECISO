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
@Table(name = "preciso_parametria_pyg")
public class PyG {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "anio")
    private String anio;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "cuenta_h")
    private String cuentaH;

    @Column(name = "nombre_cuenta")
    private String nombreCuenta;

    @Column(name = "nombre_cuenta_h")
    private String nombreCuentaH;

    @Column(name = "tipo")
    private String tipo;

    @Column(name = "stage")
    private String stage;

    @Column(name = "divisa")
    private String divisa;

    @Column(name = "valor")
    private Double valor;

    @Column(name = "naturaleza")
    private String naturaleza;

}
