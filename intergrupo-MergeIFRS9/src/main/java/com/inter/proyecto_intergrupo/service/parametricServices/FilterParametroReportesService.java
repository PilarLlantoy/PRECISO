package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.CondicionRC;
import com.inter.proyecto_intergrupo.model.parametric.FilterParametroReportes;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CondicionRCRepository;
import com.inter.proyecto_intergrupo.repository.parametric.FilterParametroReportesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
@Transactional
public class FilterParametroReportesService {

    @Autowired
    private final FilterParametroReportesRepository filterParametroReportesRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public FilterParametroReportesService(FilterParametroReportesRepository filterParametroReportesRepository) {
        this.filterParametroReportesRepository = filterParametroReportesRepository;
    }

    public List <FilterParametroReportes> findAll(){return filterParametroReportesRepository.findAll();}
    public List<FilterParametroReportes> findAllActive() {
        return filterParametroReportesRepository.findByEstado(true);
    }

    public FilterParametroReportes findById(int id){
        return filterParametroReportesRepository.findAllById(id);
    }


    public FilterParametroReportes modificar(FilterParametroReportes filtro){
        filterParametroReportesRepository.save(filtro);
       return filtro;
    }

    public void deleteById(int id){
        filterParametroReportesRepository.deleteById(id);
    }


}
