package com.inter.proyecto_intergrupo.service.parametricServices;


import com.inter.proyecto_intergrupo.model.parametric.Conciliation;
import com.inter.proyecto_intergrupo.model.parametric.ConciliationRoute;
import com.inter.proyecto_intergrupo.model.parametric.EventMatrix;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.EventMatrixRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Service
@Transactional
public class EventMatrixService {

    @Autowired
    private final EventMatrixRepository eventMatrixRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public EventMatrixService(EventMatrixRepository eventMatrixRepository) {
        this.eventMatrixRepository = eventMatrixRepository;
    }

    public List<EventMatrix> findAllActive() {
        return eventMatrixRepository.findByEstado(true);
    }

    public List<EventMatrix> findAllActiveOrdered() {
        return eventMatrixRepository.findByEstadoOrderByConciliacionIdAscInventarioConciliacionIdAsc(true);
    }

    public List<Object[]> findAllOrdered() {
        Query query = entityManager.createNativeQuery(
                "SELECT b.nombre,c.detalle,a.consecutivo,d.nombre_tipo_evento,e.cuenta_ganancia as c1,f.cuenta_ganancia as c2,a.hom_centros,a.aplica_pyg,a.estado,a.id FROM preciso_matriz_eventos a\n" +
                        "left join (select id,nombre from preciso_conciliaciones) b on a.id_conciliacion = b.id\n" +
                        "left join (select id,detalle from preciso_rutas_conciliaciones) c on a.id_inventario_conciliacion=c.id\n" +
                        "left join preciso_tipo_evento d on a.id_tipo_evento=d.id_tipo_evento\n" +
                        "left join (select id_matriz_evento,cuenta_ganancia from preciso_cuentas_matriz_eventos where tipo='1') e on a.id=e.id_matriz_evento\n" +
                        "left join (select id_matriz_evento,cuenta_ganancia from preciso_cuentas_matriz_eventos where tipo='2') f on a.id=f.id_matriz_evento\n" +
                        "ORDER BY b.nombre,c.detalle,a.consecutivo");
        return query.getResultList();
    }
    public List<EventMatrix> findAllByEstadoAndConciliacionAndInventarioConciliacion(Conciliation concil, ConciliationRoute inv) {
        return eventMatrixRepository.findAllByEstadoAndConciliacionAndInventarioConciliacion(true, concil, inv);
    }

    public List<EventMatrix> findByConciliationxInventarioxTipoEvento(int idConciliation, int idInventario, int idTipoEvento) {
        Query query = entityManager.createNativeQuery(
                "SELECT * FROM preciso_matriz_eventos " +
                        "WHERE id_conciliacion = :idConciliation " +
                        "AND id_inventario_conciliacion = :idInventario " +
                        "AND id_tipo_evento = :idTipoEvento",
                EventMatrix.class); // Mapear directamente a la clase EventMatrix
        query.setParameter("idConciliation", idConciliation);
        query.setParameter("idInventario", idInventario);
        query.setParameter("idTipoEvento", idTipoEvento);
        return query.getResultList();
    }


    public List<Integer> findMatrices(int concil, int inv) {
        Query query = entityManager.createNativeQuery(
                "SELECT id FROM preciso_matriz_eventos WHERE id_conciliacion = :concil AND id_inventario_conciliacion= :inv");
        query.setParameter("concil", concil);
        query.setParameter("inv", inv);
        return query.getResultList();
    }

    public EventMatrix findById(int id){
        return eventMatrixRepository.findAllById(id);
    }

    public EventMatrix modificar(EventMatrix eventMatrix){
        eventMatrixRepository.save(eventMatrix);
       return eventMatrix;
    }

    public List<String> findCuentaGanancia(Integer idTipoEvento, Integer idConciliacion, Integer idInventarioConciliacion) {
        StringBuilder queryBuilder = new StringBuilder("SELECT pcm.cuenta_ganancia FROM preciso_cuentas_matriz_eventos pcm " +
                "JOIN preciso_matriz_eventos pme ON pcm.id_matriz_evento = pme.id WHERE 1=1");

        // Agregar condiciones según los parámetros
        if (idTipoEvento != null) {
            queryBuilder.append(" AND pme.id_tipo_evento = ").append(idTipoEvento);
        }
        if (idConciliacion != null && idTipoEvento != null) {
            queryBuilder.append(" AND pme.id_conciliacion = ").append(idConciliacion);
        }
        if (idInventarioConciliacion != null && idConciliacion != null && idTipoEvento != null) {
            queryBuilder.append(" AND pme.id_inventario_conciliacion = ").append(idInventarioConciliacion);
        }

        // Ejecutar la consulta y devolver resultados
        Query query = entityManager.createNativeQuery(queryBuilder.toString());
        return query.getResultList();
    }

    public List<EventMatrix> findByParams(Integer idTipoEvento, Integer idConciliacion, Integer idInventarioConciliacion, String cuentaGanancia, String estado) {
        // Primer query con LEFT JOIN
        StringBuilder queryBuilder1 = new StringBuilder("SELECT b.nombre,c.detalle,a.consecutivo,d.nombre_tipo_evento,e.cuenta_ganancia as c1,f.cuenta_ganancia as c2,a.hom_centros,a.aplica_pyg,a.estado,a.id  FROM preciso_matriz_eventos a\n" +
                "left join (select id,nombre from preciso_conciliaciones) b on a.id_conciliacion = b.id\n" +
                "left join (select id,detalle from preciso_rutas_conciliaciones) c on a.id_inventario_conciliacion=c.id\n" +
                "left join preciso_tipo_evento d on a.id_tipo_evento=d.id_tipo_evento\n" +
                "left join (select id_matriz_evento,cuenta_ganancia from preciso_cuentas_matriz_eventos where tipo='1') e on a.id=e.id_matriz_evento\n" +
                "left join (select id_matriz_evento,cuenta_ganancia from preciso_cuentas_matriz_eventos where tipo='2') f on a.id=f.id_matriz_evento\n" +
                "WHERE 1=1 ");

        if (idConciliacion != 0) {
            queryBuilder1.append(" AND a.id_conciliacion = ").append(idConciliacion);
        }
        if (idInventarioConciliacion != 0) {
            queryBuilder1.append(" AND a.id_inventario_conciliacion = ").append(idInventarioConciliacion);
        }
        if (idTipoEvento != 0) {
            queryBuilder1.append(" AND a.id_tipo_evento = ").append(idTipoEvento);
        }
        if (cuentaGanancia != null && !cuentaGanancia.equalsIgnoreCase("0")) {
            queryBuilder1.append(" AND (e.cuenta_ganancia = '").append(cuentaGanancia).append("' OR f.cuenta_ganancia = '").append(cuentaGanancia).append("')");
        }
        if (estado != null && !estado.equalsIgnoreCase("-1")) {
            queryBuilder1.append(" AND a.estado = ").append(estado).append(" ");
        }
        queryBuilder1.append(" ORDER BY b.nombre,c.detalle,a.consecutivo").append(" ");

        // Ejecutar la consulta combinada y devolver resultados
        Query query = entityManager.createNativeQuery(queryBuilder1.toString());
        return query.getResultList();
    }


}
