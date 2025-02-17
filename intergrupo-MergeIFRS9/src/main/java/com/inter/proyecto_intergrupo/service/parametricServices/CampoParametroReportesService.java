package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.AccountingRoute;
import com.inter.proyecto_intergrupo.model.parametric.CampoParamReportes;
import com.inter.proyecto_intergrupo.model.parametric.CampoRC;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CampoParametroReportesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Service
@Transactional
public class CampoParametroReportesService {

    @Autowired
    private final CampoParametroReportesRepository campoParametroReportesRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public CampoParametroReportesService(CampoParametroReportesRepository campoParametroReportesRepository) {
        this.campoParametroReportesRepository = campoParametroReportesRepository;
    }

    public List <CampoParamReportes> findAll(){return campoParametroReportesRepository.findAllByOrderByDetalleAsc();}
    public List<CampoParamReportes> findAllActive() {
        return campoParametroReportesRepository.findByEstado(true);
    }

    public CampoParamReportes findById(int id){
        return campoParametroReportesRepository.findAllById(id);
    }

    public CampoParamReportes findByName(String detalle){
        return campoParametroReportesRepository.findAllByDetalle(detalle);
    }

    public CampoParamReportes modificar(CampoParamReportes campo){
        campoParametroReportesRepository.save(campo);
       return campo;
    }

    public void deleteById(int principal)
    {
        Query validate = entityManager.createNativeQuery("DELETE from preciso_campos_param_reportes WHERE " +
                " id_campo = ? ");
        validate.setParameter(1,principal);
        validate.executeUpdate();
    }

    public List<CampoParamReportes> findCamposByParametroVsDetalle(int idParametro, String detalle) {
        Query query = entityManager.createNativeQuery(
                "SELECT * FROM preciso_campos_param_reportes WHERE id_parametro = :idParametro AND detalle = :detalle ",CampoParamReportes.class);
        query.setParameter("idParametro", idParametro);
        query.setParameter("detalle", detalle);
        return query.getResultList();
    }

    public List<Object[]> findCamposByFuente(int id) {
        Query query = entityManager.createNativeQuery(
                "SELECT pc.id_campo, " +
                        "pc.detalle " +
                        "FROM PRECISO.dbo.preciso_campos_param_reportes pc " +
                        "WHERE pc.id_parametro = :id"
        );
        query.setParameter("id", id);
        return query.getResultList();
    }


}
