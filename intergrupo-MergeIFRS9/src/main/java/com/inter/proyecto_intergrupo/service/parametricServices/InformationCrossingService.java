package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.ControlPanelIfrs;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.LogAccountingLoadRepository;
import com.inter.proyecto_intergrupo.repository.parametric.LogInformationCrossingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InformationCrossingService {
    @Autowired
    private final LogInformationCrossingRepository logInformationCrossingRepository;
    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    private AuditRepository auditRepository;
    @Autowired
    private LogAccountingLoadRepository logAccountingLoadRepository;

    @Autowired
    private ConciliationService conciliationService;

    @Autowired
    private ConciliationRouteService conciliationRouteService;

    @Autowired
    private EventTypeService eventTypeService;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Autowired
    public InformationCrossingService(LogInformationCrossingRepository logInformationCrossingRepository) {
        this.logInformationCrossingRepository = logInformationCrossingRepository;
    }

    public List<LogInformationCrossing> findAllLog(Conciliation concil, String fecha, EventType evento) {
        LocalDate localDate = LocalDate.parse(fecha);
        Date fechaDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        return logInformationCrossingRepository.findAllByIdConciliacionAndFechaProcesoAndIdEventoOrderByIdDesc(concil,fechaDate, evento);
    }

    public void loadLogInformationCrossing(User user,int concil, int event, String fecha, String tipo, String estado, String mensaje)
    {
        LocalDate localDate = LocalDate.parse(fecha);
        Date fechaDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date today=new Date();
        LogInformationCrossing insert = new LogInformationCrossing();

        insert.setFechaProceso(fechaDate);
        insert.setFechaPreciso(today);
        if(user!=null)
            insert.setUsuario(user.getUsuario());
        else
            insert.setUsuario("Automático");
        insert.setTipoProceso(tipo);
        insert.setNovedad(mensaje);
        insert.setEstadoProceso(estado);
        if(user!=null)
            insert.setUsuario(user.getUsuario());
        else
            insert.setUsuario("Automático");
        Conciliation conciliation = conciliationService.findById(concil);
        insert.setIdConciliacion(conciliation);
        EventType evento = eventTypeService.findAllById(event);
        insert.setIdEvento(evento);
        logInformationCrossingRepository.save(insert);
    }

    public List<Object> findTemporal(){
        List<Object> listTemp = new ArrayList<>();
        try {
            Query querySelect = entityManager.createNativeQuery("SELECT * FROM TEMPORAL_ci ");
            listTemp = querySelect.getResultList();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return listTemp;
    }

    public void loadLogCargue(User user, int idConcil, String fecha, String tipo, String estado, String mensaje)
    {
        LocalDate localDate = LocalDate.parse(fecha);
        Date fechaDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date today=new Date();
        Conciliation concil= conciliationService.findById(idConcil);
        LogInformationCrossing insert = new LogInformationCrossing();
        insert.setFechaProceso(fechaDate);
        insert.setFechaPreciso(today);
        if(user!=null)
            insert.setUsuario(user.getUsuario());
        else
            insert.setUsuario("Automático");
        insert.setTipoProceso(tipo);
        insert.setNovedad(mensaje);
        insert.setEstadoProceso(estado);
        if(user!=null)
            insert.setUsuario(user.getUsuario());
        else
            insert.setUsuario("Automático");
        insert.setIdConciliacion(concil);
        logInformationCrossingRepository.save(insert);
    }

    public void recreateTable(ConciliationRoute data, int idConcil,String fecha) {
        System.out.println("CREANDO LA TABLA FINAL DE ESE INVENTARIO");
        System.out.println("#############################");

        String tableName = "preciso_ci_" + idConcil + "_" + data.getId();

        // PASO 1.
        // Validar si la tabla no existe, hay que crearla copiando la estructura de TEMPORAL_ci
        String createTableQuery = "IF OBJECT_ID('" + tableName + "', 'U') IS NULL " +
                "BEGIN CREATE TABLE ";
        createTableQuery+=(tableName)+(" (");
        for (int i = 0; i < data.getCampos().size(); i++) {
            CampoRConcil column = data.getCampos().get(i);
            createTableQuery+=(column.getNombre())+(" ")+(column.getTipo());
            if (column.getTipo().equalsIgnoreCase("VARCHAR"))
                createTableQuery+=("(MAX)");
            if (i < data.getCampos().size() - 1)
                createTableQuery+=(", ");
        }
        if (!data.getCampos().isEmpty())
            createTableQuery+=(", ");
        createTableQuery+=("INVENTARIO VARCHAR(MAX), ")
                +("ID_INVENTARIO INT, ")
                +("FECHA_CONCILIACION DATE, ")
                +("TIPO_EVENTO VARCHAR(MAX), ")
                +("CDGO_MATRIZ_EVENTO INT, ")
                +("CENTRO_CONTABLE VARCHAR(MAX), ")
                +("CUENTA_CONTABLE_1 VARCHAR(MAX), ")
                +("DIVISA_CUENTA_1 VARCHAR(MAX), ")
                +("VALOR_CUENTA_1 FLOAT, ")
                +("CUENTA_CONTABLE_2 VARCHAR(MAX), ")
                +("DIVISA_CUENTA_2 VARCHAR(MAX), ")
                +("VALOR_CUENTA_2 FLOAT");
        createTableQuery+=("); END;");

        // PASO 2.
        // Eliminar los registros de la fecha del cruce
        String deleteQuery = "DELETE FROM " + tableName + " WHERE FECHA_CONCILIACION = :fechaConciliacion";

        // PASO 3.
        // Insertar registros desde "preciso_rc_<data.getId()>" a la nueva tabla de esa fecha
        String sourceTableName = "preciso_rconcil_" + data.getId();
        String insertDataQuery = "INSERT INTO "+tableName+" (";
        for (int i = 0; i < data.getCampos().size(); i++) {
            CampoRConcil column = data.getCampos().get(i);
            insertDataQuery+=(column.getNombre());
            if (i < data.getCampos().size() - 1)
                insertDataQuery+=", ";
        }
        insertDataQuery+=(", INVENTARIO, ")
                +("ID_INVENTARIO, ")
                +("FECHA_CONCILIACION, ")
                +("TIPO_EVENTO, ")
                +("CDGO_MATRIZ_EVENTO, ")
                +("CENTRO_CONTABLE, ")
                +("CUENTA_CONTABLE_1, ")
                +("DIVISA_CUENTA_1, ")
                +("VALOR_CUENTA_1, ")
                +("CUENTA_CONTABLE_2, ")
                +("DIVISA_CUENTA_2, ")
                +("VALOR_CUENTA_2");
        insertDataQuery+=(") SELECT ");
        // Agregar los campos correspondientes en el SELECT
        for (int i = 0; i < data.getCampos().size(); i++) {
            CampoRConcil column = data.getCampos().get(i);
            insertDataQuery+=(column.getNombre());
            if (i < data.getCampos().size() - 1)
                insertDataQuery+=(", ");
        }
        insertDataQuery+=(", INVENTARIO, ")
                +("ID_INVENTARIO, ")
                +("FECHA_CONCILIACION, ")
                +("TIPO_EVENTO, ")
                +("CDGO_MATRIZ_EVENTO, ")
                +("CENTRO_CONTABLE, ")
                +("CUENTA_CONTABLE_1, ")
                +("DIVISA_CUENTA_1, ")
                +("VALOR_CUENTA_1, ")
                +("CUENTA_CONTABLE_2, ")
                +("DIVISA_CUENTA_2, ")
                +("VALOR_CUENTA_2");
        insertDataQuery+=(" FROM TEMPORAL_ci;");

        // PASO 4.
        // Ejecutar las consultas
        Query createTable = entityManager.createNativeQuery(createTableQuery);
        createTable.executeUpdate();

        Query deleteRecords = entityManager.createNativeQuery(deleteQuery);
        deleteRecords.setParameter("fechaConciliacion", fecha);
        deleteRecords.executeUpdate();

        Query insertData = entityManager.createNativeQuery(insertDataQuery);
        insertData.executeUpdate();
    }






    public void creatTablaTemporalCruce(ConciliationRoute data, String fecha){
        StringBuilder createTableQuery = new StringBuilder("CREATE TABLE ");
        String tableName = "TEMPORAL_ci";
        createTableQuery.append(tableName).append(" (");

        // Paso 1: Agregar los campos existentes de la tabla original
        for (int i = 0; i < data.getCampos().size(); i++) {
            CampoRConcil column = data.getCampos().get(i);
            createTableQuery.append(column.getNombre())
                    .append(" ")
                    .append(column.getTipo());
            if (column.getTipo().equalsIgnoreCase("VARCHAR"))
                createTableQuery.append("(MAX)"); // Longitud de MAX para VARCHAR
            if (i < data.getCampos().size() - 1)
                createTableQuery.append(", ");
        }

        // Paso 2: Agregar los nuevos campos específicos
        if (!data.getCampos().isEmpty()) {
            createTableQuery.append(", ");
        }
        createTableQuery.append("INVENTARIO VARCHAR(MAX), ")
                .append("ID_INVENTARIO INT IDENTITY(1,1), ")
                .append("FECHA_CONCILIACION DATE, ")
                .append("TIPO_EVENTO VARCHAR(MAX), ")
                .append("CDGO_MATRIZ_EVENTO INT, ")
                .append("CENTRO_CONTABLE VARCHAR(MAX), ")
                .append("CUENTA_CONTABLE_1 VARCHAR(MAX), ")
                .append("DIVISA_CUENTA_1 VARCHAR(MAX), ")
                .append("VALOR_CUENTA_1 FLOAT, ")
                .append("CUENTA_CONTABLE_2 VARCHAR(MAX), ")
                .append("DIVISA_CUENTA_2 VARCHAR(MAX), ")
                .append("VALOR_CUENTA_2 FLOAT");
        createTableQuery.append(");");

        // Validar si la tabla ya existe y eliminarla
        String dropTableQuery = "IF OBJECT_ID('" + tableName + "', 'U') IS NOT NULL " +
                "BEGIN DROP TABLE " + tableName + "; END;";
        Query dropTable = entityManager.createNativeQuery(dropTableQuery);
        dropTable.executeUpdate();

        Query createTable = entityManager.createNativeQuery(createTableQuery.toString());
        createTable.executeUpdate();

        // Paso 3: Insertar registros desde "preciso_rc_<data.getId()>" a la nueva tabla
        String sourceTableName = "preciso_rconcil_" + data.getId();
        StringBuilder insertDataQuery = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        // Agregar solo los nombres de los campos existentes en la tabla original
        for (int i = 0; i < data.getCampos().size(); i++) {
            CampoRConcil column = data.getCampos().get(i);
            insertDataQuery.append(column.getNombre());
            if (i < data.getCampos().size() - 1) {
                insertDataQuery.append(", ");
            }
        }
        insertDataQuery.append(") SELECT ");
        for (int i = 0; i < data.getCampos().size(); i++) {
            CampoRConcil column = data.getCampos().get(i);
            insertDataQuery.append(column.getNombre());
            if (i < data.getCampos().size() - 1)
                insertDataQuery.append(", ");
        }

        insertDataQuery.append(" FROM ").append(sourceTableName).append(" WHERE periodo_preciso = ?;");

        // Ejecutar la consulta para insertar los datos
        Query insertData = entityManager.createNativeQuery(insertDataQuery.toString());
        insertData.setParameter(1, fecha); // Pasar el valor de la fecha como parámetro
        insertData.executeUpdate();
    }

    public void completarTablaCruce(ConciliationRoute data,
                                    String fecha,
                                    EventType tipoEvento,
                                    EventMatrix matriz,
                                    AccountEventMatrix cuenta1,
                                    AccountEventMatrix cuenta2,
                                    String condicion) {

        String valorCuenta = null;
        if (cuenta2 != null && cuenta2.getCampoValorCuenta() != null) {
            valorCuenta = cuenta2.getCampoValorCuenta().getNombre();
        } else if (cuenta2 != null && cuenta2.getCampoValorOp1() != null) {
            valorCuenta = cuenta2.getCampoValorOp1().getNombre() + "*" + cuenta2.getValorOp2();
        }

        // Crear la base de la consulta SQL
        StringBuilder queryBuilder = new StringBuilder("UPDATE TEMPORAL_ci SET ");
        queryBuilder.append("INVENTARIO = ?, ");
        queryBuilder.append("FECHA_CONCILIACION = ?, ");
        queryBuilder.append("TIPO_EVENTO = ?, ");
        queryBuilder.append("CDGO_MATRIZ_EVENTO = ?, ");
        queryBuilder.append("CENTRO_CONTABLE = ?, ");
        queryBuilder.append("CUENTA_CONTABLE_1 = ?, ");
        queryBuilder.append("DIVISA_CUENTA_1 = ").append(cuenta1.getCampoDivisa().getNombre()).append(", ");
        queryBuilder.append("VALOR_CUENTA_1 = ").append(cuenta1.getCampoValorCuenta().getNombre());

        // Si cuenta2 no es nula, agregar los valores relacionados a cuenta2
        if (cuenta2 != null) {
            queryBuilder.append(", CUENTA_CONTABLE_2 = ?, ");
            queryBuilder.append("DIVISA_CUENTA_2 = ").append(cuenta2.getCampoDivisa().getNombre()).append(", ");
            queryBuilder.append("VALOR_CUENTA_2 = ").append(valorCuenta).append(" ");
        }

        if(condicion!=null){
            queryBuilder.append(" WHERE "+condicion);
        }

        // Completar la consulta
        String query = queryBuilder.toString();

        // Crear el query y setear los parámetros
        Query updateQuery = entityManager.createNativeQuery(query);
        updateQuery.setParameter(1, data.getDetalle());
        updateQuery.setParameter(2, fecha);
        updateQuery.setParameter(3, tipoEvento.getNombre());
        updateQuery.setParameter(4, matriz.getId());
        updateQuery.setParameter(5, matriz.getCentroContable());
        updateQuery.setParameter(6, cuenta1.getCuentaGanancia());

        // Si cuenta2 no es nula, agregar el parámetro adicional
        if (cuenta2 != null) {
            updateQuery.setParameter(7, cuenta2.getCuentaGanancia());
        }

        // Ejecutar la consulta
        updateQuery.executeUpdate();
    }


    public String conditionData(ConciliationRoute data, EventMatrix matriz){
        String nombreTabla = "TEMPORAL_ci";
        Query querySelect = entityManager.createNativeQuery("SELECT " +
                "a.id_campo, c.nombre, a.condicion, a.valor_condicion" +
                "  FROM [PRECISO].[dbo].[preciso_condiciones_matriz_evento] a" +
                "  left join [PRECISO].[dbo].[preciso_matriz_eventos] b " +
                "on a.id_matriz = b.id left join [PRECISO].[dbo].[preciso_campos_rconcil] c " +
                "on a.id_matriz = b.id and a.id_campo = c.id_campo " +
                "where b.id_inventario_conciliacion = ?  and a.id_matriz = ? " +
                "and a.estado=1 order by a.id_campo");
        querySelect.setParameter(1,data.getId());
        querySelect.setParameter(2,matriz.getId());
        List<Object[]> condicionesLista = querySelect.getResultList();
        if(!condicionesLista.isEmpty()){
            String condicion = "(";
            String campo = condicionesLista.get(0)[2].toString();
            for(Object[] obj : condicionesLista){
                if(campo.equals(obj[2]) && !condicion.equals("(")){
                    condicion = condicion + " OR ";
                }
                else if(!condicion.equals("(")){
                    campo = obj[2].toString();
                    condicion = condicion + ") AND (";
                }

                String operacion = null;

                if (obj[2].equals("igual")) operacion = " = '" + obj[3].toString() + "'";
                if (obj[2].equals("diferente")) operacion = " <> '" + obj[3].toString() + "'";
                if (obj[2].equals("contiene")) operacion = " LIKE '%" + obj[3].toString() + "%'";
                if (obj[2].equals("noContiene")) operacion = " NOT LIKE '%" + obj[3].toString() + "%'";
                if (obj[2].equals("mayorQue")) operacion = " > '" + obj[3].toString() + "'";
                if (obj[2].equals("menorQue")) operacion = " < '" + obj[3].toString() + "'";
                if (obj[2].equals("mayorIgualQue")) operacion = " >= '" + obj[3].toString() + "'";
                if (obj[2].equals("menorIgualQue")) operacion = " <= '" + obj[3].toString() + "'";

                // Agrupa: se usará una expresión REGEXP para buscar múltiples valores
                if (obj[2].equals("agrupa")) operacion = " REGEXP '" + obj[3].toString().replace(",", "|") + "'";

                // No Agrupa: Se niega la expresión REGEXP
                if (obj[2].equals("noAgrupa")) operacion = " NOT REGEXP '" + obj[3].toString().replace(",", "|") + "'";

                condicion = condicion + obj[1] + operacion;
            }
            condicion+=")";
            System.out.println("CONDICION "+  condicion);
            return condicion;
        }
        return null;
    }

    public void validationData(ConciliationRoute data, EventMatrix matriz, String condicion) {
        String nombreTabla = "TEMPORAL_ci";
        Query querySelect = entityManager.createNativeQuery(
                "SELECT \n" +
                        "\t\tb.nombre as campo_validacion, \n" +
                        "\t\ta.valor_validacion, \n" +
                        "\t\tc.nombre as campo_afecta, \n" +
                        "\t\td.nombre as campo,\n" +
                        "\t\ta.valor_operacion,\n" +
                        "\t\tCASE a.operacion WHEN 'Suma' THEN '+' WHEN 'Resta' THEN '-' WHEN 'Multiplica' THEN '*' WHEN 'Divida' THEN '/' ELSE '' END as Operacion \n" +
                        "\t\t\t FROM PRECISO.dbo.preciso_validaciones_matriz_evento a \n" +
                        "\t\t\t\tLEFT JOIN PRECISO.dbo.preciso_campos_rconcil b ON a.id_campo_validacion = b.id_campo \n" +
                        "\t\t\t\tLEFT JOIN PRECISO.dbo.preciso_campos_rconcil c ON a.id_campo_afecta = c.id_campo\n" +
                        "\t\t\t\tLEFT JOIN PRECISO.dbo.preciso_campos_rconcil d ON a.id_campo_referencia = d.id_campo\n" +
                        "WHERE a.id_me=? AND a.estado = 1");
        querySelect.setParameter(1, matriz.getId());
        List<Object[]> validacionLista = querySelect.getResultList();

        if (!validacionLista.isEmpty()) {
            for (Object[] obj : validacionLista) {
                String operacion = obj[4] != null ? obj[4].toString() : "";

                // Determinar cuál campo actualizar
                String campoActualizar = obj[2] != null ? obj[2].toString() : obj[0].toString();

                // Construir la consulta dependiendo de si hay operación o no
                String queryUpdate;
                if (!operacion.isEmpty() && operacion.isBlank()) {
                    queryUpdate = "UPDATE " + nombreTabla + " SET " +
                            campoActualizar + " = CAST(TRY_CAST(" + obj[0].toString() + " AS DECIMAL(38, 0)) * 0.01 " +
                            operacion + obj[3].toString() + " AS VARCHAR) " +
                            "WHERE " + obj[1].toString() + " = '" + obj[2].toString() + "' AND " + condicion;
                } else {
                    queryUpdate = "UPDATE " + nombreTabla + " SET " +
                            campoActualizar + " = '" + obj[4].toString() + "' " +
                            "WHERE " + obj[0].toString() + " = '" + obj[1].toString() + "' AND " + condicion;
                }

                // Ejecutar la consulta
                System.out.println("QUERY -> " + queryUpdate);
                jdbcTemplate.execute(queryUpdate);
            }
        }
    }



    public List<Object[]> findAllData(Conciliation concil, String fecha, EventType evento) {
        List<Object[]>list = new ArrayList<>();
        try {
            List<ConciliationRoute> listRoutes = conciliationRouteService.getRoutesByConciliation(concil.getId()); // RUTAS CONCILIACIONES
            StringBuilder queryBuilder = new StringBuilder("");
            String nombreTabla = "preciso_ci_" + concil.getId();
            queryBuilder.append("IF EXISTS (SELECT 1 \n" +
                    "           FROM INFORMATION_SCHEMA.TABLES \n" +
                    "           WHERE TABLE_NAME = '"+nombreTabla+"')\n" +
                    "BEGIN " +
                    "SELECT FECHA_CONCILIACION, CENTRO_CONTABLE, CUENTA_CONTABLE, DIVISA_CUENTA, sum([TOTAL_VALOR_CUENTA]) AS TOTAL_VALOR_CUENTA " +
                    "FROM " + nombreTabla + " " +
                    "WHERE FECHA_CONCILIACION = :fecha AND TIPO_EVENTO = :tipoEvento " +  // Corregido el espacio
                    "GROUP BY [FECHA_CONCILIACION], [CENTRO_CONTABLE], [CUENTA_CONTABLE], [DIVISA_CUENTA], TIPO_EVENTO");

            queryBuilder.append(" ORDER BY FECHA_CONCILIACION, CENTRO_CONTABLE, CUENTA_CONTABLE, DIVISA_CUENTA, TIPO_EVENTO; END " +
                    "ELSE\n" +
                    "BEGIN\n" +
                    "    SELECT NULL AS FECHA_CONCILIACION, NULL AS CENTRO_CONTABLE, NULL AS CUENTA_CONTABLE, \n" +
                    "           NULL AS DIVISA_CUENTA, NULL AS TOTAL_VALOR_CUENTA\n" +
                    "    WHERE 1 = 0;\n" +
                    "END");

            // Crear la consulta
            Query querySelect = entityManager.createNativeQuery(queryBuilder.toString());

            // Establecer el parámetro de fecha
            querySelect.setParameter("fecha", fecha);
            querySelect.setParameter("tipoEvento", evento.getNombre());
            list= querySelect.getResultList();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return list;

    }


    public List<Object[]> processList(List<Object[]> datos, List<String> colAroutes) {
        List<Object[]> processedList = new ArrayList<>();
        for (Object[] row : datos) {
            Object[] processedRow = new Object[row.length];

            for (int i = 0; i < row.length; i++) {
                // Verificar si la columna es "SALDO INVENTARIO"
                if (colAroutes.get(i).equals("SALDO INVENTARIO")) {
                    try {
                        // Convertir el valor a BigDecimal para evitar notación científica
                        BigDecimal decimalValue = new BigDecimal(row[i].toString());

                        // Usar DecimalFormat con soporte para todos los decimales sin truncar
                        DecimalFormat decimalFormat = new DecimalFormat("#,###.############");
                        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
                        String formattedValue = decimalFormat.format(decimalValue);

                        processedRow[i] = formattedValue;
                    } catch (Exception e) {
                        // Si hay algún error en la conversión, dejamos el valor tal cual está
                        processedRow[i] = row[i];
                    }
                } else {
                    // Si no es de tipo "SALDO INVENTARIO", dejamos el valor tal cual está
                    processedRow[i] = row[i];
                }
            }
            processedList.add(processedRow);
        }

        return processedList;
    }

    public Object[] findLatestLog(String fechaPreciso, int idConciliacion, int idEvento) {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("SELECT TOP 1 fecha_preciso, id_conciliacion, id_evento, confirmar_conciliacion, id_lci ")
                .append("FROM preciso_log_cruce_informacion ")
                .append("WHERE fecha_proceso LIKE '")
                .append(fechaPreciso).append("%' ")  // Insertamos el valor real
                .append("AND id_evento = ")
                .append(idEvento).append(" ")  // Insertamos el valor real
                .append("AND id_conciliacion = ")
                .append(idConciliacion).append(" ")
                .append("ORDER BY fecha_preciso DESC");

        // Imprimir el query con los valores ya insertados
        System.out.println("QUERY COMPLETO: " + queryBuilder.toString());

        // Crear la consulta con parámetros dinámicos (más seguro)
        Query querySelect = entityManager.createNativeQuery(queryBuilder.toString());

        try {
            return (Object[]) querySelect.getSingleResult(); // Retorna el único resultado esperado
        } catch (NoResultException e) {
            return null; // Retorna null si no encuentra nada
        }
    }

    public void confirmarConciliacion(int id){
        Query query = entityManager.createNativeQuery("UPDATE preciso_log_cruce_informacion SET confirmar_conciliacion = 1 , tipo_proceso='Confirmada para conciliación'" +
                "WHERE id_lci = ?");
        query.setParameter(1, id);
        query.executeUpdate();
    }


}