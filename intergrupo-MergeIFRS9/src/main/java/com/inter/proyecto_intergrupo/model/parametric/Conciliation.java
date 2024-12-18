package com.inter.proyecto_intergrupo.model.parametric;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_conciliaciones")
public class Conciliation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "detalle")
    private String detalle;

    @Column(name = "periodicidad")
    private String periodicidad;

    @ManyToOne
    @JoinColumn(name = "id_pais", nullable = false)
    private Country pais;

    @ManyToOne
    @JoinColumn(name = "id_sf", nullable = false)
    private SourceSystem sf;

    @Column(name = "activo", columnDefinition = "BIT DEFAULT 1")
    private boolean activo = true;

    @Column(name = "festivo")
    private boolean festivo;

    @ManyToOne
    @JoinColumn(name = "id_sfc", nullable = false)
    private SourceSystem sfc;
    /*

    @ManyToOne
    @JoinColumn(name = "centro")
    private CampoRC centro;

    @ManyToOne
    @JoinColumn(name = "cuenta")
    private AccountingRoute cuenta;

    @ManyToOne
    @JoinColumn(name = "divisa")
    private AccountingRoute divisa;

    @ManyToOne
    @JoinColumn(name = "saldo")
    private AccountingRoute saldo;*/

    @Column(name = "centro")
    private String centro;

    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "divisa")
    private String divisa;

    @Column(name = "saldo")
    private String saldo;

    @Builder.Default
    @Column(name = "aplica_lunes", columnDefinition = "BIT DEFAULT 0")
    private boolean lunes = false;

    @Builder.Default
    @Column(name = "aplica_martes", columnDefinition = "BIT DEFAULT 0")
    private boolean martes = false;

    @Builder.Default
    @Column(name = "aplica_miercoles", columnDefinition = "BIT DEFAULT 0")
    private boolean miercoles = false;

    @Builder.Default
    @Column(name = "aplica_jueves", columnDefinition = "BIT DEFAULT 0")
    private boolean jueves = false;

    @Builder.Default
    @Column(name = "aplica_viernes", columnDefinition = "BIT DEFAULT 0")
    private boolean viernes = false;

    @Builder.Default
    @Column(name = "aplica_sabado", columnDefinition = "BIT DEFAULT 0")
    private boolean sabado = false;

    @Builder.Default
    @Column(name = "aplica_domingo", columnDefinition = "BIT DEFAULT 0")
    private boolean domingo = false;

    @ManyToOne
    @JoinColumn(name = "id_rc")
    private AccountingRoute rutaContable;

    @OneToMany(mappedBy = "conciliacion", cascade = CascadeType.ALL)
    private List<AccountConcil> arregloCuentas;

    @OneToMany(mappedBy = "conciliacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserConciliation> userConciliations = new ArrayList<>();

}
