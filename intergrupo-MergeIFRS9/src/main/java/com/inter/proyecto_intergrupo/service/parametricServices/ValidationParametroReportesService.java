package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.SourceParametroReportes;
import com.inter.proyecto_intergrupo.model.parametric.StructureParametroReportes;
import com.inter.proyecto_intergrupo.model.parametric.ValidationParametroReportes;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.StructureParametroReportesRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ValidationParametroReportesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
@Transactional
public class ValidationParametroReportesService {

    @Autowired
    private final ValidationParametroReportesRepository validationParametroReportesRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public ValidationParametroReportesService(
            ValidationParametroReportesRepository validationParametroReportesRepository)
    {
        this.validationParametroReportesRepository = validationParametroReportesRepository;
    }

    public List <ValidationParametroReportes> findAll(){return validationParametroReportesRepository.findAll();}

    public List<ValidationParametroReportes> findAllActive() {
        return validationParametroReportesRepository.findByEstado(true);
    }

    public List<ValidationParametroReportes> findByParamByFuente(int idParam, int idFuente) {
        return validationParametroReportesRepository.findByFuenteIdAndParametroReportesId(idFuente, idParam);
    }

    public ValidationParametroReportes findById(int id){
        return validationParametroReportesRepository.findAllById(id);
    }


    public ValidationParametroReportes modificar(ValidationParametroReportes estructura){
        validationParametroReportesRepository.save(estructura);
       return estructura;
    }

    public void deleteById(int id){
        validationParametroReportesRepository.deleteById(id);
    }

}
