package com.inter.proyecto_intergrupo.model.briefcase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_uvr_icrf")
public class UVRIcrf implements Serializable{

    @Id
    @Column(name = "fecha")
    private Date fecha;

    @Column(name = "peso_cop_uvr")
    private Double pesoCopUvr;

    @Column(name = "variacion_anual")
    private Double variacionAnual;

    @Column(name = "periodo")
    private String periodo;

}
