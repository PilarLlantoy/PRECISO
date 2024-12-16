package com.inter.proyecto_intergrupo.service.parametricServices;
import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.AccountingRouteRepository;
import com.inter.proyecto_intergrupo.repository.parametric.LogAccountingLoadRepository;
import com.inter.proyecto_intergrupo.repository.parametric.LogInformationCrossingRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
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
    private EventTypeService eventTypeService;


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

    public void recreateTable(ConciliationRoute data, int idConcil) {

        System.out.println("CREANDO LA TERMPORAL");
        System.out.println("#############################");
        StringBuilder createTableQuery = new StringBuilder("CREATE TABLE ");
        String tableName = "preciso_ci_" + idConcil + "_" + data.getId();
        createTableQuery.append(tableName).append(" (");

        // Paso 1: Agregar los campos existentes de la tabla original
        for (int i = 0; i < data.getCampos().size(); i++) {
            CampoRConcil column = data.getCampos().get(i);
            createTableQuery.append(column.getNombre())
                    .append(" ")
                    .append(column.getTipo());

            if (column.getTipo().equalsIgnoreCase("VARCHAR")) {
                createTableQuery.append("(MAX)"); // Longitud de MAX para VARCHAR
            }

            if (i < data.getCampos().size() - 1) {
                createTableQuery.append(", ");
            }
        }

        // Paso 2: Agregar los nuevos campos específicos
        if (!data.getCampos().isEmpty()) {
            createTableQuery.append(", ");
        }

        createTableQuery.append("INVENTARIO VARCHAR(MAX), ")
                .append("ID_INVENTARIO INT, ")
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

        // *** Cerrar la definición de la tabla ***
        createTableQuery.append(");"); // Cerramos correctamente con paréntesis y punto y coma

        // Validar si la tabla ya existe y eliminarla
        String dropTableQuery = "IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '"
                + tableName + "' AND TABLE_SCHEMA = 'dbo') " +
                "BEGIN DROP TABLE " + tableName + "; END;";

        // Ejecutar las consultas
        Query dropTable = entityManager.createNativeQuery(dropTableQuery);
        dropTable.executeUpdate();

        Query createTable = entityManager.createNativeQuery(createTableQuery.toString());
        createTable.executeUpdate();
    }

    public void rellenarTablaCruceTotal(ConciliationRoute data, int idConcil){
        String tableName = "preciso_ci_" + idConcil + "_" + data.getId();
        String tableNameTemporal = "TEMPORAL_ci";

        // Paso 3: Insertar registros desde "TEMPORAL_ci" a la nueva tabla
        StringBuilder insertDataQuery = new StringBuilder("INSERT INTO ").append(tableName).append(" (");

        // Agregar solo los nombres de los campos existentes en la tabla original
        for (int i = 0; i < data.getCampos().size(); i++) {
            CampoRConcil column = data.getCampos().get(i);
            insertDataQuery.append(column.getNombre());
            if (i < data.getCampos().size() - 1)
                insertDataQuery.append(", ");
        }
        insertDataQuery.append(", INVENTARIO, ")
                .append("ID_INVENTARIO, ")
                .append("FECHA_CONCILIACION, ")
                .append("TIPO_EVENTO, ")
                .append("CDGO_MATRIZ_EVENTO, ")
                .append("CENTRO_CONTABLE, ")
                .append("CUENTA_CONTABLE_1, ")
                .append("DIVISA_CUENTA_1, ")
                .append("VALOR_CUENTA_1, ")
                .append("CUENTA_CONTABLE_2, ")
                .append("DIVISA_CUENTA_2, ")
                .append("VALOR_CUENTA_2");

        insertDataQuery.append(") SELECT ");
        // Agregar los campos correspondientes en el SELECT
        for (int i = 0; i < data.getCampos().size(); i++) {
            CampoRConcil column = data.getCampos().get(i);
            insertDataQuery.append(column.getNombre());
            if (i < data.getCampos().size() - 1) {
                insertDataQuery.append(", ");
            }
        }
        insertDataQuery.append(", INVENTARIO, ")
                .append("ID_INVENTARIO, ")
                .append("FECHA_CONCILIACION, ")
                .append("TIPO_EVENTO, ")
                .append("CDGO_MATRIZ_EVENTO, ")
                .append("CENTRO_CONTABLE, ")
                .append("CUENTA_CONTABLE_1, ")
                .append("DIVISA_CUENTA_1, ")
                .append("VALOR_CUENTA_1, ")
                .append("CUENTA_CONTABLE_2, ")
                .append("DIVISA_CUENTA_2, ")
                .append("VALOR_CUENTA_2");

        insertDataQuery.append(" FROM ").append(tableNameTemporal).append(";");

        // Ejecutar la consulta para insertar los datos
        Query insertData = entityManager.createNativeQuery(insertDataQuery.toString());
        insertData.executeUpdate();
    }


    public void creatTablaTemporalCruce(ConciliationRoute data){
        StringBuilder createTableQuery = new StringBuilder("CREATE TABLE ");
        String tableName = "TEMPORAL_ci";
        createTableQuery.append(tableName).append(" (");

        // Paso 1: Agregar los campos existentes de la tabla original
        for (int i = 0; i < data.getCampos().size(); i++) {
            CampoRConcil column = data.getCampos().get(i);
            createTableQuery.append(column.getNombre())
                    .append(" ")
                    .append(column.getTipo());

            if (column.getTipo().equalsIgnoreCase("VARCHAR")) {
                createTableQuery.append("(MAX)"); // Longitud de MAX para VARCHAR
            }

            if (i < data.getCampos().size() - 1) {
                createTableQuery.append(", ");
            }
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

        // *** Cerrar la definición de la tabla ***
        createTableQuery.append(");"); // Cerramos correctamente con paréntesis y punto y coma

        // Validar si la tabla ya existe y eliminarla
        String dropTableQuery = "IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '"
                + tableName + "' AND TABLE_SCHEMA = 'dbo') " +
                "BEGIN DROP TABLE " + tableName + "; END;";

        // Ejecutar las consultas
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

        // Agregar los campos correspondientes en el SELECT
        for (int i = 0; i < data.getCampos().size(); i++) {
            CampoRConcil column = data.getCampos().get(i);
            insertDataQuery.append(column.getNombre());

            if (i < data.getCampos().size() - 1) {
                insertDataQuery.append(", ");
            }
        }

        insertDataQuery.append(" FROM ").append(sourceTableName).append(";");

        // Ejecutar la consulta para insertar los datos
        Query insertData = entityManager.createNativeQuery(insertDataQuery.toString());
        insertData.executeUpdate();
    }

    public void completarTablaCruce(ConciliationRoute data,
                                    String fecha,
                                    EventType tipoEvento,
                                    EventMatrix matriz,
                                    AccountEventMatrix cuenta1,
                                    AccountEventMatrix cuenta2
                                    ) {

        String valorCuenta = null;
        if(cuenta2.getCampoValorCuenta()!=null)
            valorCuenta=cuenta2.getCampoValorCuenta().getNombre();
        else
            valorCuenta=cuenta2.getCampoValorOp1().getNombre()+"*"+cuenta2.getValorOp2();


        // Crear la consulta SQL para insertar los valores
        String tableName = "TEMPORAL_ci";
        Query updateQuery = entityManager.createNativeQuery("UPDATE " +tableName+
                " SET INVENTARIO = ?, " +
                "    FECHA_CONCILIACION = ?, " +
                "    TIPO_EVENTO = ?, " +
                "    CDGO_MATRIZ_EVENTO = ?, " +
                "    CENTRO_CONTABLE = ?, " +

                "    CUENTA_CONTABLE_1 = ?, " +
                "    DIVISA_CUENTA_1 = " +cuenta1.getCampoDivisa().getNombre()+", "+
                "    VALOR_CUENTA_1 = "+cuenta1.getCampoValorCuenta().getNombre()+", "+

                "    CUENTA_CONTABLE_2 = ?, " +
                "    DIVISA_CUENTA_2 = " +cuenta2.getCampoDivisa().getNombre()+", "+
                "    VALOR_CUENTA_2 = "+valorCuenta+" "+
                ""); // Reemplaza <CONDICION> con la condición adecuada para identificar los registros a actualizar

        updateQuery.setParameter(1,data.getDetalle());
        updateQuery.setParameter(2,fecha);
        updateQuery.setParameter(3,tipoEvento.getNombre());
        updateQuery.setParameter(4,matriz.getId());
        updateQuery.setParameter(5,matriz.getCentroContable());
        updateQuery.setParameter(6,cuenta1.getCuentaGanancia());
        updateQuery.setParameter(7,cuenta2.getCuentaGanancia());
        updateQuery.executeUpdate();
    }


    public void conditionData(ConciliationRoute data, EventMatrix matriz){
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
                if(obj[2].equals("igual")) operacion = " = '";
                if(obj[2].equals("noContiene")) operacion = " != '";
                if(obj[2].equals("diferente")) operacion = " != '";

                condicion=condicion+obj[1]+operacion+obj[3].toString()+"'";//nombre condicion valor ->  PE: Divisa_entrada	= COP
            }

            Query deleteSelect = entityManager.createNativeQuery("DELETE FROM "+nombreTabla+" WHERE NOT("+ condicion +"));");
            deleteSelect.executeUpdate();

        }

    }

    public List<Object[]> conditionDataSelect(ConciliationRoute data, int idConcil){
        String nombreTabla = "preciso_ci_"+idConcil+"_"+data.getId();

        Query querySelect = entityManager.createNativeQuery("SELECT\n" +
                "\t\ta.id_campo,\n" +
                "\t\tc.nombre,\n" +
                "\t\ta.condicion,\n" +
                "\t\ta.valor_condicion\t  \n" +
                "  FROM [PRECISO].[dbo].[preciso_condiciones_matriz_evento] a\n" +
                "  left join [PRECISO].[dbo].[preciso_matriz_eventos] b\n" +
                "\ton a.id_matriz = b.id\n" +
                "  left join [PRECISO].[dbo].[preciso_campos_rconcil] c\n" +
                "\ton a.id_matriz = b.id \n" +
                "\tand a.id_campo = c.id_campo\n" +
                "  where b.id_inventario_conciliacion = ? \n" +
                "    and a.estado=1 \n" +
                "  order by a.id_campo\n");
        querySelect.setParameter(1,data.getId());

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
                if(obj[2].equals("igual")) operacion = " = '";
                if(obj[2].equals("noContiene")) operacion = " != '";
                if(obj[2].equals("diferente")) operacion = " != '";

                condicion=condicion+obj[1]+operacion+obj[3].toString()+"'";//nombre condicion valor ->  PE: Divisa_entrada	= COP
            }

            Query query = entityManager.createNativeQuery("SELECT * FROM "+nombreTabla+" WHERE ("+ condicion +"));");

            return query.getResultList();

        }
        return null;

    }

    public List<Object[]> findAllData(Conciliation concil, String fecha,EventType evento) {
        // Construir la consulta básica
        String nombreTabla = "preciso_ci_1009_1010";
        StringBuilder queryBuilder = new StringBuilder("SELECT [FECHA_CONCILIACION],\n" +
                "    [CENTRO_CONTABLE],\n" +
                "    [CUENTA_CONTABLE_1],\n" +
                "    [DIVISA_CUENTA_1],\n" +
                "    SUM([VALOR_CUENTA_1]) AS TOTAL_VALOR_CUENTA_1" +
                " FROM " + nombreTabla+" GROUP BY \n" +
                "    [FECHA_CONCILIACION],\n" +
                "    [CENTRO_CONTABLE],\n" +
                "    [CUENTA_CONTABLE_1],\n" +
                "    [DIVISA_CUENTA_1]");
        // Crear la consulta
        Query querySelect = entityManager.createNativeQuery(queryBuilder.toString());

        return querySelect.getResultList();
    }

    public List<Object[]> processList(List<Object[]> datos, List<String> colAroutes) {
        List<Object[]> processedList = new ArrayList<>();

        for (Object[] row : datos) {
            Object[] processedRow = new Object[row.length];

            for (int i = 0; i < row.length; i++) {
                // Verificar si la columna es "SALDO INVENTARIO"
                if (colAroutes.get(i).equals("SALDO INVENTARIO")) {
                    System.out.println("SALDO INVENTARIO " + row[i].toString());
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


}