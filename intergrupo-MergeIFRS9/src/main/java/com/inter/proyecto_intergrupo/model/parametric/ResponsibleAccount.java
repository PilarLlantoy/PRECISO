package com.inter.proyecto_intergrupo.model.parametric;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_cuentas_responsables")
public class ResponsibleAccount {

    @Id
    @Column(name = "cuenta_local")
    private Long cuentaLocal;

    @Column(name = "input")
    private String entrada;

    @Column(name = "componente")
    private String componente;

    @Column(name = "aplica_sicc")
    private Boolean sicc;

    @Column(name = "aplica_base_fiscal")
    private Boolean baseFiscal;

    @Column(name = "aplica_metodologia")
    private Boolean metodologia;

    @Column(name = "aplica_mis")
    private Boolean mis;

    @Column(name="centro")
    private String centro;

    @OneToMany(mappedBy = "idCuentaLocal", cascade = CascadeType.ALL)
    private List<UserAccount> userAccountList;
}
