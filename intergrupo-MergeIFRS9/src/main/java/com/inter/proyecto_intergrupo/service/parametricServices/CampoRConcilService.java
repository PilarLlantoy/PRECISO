package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Cargo;
import com.inter.proyecto_intergrupo.model.parametric.AccountingRoute;
import com.inter.proyecto_intergrupo.model.parametric.CampoRC;
import com.inter.proyecto_intergrupo.model.parametric.CampoRConcil;
import com.inter.proyecto_intergrupo.model.parametric.ConciliationRoute;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CampoRCRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CampoRConcilRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Service
@Transactional
public class CampoRConcilService {

    @Autowired
    private final CampoRConcilRepository campoRCRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public CampoRConcilService(CampoRConcilRepository campoRCRepository) {
        this.campoRCRepository = campoRCRepository;
    }

    public List <CampoRConcil> findAll(){return campoRCRepository.findAllByOrderByNombreAsc();}
    public List<CampoRConcil> findAllActive() {
        return campoRCRepository.findByEstado(true);
    }

    public CampoRConcil findById(int id){
        return campoRCRepository.findAllById(id);
    }

    public CampoRConcil modificar(CampoRConcil campo){
        campoRCRepository.save(campo);
       return campo;
    }

    public void deleteById(int id){
        campoRCRepository.deleteById(id);
    }

    public void recreateTable(ConciliationRoute data){
        StringBuilder createTableQuery = new StringBuilder("CREATE TABLE ");
        createTableQuery.append("preciso_rconcil_"+data.getId()).append(" (");

        for (int i = 0; i < data.getCampos().size(); i++) {
            CampoRConcil column = data.getCampos().get(i);
            createTableQuery.append("["+column.getNombre()+"]")
                    .append(" ")
                    .append(column.getTipo());

            if (column.getTipo().equalsIgnoreCase("VARCHAR")) {
                createTableQuery.append("(").append(column.getLongitud()).append(")");
            }

            if (i < data.getCampos().size() - 1) {
                createTableQuery.append(", ");
            }
        }
        if(!data.getCampos().isEmpty())
            createTableQuery.append(",");
        createTableQuery.append("periodo_preciso DATE ,id_preciso BIGINT IDENTITY(1,1) PRIMARY KEY);");

        Query queryTable = entityManager.createNativeQuery("IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'preciso_rconcil_"+data.getId()+"' AND TABLE_SCHEMA = 'dbo') BEGIN DROP TABLE preciso_rconcil_"+data.getId()+"; END; \n "+createTableQuery.toString());
        queryTable.executeUpdate();
    }

    public List<String> validatePrincipal(String principal)
    {
        Query validate = entityManager.createNativeQuery("SELECT DISTINCT nv.nombre FROM preciso_campos_rc as nv \n" +
                "inner join preciso_rutas_contables b on nv.id_rc = b.id_rc \n" +
                "WHERE  nv.visualizacion=1 and nv.estado=1 and b.nombre = :principal");
        validate.setParameter("principal", principal);
        return validate.getResultList();
    }

    public List<String> validatePrincipal2(String principal) {
        Query validate = entityManager.createNativeQuery(
                "SELECT nv.nombre FROM preciso_rutas_contables nv WHERE nv.id_sf = :principal AND nv.activo = 1");
        validate.setParameter("principal", principal);
        return validate.getResultList();
    }

    public List<Object[]> findCamposByRutaConcil(int idRConcil) {
        Query query = entityManager.createNativeQuery(
                "SELECT id_campo, nombre FROM preciso_campos_rconcil WHERE id_rconcil = :idRConcil");
        query.setParameter("idRConcil", idRConcil);
        return query.getResultList();
    }

    public List<CampoRConcil> findCamposByRutaConcilVsNombre(int idRConcil,String nombre) {
        Query query = entityManager.createNativeQuery(
                "SELECT * FROM preciso_campos_rconcil WHERE id_rconcil = :idRConcil AND nombre = :nombre ",CampoRConcil.class);
        query.setParameter("idRConcil", idRConcil);
        query.setParameter("nombre", nombre);
        return query.getResultList();
    }

}
