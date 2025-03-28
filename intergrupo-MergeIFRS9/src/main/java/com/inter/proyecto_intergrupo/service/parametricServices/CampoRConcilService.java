package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Cargo;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CampoRCRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CampoRConcilRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    /*public void recreateTable(ConciliationRoute data){
        StringBuilder createTableQuery = new StringBuilder("CREATE TABLE ");
        createTableQuery.append("preciso_rconcil_"+data.getId()).append(" (");

        for (int i = 0; i < data.getCampos().size(); i++) {
            CampoRConcil column = data.getCampos().get(i);
            createTableQuery.append("["+column.getNombre()+"]")
                    .append(" ");

            if(column.getTipo().equalsIgnoreCase("Date") || column.getTipo().equalsIgnoreCase("DateTime")|| column.getTipo().equalsIgnoreCase("Time"))
                createTableQuery.append("VARCHAR").append("(MAX)");
            else
                createTableQuery.append(column.getTipo());

            if (column.getTipo().equalsIgnoreCase("VARCHAR")) {
                //createTableQuery.append("(").append(column.getLongitud()).append(")");
                createTableQuery.append("(MAX)");
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
    }*/

    public void recreateTable(ConciliationRoute data) {
        String tableName = "preciso_rconcil_" + data.getId();
        String tableNameCruce = "preciso_ci_"+data.getConciliacion().getId() +"_"+ data.getId();

        // Verificar si la tabla existe
        Query tableCheckQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ? AND TABLE_SCHEMA = 'dbo'"
        );
        tableCheckQuery.setParameter(1, tableName);

        List<Integer> tableExists = tableCheckQuery.getResultList();

        if (tableExists.get(0)==0) {
            //  La tabla no existe, se debe crear
            createTable(data, tableName);
        } else {
            // La tabla existe, verificar y modificar columnas
            updateTable(data, tableName);
            updateTable(data, tableNameCruce);
        }
    }

    private void createTable(ConciliationRoute data, String tableName) {
        StringBuilder createTableQuery = new StringBuilder("CREATE TABLE " + tableName + " (");

        for (CampoRConcil column : data.getCampos()) {
            createTableQuery.append("[").append(column.getNombre()).append("] ")
                    .append(getSqlType(column))
                    .append(", ");
        }

        createTableQuery.append("periodo_preciso DATE, id_preciso BIGINT IDENTITY(1,1) PRIMARY KEY);");

        entityManager.createNativeQuery(createTableQuery.toString()).executeUpdate();
    }

    private void updateTable(ConciliationRoute data, String tableName) {
        // Obtener las columnas actuales
        Query columnQuery = entityManager.createNativeQuery(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND TABLE_SCHEMA = 'dbo'"
        );
        columnQuery.setParameter(1, tableName);

        List<String> existingColumns = (List<String>) columnQuery.getResultList().stream()
                .map(Object::toString)
                .collect(Collectors.toList());

        // Comparar y actualizar
        for (CampoRConcil column : data.getCampos()) {
            if (!existingColumns.contains(column.getNombre())) {
                //Agregar columna si no existe
                entityManager.createNativeQuery(
                        "ALTER TABLE " + tableName + " ADD [" + column.getNombre() + "] " + getSqlType(column)
                ).executeUpdate();
            }
        }

        List<String> columnasOmitir = Arrays.asList("periodo_preciso", "id_preciso", "inventario_precisokey","id_inventario_precisokey","fecha_conciliacion_precisokey","tipo_evento_precisokey","cdgo_matriz_evento_precisokey","centro_contable_precisokey","cuenta_contable_1_precisokey","divisa_cuenta_1_precisokey","valor_cuenta_1_precisokey","cuenta_contable_2_precisokey","divisa_cuenta_2_precisokey","valor_cuenta_2_precisokey");

        // Opcional: Eliminar columnas que ya no deberían estar
        for (String existingColumn : existingColumns) {
            boolean stillExists = data.getCampos().stream()
                    .anyMatch(c -> c.getNombre().equalsIgnoreCase(existingColumn));

            if (!stillExists &&  !columnasOmitir.contains(existingColumn.toLowerCase())) {
                entityManager.createNativeQuery(
                        "ALTER TABLE " + tableName + " DROP COLUMN [" + existingColumn + "]"
                ).executeUpdate();
            }
        }
    }

    private String getSqlType(CampoRConcil column) {
        /*if (column.getTipo().equalsIgnoreCase("Date") || column.getTipo().equalsIgnoreCase("DateTime") || column.getTipo().equalsIgnoreCase("Time")) {
            return "VARCHAR(MAX)";
        } else if (column.getTipo().equalsIgnoreCase("VARCHAR")) {
            return "VARCHAR(MAX)";
        } else {
            return column.getTipo();
        }*/
        return "VARCHAR(MAX)";
    }

    public List<String> validatePrincipal(String principal)
    {
        Query validate = entityManager.createNativeQuery("SELECT DISTINCT nv.nombre FROM preciso_campos_rc as nv \n" +
                "inner join preciso_rutas_contables b on nv.id_rc = b.id_rc \n" +
                "WHERE  nv.visualizacion=1 and nv.estado=1 and b.nombre = :principal");
        validate.setParameter("principal", principal);
        return validate.getResultList();
    }

    public Integer findConsecutivo(EventMatrix matrixExists)
    {
        Query validate = entityManager.createNativeQuery("select isnull(max(consecutivo),0) from preciso_matriz_eventos where id_conciliacion = ? and id_inventario_conciliacion =  ? ");
        validate.setParameter(1,matrixExists.getConciliacion().getId());
        validate.setParameter(2,matrixExists.getInventarioConciliacion().getId());
        return (Integer) validate.getSingleResult();
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

    public List<CampoRConcil> findCamposByRutaConcil2(int idRConcil) {
        Query query = entityManager.createNativeQuery(
                "SELECT * FROM preciso_campos_rconcil WHERE id_rconcil = :idRConcil", CampoRConcil.class);
        query.setParameter("idRConcil", idRConcil);
        return query.getResultList();
    }

}
