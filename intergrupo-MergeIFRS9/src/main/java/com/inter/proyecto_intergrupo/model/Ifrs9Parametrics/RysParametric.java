package com.inter.proyecto_intergrupo.model.Ifrs9Parametrics;

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
@Table(name = "nexco_rys_parametrica")
public class RysParametric implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rys")
    private Long idRys;

    @Column(name = "codigo")
    private String codigo;

    @Column(name = "codigo_nombre")
    private String codigoNombre;

    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "cuenta_pyg")
    private String cuentaPyg;

    @Column(name = "cuenta_neocon")
    private String cuentaNeocon;

    @Column(name = "cuenta_neocon_pyg")
    private String cuentaNeoconPyg;

}
