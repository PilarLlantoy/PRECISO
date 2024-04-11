package com.inter.proyecto_intergrupo.service.eeffconsolidated;


import com.inter.proyecto_intergrupo.model.eeffConsolidated.DatesLoadEeFF;
import com.inter.proyecto_intergrupo.model.parametric.StatusInfo;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;

import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.DatesLoadEeffRepository;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class DatesLoadEeffService {

    @Autowired
    private DatesLoadEeffRepository datesLoadEeffRepository;


    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private statusInfoRepository statusInfoRepositoryL;

    public DatesLoadEeffService(DatesLoadEeffRepository datesLoadEeffRepository, EntityManager entityManager) {
        this.datesLoadEeffRepository = datesLoadEeffRepository;
        this.entityManager = entityManager;
    }

    public List<DatesLoadEeFF> getAllEeffConsolidatedVerificacion(String periodo) {
        return datesLoadEeffRepository.findByPeriodo(periodo);
    }

    public List<StatusInfo> getEliminacionesByPeriodo(String periodo) {
        List<String> eliminaciones = List.of("ELIMINACIONES CARGUE CONFIRMADO", "ELIMINACIONES AUTORIZACION","ELIMINACIONES PATRIMONIALES CONFIRMADO", "AUTORIZACIÓN ELIMINACIONES PATRIMONIALES", "VALOR EN RIESGO TOTAL","PATRIMONIO TECNICO");
        return statusInfoRepositoryL.findByInputInAndPeriodo(eliminaciones, periodo);
    }

    public void guardarFechasEnTabla(String entidad, String periodo, String nombreArchivo, Date fechaCargue) {
        DatesLoadEeFF existingRecord = datesLoadEeffRepository.findByEntidadAndPeriodo(entidad, periodo);

        if (existingRecord == null) {
            DatesLoadEeFF datesLoadEeFF = new DatesLoadEeFF();
            datesLoadEeFF.setEntidad(entidad);
            datesLoadEeFF.setPeriodo(periodo);
            datesLoadEeFF.setEstado("PENDING");

            if (nombreArchivo.equals("PUC")) {
                datesLoadEeFF.setCarguePuc(fechaCargue != null ? fechaCargue : new Date());
            } else if (nombreArchivo.equals("Eeff")) {
                datesLoadEeFF.setCargueEeff(fechaCargue != null ? fechaCargue : new Date());
            } else if (nombreArchivo.equals("SFC")) {
                datesLoadEeFF.setCargueSoporteSfc(fechaCargue != null ? fechaCargue : new Date());
            }
            datesLoadEeffRepository.save(datesLoadEeFF);
        } else {
            if (nombreArchivo.equals("PUC")) {
                existingRecord.setCarguePuc(fechaCargue != null ? fechaCargue : new Date());
            } else if (nombreArchivo.equals("Eeff")) {
                existingRecord.setCargueEeff(fechaCargue != null ? fechaCargue : new Date());
            } else if (nombreArchivo.equals("SFC")) {
                existingRecord.setCargueSoporteSfc(fechaCargue != null ? fechaCargue : new Date());
            }
            datesLoadEeffRepository.save(existingRecord);
        }
        validarFechasYActualizarEstado(entidad, periodo);
    }

    public void validarFechasYActualizarEstado(String entidad, String periodo) {
        DatesLoadEeFF record = datesLoadEeffRepository.findByEntidadAndPeriodo(entidad, periodo);

        if (record != null) {
            Date carguePuc = record.getCarguePuc();
            Date cargueEeff = record.getCargueEeff();
            Date cargueSoporteSfc = record.getCargueSoporteSfc();

            if (carguePuc != null && cargueEeff != null && cargueSoporteSfc != null) {
                record.setEstado("SUCCESS");
                datesLoadEeffRepository.save(record);
            } else {
                record.setEstado("PENDING");
                datesLoadEeffRepository.save(record);

            }
        }
    }
    public void  guardarInfoBanco(String entidad, String periodo) {

        String pucQuery = "SELECT TOP 1 * FROM cuentas_puc";

        Query pucSqlQuery = entityManager.createNativeQuery(pucQuery);
        List<Object[]> pucResultList = pucSqlQuery.getResultList();

        if (!pucResultList.isEmpty()) {
            guardarFechasEnTabla("Banco", periodo, "PUC", null);
            // Después de guardar las fechas, actualiza el estado
            actualizarEstado("Banco", periodo);
        }

    }

    private void actualizarEstado(String entidad, String periodo) {
        DatesLoadEeFF record = datesLoadEeffRepository.findByEntidadAndPeriodo(entidad, periodo);

        if (record != null) {
            record.setEstado("CONFIRMADO");
            datesLoadEeffRepository.save(record);
        }
    }

}
