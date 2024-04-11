package com.inter.proyecto_intergrupo.model.parametric;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_terceros")
public class Third {

    @Id
    @Column(name = "nit_contraparte")
    String nit;

    @Column(name = "contraparte")
    String contraparte;

    @Column(name = "codigo_cliente")
    String codigoCliente;

    @Column(name = "tipo")
    int tipo;

    @Column(name = "dv")
    int dv;

    @Column(name = "fecha")
    Date fecha;

    @Column(name = "yntp")
    String yntp;

    @Column(name = "marca_tipo_institucion")
    int marcaTipoInstitucion;
}
