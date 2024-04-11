package com.inter.proyecto_intergrupo.model.information;

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
@Table(name = "nexco_neocon60_cuadre")
public class Neocon60Cuadre implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Neocon60")
    private Long idNeocon60;

    @Column(name = "codicons")
    private String codicons;

    @Column(name = "divisa")
    private String divisa;

    @Column(name = "base")
    private Double base;

    @Column(name = "ajuste")
    private Double ajuste;

    @Column(name = "ajuste2")
    private Double ajuste2;

    @Column(name = "carga")
    private Double carga;

    @Column(name = "intergrupo")
    private Double intergrupo;

    @Column(name = "contingentes")
    private Double contingentes;

    @Column(name = "plano")
    private Double plano;

    @Column(name = "total")
    private Double total;

    @Column(name = "periodo")
    private String periodo;

    @Column(name = "estado")
    private String estado;

}
