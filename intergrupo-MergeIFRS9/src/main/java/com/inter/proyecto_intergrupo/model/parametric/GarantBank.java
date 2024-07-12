package com.inter.proyecto_intergrupo.model.parametric;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_banco_garante")
public class GarantBank {

    @Column(name = "nit")
    private String nit;

    @Column(name = "nombre_banco_real")
    private String nombreBancoReal;

    @Column(name = "pais")
    private String pais;

    @Id
    @Column(name = "nombre_similar")
    private String nombreSimilar;
}
