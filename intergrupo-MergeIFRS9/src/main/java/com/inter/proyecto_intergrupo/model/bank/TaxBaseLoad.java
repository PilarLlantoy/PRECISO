package com.inter.proyecto_intergrupo.model.bank;

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
@Table(name = "nexco_base_fiscal_carga")
public class TaxBaseLoad {

    @Column(name = "peticion")
    private String peticion;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carga")
    private Long idCarga;

    @Column(name = "nit")
    private String nit;

    @Column(name = "estado")
    private String estado;

    @Column(name = "fecha")
    private String fecha;

    @Column(name = "fecha_fichero")
    private String fechaFichero;

}
