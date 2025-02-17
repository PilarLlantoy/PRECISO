package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.AdditionalSourceParametroReportes;
import com.inter.proyecto_intergrupo.model.parametric.SourceParametroReportes;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.AdditionalSourceParametroReportesRepository;
import com.inter.proyecto_intergrupo.repository.parametric.SourceParametroReportesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
@Transactional
public class AdditionalSourceParametroReportesService {

    @Autowired
    private final AdditionalSourceParametroReportesRepository additionalSourceParametroReportesRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public AdditionalSourceParametroReportesService(AdditionalSourceParametroReportesRepository additionalSourceParametroReportesRepository) {
        this.additionalSourceParametroReportesRepository = additionalSourceParametroReportesRepository;
    }

    public List <AdditionalSourceParametroReportes> findAll(){return additionalSourceParametroReportesRepository.findAll();}
    public List<AdditionalSourceParametroReportes> findAllActive() {
        return additionalSourceParametroReportesRepository.findByEstado(true);
    }

    public AdditionalSourceParametroReportes findById(int id){
        return additionalSourceParametroReportesRepository.findAllById(id);
    }


    public AdditionalSourceParametroReportes modificar(AdditionalSourceParametroReportes fuente){
        additionalSourceParametroReportesRepository.save(fuente);
       return fuente;
    }

    public void deleteById(int id){
        additionalSourceParametroReportesRepository.deleteById(id);
    }


}
