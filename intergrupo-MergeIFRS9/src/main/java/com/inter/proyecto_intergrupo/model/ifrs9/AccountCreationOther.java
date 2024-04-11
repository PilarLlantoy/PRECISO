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
@Table(name = "nexco_creacion_cuentas_otros")
public class AccountCreationOther implements Serializable{

    @Id
    @Column(name = "id_cuentas")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCuentas;

    @Column(name = "empresa")
    private String empresa;

    @Column(name = "nucta")
    private String nucta;

    @Column(name = "nombre_cuenta_larga")
    private String nombreCuentaLarga;

    @Column(name = "nombre_cuenta_corta")
    private String nombreCuentaCorta;

    @Column(name = "justificacion_global")
    private String justificacionGlobal;

    @Column(name = "dinamica_cuenta")
    private String dinamicaCuenta;

    @Column(name = "tipo_cta")
    private String tipoCta;

    @Column(name = "indic")
    private String indic;

    @Column(name = "clave_acceso")
    private String claveAcceso;

    @Column(name = "mon")
    private String mon;

    @Column(name = "centro_origen")
    private String centroOrigen;

    @Column(name = "centro_destino")
    private String centroDestino;

    @Column(name = "codigo_gestion")
    private String codigoGestion;

    @Column(name = "epigrafe")
    private String epigrafe;

    @Column(name = "consolid")
    private String consolid;

    @Column(name = "tipo_apunte")
    private String tipoApunte;

    @Column(name = "codigo_control")
    private String codigoControl;

    @Column(name = "indicador_cierre")
    private String indicadorCierre;

    @Column(name = "dias_plazo")
    private String diasPlazo;

    @Column(name = "indicador_cuenta")
    private String indicadorCuenta;

    @Column(name = "interfaz")
    private String interfaz;

    @Column(name = "responsable_control_operativo_cen_ope_res1")
    private String responsablecontroloperativocenoperes1;

    @Column(name = "responsable_control_operativo_cen_ope_res2")
    private String responsablecontroloperativocenoperes2;

    @Column(name = "responsable_control_operativo_cen_ope_res3")
    private String responsablecontroloperativocenoperes3;

    @Column(name = "responsable_control_operativo_cen_ope_res4")
    private String responsablecontroloperativocenoperes4;

    @Column(name = "responsable_control_operativo_cen_ope_res5")
    private String responsablecontroloperativocenoperes5;

    @Column(name = "responsable_control_operativo_cen_ope_res6")
    private String responsablecontroloperativocenoperes6;

    @Column(name = "responsable_control_de_gestion_cen_ges_res1")
    private String responsablecontroldegestioncengesres1;

    @Column(name = "responsable_control_de_gestion_cen_ges_res2")
    private String responsablecontroldegestioncengesres2;

    @Column(name = "responsable_control_de_gestion_cen_ges_res3")
    private String responsablecontroldegestioncengesres3;

    @Column(name = "responsable_control_de_gestion_cen_ges_res4")
    private String responsablecontroldegestioncengesres4;

    @Column(name = "responsable_control_de_gestion_cen_ges_res5")
    private String responsablecontroldegestioncengesres5;

    @Column(name = "responsable_control_de_gestion_cen_ges_res6")
    private String responsablecontroldegestioncengesres6;

    @Column(name = "responsable_control_administrativo_cen_adm_res1")
    private String responsablecontroladministrativocenadmres1;

    @Column(name = "responsable_control_administrativo_cen_adm_res2")
    private String responsablecontroladministrativocenadmres2;

    @Column(name = "responsable_control_administrativo_cen_adm_res3")
    private String responsablecontroladministrativocenadmres3;

    @Column(name = "responsable_control_administrativo_cen_adm_res4")
    private String responsablecontroladministrativocenadmres4;

    @Column(name = "responsable_control_administrativo_cen_adm_res5")
    private String responsablecontroladministrativocenadmres5;

    @Column(name = "responsable_control_administrativo_cen_adm_res6")
    private String responsablecontroladministrativocenadmres6;

    @Column(name = "contrapartida_orden")
    private String contrapartidaOrden;

    @Column(name = "tipo_cuenta_orden")
    private String tipoCuentaOrden;

    @Column(name = "contrapartida_resultados")
    private String contrapartidaResultados;

    @Column(name = "creador")
    private String creador;

    @Column(name = "periodo")
    private String periodo;

    @Column(name = "estado_general")
    private String estadoGeneral;

    @Column(name = "estado_consolidacion")
    private String estadoConsolidacion;

    @Column(name = "estado_control")
    private String estadoControl;

    @Column(name = "estado_gestion")
    private String estadoGestion;

    @Column(name = "comentario_consolidacion")
    private String comentarioConsolidacion;

    @Column(name = "comentario_control")
    private String comentarioControl;

    @Column(name = "comentario_gestion")
    private String comentarioGestion;

    @Column(name = "usuario_consolidacion")
    private String usuarioConsolidacion;

    @Column(name = "usuario_gestion")
    private String usuarioGestion;

    @Column(name = "usuario_control")
    private String usuarioControl;

    @Column(name = "estado_plano")
    private String estadoPlano;

    @Column(name = "lote_plano")
    private Long lotePlano;
}
