package com.inter.proyecto_intergrupo.model.parametric;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "preciso_divisas_valor")
public class ChangeCurrency implements Serializable
{
    @Id
    @Column(name = "fecha")
    private Date fecha;

    @Id
    @Column(name = "divisa")
    private String divisa;

    @Column(name = "valor")
    private Double valor;
}
