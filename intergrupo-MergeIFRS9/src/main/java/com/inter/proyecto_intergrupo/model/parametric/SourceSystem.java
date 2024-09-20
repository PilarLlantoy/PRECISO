package com.inter.proyecto_intergrupo.model.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_sistema_fuente")
public class SourceSystem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sf")
    private int id;

    @Column(name = "nombre_sf")
    @NotEmpty(message = "El nombre no puede estar vacio")
    private String nombre;

    @Column(name = "sigla_sf")
    private String sigla;

    @Column(name = "festivo")
    private boolean festivo;

    @ManyToOne
    @JoinColumn(name = "id_pais", nullable = false)
    private Country pais;

    @Builder.Default
    @Column(name = "activo", columnDefinition = "BIT DEFAULT 1")
    private boolean activo = true;

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

    @OneToMany(mappedBy = "sf", cascade = CascadeType.ALL)
    private List<Conciliation> conciliaciones;

    @OneToMany(mappedBy = "sfc", cascade = CascadeType.ALL)
    private List<Conciliation> conciliacionesContables;


    /*
    @OneToMany(mappedBy = "sfrc", cascade = CascadeType.ALL)
    private List<AccountingRoute> rutasContables;
    */

    @OneToMany(mappedBy = "sistemaFuente", cascade = CascadeType.ALL)
    private List<CloseDateSourceSystem> fechasCierre;

}
