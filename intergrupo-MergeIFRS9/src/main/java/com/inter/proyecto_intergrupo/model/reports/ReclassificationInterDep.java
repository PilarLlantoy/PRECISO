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
@Table(name = "nexco_reclasificacion_dep")
public class ReclassificationInterDep implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Integer id;

    @Column(name = "contrato")
    String contrato;

    @Column(name = "centro")
    String centro;

    @Column(name = "nit")
    String nit;

    @Column(name = "cuenta")
    String cuenta;

    @Column(name = "cuenta_nueva")
    String cuentaNueva;

    @Column(name = "producto")
    String producto;

    @Column(name = "sector_actual")
    String sectorActual;

    @Column(name = "saldo")
    Double saldo;

    @Column(name = "producto_nuevo")
    String productoNuevo;

    @Column(name = "sector_nuevo")
    String sectorNuevo;

    @Column(name = "periodo")
    String periodo;

}
