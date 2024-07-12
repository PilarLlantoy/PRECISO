package com.inter.proyecto_intergrupo.model.parametric;

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
@Table(name = "preciso_reclasificaciones")
public class Reclassification {

    @Id
    @Column(name = "nit_contraparte")
    private Long nitContraparte;

    @Column(name = "cuenta_local")
    private String cuentaLocal;

    @Column(name = "cuenta_local_reclasificada")
    private String cuentaLocalReclasificada;

}
