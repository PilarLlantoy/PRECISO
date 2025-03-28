package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.ResultingFieldParametroReportes;
import com.inter.proyecto_intergrupo.model.parametric.SourceParametroReportes;
import com.inter.proyecto_intergrupo.model.parametric.StructureParametroReportes;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ResultingFieldsParametroReportesRepository;
import com.inter.proyecto_intergrupo.repository.parametric.StructureParametroReportesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
@Transactional
public class ResultingFieldsParametroReportesService {

    @Autowired
    private final ResultingFieldsParametroReportesRepository resultingFieldsParametroReportesRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public ResultingFieldsParametroReportesService(
            ResultingFieldsParametroReportesRepository resultingFieldsParametroReportesRepository)
    {
        this.resultingFieldsParametroReportesRepository = resultingFieldsParametroReportesRepository;
    }

    public List <ResultingFieldParametroReportes> findAll(){return resultingFieldsParametroReportesRepository.findAll();}

    public List<ResultingFieldParametroReportes> findAllActive() {
        return resultingFieldsParametroReportesRepository.findByEstado(true);
    }

    public List<ResultingFieldParametroReportes> findByParamByFuente(int idParam, int idFuente) {
        return resultingFieldsParametroReportesRepository.findByFuenteIdAndParametroReportesId(idFuente, idParam);
    }

    public ResultingFieldParametroReportes findById(int id){
        return resultingFieldsParametroReportesRepository.findAllById(id);
    }


    public ResultingFieldParametroReportes modificar(ResultingFieldParametroReportes camposResultantes){
        resultingFieldsParametroReportesRepository.save(camposResultantes);
       return camposResultantes;
    }

    public void deleteById(int id){
        resultingFieldsParametroReportesRepository.deleteById(id);
    }

    // Método para eliminar todas las estructuras asociadas a una fuente
    public void deleteAllEstructurasFromFuente(SourceParametroReportes fuente) {
        resultingFieldsParametroReportesRepository.deleteByFuente(fuente);
    }
}
