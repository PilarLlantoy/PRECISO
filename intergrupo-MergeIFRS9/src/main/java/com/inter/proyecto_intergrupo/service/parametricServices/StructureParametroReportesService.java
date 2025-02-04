package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.SourceParametroReportes;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.SourceParametroReportesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
@Transactional
public class StructureParametroReportesService {

    @Autowired
    private final SourceParametroReportesRepository sourceParametroReportesRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public StructureParametroReportesService(SourceParametroReportesRepository sourceParametroReportesRepository) {
        this.sourceParametroReportesRepository = sourceParametroReportesRepository;
    }

    public List <SourceParametroReportes> findAll(){return sourceParametroReportesRepository.findAll();}
    public List<SourceParametroReportes> findAllActive() {
        return sourceParametroReportesRepository.findByEstado(true);
    }

    public SourceParametroReportes findById(int id){
        return sourceParametroReportesRepository.findAllById(id);
    }


    public SourceParametroReportes modificar(SourceParametroReportes fuente){
        sourceParametroReportesRepository.save(fuente);
       return fuente;
    }

    public void deleteById(int id){
        sourceParametroReportesRepository.deleteById(id);
    }


}
