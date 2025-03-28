package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.admin.UserConciliationRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ConciliationRepository;
import com.inter.proyecto_intergrupo.repository.parametric.LogConciliationRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ParametrosReportesRepository;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ParametrosReportesService {

    @Autowired
    private final ParametrosReportesRepository parametrosReportesRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private UserConciliationRepository userConciliationRepository;

    @Autowired
    private EventTypeService eventTypeService;

    @Autowired
    private UserService userService;

    @Autowired
    private ConciliationRouteService conciliationRouteService;

    @Autowired
    private LogConciliationRepository logConciliationRepository;

    @Autowired
    public ParametrosReportesService(ParametrosReportesRepository parametrosReportesRepository) {
        this.parametrosReportesRepository = parametrosReportesRepository;
    }

    public List <ParametrosReportes> findAll(){return parametrosReportesRepository.findAll();}
    public List<ParametrosReportes> findAllActive() {
        return parametrosReportesRepository.findByActivo(true);
    }

    public ParametrosReportes findById(int id){
        return parametrosReportesRepository.findAllById(id);
    }

    public ParametrosReportes findByName(String nombre){
        return parametrosReportesRepository.findAllByNombre(nombre);
    }

    public ParametrosReportes modificar(ParametrosReportes parametro){
        parametrosReportesRepository.save(parametro);
       return parametro;
    }

    public List<Object[]> findCampoReportexParamxFuentexCampoFuente(
            int parametroId,int fuenteId,int campoFuenteId) {
        Query query = entityManager.createNativeQuery(
                "select id_campo, detalle from preciso_validaciones_parametros_reportes a\n" +
                        "  left join preciso_campos_param_reportes b on campo_reporte_id=id_campo\n" +
                        "  where a.id_fuente=:fuenteId and a.id_parametro=:parametroId and a.campo_fuente_id=:campoFuenteId");
        query.setParameter("parametroId", parametroId);
        query.setParameter("fuenteId", fuenteId);
        query.setParameter("campoFuenteId", campoFuenteId);
        return query.getResultList();
    }


}
