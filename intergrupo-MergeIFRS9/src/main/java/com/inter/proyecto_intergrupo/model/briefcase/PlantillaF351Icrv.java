package com.inter.proyecto_intergrupo.model.briefcase;

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
@Table(name = "nexco_plantilla_f351_icrv")
public class PlantillaF351Icrv implements Serializable{

    @Id 
    @Column(name = "id_f")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idF;

    @Column(name = "fecha_proceso")
    private String fechaProceso;

    @Column(name = "nro_asignado")
    private String nroAsignado;

    @Column(name = "codigo_puc")
    private String codigoPuc;

    @Column(name = "nit")
    private String nit;

    @Column(name = "documento_emisor")
    private String documentoEmisor;

    @Column(name = "razon_social_emisor")
    private String razonSocialEmisor;

    @Column(name = "vinculado")
    private String vinculado;

    @Column(name = "aval")
    private String aval;

    @Column(name = "tipo_identificacion_aval")
    private String tipoIdentificacionAval;

    @Column(name = "identificacion_aval")
    private String identificacionAval;

    @Column(name = "razon_social_aval")
    private String razonSocialAval;

    @Column(name = "periodo")
    private String periodo;

}
