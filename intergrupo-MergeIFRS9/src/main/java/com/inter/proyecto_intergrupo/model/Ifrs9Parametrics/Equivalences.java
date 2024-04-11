package com.inter.proyecto_intergrupo.model.Ifrs9Parametrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_equivalencias_ifrs")
public class Equivalences implements Serializable{

    @Id
    @Column(name = "cuenta_contable")
    private String cuentaContable;

    @Column(name = "contrapartida")
    private String contrapartida;

}
