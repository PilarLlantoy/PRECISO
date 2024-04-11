package com.inter.proyecto_intergrupo.model.temporal;

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
@Table(name = "nexco_provision_general_interes_temp")
public class GeneralInterestProvisionTemporal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prov")
    private Long idProv;

    @Column(name = "tp")
    private String tp;

    @Column(name = "nit")
    private String nit;

    @Column(name = "tp_car")
    private String tpCar;

    @Column(name = "suc_obl_al")
    private String sucOblAl;

    @Column(name = "numero_op")
    private String numeroOp;

    @Column(name = "sucursal")
    private String sucursal;

    @Column(name = "lin_subpro")
    private String linSubpro;

    @Column(name = "cartera")
    private String cartera;

    @Column(name = "prod_alta")
    private String prodAlta;

    @Column(name = "alivios")
    private String alivios;

    @Column(name = "alivios_covid")
    private String aliviosCovid;

    @Column(name = "capital")
    private Double capital;

    @Column(name = "intereses")
    private Double intereses;

    @Column(name = "vr_int_cte")
    private Double vrIntCte;

    @Column(name = "vr_mora")
    private Double vrMora;

    @Column(name = "cxc")
    private Double cxc;

    @Column(name = "provis_cap")
    private Double provisCap;

    @Column(name = "provis_int")
    private Double provisInt;

    @Column(name = "califica_7_ini")
    private String califica7Ini;

    @Column(name = "califica_ini")
    private String calificaIni;

    @Column(name = "fec_vto_i")
    private String fecVtoI;

    @Column(name = "fecha_ini_mora")
    private String fechaIniMora;

    @Column(name = "lineas_sfc")
    private String lineasSfc;

    @Column(name = "nueva_califica")
    private String nuevaCalifica;

    @Column(name = "nueva_califica_7")
    private String nuevaCalifica7;

    @Column(name = "califica")
    private String califica;

    @Column(name = "califica_7")
    private String califica7;

    @Column(name = "aceptado")
    private String aceptado;

    @Column(name = "aplicado")
    private String aplicado;

    @Column(name = "estado")
    private String estado;

    @Column(name = "fecha_vcto_alivio")
    private String fechaVctoAlivio;

    @Column(name = "fecha_vcto_pad")
    private String fechaVctoPad;

    @Column(name = "periodo_gracia_int")
    private String periodoGraciaInt;

    @Column(name = "tipo")
    private String tipo;

    @Column(name = "bolsillos")
    private String bolsillos;

    @Column(name = "filtro")
    private String filtro;

    @Column(name = "interes")
    private Double interes;

    @Column(name = "prov_interes_ini")
    private Double provInteresIni;

    @Column(name = "prov_gral_int_fm")
    private Double provGralIntFm;

    @Column(name = "califica_7_pda")
    private String califica7Pda;

    @Column(name = "marca")
    private String marca;

    @Column(name = "califica_pda")
    private String calificaPda;

    @Column(name = "prov_gral_int_100")
    private Double provGralInt100;

    @Column(name = "marca_mrco")
    private String marcaMrco;

    @Column(name = "check_int")
    private String checkInt;

    @Column(name = "saldo_intereses_bolsillo")
    private Double saldoInteresesBolsillo;

    @Column(name = "intereses_bolsillos")
    private Double interesesBolsillo;

    @Column(name = "prov_gral_int")
    private Double provGralInt;

    @Column(name = "check_bolsillo")
    private String checkBolsillo;

    @Column(name = "check_prov_int")
    private String checkProvInt;

    @Column(name = "tp_fidei")
    private String tpFidei;

    @Column(name = "nit_fidei")
    private String nitFidei;

    @Column(name = "tp_def")
    private String tpDef;

    @Column(name = "nit_def")
    private String nitDef;

    @Column(name = "ciiu")
    private String ciiu;

    @Column(name = "nombre_2")
    private String nombre2;

    @Column(name = "corazu")
    private String corazu;

    @Column(name = "sector")
    private String sector;

    @Column(name = "codigo_ifrs9")
    private String codigoIfrs9;

    @Column(name = "nit_sin_dv")
    private String nitSinDv;

    @Column(name = "dv")
    private String dv;

    @Column(name = "banca")
    private String banca;

    @Column(name = "nivel_ventas_2")
    private String nivelVentas2;

    @Column(name = "periodo")
    private String periodo;

}
