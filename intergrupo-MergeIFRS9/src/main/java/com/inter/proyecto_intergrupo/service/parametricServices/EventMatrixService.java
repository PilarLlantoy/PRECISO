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

    public List<EventMatrix> findAllOrdered() {
        return eventMatrixRepository.findByOrderByConciliacionIdAscInventarioConciliacionIdAsc();
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

    public List<EventMatrix> findByParams(Integer idTipoEvento, Integer idConciliacion, Integer idInventarioConciliacion, String cuentaGanancia) {
        // Primer query con LEFT JOIN
        StringBuilder queryBuilder1 = new StringBuilder("SELECT distinct pme.* FROM preciso_matriz_eventos pme " +
                "LEFT JOIN preciso_cuentas_matriz_eventos pcm ON pcm.id_matriz_evento = pme.id WHERE 1=1 ");

        if (idConciliacion != 0) {
            queryBuilder1.append(" AND pme.id_conciliacion = ").append(idConciliacion);
        }
        if (idInventarioConciliacion != 0) {
            queryBuilder1.append(" AND pme.id_inventario_conciliacion = ").append(idInventarioConciliacion);
        }
        if (idTipoEvento != 0) {
            queryBuilder1.append(" AND pme.id_tipo_evento = ").append(idTipoEvento);
        }
        if (cuentaGanancia != null && !cuentaGanancia.equalsIgnoreCase("0")) {
            queryBuilder1.append(" AND pcm.cuenta_ganancia = '").append(cuentaGanancia).append("'");
        }

        // Ejecutar la consulta combinada y devolver resultados
        Query query = entityManager.createNativeQuery(queryBuilder1.toString(), EventMatrix.class);
        return query.getResultList();
    }


}
