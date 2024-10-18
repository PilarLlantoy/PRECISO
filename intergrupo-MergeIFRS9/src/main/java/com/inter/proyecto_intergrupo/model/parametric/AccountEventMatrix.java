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
@Table(name = "preciso_cuentas_matriz_eventos")
public class AccountEventMatrix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cuenta_me")
    private int id;

    @Column(name = "tipo") //si es cargue cuenta 1 o 2
    private String tipo;

    @ManyToOne
    @JoinColumn(name = "id_ruta_contable", nullable = false)
    private AccountingRoute rutaContable;

    @ManyToOne
    @JoinColumn(name = "id_campo_ruta_contable", nullable = false)
    private CampoRC campoRutaContable;


    @Column(name = "construye_cuenta", columnDefinition = "BIT DEFAULT 0")
    private boolean construyeCuenta = false;

    @Column(name = "cuenta_ganancia")
    private String cuentaGanancia;

    @Column(name = "cuenta_perdida")
    private String cuentaPerdida;


    @Column(name = "parametrizado", columnDefinition = "BIT DEFAULT 0")
    private boolean parametrizado = false;


    @Column(name = "maneja_divisa", columnDefinition = "BIT DEFAULT 0")
    private boolean manejaDivisa = false;

    @ManyToOne
    @JoinColumn(name = "id_campo_divisa")
    private CampoRC campoDivisa;

    @Column(name = "convierte_divisa", columnDefinition = "BIT DEFAULT 0")
    private boolean convierteDivisa = false;

    @Column(name = "convierte_UVR_COP", columnDefinition = "BIT DEFAULT 0")
    private boolean convierteUVRaCOP = false;


    @Column(name = "maneja_formula", columnDefinition = "BIT DEFAULT 0")
    private boolean manejaFormula = false;

    @Column(name = "valor_absoluto", columnDefinition = "BIT DEFAULT 0")
    private boolean valorAbsoluto = false;


    @ManyToOne
    @JoinColumn(name = "id_campo_valor_cuenta")
    private CampoRC campoValorCuenta;

    @ManyToOne
    @JoinColumn(name = "id_campo_valor_operacion1")
    private CampoRC campoValorOp1;

    @Column(name = "operacion")
    private String operacion;

    @ManyToOne
    @JoinColumn(name = "id_campo_valor_operacion2")
    private CampoRC campoValorOp2;

    @Column(name = "valor_operacion2")
    private String valorOp2;


    @ManyToOne
    @JoinColumn(name = "id_matriz_evento", nullable = false)
    private EventMatrix matrizEvento;

    @Column(name = "estado", columnDefinition = "BIT DEFAULT 1")
    private boolean estado = true;


    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConstructionParameter> parametros = new ArrayList<>();


}
