package com.inter.proyecto_intergrupo.model.information;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_cambios_cuentas")
public class ChangeAccountQuery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cambio")
    private Long idCambio;

    @Column(name = "empresa")
    private String empresa;

    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "codicons_anterior")
    private String codiconsAnterior;

    @Column(name = "codicons_nuevo")
    private String codiconsNuevo;

    @Column(name = "fecha_corte")
    private String fechaCorte;

    @Column(name = "perimetro_ifrs9")
    private String perimetroIfrs9;

    @Column(name = "observacion")
    private String observacion;

    @Column(name = "cambio")
    private String cambio;

    @Column(name = "tipo_marca")
    private String tipoMarca;
}
