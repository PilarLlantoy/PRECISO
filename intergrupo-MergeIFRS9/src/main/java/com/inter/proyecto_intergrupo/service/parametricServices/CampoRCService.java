package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.AccountingRoute;
import com.inter.proyecto_intergrupo.model.parametric.CampoRC;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CampoRCRepository;
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
public class CampoRCService {

    @Autowired
    private final CampoRCRepository campoRCRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public CampoRCService(CampoRCRepository campoRCRepository) {
        this.campoRCRepository = campoRCRepository;
    }

    public List <CampoRC> findAll(){return campoRCRepository.findAllByOrderByNombreAsc();}
    public List<CampoRC> findAllActive() {
        return campoRCRepository.findByEstado(true);
    }

    public CampoRC findById(int id){
        return campoRCRepository.findAllById(id);
    }

    public CampoRC findByName(String nombre){
        return campoRCRepository.findAllByNombre(nombre);
    }

    public CampoRC modificar(CampoRC campo){
        campoRCRepository.save(campo);
       return campo;
    }

    public void recreateTable(AccountingRoute data){
        StringBuilder createTableQuery = new StringBuilder("CREATE TABLE ");
        createTableQuery.append("preciso_rc_"+data.getId()).append(" (");

        for (int i = 0; i < data.getCampos().size(); i++) {
            CampoRC column = data.getCampos().get(i);
            createTableQuery.append(column.getNombre())
                    .append(" ")
                    .append(column.getTipo());

            if (column.getTipo().equalsIgnoreCase("VARCHAR")) {
                //createTableQuery.append("(").append(column.getLongitud()).append(")"); //Con Longitud de RC
                createTableQuery.append("(MAX)"); //Con Longitud de MAX
            }

            if (i < data.getCampos().size() - 1) {
                createTableQuery.append(", ");
            }
        }
        if(!data.getCampos().isEmpty())
            createTableQuery.append(",");
        createTableQuery.append("periodo_preciso DATE ,id_preciso BIGINT IDENTITY(1,1) PRIMARY KEY);");

        Query queryTable = entityManager.createNativeQuery("IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'preciso_rc_"+data.getId()+"' AND TABLE_SCHEMA = 'dbo') BEGIN DROP TABLE preciso_rc_"+data.getId()+"; END; \n "+createTableQuery.toString());
        queryTable.executeUpdate();
    }

    public void deleteById(int id){
        campoRCRepository.deleteById(id);
    }

    public List<String> validatePrincipal(String principal)
    {
        Query validate = entityManager.createNativeQuery("SELECT nv.nombre FROM preciso_campos_rc as nv WHERE " +
                "nv.visualizacion=1 and nv.estado=1 and nv.id_rc = 1");
        //validate.setParameter(1,principal);
        return validate.getResultList();
    }


    public List<String> validatePrincipal2(String principal)
    {
        Query validate = entityManager.createNativeQuery("SELECT nv.nombre FROM preciso_rutas_contables as nv WHERE " +
                "nv.id_sf=1 and nv.activo=1");
        //validate.setParameter(1,principal);
        return validate.getResultList();
    }

    public List<Object[]> findCamposByRutaCont(int idRCont) {
        Query query = entityManager.createNativeQuery(
                "SELECT id_campo, nombre FROM preciso_campos_rc WHERE id_rc = :idRCont");
        query.setParameter("idRCont", idRCont);
        return query.getResultList();
    }


}
