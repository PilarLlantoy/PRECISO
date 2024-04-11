package com.inter.proyecto_intergrupo.model.reports;

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
@Table(name = "nexco_reclasificacion_all")
public class ReclassificationInterAll implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Integer id;

    @Column(name = "centro")
    String centro;

    @Column(name = "contrato")
    String contrato;

    @Column(name = "nit")
    String nit;

    @Column(name = "yntp")
    String yntp;

    @Column(name = "saldo")
    Double saldo;

    @Column(name = "cuenta")
    String cuenta;

    @Column(name = "cuenta_nueva")
    String cuentaNueva;

    @Column(name = "periodo")
    String periodo;

    @Column(name = "origen_info")
    String origenInfo;

}
