package com.inter.proyecto_intergrupo.service.parametricServices;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    public void loadLogInformationCrossing(User user,int concil, int event, String fecha, String tipo, String estado, String mensaje) {
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

    public List<Object[]> findDataTable(List<ConciliationRoute> listRoutes, String fecha){
        List<Object[]> listTemp = new ArrayList<>();
        try {
            Query querySelect = entityManager.createNativeQuery("SELECT TOP 1 * FROM preciso_ci_"+listRoutes.get(0).getConciliacion().getId()+" WHERE FECHA_CONCILIACION_PRECISOKEY LIKE '"+fecha+"%';");
            listTemp = querySelect.getResultList();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return listTemp;
    }

    public List<Object[]> findByJob(String fecha) {
        String sql = "select a.id,b.id_tipo_evento\n" +
                "from (select * from preciso_conciliaciones where activo = 1) a \n" +
                "inner join (select distinct id_conciliacion,id_tipo_evento from preciso_matriz_eventos) b on a.id = b.id_conciliacion\n" +
                "left join (select distinct estado_proceso,id_conciliacion from preciso_log_cruce_informacion where fecha_preciso like '"+fecha+"%' and estado_proceso = 'Exitoso') c on a.id = c.id_conciliacion\n" +
                "where c.estado_proceso is null";
        Query querySelect = entityManager.createNativeQuery(sql);
        return querySelect.getResultList();
    }

    public void loadLogCargue(User user, int idConcil, String fecha, String tipo, String estado, String mensaje) {
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

    public void recreateTable(ConciliationRoute data, int idConcil,String fecha,EventType tipoEvento) {
        //System.out.println("CREANDO LA TABLA FINAL DE ESE INVENTARIO");
        //System.out.println("#############################");

        String tableName = "preciso_ci_" + idConcil + "_" + data.getId();

        List<CampoRConcil> listCampos = conciliationRouteService.getCamposRcon(data);

        // PASO 1.
        // Validar si la tabla no existe, hay que crearla copiando la estructura de TEMPORAL_ci
        String createTableQuery = "IF OBJECT_ID('" + tableName + "', 'U') IS NULL " +
                "BEGIN CREATE TABLE ";
        createTableQuery+=(tableName)+(" (");
        for (int i = 0; i < listCampos.size(); i++) {
            CampoRConcil column = listCampos.get(i);
            createTableQuery+=(column.getNombre())+(" ");
            if(column.getTipo().equalsIgnoreCase("DATE"))
                createTableQuery+=("VARCHAR");
            else
                createTableQuery+=(column.getTipo());
            if (column.getTipo().equalsIgnoreCase("VARCHAR") || column.getTipo().equalsIgnoreCase("DATE"))
                createTableQuery+=("(MAX)");
            if (i < listCampos.size() - 1)
                createTableQuery+=(", ");
        }
        if (!listCampos.isEmpty())
            createTableQuery+=(", ");
        createTableQuery+=("INVENTARIO_PRECISOKEY VARCHAR(MAX), ")
                +("ID_INVENTARIO_PRECISOKEY INT, ")
                +("FECHA_CONCILIACION_PRECISOKEY DATE, ")
                +("TIPO_EVENTO_PRECISOKEY VARCHAR(MAX), ")
                +("CDGO_MATRIZ_EVENTO_PRECISOKEY INT, ")
                +("CENTRO_CONTABLE_PRECISOKEY VARCHAR(MAX), ")
                +("CUENTA_CONTABLE_1_PRECISOKEY VARCHAR(MAX), ")
                +("DIVISA_CUENTA_1_PRECISOKEY VARCHAR(MAX), ")
                +("VALOR_CUENTA_1_PRECISOKEY FLOAT, ")
                +("CUENTA_CONTABLE_2_PRECISOKEY VARCHAR(MAX), ")
                +("DIVISA_CUENTA_2_PRECISOKEY VARCHAR(MAX), ")
                +("VALOR_CUENTA_2_PRECISOKEY FLOAT");
        createTableQuery+=("); END;");

        // PASO 2.
        // Eliminar los registros de la fecha del cruce
        String deleteQuery = "DELETE FROM " + tableName + " WHERE FECHA_CONCILIACION_PRECISOKEY = :fechaConciliacion";

        // PASO 3.
        // Insertar registros desde "preciso_rc_<data.getId()>" a la nueva tabla de esa fecha
        String insertDataQuery = "INSERT INTO "+tableName+" (";
        for (int i = 0; i < listCampos.size(); i++) {
            CampoRConcil column = listCampos.get(i);
            insertDataQuery+=(column.getNombre());
            if (i < listCampos.size() - 1)
                insertDataQuery+=", ";
        }
        insertDataQuery+=(", INVENTARIO_PRECISOKEY, ")
                +("ID_INVENTARIO_PRECISOKEY, ")
                +("FECHA_CONCILIACION_PRECISOKEY, ")
                +("TIPO_EVENTO_PRECISOKEY, ")
                +("CDGO_MATRIZ_EVENTO_PRECISOKEY, ")
                +("CENTRO_CONTABLE_PRECISOKEY, ")
                +("CUENTA_CONTABLE_1_PRECISOKEY, ")
                +("DIVISA_CUENTA_1_PRECISOKEY, ")
                +("VALOR_CUENTA_1_PRECISOKEY, ")
                +("CUENTA_CONTABLE_2_PRECISOKEY, ")
                +("DIVISA_CUENTA_2_PRECISOKEY, ")
                +("VALOR_CUENTA_2_PRECISOKEY");
        insertDataQuery+=(") SELECT ");

        // Agregar los campos correspondientes en el SELECT
        for (int i = 0; i < listCampos.size(); i++) {
            CampoRConcil column = listCampos.get(i);
            insertDataQuery+=(column.getNombre());
            if (i < listCampos.size() - 1)
                insertDataQuery+=(", ");
        }

        insertDataQuery+=(", ISNULL(INVENTARIO_PRECISOKEY,'"+data.getDetalle()+"')AS INVENTARIO_PRECISOKEY, ")
                +("ID_INVENTARIO_PRECISOKEY, ")
                +("ISNULL(FECHA_CONCILIACION_PRECISOKEY,'"+fecha+"') AS FECHA_CONCILIACION_PRECISOKEY, ")
                +("ISNULL(TIPO_EVENTO_PRECISOKEY,'"+tipoEvento.getNombre()+"') AS TIPO_EVENTO_PRECISOKEY, ")
                +("CDGO_MATRIZ_EVENTO_PRECISOKEY, ")
                +("CENTRO_CONTABLE_PRECISOKEY, ")
                +("CUENTA_CONTABLE_1_PRECISOKEY, ")
                +("DIVISA_CUENTA_1_PRECISOKEY, ")
                +("VALOR_CUENTA_1_PRECISOKEY, ")
                +("CUENTA_CONTABLE_2_PRECISOKEY, ")
                +("DIVISA_CUENTA_2_PRECISOKEY, ")
                +("VALOR_CUENTA_2_PRECISOKEY");
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

        List<CampoRConcil> listCampos = conciliationRouteService.getCamposRcon(data);

        // Paso 1: Agregar los campos existentes de la tabla original
        for (int i = 0; i < listCampos.size(); i++) {
            CampoRConcil column = listCampos.get(i);
            createTableQuery.append(column.getNombre())
                    .append(" ");
            if (column.getTipo().equalsIgnoreCase("DATE"))
                createTableQuery.append("VARCHAR");
            else
                createTableQuery.append(column.getTipo());
            if (column.getTipo().equalsIgnoreCase("VARCHAR") || column.getTipo().equalsIgnoreCase("DATE"))
                createTableQuery.append("(MAX)"); // Longitud de MAX para VARCHAR
            if (i < listCampos.size() - 1)
                createTableQuery.append(", ");
        }

        // Paso 2: Agregar los nuevos campos específicos
        if (!listCampos.isEmpty()) {
            createTableQuery.append(", ");
        }
        createTableQuery.append("INVENTARIO_PRECISOKEY VARCHAR(MAX), ")
                .append("ID_INVENTARIO_PRECISOKEY INT IDENTITY(1,1), ")
                .append("FECHA_CONCILIACION_PRECISOKEY DATE, ")
                .append("TIPO_EVENTO_PRECISOKEY VARCHAR(MAX), ")
                .append("CDGO_MATRIZ_EVENTO_PRECISOKEY INT, ")
                .append("CENTRO_CONTABLE_PRECISOKEY VARCHAR(MAX), ")
                .append("CUENTA_CONTABLE_1_PRECISOKEY VARCHAR(MAX), ")
                .append("DIVISA_CUENTA_1_PRECISOKEY VARCHAR(MAX), ")
                .append("VALOR_CUENTA_1_PRECISOKEY FLOAT, ")
                .append("CUENTA_CONTABLE_2_PRECISOKEY VARCHAR(MAX), ")
                .append("DIVISA_CUENTA_2_PRECISOKEY VARCHAR(MAX), ")
                .append("VALOR_CUENTA_2_PRECISOKEY FLOAT");
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
        for (int i = 0; i < listCampos.size(); i++) {
            CampoRConcil column = listCampos.get(i);
            insertDataQuery.append(column.getNombre());
            if (i < listCampos.size() - 1) {
                insertDataQuery.append(", ");
            }
        }
        insertDataQuery.append(") SELECT ");
        for (int i = 0; i < listCampos.size(); i++) {
            CampoRConcil column = listCampos.get(i);
            insertDataQuery.append(column.getNombre());
            if (i < listCampos.size() - 1)
                insertDataQuery.append(", ");
        }

        insertDataQuery.append(" FROM ").append(sourceTableName).append(" WHERE periodo_preciso = ?;");

        // Ejecutar la consulta para insertar los datos
        Query insertData = entityManager.createNativeQuery(insertDataQuery.toString());
        insertData.setParameter(1, fecha); // Pasar el valor de la fecha como parámetro
        insertData.executeUpdate();
    }

    public String operacionSimbolo(String op){
        if(op.equals("Multiplica")) return "*";
        if(op.equals("Suma")) return "+";
        if(op.equals("Resta")) return "-";
        if(op.equals("Divida")) return "/";
        return "";
    }

    public void completarTablaCruce(ConciliationRoute data,
                                    String fecha,
                                    EventType tipoEvento,
                                    EventMatrix matriz,
                                    AccountEventMatrix cuenta1,
                                    AccountEventMatrix cuenta2,
                                    String condicion) {
        String valorCuenta1 = null;
        if (cuenta1 != null && !cuenta1.isManejaFormula())
            valorCuenta1 = cuenta1.getCampoValorCuenta().getNombre();
        else if (cuenta1 != null)
            valorCuenta1 = "CASE WHEN "+cuenta1.getCampoValorOp1().getNombre() + " LIKE '%,%' THEN CAST(REPLACE(REPLACE("+cuenta1.getCampoValorOp1().getNombre() + ", '.', ''), ',', '.') AS DECIMAL(10, 2)) ELSE CAST("+cuenta1.getCampoValorOp1().getNombre() +" AS DECIMAL(10, 2)) END " + operacionSimbolo(cuenta1.getOperacion()) + " " +
                    (cuenta1.getCampoValorOp2() != null ? cuenta1.getCampoValorOp2().getNombre() : cuenta1.getValorOp2());

        // Aplicar ABS si cuenta1.isValorAbsoluto() es true
        if (cuenta1 != null && cuenta1.isValorAbsoluto()) {
            valorCuenta1 = "ABS(" + valorCuenta1 + ")";
        }

        String valorCuenta2 = null;
        if (cuenta2 != null && !cuenta2.isManejaFormula())
            valorCuenta2 = cuenta2.getCampoValorCuenta().getNombre();
        else if (cuenta2 != null)
            valorCuenta2 = "CASE WHEN "+cuenta2.getCampoValorOp1().getNombre() + " LIKE '%,%' THEN CAST(REPLACE(REPLACE("+cuenta2.getCampoValorOp1().getNombre() + ", '.', ''), ',', '.') AS DECIMAL(10, 2)) ELSE "+cuenta2.getCampoValorOp1().getNombre() +" END " + operacionSimbolo(cuenta2.getOperacion()) + " " +
                    (cuenta2.getCampoValorOp2() != null ? cuenta2.getCampoValorOp2().getNombre() : cuenta2.getValorOp2());

        // Aplicar ABS si cuenta2.isValorAbsoluto() es true
        if (cuenta2 != null && cuenta2.isValorAbsoluto()) {
            valorCuenta2 = "ABS(" + valorCuenta2 + ")";
        }

        // Construcción de la consulta SQL
        StringBuilder queryBuilder = new StringBuilder("UPDATE TEMPORAL_ci SET ");
        queryBuilder.append("INVENTARIO_PRECISOKEY = ?, ");
        queryBuilder.append("FECHA_CONCILIACION_PRECISOKEY = ?, ");
        queryBuilder.append("TIPO_EVENTO_PRECISOKEY = ?, ");
        queryBuilder.append("CDGO_MATRIZ_EVENTO_PRECISOKEY = ?, ");
        queryBuilder.append("CENTRO_CONTABLE_PRECISOKEY = ? ");

        if (cuenta1 != null) {
            queryBuilder.append(", CUENTA_CONTABLE_1_PRECISOKEY = ?, ");
            // Aplicar conversión de UVR a COP en DIVISA_CUENTA_1
            if (cuenta1.isConvierteUVRaCOP())
                queryBuilder.append("DIVISA_CUENTA_1_PRECISOKEY = CASE WHEN ").append(cuenta1.getCampoDivisa().getNombre())
                        .append(" = 'UVR' THEN 'COP' ELSE ").append(cuenta1.getCampoDivisa().getNombre()).append(" END, ");
            else if (cuenta1.isConvierteDivisa())
                queryBuilder.append("DIVISA_CUENTA_1_PRECISOKEY = CASE ")
                        .append("WHEN ").append(cuenta1.getCampoDivisa().getNombre()).append(" IN ('USD', 'EUR', 'COP') THEN ")
                        .append(cuenta1.getCampoDivisa().getNombre()).append(" ")
                        .append("WHEN ").append(cuenta1.getCampoDivisa().getNombre()).append(" = 'COD' THEN 'COP' ")
                        .append("ELSE 'RST' END, ");
            else
                queryBuilder.append("DIVISA_CUENTA_1_PRECISOKEY = ").append(cuenta1.getCampoDivisa().getNombre()).append(", ");

            queryBuilder.append("VALOR_CUENTA_1_PRECISOKEY = ").append(valorCuenta1).append(" ");
        }

        // Si cuenta2 no es nula, agregar los valores relacionados a cuenta2
        if (cuenta2 != null) {
            queryBuilder.append(", CUENTA_CONTABLE_2_PRECISOKEY = ?, ");

            // Aplicar conversión de UVR a COP en DIVISA_CUENTA_2
            if (cuenta2.isConvierteUVRaCOP())
                queryBuilder.append("DIVISA_CUENTA_2_PRECISOKEY = CASE WHEN ").append(cuenta2.getCampoDivisa().getNombre())
                        .append(" = 'UVR' THEN 'COP' ELSE ").append(cuenta2.getCampoDivisa().getNombre()).append(" END, ");
            else if (cuenta2.isConvierteDivisa())
                queryBuilder.append("DIVISA_CUENTA_2_PRECISOKEY = CASE ")
                        .append("WHEN ").append(cuenta2.getCampoDivisa().getNombre()).append(" IN ('USD', 'EUR', 'COP') THEN ")
                        .append(cuenta2.getCampoDivisa().getNombre()).append(" ")
                        .append("WHEN ").append(cuenta2.getCampoDivisa().getNombre()).append(" = 'COD' THEN 'COP' ")
                        .append("ELSE 'RST' END, ");
            else
                queryBuilder.append("DIVISA_CUENTA_2_PRECISOKEY = ").append(cuenta2.getCampoDivisa().getNombre()).append(", ");

            queryBuilder.append("VALOR_CUENTA_2_PRECISOKEY = ").append(valorCuenta2).append(" ");
        }

        if (condicion != null)
            queryBuilder.append(" WHERE ").append(condicion);

        // Completar la consulta
        String query = queryBuilder.toString();

        // Crear el query y setear los parámetros
        Query updateQuery = entityManager.createNativeQuery(query);
        int columna=1;
        updateQuery.setParameter(columna++, data.getDetalle());
        updateQuery.setParameter(columna++, fecha);
        updateQuery.setParameter(columna++, tipoEvento.getNombre());
        updateQuery.setParameter(columna++, matriz.getId());
        updateQuery.setParameter(columna++, matriz.isManejaCC() ? matriz.getCampoCC().getNombre() : matriz.getCentroContable());
        if (cuenta1 != null)
            updateQuery.setParameter(columna++, cuenta1.getCuentaGanancia());

        // Si cuenta2 no es nula, agregar el parámetro adicional
        if (cuenta2 != null)
            updateQuery.setParameter(columna++, cuenta2.getCuentaGanancia());

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
                if (obj[2].equals("agrupa")) operacion = " LIKE '%" + obj[3].toString() + "%'";

                // No Agrupa: Se niega la expresión REGEXP
                if (obj[2].equals("noAgrupa")) operacion = " NOT LIKE '%" + obj[3].toString() + "%'";

                condicion = condicion + obj[1] + operacion;
            }
            condicion+=")";
            //System.out.println("CONDICION "+  condicion);
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
                        "\t\ta.campo_afecta_cruce as campo_afecta, \n" +
                        "\t\td.nombre as campo,\n" +
                        "\t\ta.valor_operacion,\n" +
                        "\t\tCASE a.operacion WHEN 'Suma' THEN '+' WHEN 'Resta' THEN '-' WHEN 'Multiplica' THEN '*' WHEN 'Divida' THEN '/' ELSE '' END as Operacion,a.adiciona_campo_afecta, a.aplica_formula \n" +
                        "\t\t\t FROM PRECISO.dbo.preciso_validaciones_matriz_evento a \n" +
                        "\t\t\t\tLEFT JOIN PRECISO.dbo.preciso_campos_rconcil b ON a.id_campo_validacion = b.id_campo \n" +
                        "\t\t\t\tLEFT JOIN PRECISO.dbo.preciso_campos_rconcil d ON a.id_campo_referencia = d.id_campo\n" +
                        "WHERE a.id_me=? AND a.estado = 1");
        querySelect.setParameter(1, matriz.getId());
        List<Object[]> validacionLista = querySelect.getResultList();

        if (!validacionLista.isEmpty()) {
            for (Object[] obj : validacionLista) {
                String operacion = obj[5] != null && (boolean) obj[7] ? obj[5].toString() : "";

                // Determinar cuál campo actualizar
                String campoActualizar = obj[2] != null && (boolean) obj[6] ? obj[2].toString() : obj[3].toString();

                // Construir la consulta dependiendo de si hay operación o no
                String queryUpdate;
                if ((boolean) obj[7] && !operacion.isEmpty() && !operacion.isBlank()) {
                    queryUpdate = "UPDATE " + nombreTabla + " SET " +
                            campoActualizar + " = CAST(CASE WHEN "+obj[3].toString()+" LIKE '%.%' THEN TRY_CAST("+obj[3].toString()+" AS DECIMAL(38, 2)) " +
                            "ELSE TRY_CAST("+obj[3].toString()+" AS DECIMAL(38, 2)) /100.0 END "+ operacion + obj[4].toString() +" AS DECIMAL(38, 2)) "+
                            "WHERE " + obj[0].toString() + " = '" + obj[1].toString() + "' AND " + condicion;
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
                    "SELECT FECHA_CONCILIACION_PRECISOKEY, CENTRO_CONTABLE_PRECISOKEY, CUENTA_CONTABLE_PRECISOKEY, DIVISA_CUENTA_PRECISOKEY, sum([TOTAL_VALOR_CUENTA_PRECISOKEY]) AS TOTAL_VALOR_CUENTA_PRECISOKEY " +
                    "FROM " + nombreTabla + " " +
                    "WHERE FECHA_CONCILIACION_PRECISOKEY = :fecha AND TIPO_EVENTO_PRECISOKEY = :tipoEvento " +  // Corregido el espacio
                    "GROUP BY [FECHA_CONCILIACION_PRECISOKEY], [CENTRO_CONTABLE_PRECISOKEY], [CUENTA_CONTABLE_PRECISOKEY], [DIVISA_CUENTA_PRECISOKEY], TIPO_EVENTO_PRECISOKEY");

            queryBuilder.append(" ORDER BY FECHA_CONCILIACION_PRECISOKEY, CENTRO_CONTABLE_PRECISOKEY, CUENTA_CONTABLE_PRECISOKEY, DIVISA_CUENTA_PRECISOKEY, TIPO_EVENTO_PRECISOKEY; END " +
                    "ELSE\n" +
                    "BEGIN\n" +
                    "    SELECT NULL AS FECHA_CONCILIACION_PRECISOKEY, NULL AS CENTRO_CONTABLE_PRECISOKEY, NULL AS CUENTA_CONTABLE_PRECISOKEY, \n" +
                    "           NULL AS DIVISA_CUENTA_PRECISOKEY, NULL AS TOTAL_VALOR_CUENTA_PRECISOKEY\n" +
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

    public List<Object[]> findEventosPorConcil(int concilId) {
        Query query = entityManager.createNativeQuery(
                "SELECT distinct a.id_tipo_evento, b.nombre_tipo_evento " +
                        "FROM preciso_matriz_eventos a " +
                        "LEFT JOIN preciso_tipo_evento b ON a.id_tipo_evento = b.id_tipo_evento" +
                        " WHERE a.id_conciliacion = :concilId");
        query.setParameter("concilId", concilId);
        return query.getResultList();
    }

    public List<Object[]> findEventosxConcilxInv(int concilId, int invId) {
        Query query = entityManager.createNativeQuery(
                "SELECT distinct a.id_tipo_evento, b.nombre_tipo_evento " +
                        "FROM preciso_matriz_eventos a " +
                        "LEFT JOIN preciso_tipo_evento b ON a.id_tipo_evento = b.id_tipo_evento" +
                        " WHERE a.id_conciliacion = :concilId AND a.id_inventario_conciliacion = :invId");
        query.setParameter("concilId", concilId);
        query.setParameter("invId", invId);
        return query.getResultList();
    }

    public List<Object[]> findAllLogByDate(String fecha) {
        Query query = entityManager.createNativeQuery(
                "WITH CTE AS (\n" +
                        "SELECT \n" +
                        "    id_lci,\n" +
                        "    estado_proceso,\n" +
                        "    fecha_proceso,\n" +
                        "    fecha_preciso,\n" +
                        "    novedad,\n" +
                        "    tipo_proceso,\n" +
                        "    usuario,\n" +
                        "    id_conciliacion,\n" +
                        "    id_evento,\n" +
                        "    COUNT(*) OVER (PARTITION BY fecha_proceso, id_conciliacion,id_evento) AS total_intentos,\n" +
                        "    ROW_NUMBER() OVER (PARTITION BY fecha_proceso, id_conciliacion,id_evento ORDER BY fecha_preciso DESC) AS row_num\n" +
                        "FROM \n" +
                        "    PRECISO.dbo.preciso_log_cruce_informacion\n" +
                        "WHERE fecha_proceso like ? \n" +
                        ")\n" +
                        "SELECT a.id_lci,\n" +
                        "b.nombre,\n" +
                        "'' as nulo,\n" +
                        "d.nombre_tipo_evento,\n" +
                        "a.fecha_proceso,\n" +
                        "a.novedad,\n" +
                        "a.fecha_preciso,\n" +
                        "a.usuario,\n" +
                        "a.tipo_proceso,\n" +
                        "a.estado_proceso,\n" +
                        "a.total_intentos,\n" +
                        "a.id_conciliacion,\n" +
                        "a.id_evento     \n" +
                        "FROM CTE a \n" +
                        "left join preciso_conciliaciones b on a.id_conciliacion =b.id\n" +
                        "left join preciso_tipo_evento d on a.id_evento =d.id_tipo_evento\n" +
                        "WHERE row_num = 1\n" +
                        "ORDER BY \n" +
                        "a.fecha_proceso, a.id_conciliacion,a.id_evento;");
        query.setParameter(1, fecha+"%");
        return query.getResultList();
    }


}