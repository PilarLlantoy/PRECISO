package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.AccountingRoute;
import com.inter.proyecto_intergrupo.model.parametric.CampoRC;
import com.inter.proyecto_intergrupo.model.parametric.CampoRConcil;
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
import java.util.stream.Collectors;

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

    /*public void recreateTable(AccountingRoute data){
        StringBuilder createTableQuery = new StringBuilder("CREATE TABLE ");
        createTableQuery.append("preciso_rc_"+data.getId()).append(" (");

        for (int i = 0; i < data.getCampos().size(); i++) {
            CampoRC column = data.getCampos().get(i);
            createTableQuery.append(column.getNombre())
                    .append(" ");
            if(column.getTipo().equalsIgnoreCase("Date") || column.getTipo().equalsIgnoreCase("DateTime")|| column.getTipo().equalsIgnoreCase("Time"))
                createTableQuery.append("VARCHAR").append("(MAX)");
            else
                createTableQuery.append(column.getTipo());

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
    }*/

    public void recreateTable(AccountingRoute data) {
        String tableName = "preciso_rc_" + data.getId();

        // Verificar si la tabla existe
        Query tableCheckQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ? AND TABLE_SCHEMA = 'dbo'"
        );
        tableCheckQuery.setParameter(1, tableName);

        List<Integer> tableExists =  tableCheckQuery.getResultList();

        if (tableExists.get(0) == 0) {
            // La tabla no existe, se debe crear
            createTable(data, tableName);
        } else {
            //La tabla existe, verificar y modificar columnas
            updateTable(data, tableName);
        }
    }

    private void createTable(AccountingRoute data, String tableName) {
        StringBuilder createTableQuery = new StringBuilder("CREATE TABLE " + tableName + " (");

        for (CampoRC column : data.getCampos()) {
            createTableQuery.append(column.getNombre()).append(" ").append(getSqlType(column)).append(", ");
        }

        createTableQuery.append("periodo_preciso DATE, id_preciso BIGINT IDENTITY(1,1) PRIMARY KEY);");

        entityManager.createNativeQuery(createTableQuery.toString()).executeUpdate();
    }

    private void updateTable(AccountingRoute data, String tableName) {
        // Obtener las columnas actuales
        Query columnQuery = entityManager.createNativeQuery(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND TABLE_SCHEMA = 'dbo'"
        );
        columnQuery.setParameter(1, tableName);

        List<String> existingColumns = (List<String>) columnQuery.getResultList().stream()
                .map(Object::toString)
                .collect(Collectors.toList());

        // Comparar y actualizar
        for (CampoRC column : data.getCampos()) {
            if (!existingColumns.contains(column.getNombre())) {
                //Agregar columna si no existe
                entityManager.createNativeQuery(
                        "ALTER TABLE " + tableName + " ADD " + column.getNombre() + " " + getSqlType(column)
                ).executeUpdate();
            }
        }

        // Opcional: Eliminar columnas que ya no deberían estar
        for (String existingColumn : existingColumns) {
            boolean stillExists = data.getCampos().stream()
                    .anyMatch(c -> c.getNombre().equalsIgnoreCase(existingColumn));

            if (!stillExists && !existingColumn.equals("periodo_preciso") && !existingColumn.equals("id_preciso")) {
                entityManager.createNativeQuery(
                        "ALTER TABLE " + tableName + " DROP COLUMN " + existingColumn
                ).executeUpdate();
            }
        }
    }

    private String getSqlType(CampoRC column) {
        /*if (column.getTipo().equalsIgnoreCase("Date") || column.getTipo().equalsIgnoreCase("DateTime") || column.getTipo().equalsIgnoreCase("Time")) {
            return "VARCHAR(MAX)";
        } else if (column.getTipo().equalsIgnoreCase("VARCHAR")) {
            return "VARCHAR(MAX)";
        } else {
            return column.getTipo();
        }*/
        return "VARCHAR(MAX)";
    }


    public void deleteById(int principal)
    {
        Query validate = entityManager.createNativeQuery("DELETE from preciso_campos_rc WHERE " +
                " id_campo = ? ");
        validate.setParameter(1,principal);
        validate.executeUpdate();
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

    public List<CampoRC> findCamposByRutaVsNombre(int idRC, String nombre) {
        Query query = entityManager.createNativeQuery(
                "SELECT * FROM preciso_campos_rc WHERE id_rc = :idRC AND nombre = :nombre ",CampoRC.class);
        query.setParameter("idRC", idRC);
        query.setParameter("nombre", nombre);
        return query.getResultList();
    }


}
