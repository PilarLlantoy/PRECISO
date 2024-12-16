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


    public List<NoteTemplate> findByParams(Integer idConciliacion, Integer idInventarioConciliacion) {
        StringBuilder queryBuilder2 = new StringBuilder("SELECT pme.* FROM preciso_plantillas_notas pme WHERE 1=1 ");
        if (idConciliacion != null) {
            queryBuilder2.append(" AND pme.id_conciliacion = ").append(idConciliacion);
        }
        if (idInventarioConciliacion != null && idConciliacion != null) {
            queryBuilder2.append(" AND pme.id_inventario_conciliacion = ").append(idInventarioConciliacion);
        }
        Query query = entityManager.createNativeQuery(queryBuilder2.toString(), NoteTemplate.class);
        return query.getResultList();
    }

}
