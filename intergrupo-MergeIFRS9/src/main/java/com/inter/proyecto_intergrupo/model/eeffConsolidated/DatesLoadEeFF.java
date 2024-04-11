package com.inter.proyecto_intergrupo.model.eeffConsolidated;
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

@Table(name = "nexco_fecha_cargue_EEFF")

public class DatesLoadEeFF {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fecha")
    private Long idFecha;

    @Column(name = "entidad")
    String entidad;

    @Column(name = "cargue_puc")
    Date  carguePuc;

    @Column(name = "cargue_eeff")
    Date cargueEeff;

    @Column(name = "cargue_soporte_sfc")
    Date cargueSoporteSfc;

    @Column(name = "periodo")
    String periodo;

    @Column(name = "estado")
    String estado;

    @Lob
    @Column(name = "soporte_sfc_descarga", columnDefinition = "VARBINARY(MAX)")
    private byte[] soporteSfcDescarga;

    }

