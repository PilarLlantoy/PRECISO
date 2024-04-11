package com.inter.proyecto_intergrupo.model.ifrs9;

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
@Table(name = "nexco_creacion_plano")
public class AccountCreationPlane {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_creacion_cuenta")
    private Long idCreacionCuentas;

    @Column(name = "EMPRESA")
    private String EMPRESA;

    @Column(name = "NUMERO_CUENTA")
    private String NUMEROCUENTA;

    @Column(name = "CUENTA4")
    private String CUENTA4;

    @Column(name = "SUBCUENTA2")
    private String SUBCUENTA2;

    @Column(name = "SUB")
    private String SUB;

    @Column(name = "SEG")
    private String SEG;

    @Column(name="STAG")
    private String STAG;

    @Column(name = "NOMBRE_CUENTA")
    private String NOMBRECUENTA;

    @Column(name = "NOMBRE_CORTO_CUENTA")
    private String NOMBRECORTOCUENTA;

    @Column(name = "TIPOCTA")
    private String TIPOCTA;

    @Column(name = "INDIC_LI")
    private String INDICLI;

    @Column(name="CLAVE_ACCESO")
    private String CLAVEACCESO;

    @Column(name = "MON")
    private String MON;

    @Column(name = "TICTOO1")
    private String TICTOO1;

    @Column(name="TICTOO2")
    private String TICTOO2;

    @Column(name = "TICTOO3")
    private String TICTOO3;

    @Column(name = "TICTOO4")
    private String TICTOO4;

    @Column(name = "TICTOO5")
    private String TICTOO5;

    @Column(name = "TICENAO")
    private String TICENAO;

    @Column(name = "CENAUO01")
    private String CENAUO01;

    @Column(name = "CENAUO02")
    private String CENAUO02;

    @Column(name = "CENAUO03")
    private String CENAUO03;

    @Column(name = "CENAUO04")
    private String CENAUO04;

    @Column(name = "CENAUO05")
    private String CENAUO05;

    @Column(name = "CENAUO06")
    private String CENAUO06;

    @Column(name = "CENAUO07")
    private String CENAUO07;

    @Column(name = "CENAUO08")
    private String CENAUO08;

    @Column(name = "CENAUO09")
    private String CENAUO09;

    @Column(name = "TICTOD1")
    private String TICTOD1;

    @Column(name = "TICTOD2")
    private String TICTOD2;

    @Column(name = "TICTOD3")
    private String TICTOD3;

    @Column(name = "TICTOD4")
    private String TICTOD4;

    @Column(name = "TICTOD5")
    private String TICTOD5;

    @Column(name = "TICENAD")
    private String TICENAD;

    @Column(name = "CENAUD01")
    private String CENAUD01;

    @Column(name = "CENAUD02")
    private String CENAUD02;

    @Column(name = "CENAUD03")
    private String CENAUD03;

    @Column(name = "CENAUD04")
    private String CENAUD04;

    @Column(name = "CENAUD05")
    private String CENAUD05;

    @Column(name = "CENAUD06")
    private String CENAUD06;

    @Column(name = "CENAUD07")
    private String CENAUD07;

    @Column(name = "CENAUD08")
    private String CENAUD08;

    @Column(name = "CENAUD09")
    private String CENAUD09;

    @Column(name = "TIPAPUN")
    private String TIPAPUN;

    @Column(name = "SIGINIC")
    private String SIGINIC;

    @Column(name = "INDICADOR_PROCESO_DE_BAJA")
    private String INDICADORPROCESODEBAJA;

    @Column(name = "IND_CUENTA_INVENTARIABLE")
    private String INDCUENTAINVENTARIABLE;

    @Column(name = "IND_CUENTA_OPERACIONAL")
    private String INDCUENTAOPERACIONAL;

    @Column(name = "COD_CDCONMEX")
    private String CODCDCONMEX;

    @Column(name = "COD_REPREGUL")
    private String CODREPREGUL;

    @Column(name = "INTERFAZ")
    private String INTERFAZ;

    @Column(name = "RESPONSABLE_CONTROL_OPERATIVO_NIVEL1")
    private String RESPONSABLECONTROLOPERATIVONIVEL1;

    @Column(name = "RESPONSABLE_CONTROL_OPERATIVO_CEN_OPE_RES1")
    private String RESPONSABLECONTROLOPERATIVOCENOPERES1;

    @Column(name = "RESPONSABLE_CONTROL_OPERATIVO_CEN_OPE_RES2")
    private String RESPONSABLECONTROLOPERATIVOCENOPERES2;

    @Column(name = "RESPONSABLE_CONTROL_OPERATIVO_CEN_OPE_RES3")
    private String RESPONSABLECONTROLOPERATIVOCENOPERES3;

    @Column(name = "RESPONSABLE_CONTROL_OPERATIVO_CEN_OPE_RES4")
    private String RESPONSABLECONTROLOPERATIVOCENOPERES4;

    @Column(name = "RESPONSABLE_CONTROL_OPERATIVO_CEN_OPE_RES5")
    private String RESPONSABLECONTROLOPERATIVOCENOPERES5;

    @Column(name = "RESPONSABLE_CONTROL_OPERATIVO_CEN_OPE_RES6")
    private String RESPONSABLECONTROLOPERATIVOCENOPERES6;

    @Column(name = "RESPONSABLE_CONTROL_DE_GESTION_NIVEL2")
    private String RESPONSABLECONTROLDEGESTIONNIVEL2;

    @Column(name = "RESPONSABLE_CONTROL_DE_GESTION_CEN_GES_RES1")
    private String RESPONSABLECONTROLDEGESTIONCENGESRES1;

    @Column(name = "RESPONSABLE_CONTROL_DE_GESTION_CEN_GES_RES2")
    private String RESPONSABLECONTROLDEGESTIONCENGESRES2;

    @Column(name = "RESPONSABLE_CONTROL_DE_GESTION_CEN_GES_RES3")
    private String RESPONSABLECONTROLDEGESTIONCENGESRES3;

    @Column(name = "RESPONSABLE_CONTROL_DE_GESTION_CEN_GES_RES4")
    private String RESPONSABLECONTROLDEGESTIONCENGESRES4;

    @Column(name = "RESPONSABLE_CONTROL_DE_GESTION_CEN_GES_RES5")
    private String RESPONSABLECONTROLDEGESTIONCENGESRES5;

    @Column(name = "RESPONSABLE_CONTROL_DE_GESTION_CEN_GES_RES6")
    private String RESPONSABLECONTROLDEGESTIONCENGESRES6;

    @Column(name = "RESPONSABLE_CONTROL_ADMINISTRATIVO_NIVEL3")
    private String RESPONSABLECONTROLADMINISTRATIVONIVEL3;

    @Column(name = "RESPONSABLE_CONTROL_ADMINISTRATIVO_CEN_ADM_RES1")
    private String RESPONSABLECONTROLADMINISTRATIVOCENADMRES1;

    @Column(name = "RESPONSABLE_CONTROL_ADMINISTRATIVO_CEN_ADM_RES2")
    private String RESPONSABLECONTROLADMINISTRATIVOCENADMRES2;

    @Column(name = "RESPONSABLE_CONTROL_ADMINISTRATIVO_CEN_ADM_RES3")
    private String RESPONSABLECONTROLADMINISTRATIVOCENADMRES3;

    @Column(name = "RESPONSABLE_CONTROL_ADMINISTRATIVO_CEN_ADM_RES4")
    private String RESPONSABLECONTROLADMINISTRATIVOCENADMRES4;

    @Column(name = "RESPONSABLE_CONTROL_ADMINISTRATIVO_CEN_ADM_RES5")
    private String RESPONSABLECONTROLADMINISTRATIVOCENADMRES5;

    @Column(name = "RESPONSABLE_CONTROL_ADMINISTRATIVO_CEN_ADM_RES6")
    private String RESPONSABLECONTROLADMINISTRATIVOCENADMRES6;

    @Column(name = "CONTRAPARTIDA_DE_ORDEN")
    private String CONTRAPARTIDADEORDEN;

    @Column(name = "CONTRAPARTIDA_DE_RESULTADOS_DH")
    private String CONTRAPARTIDADERESULTADOSDH;

    @Column(name = "CODIGO_GESTION")
    private String CODIGOGESTION;

    @Column(name = "EPIGRAFE")
    private String EPIGRAFE;

    @Column(name = "CONSOLID")
    private String CONSOLID;

    @Column(name = "CODIGO_DE_CONTROL")
    private String CODIGODECONTROL;

    @Column(name = "DIAS_DE_PLAZO")
    private String DIASDEPLAZO;

    @Column(name = "INDICADOR_DE_LA_CUENTA")
    private String INDICADORDELACUENTA;

    @Column(name = "TIPO_DE_APUNTE")
    private String TIPODEAPUNTE;

    @Column(name = "INVENTARIABLE")
    private String INVENTARIABLE;

}
