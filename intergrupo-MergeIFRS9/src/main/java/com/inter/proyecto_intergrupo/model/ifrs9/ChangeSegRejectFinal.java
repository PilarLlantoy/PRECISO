package com.inter.proyecto_intergrupo.model.ifrs9;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nexco_cambio_segementos_rechazos_final")
public class ChangeSegRejectFinal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cambio_seg")
    private Long idCambioSeg;

    @Column(name = "codigo_cliente")
    private String codigoCliente;

    @Column(name = "tipo_cliente")
    private String tipoCliente;

    @Column(name = "codigo_tipo_inst")
    private String codigoTipoInst;

    @Column(name = "segmento_actual")
    private String segmentoActual;

    @Column(name = "contrato")
    private String contrato;

    @Column(name = "cuenta_rechazo")
    private String cuentaRechazo;

    @Column(name = "segmento_rechazo")
    private String segmentoRechazo;

    @Column(name = "segmento_real")
    private String segmentoReal;

    @Column(name="tipo_de_rechazo")
    private String tipoDeRechazo;

    @Column(name="nombre_cliente")
    private String nombreCliente;

    @Column(name="corazu")
    private String corazu;

    @Column(name="sub_corazu")
    private String subCorazu;

    @Column(name="tercero")
    private String tercero;

    @Column(name="ciuu")
    private String ciuu;

    @Column(name="numero_empleados")
    private Double numeroEmpleados;

    @Column(name="total_activos")
    private Double totalActivos;

    @Column(name="total_ventas")
    private Double totalVentas;

    @Column(name="periodo")
    private String periodo;

    @Column(name="origen")
    private String origen;

}
