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

    public List<Object[]> findCampoReporteFiltro(int reporteId, String tipo) {
        System.out.println(reporteId + "  "+tipo);
        Query query = entityManager.createNativeQuery(
                "select id_campo, detalle, tipo from preciso_campos_param_reportes a\n" +
                        "  where a.id_parametro= :reporteId and a.filtrado=1 and a.tipo_filtro= :tipo");
        query.setParameter("reporteId", reporteId);
        query.setParameter("tipo", tipo);
        return query.getResultList();
    }


    public List<Object[]> findDatosxEstructuraInventario(int id, List<String> campos) {
        // Construir el nombre de la tabla dinámicamente
        String nombreTabla = "preciso_rconcil_" + id;

        // Construir la parte de la consulta SQL con los campos dinámicos
        String camposStr = String.join(", ", campos);  // Unir los campos de la lista con comas
        String sqlQuery = "SELECT " + camposStr + " FROM " + nombreTabla + " a";

        // Crear la consulta nativa
        Query query = entityManager.createNativeQuery(sqlQuery);

        // Ejecutar la consulta y devolver los resultados
        return query.getResultList();
    }

    public List<Object[]> findDatosxEstructuraContable(int id, List<String> campos) {
        // Construir el nombre de la tabla dinámicamente
        String nombreTabla = "preciso_rc_" + id;

        // Construir la parte de la consulta SQL con los campos dinámicos
        String camposStr = String.join(", ", campos);  // Unir los campos de la lista con comas
        String sqlQuery = "SELECT TOP 100 " + camposStr + " FROM " + nombreTabla + " a";

        // Crear la consulta nativa
        Query query = entityManager.createNativeQuery(sqlQuery);

        // Ejecutar la consulta y devolver los resultados
        return query.getResultList();
    }

    public void deleteById(int id){
        parametrosReportesRepository.deleteById(id);
    }


}
