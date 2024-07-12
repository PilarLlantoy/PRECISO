package com.inter.proyecto_intergrupo.model.parametric;

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
@Table(name = "preciso_parametrica_metodo_comer")
public class ComerParametric implements Serializable {

    @Id
    @Column(name = "cuenta_local")
    String cuentaLocal;

    @Id
    @Column(name = "clase")
    String clase;

    @Column(name = "nombre_clase")
    String nombreClase;

    @Id
    @Column(name = "doc_compr")
    String docCompr;

    @Column(name = "prorrata_iva")
    String proIva;

    @Column(name = "tipo_importe")
    String importe;
}
