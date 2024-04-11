package com.inter.proyecto_intergrupo.model.ifrs9;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_anexos")
public class Anexo implements Serializable{

    @Id
    @Column(name = "identificacion")
    private String ident;

    @Id
    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "empresa")
    private String empresa;

    @Id
    @Column(name = "contrato")
    private String contrato;

    @Column(name = "divisa")
    private String divisa;

    @Column(name = "aplicativo")
    private String aplicativo;

    @Column(name = "centro")
    private String centro;

    @Column(name = "tipo")
    private String tipo;

    @Column(name = "digito_verif")
    private String digitover;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "forigen")
    private String forigen;

    @Column(name = "fcierr")
    private String fcierr;

    @Column(name = "saldo")
    private Double saldo;

    @Id
    @Column(name = "periodo")
    private String perido;


}
