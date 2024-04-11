package com.inter.proyecto_intergrupo.model.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportPrimary {
    private String origen;
    private String instrumento;
    private String subproducto;
    private String cves;
    private String acumulado;
    private String contrato;
    private String stageEspana;
    private String productoEspana;
    private String sector;
    private String signo;
}
