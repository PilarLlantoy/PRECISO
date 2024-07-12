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
@Table(name = "preciso_cuenta_generica")
public class GenericAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "empresa")
    private String empresa;

    @Column(name = "cod_cons")
    private String codCons;

    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "observaciones")
    private String observaciones;

    @Column(name = "divisa")
    private String divisa;

    @Column(name = "concepto")
    private String concepto;

    @Column(name = "fecha")
    private String fecha;

}
