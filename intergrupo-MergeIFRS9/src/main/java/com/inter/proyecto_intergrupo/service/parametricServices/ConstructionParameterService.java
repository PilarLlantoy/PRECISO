package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.AccountEventMatrix;
import com.inter.proyecto_intergrupo.model.parametric.ConstructionParameter;
import com.inter.proyecto_intergrupo.model.parametric.ValidationRC;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ConstructionParameterRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ValidationRCRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Service
@Transactional
public class ConstructionParameterService {

    @Autowired
    private final ConstructionParameterRepository constructionParameterRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public ConstructionParameterService(ConstructionParameterRepository constructionParameterRepository) {
        this.constructionParameterRepository = constructionParameterRepository;
    }

    public List <ConstructionParameter> findAll(){return constructionParameterRepository.findAll();}
    public List<ConstructionParameter> findAllActive() {
        return constructionParameterRepository.findByEstado(true);
    }

    public ConstructionParameter findById(int id){
        return constructionParameterRepository.findAllById(id);
    }


    public List<ConstructionParameter> findByAccount(AccountEventMatrix cuenta) {
        return constructionParameterRepository.findByAccount(cuenta);
    }

    public List<Object[]> findParamsByAccount(int id) {
        Query query = entityManager.createNativeQuery(
                "SELECT pc.id, " +
                        "pc.estado, " +
                        "pc.id_cuenta_me, " +
                        "pc.id_campo_conciliacion, " +
                        "pc.id_campo_cont_resultante, " +
                        "pc.id_campo_cont_validar, " +
                        "pc.id_ruta_contable, " +
                        "a.nombre AS nombre_campo_conciliacion, " +
                        "b.nombre AS nombre_ruta_contable, " +
                        "c.nombre AS nombre_campo_val, " +
                        "d.nombre AS nombre_campo_resul " +
                        "FROM PRECISO.dbo.preciso_parametros_construccion pc " +
                        "LEFT JOIN PRECISO.dbo.preciso_campos_rconcil a ON pc.id_campo_conciliacion = a.id_campo " +
                        "LEFT JOIN PRECISO.dbo.preciso_rutas_contables b ON pc.id_ruta_contable = b.id_rc " +
                        "LEFT JOIN PRECISO.dbo.preciso_campos_rc c ON pc.id_campo_cont_validar = c.id_campo " +
                        "LEFT JOIN PRECISO.dbo.preciso_campos_rc d ON pc.id_campo_cont_resultante = d.id_campo " +
                        "WHERE pc.id_cuenta_me = :id"
        );
        query.setParameter("id", id);
        return query.getResultList();
    }


    public ConstructionParameter modificar(ConstructionParameter cp){
        constructionParameterRepository.save(cp);
       return cp;
    }

    public void deleteById(int id){
        constructionParameterRepository.deleteById(id);
    }


}
