package com.inter.proyecto_intergrupo.model.ifrs9;

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
@Table(name = "nexco_segmentos_final_temp")
public class SegmentosFinalTemp implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "identificacion")
    private String identificacion;

    @Column(name = "numero_cliente")
    private String numeroCliente;

    @Column(name = "tipo_persona")
    private String tipoPersona;

    @Column(name = "segmento_finrep_new")
    private String segmentoFinrepNew;

    @Column(name = "observaciones")
    private String observaciones;

}
