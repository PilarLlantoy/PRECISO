package com.inter.proyecto_intergrupo.model.parametric;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_contratos")
public class Contract {

    @Id
    @Column(name = "id_contrato")
    private String contrato;

    @Column(name = "tipo_aval_origen")
    private String tipoAvalOrigen;

    @Column(name = "archivo_entrada")
    private String archivoEntrada;

    @Column(name = "tipo_aval")
    private String tipoAval;

    @Column(name = "tipo_proceso")
    private String tipoProceso;

    @Column(name = "banco")
    private String banco;

    @ManyToOne()
    @JoinColumn(name="id_pais")
    private Country paisContrato;

}
