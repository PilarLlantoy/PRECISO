package com.inter.proyecto_intergrupo.service.parametricServices;


import com.inter.proyecto_intergrupo.model.parametric.EventMatrix;
import com.inter.proyecto_intergrupo.model.parametric.NoteTemplate;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.EventMatrixRepository;
import com.inter.proyecto_intergrupo.repository.parametric.NoteTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Service
@Transactional
public class NoteTemplateService {

    @Autowired
    private final NoteTemplateRepository noteTemplateRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public NoteTemplateService(NoteTemplateRepository noteTemplateRepository) {
        this.noteTemplateRepository = noteTemplateRepository;
    }

    public List<NoteTemplate> findAllActive() {
        return noteTemplateRepository.findByEstado(true);
    }

    public NoteTemplate findById(int id){
        return noteTemplateRepository.findAllById(id);
    }

    public NoteTemplate modificar(NoteTemplate noteTemplate){
        noteTemplateRepository.save(noteTemplate);
       return noteTemplate;
    }

    /*
    public List<EventMatrix> findByParams(Integer idTipoEvento, Integer idConciliacion, Integer idInventarioConciliacion, String cuentaGanancia) {
        // Primer query con LEFT JOIN
        StringBuilder queryBuilder1 = new StringBuilder("SELECT pme.* FROM preciso_matriz_eventos pme " +
                "LEFT JOIN preciso_cuentas_matriz_eventos pcm ON pcm.id_matriz_evento = pme.id WHERE tipo=1 ");

        // Agregar condiciones según los parámetros para el primer query
        if (idTipoEvento != null) {
            queryBuilder1.append(" AND pme.id_tipo_evento = ").append(idTipoEvento);
        }
        if (idConciliacion != null && idTipoEvento != null) {
            queryBuilder1.append(" AND pme.id_conciliacion = ").append(idConciliacion);
        }
        if (idInventarioConciliacion != null && idConciliacion != null && idTipoEvento != null) {
            queryBuilder1.append(" AND pme.id_inventario_conciliacion = ").append(idInventarioConciliacion);
        }
        if (cuentaGanancia != null && !cuentaGanancia.isEmpty() && idInventarioConciliacion != null && idConciliacion != null && idTipoEvento != null) {
            queryBuilder1.append(" AND pcm.cuenta_ganancia = '").append(cuentaGanancia).append("'");
        }

        // Segundo query sin LEFT JOIN
        StringBuilder queryBuilder2 = new StringBuilder("SELECT pme.* FROM preciso_matriz_eventos pme WHERE 1=1 ");

        // Agregar condiciones según los parámetros para el segundo query
        if (idTipoEvento != null) {
            queryBuilder2.append(" AND pme.id_tipo_evento = ").append(idTipoEvento);
        }
        if (idConciliacion != null && idTipoEvento != null) {
            queryBuilder2.append(" AND pme.id_conciliacion = ").append(idConciliacion);
        }
        if (idInventarioConciliacion != null && idConciliacion != null && idTipoEvento != null) {
            queryBuilder2.append(" AND pme.id_inventario_conciliacion = ").append(idInventarioConciliacion);
        }

        // Combinar ambas consultas usando UNION ALL
        StringBuilder combinedQuery = new StringBuilder();
        combinedQuery.append(queryBuilder1);
        combinedQuery.append(" UNION ");
        combinedQuery.append(queryBuilder2);

        // Ejecutar la consulta combinada y devolver resultados
        Query query = entityManager.createNativeQuery(combinedQuery.toString(), EventMatrix.class);
        return query.getResultList();
    }

     */




}
