package com.inter.proyecto_intergrupo.model.parametric;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_operacion_riesgo")
public class OperationAccount {

    @Id
    @Column(name = "cuenta_local")
    private String cuentaLocal;

    @Column(name = "tipo_operacion")
    private String operacion;

    @Column(name = "tipo_riesgo")
    private String riesgo;
}
