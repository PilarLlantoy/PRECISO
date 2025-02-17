package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.SourceParametroReportes;
import com.inter.proyecto_intergrupo.model.parametric.StructureParametroReportes;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.SourceParametroReportesRepository;
import com.inter.proyecto_intergrupo.repository.parametric.StructureParametroReportesRepository;
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
    private final StructureParametroReportesRepository structureParametroReportesRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public StructureParametroReportesService(
            StructureParametroReportesRepository structureParametroReportesRepository)
    {
        this.structureParametroReportesRepository = structureParametroReportesRepository;
    }

    public List <StructureParametroReportes> findAll(){return structureParametroReportesRepository.findAll();}

    public List<StructureParametroReportes> findAllActive() {
        return structureParametroReportesRepository.findByEstado(true);
    }

    public List<StructureParametroReportes> findByParamByFuente(int idParam, int idFuente) {
        return structureParametroReportesRepository.findByFuenteIdAndParametroReportesId(idFuente, idParam);
    }

    public StructureParametroReportes findById(int id){
        return structureParametroReportesRepository.findAllById(id);
    }


    public StructureParametroReportes modificar(StructureParametroReportes estructura){
        structureParametroReportesRepository.save(estructura);
       return estructura;
    }

    public void deleteById(int id){
        structureParametroReportesRepository.deleteById(id);
    }

    // Método para eliminar todas las estructuras asociadas a una fuente
    public void deleteAllEstructurasFromFuente(SourceParametroReportes fuente) {
        structureParametroReportesRepository.deleteByFuente(fuente);
    }
}
