package com.inter.proyecto_intergrupo.model.reportNIC34;

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
@Table(name = "nexco_param_cuenta_banco")
public class AccountBanco implements Serializable{

    @Id
    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "naturaleza")
    private String naturaleza;

}
