package com.inter.proyecto_intergrupo.controller.ifrs9;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_perimeter")
public class PerimeterIfrs9 {

    @Id
    @Column(name="cuenta")
    private String cuenta;

    @Column(name="descripcion")
    private String descripcion;

    @Column(name="entrada")
    private String entrada;
}
