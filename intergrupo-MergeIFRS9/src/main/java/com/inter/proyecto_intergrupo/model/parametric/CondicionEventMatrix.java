package com.inter.proyecto_intergrupo.model.parametric;

import lombok.*;

import javax.persistence.*;
@Getter
@Setter
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_condiciones_matriz_evento")
public class CondicionEventMatrix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_condicion_me")
    private int id;

    @Column(name = "valorCondicion")
    private String valorCondicion;

    @Column(name = "condicion")
    private String condicion;

    @ManyToOne
    @JoinColumn(name = "id_matriz", nullable = false)
    private EventMatrix matrizEvento;

    @ManyToOne
    @JoinColumn(name = "id_campo", nullable = false)
    private CampoRConcil campo;

    @Column(name = "estado", columnDefinition = "BIT DEFAULT 1")
    private boolean estado = true;

}
