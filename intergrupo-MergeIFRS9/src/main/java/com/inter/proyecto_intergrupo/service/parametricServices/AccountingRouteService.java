package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.AccountingRouteRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CampoRCRepository;
import com.inter.proyecto_intergrupo.repository.parametric.LogAccountingLoadRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.math.BigDecimal;

import static com.inter.proyecto_intergrupo.controller.parametric.AccountingLoadController.rutaArchivoFormato;


@Service
@EnableTransactionManagement
public class AccountingRouteService {

    @Autowired
    private final AccountingRouteRepository accountingRouteRepository;

    @Autowired
    private CampoRCRepository campoRCRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JobAutoService jobAutoService;

    @Autowired
    private CampoRCService campoRCService;

    @Autowired
    private MasterInventService masterInventService;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private LogAccountingLoadRepository logAccountingLoadRepository;

    @Autowired
    public AccountingRouteService(AccountingRouteRepository accountingRouteRepository) {
        this.accountingRouteRepository = accountingRouteRepository;
    }

    public List <AccountingRoute> findAll(){return accountingRouteRepository.findAll();}

    public List<AccountingRoute> findAllActive() {
        return accountingRouteRepository.findByActivo(true);
    }

    public List<CampoRC> getCamposRC(AccountingRoute data) {
        Query querySelect = entityManager.createNativeQuery(
                "select b.* from preciso_rutas_contables a, preciso_campos_rc b where a.id_rc = b.id_rc and a.id_rc = " + data.getId(),CampoRC.class);
        return querySelect.getResultList();
    }

    public List<Object[]> processList(List<Object[]> aroutes, List<CampoRC> colAroutes) {
        List<Object[]> processedList = new ArrayList<>();

        for (Object[] row : aroutes) {
            Object[] processedRow = new Object[row.length];

            for (int i = 0; i < row.length; i++) {
                // Obtener el tipo de la columna desde colAroutes
                String tipo = "";
                if (colAroutes.size() == i)
                    tipo = "Ultimo";
                else
                    tipo = colAroutes.get(i).getTipo();

                if ("Float".equalsIgnoreCase(tipo) || "Integer".equalsIgnoreCase(tipo) || "Bigint".equalsIgnoreCase(tipo)) {
                    try {
                        // Convertir el valor a BigDecimal para evitar notación científica
                        BigDecimal decimalValue = new BigDecimal(row[i].toString());

                        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
                        symbols.setGroupingSeparator(','); // Separador de miles como punto
                        symbols.setDecimalSeparator('.'); // Separador decimal como coma

                        DecimalFormat decimalFormat = new DecimalFormat("#,##0.##########", symbols);
                        decimalFormat.setParseBigDecimal(true);

                        // Formatear el valor
                        String formattedValue = decimalFormat.format(decimalValue);

                        processedRow[i] = formattedValue;
                    } catch (Exception e) {
                        // Si hay algún error en la conversión, dejamos el valor tal cual está
                        processedRow[i] = row[i];
                    }
                } else {
                    // Si no es de tipo "Float", dejamos el valor tal cual está
                    processedRow[i] = row[i];
                }
            }
            processedList.add(processedRow);
        }

        return processedList;
    }

    public AccountingRoute findById(int id){
        return accountingRouteRepository.findAllById(id);
    }

    public List<CampoRC> findByCampoVisual(AccountingRoute accountingRoute){
        Query querySelect = entityManager.createNativeQuery("select * from preciso_campos_rc where id_rc = ? and visualizacion = 1 ", CampoRC.class);
        querySelect.setParameter(1,accountingRoute.getId());
        return querySelect.getResultList();
    }

    public List<AccountingRoute> findByJob() {
        LocalTime now = LocalTime.now(); // Hora actual
        LocalTime thirtyMinutesBefore = now.minusMinutes(29); // Hora hace 30 minutos

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        String timeBefore = thirtyMinutesBefore.format(formatter);
        String timeNow = now.format(formatter);

        String sql = "SELECT * FROM preciso_rutas_contables WHERE activo = 1 AND hora_cargue BETWEEN '"+timeBefore+"' AND '"+timeNow+"'";

        Query querySelect = entityManager.createNativeQuery(sql, AccountingRoute.class);
        return querySelect.getResultList();
    }

    public List<AccountingRoute> findByJobNotLoad(String fecha) {
        String sql = "SELECT * FROM (select * from preciso_rutas_contables WHERE activo = 1) a\n" +
                "left join (select id_rc,estado_proceso from preciso_log_cargues_contables where fecha_cargue like '"+fecha+"%' group by id_rc,estado_proceso) b on a.id_rc = b.id_rc\n" +
                "where b.id_rc is null ";
        Query querySelect = entityManager.createNativeQuery(sql, AccountingRoute.class);
        return querySelect.getResultList();
    }

    public AccountingRoute findByName(String nombre){
        return accountingRouteRepository.findAllByNombre(nombre);
    }

    public AccountingRoute modificar(AccountingRoute conciliacion){
        accountingRouteRepository.save(conciliacion);
       return conciliacion;
    }

    public List<LogAccountingLoad> findAllLog(AccountingRoute ac, String fecha) {
        LocalDate localDate = LocalDate.parse(fecha);
        Date fechaDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        return logAccountingLoadRepository.findAllByIdRcAndFechaCargueOrderByIdDesc(ac,fechaDate);
    }

    public List<Object[]> findAllLogByDate(String fecha) {
        Query query = entityManager.createNativeQuery(
                "WITH CTE AS (\n" +
                        "SELECT \n" +
                        "[id_lcc],\n" +
                        "[cantidad_registros],\n" +
                        "[estado_proceso],\n" +
                        "[fecha_cargue],\n" +
                        "[fecha_preciso],\n" +
                        "[novedad],\n" +
                        "[tipo_proceso],\n" +
                        "[usuario],\n" +
                        "[id_rc],\n" +
                        "COUNT(*) OVER (PARTITION BY [fecha_cargue], [id_rc]) AS total_intentos,\n" +
                        "ROW_NUMBER() OVER (PARTITION BY [fecha_cargue], [id_rc] ORDER BY [fecha_preciso] DESC) AS row_num\n" +
                        "FROM \n" +
                        "[PRECISO].[dbo].[preciso_log_cargues_contables]\n" +
                        "WHERE fecha_cargue like :fechaVarP \n" +
                        ")\n" +
                        "SELECT a.id_lcc,\n" +
                        "ISNULL(a.cantidad_registros,0) as cantidad_registros, \n" +
                        "ISNULL(a.estado_proceso,'Fallido') as estado_proceso, \n" +
                        "ISNULL(a.fecha_cargue,cast( :fechaVar as date)) as fecha_cargue, \n" +
                        "ISNULL(a.fecha_preciso,cast( :fechaVar as date)) as fecha_preciso,\n" +
                        "ISNULL(a.novedad,'') as novedad,\n" +
                        "ISNULL(a.tipo_proceso,'') as tipo_proceso,\n" +
                        "ISNULL(a.usuario,'Sin Ejecutar') as usuario, \n" +
                        "b.id_rc, \n" +
                        "ISNULL(a.total_intentos,0) as total_intentos \n" +
                        "FROM preciso_rutas_contables b\n" +
                        "left join (SELECT * FROM CTE WHERE row_num = 1 ) a on b.id_rc =a.id_rc \n" +
                        "ORDER BY \n" +
                        "b.id_rc,a.fecha_cargue;");
        query.setParameter("fechaVar", fecha);
        query.setParameter("fechaVarP", fecha+"%");
        return query.getResultList();
    }

    public List<Object[]> findAllData(AccountingRoute data, String fecha, String cadena, String campo,List<CampoRC> camposAux) {
        String campos = camposAux.stream()
                .map(CampoRC::getNombre)
                .collect(Collectors.joining(","));

        // Construir la consulta básica
        StringBuilder queryBuilder = new StringBuilder("SELECT " + campos + ", periodo_preciso " +
                "FROM preciso_rc_" + data.getId() + " WHERE periodo_preciso = :fecha");

        // Verificar si cadena no es nula o vacía
        if (cadena != null && !cadena.isEmpty()) {
            queryBuilder.append(" AND " + campo + " LIKE :cadena");
        }

        // Crear la consulta
        Query querySelect = entityManager.createNativeQuery(queryBuilder.toString());
        querySelect.setParameter("fecha", fecha);

        // Si cadena no es nula, añadir el parámetro para LIKE
        if (cadena != null && !cadena.isEmpty()) {
            querySelect.setParameter("cadena", cadena + "%");
        }

        return querySelect.getResultList();
    }

    public boolean findAllDataValidation(AccountingRoute data, String fecha) {
        StringBuilder queryBuilder = new StringBuilder("SELECT * " +
                "FROM preciso_rc_" + data.getId() + " WHERE periodo_preciso = :fecha");

        Query querySelect = entityManager.createNativeQuery(queryBuilder.toString());
        querySelect.setParameter("fecha", fecha);

        return !querySelect.getResultList().isEmpty();
    }

    public boolean findAllDataTemporal(AccountingRoute data, String fecha) {
        StringBuilder queryBuilder = new StringBuilder("SELECT * " +
                "FROM PRECISO_TEMP_CONTABLES ");

        Query querySelect = entityManager.createNativeQuery(queryBuilder.toString());

        return querySelect.getResultList().isEmpty();
    }


    public String encontrarUltimaFechaSubida(AccountingRoute data) {
        Query querySelect = entityManager.createNativeQuery(
                "SELECT MAX(periodo_preciso) AS ultimo_periodo_preciso FROM preciso_rc_" + data.getId()
        );
        Object result = querySelect.getSingleResult();
        return result != null ? result.toString() : null;
    }

    public String ensureTrailingSlash(String path) {
        if (!path.endsWith("\\")) {
            path += "\\";
        }
        return path;
    }

    public void createTableTemporal(AccountingRoute data) {
        String nombreTabla = "PRECISO_TEMP_CONTABLES";
        /*Query queryDrop = entityManager.createNativeQuery(
                "IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '" + nombreTabla + "' AND TABLE_SCHEMA = 'dbo') " +
                        "BEGIN DROP TABLE " + nombreTabla + " END;");
        queryDrop.executeUpdate();*/

        String queryDrop = "IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '" + nombreTabla + "' AND TABLE_SCHEMA = 'dbo') " +
                        "BEGIN DROP TABLE " + nombreTabla + " END;";
        System.out.println("QUERY -> "+queryDrop);
        jdbcTemplate.execute(queryDrop);

        StringBuilder createTableQuery = new StringBuilder("CREATE TABLE ");
        createTableQuery.append(nombreTabla).append(" (");

        List<String> primaryKeys = new ArrayList<>(); // Almacenar nombres de columnas que son claves primarias

        List<CampoRC> lista =getCamposRC(data);

        for (int i = 0; i < lista.size(); i++) {
            CampoRC column = lista.get(i);

            // Verificar si la columna es clave primaria y asignar tamaño limitado
            //if (column.getTipo().equalsIgnoreCase("VARCHAR") || column.getTipo().equalsIgnoreCase("DATE")){
                if (column.isPrimario()) {
                    createTableQuery.append(column.getNombre()).append(" VARCHAR(8000)");
                    primaryKeys.add(column.getNombre());
                } else {
                    createTableQuery.append(column.getNombre()).append(" VARCHAR(MAX)");
                }
            /*}
            else{
                createTableQuery.append(column.getNombre())
                        .append(" ")
                        .append(column.getTipo());
            }*/

            if (i < lista.size() - 1) {
                createTableQuery.append(", ");
            }
        }

        // Si existen claves primarias, agregarlas a la consulta
        /*if (!primaryKeys.isEmpty()) {
            createTableQuery.append(", PRIMARY KEY (");
            createTableQuery.append(String.join(", ", primaryKeys)); // Agregar las columnas de clave primaria
            createTableQuery.append(")");
        }*/

        createTableQuery.append(");");

        try {
            //entityManager.createNativeQuery(createTableQuery.toString()).executeUpdate();
            System.out.println("QUERY -> "+createTableQuery.toString());
            jdbcTemplate.execute(createTableQuery.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Locale convertRegion(String idioma)
    {
        Locale locale;
        switch (idioma) {
            case "InglésUSA":
                locale = Locale.ENGLISH;
                break;
            default:
                locale = new Locale("es", "ES");
        }
        return locale;
    }

    public String todayDateConvert(String formato,String fecha,String idioma,AccountingRoute data) {
        LocalDate fechaHoy = LocalDate.now();
        LocalDate today = fechaHoy;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formato,convertRegion(idioma));
        if(fecha.isEmpty()) {
            return today.format(formatter).replace(".","");
        }
        else
        {
            LocalDate fecha2 = LocalDate.parse(fecha);
            LocalDate fechaCast = fecha2;
            return fechaCast.format(formatter).replace(".","");
        }
    }

    public void importXlsx(AccountingRoute data, String ruta,String fecha, String fuente) throws PersistenceException, IOException {
        String fichero=ensureTrailingSlash(data.getRuta()) + data.getNombreArchivo() + data.getComplementoArchivo() +"."+ data.getTipoArchivo();
        if(data.isSiglasFechas()){
            fichero=ensureTrailingSlash(data.getRuta()) + data.getNombreArchivo() + todayDateConvert(data.getFormatoFecha(),fecha,data.getIdiomaFecha(),data) + data.getComplementoArchivo() +"."+ data.getTipoArchivo();
        }
        List<CampoRC> lista =getCamposRC(data);

        if(fuente !=null)
            fichero=fuente;
        if (fichero != null && !fichero.isEmpty()) {
            StringBuilder sqlQueryBuilder = new StringBuilder("INSERT INTO PRECISO_TEMP_CONTABLES (");
            List<CampoRC> campos = lista;
            // Construir la parte de columnas dinámicamente
            for (int i = 0; i < campos.size(); i++) {
                sqlQueryBuilder.append(campos.get(i).getNombre());
                if (i < campos.size() - 1) {
                    sqlQueryBuilder.append(", ");
                }
            }
            sqlQueryBuilder.append(") VALUES (");
            // Construir los valores dinámicamente con placeholders
            for (int i = 0; i < campos.size(); i++) {
                sqlQueryBuilder.append("?");
                if (i < campos.size() - 1) {
                    sqlQueryBuilder.append(", ");
                }
            }
            sqlQueryBuilder.append(")");

            String sqlQuery = sqlQueryBuilder.toString();
            Iterator<Row> rows;

            try (FileInputStream fis = new FileInputStream(fichero)) {
                Workbook workbook = WorkbookFactory.create(fis);
                Sheet sheet = workbook.getSheetAt(0);
                rows = sheet.iterator();

                int firstRow = 1;
                while (rows.hasNext()) {
                    Row row = rows.next();
                    if (firstRow > 1) {
                        // Usar JdbcTemplate para ejecutar la consulta con lambda
                        jdbcTemplate.update(sqlQuery, ps -> {
                            DataFormatter formatter = new DataFormatter();
                            for (int i = 0; i < campos.size(); i++) {
                                Cell cell = row.getCell(i);
                                Object value = null;

                                // Manejar tipo Float
                                if (campos.get(i).getTipo().equalsIgnoreCase("Float")) {
                                    if (cell != null) {
                                        // Obtener el valor numérico
                                        String numericValue = "";
                                        if(!campos.get(i).getSeparador().equalsIgnoreCase("."))
                                            numericValue = formatter.formatCellValue(cell).replace(".","").replace(",",".");
                                        else
                                            numericValue = formatter.formatCellValue(cell).replace(",","");
                                        value = Double.parseDouble(numericValue);
                                    } else {
                                        value = null;
                                    }
                                }
                                // Manejar tipo Date o Datetime
                                else if (campos.get(i).getTipo().equalsIgnoreCase("Date") || campos.get(i).getTipo().equalsIgnoreCase("Datetime")) {
                                    String fechaLeida = cell != null ? formatter.formatCellValue(cell) : null;
                                    if (fechaLeida != null && !fechaLeida.matches("\\d{2}/\\d{2}/\\d{4}")) {
                                        // Por ejemplo, convierte de `1/18/99` a `18/01/1999` si es necesario
                                        fechaLeida = convertirFormatoExcel(fechaLeida, campos.get(i).getFormatoFecha());
                                    }
                                    value = fechaLeida != null ? formatoFecha(fechaLeida, campos.get(i).getFormatoFecha(), campos.get(i).getSeparador()) : null;
                                }
                                // Manejo de otros tipos
                                else {
                                    value = cell != null ? formatter.formatCellValue(cell) : null;
                                }

                                // Establecer el parámetro correspondiente en el PreparedStatement
                                ps.setObject(i + 1, value);
                            }
                        });
                    } else {
                        firstRow++;
                    }
                }
            }
        }
    }

    private static String convertirFormatoExcel(String fechaExcel,String formato) {
        try {
            System.out.println(fechaExcel+" "+ formato);
            // Detección de formato "M/d/YY" (de Excel)
            SimpleDateFormat sdfEntrada = new SimpleDateFormat("M/d/yy");
            Date fechaDate = sdfEntrada.parse(fechaExcel);

            // Convertir a formato `dd/MM/yyyy`
            String data = formato.replaceAll("YYYY", "yyyy")
                    .replaceAll("MM", "MM")
                    .replaceAll("DD", "dd")
                    .replaceAll("YY", "yy");
            SimpleDateFormat sdfSalida = new SimpleDateFormat(data);
            return sdfSalida.format(fechaDate);
        } catch (Exception e) {
            throw new IllegalArgumentException("No se pudo convertir la fecha de Excel: " + fechaExcel, e);
        }
    }

    public Date formatoFecha(String fecha, String formatoActual, String separador) {
        try {
            String formatoSalida1 = formatoActual.replaceAll("YYYY", "yyyy")
                    .replaceAll("MM", "MM")
                    .replaceAll("DD", "dd")
                    .replaceAll("YY", "yy");
            System.out.println(formatoSalida1);
            SimpleDateFormat formatoEntrada = new SimpleDateFormat(formatoSalida1);
            formatoEntrada.setLenient(false);

            Date fechaDate = formatoEntrada.parse(fecha);

            /*String formatoSalida = formatoActual.replaceAll("YYYY", "yyyy")
                    .replaceAll("MM", "MM")
                    .replaceAll("DD", "dd")
                    .replaceAll("YY", "yy");
            System.out.println(formatoSalida);
            SimpleDateFormat formatoSalidaSDF = new SimpleDateFormat(formatoSalida);

            return formatoSalidaSDF.format(fechaDate);*/
            return fechaDate;
        } catch (Exception e) {
            throw new IllegalArgumentException("Fecha inválida o formato incorrecto: " + fecha + " con formato " + formatoActual, e);
        }
    }

    public void bulkImport(AccountingRoute data, String ruta,String fecha, String fuente) throws PersistenceException  {
        String nombreTabla = "PRECISO_TEMP_CONTABLES";
        String extension="";
        String delimitador=data.getDelimitador();

        /*if(data.getTipoArchivo().equals("XLS") || data.getTipoArchivo().equals("XLSX"))
            delimitador=";";*/
        int cantFilas=data.getFilasOmitidas()+1;

        String complement = "FIELDTERMINATOR = '"+delimitador+"', ROWTERMINATOR = '\\n', FIRSTROW = "+cantFilas;

        if(data.getTipoArchivo().equals("XLS") || data.getTipoArchivo().equals("XLSX") || data.getTipoArchivo().equals("CSV") || data.getTipoArchivo().equals("TXT"))
            extension="."+data.getTipoArchivo();
        if(delimitador.equalsIgnoreCase(""))
            complement="FORMATFILE = '" + ruta + "', ROWTERMINATOR = '\\r\\n', FIRSTROW = " + cantFilas;

        String fichero=ensureTrailingSlash(data.getRuta()) + data.getNombreArchivo() + todayDateConvert(data.getFormatoFecha(),fecha,data.getIdiomaFecha(),data) + data.getComplementoArchivo() + extension;
        if(fuente !=null)
            fichero=fuente;

        /*Query queryBulk = entityManager.createNativeQuery("BULK INSERT " + (nombreTabla) +
                " FROM '" + fichero +
                "' WITH ("+complement+ ")");
        queryBulk.executeUpdate();*/

        String queryBulk = "BULK INSERT " + (nombreTabla) +
                " FROM '" + fichero +
                "' WITH ("+complement+ ")";

        System.out.println("QUERY -> "+queryBulk);
        jdbcTemplate.execute(queryBulk);

        List<CampoRC> lista =getCamposRC(data);

        String update="";
        String update1="";
        for (CampoRC campo:lista) {
            if(!update.isEmpty() && campo.getTipo().equalsIgnoreCase("Float"))
                update=update+",";
            if(campo.getTipo().equalsIgnoreCase("Float") && (campo.getSeparador() == null||(campo.getSeparador() != null && (campo.getSeparador().equalsIgnoreCase("") || campo.getSeparador().equalsIgnoreCase("."))))) {
                update = update + campo.getNombre() + " = REPLACE(REPLACE(REPLACE(" + campo.getNombre() + ",' .00','0.00'),' ',''),',','')";
            }
            else if(campo.getTipo().equalsIgnoreCase("Float") && campo.getSeparador().equalsIgnoreCase(",")) {
                update = update + campo.getNombre() + " = REPLACE(REPLACE(REPLACE(REPLACE(" + campo.getNombre() + ",' ,00','0,00'),' ',''),'.',''),',','.')";
            }
            if(!update1.isEmpty() && campo.getTipo().equalsIgnoreCase("Bit"))
                update1=update1+",";
            if(campo.getTipo().equalsIgnoreCase("Bit"))
                update1 = update1 + campo.getNombre() + " = REPLACE(REPLACE(TRIM(UPPER(" + campo.getNombre() + ")),'SI','1'),'NO','0')";

        }
        if(!update.isEmpty())
        {
            String queryUpdate = "UPDATE PRECISO_TEMP_CONTABLES SET " + update;
            System.out.println("QUERY -> "+queryUpdate);
            jdbcTemplate.execute(queryUpdate);
        }
        if(!update1.isEmpty())
        {
            String queryUpdate1 = "UPDATE PRECISO_TEMP_CONTABLES SET " + update1;
            System.out.println("QUERY -> "+queryUpdate1);
            jdbcTemplate.execute(queryUpdate1);
        }
    }

    public void conditionData(AccountingRoute data){
        String nombreTabla = "PRECISO_TEMP_CONTABLES";
        Query querySelect = entityManager.createNativeQuery("SELECT a.valor_condicion, a.id_campo, b.nombre FROM PRECISO.dbo.preciso_condiciones_rc a \n" +
                "left join PRECISO.dbo.preciso_campos_rc b on a.id_rc = b.id_rc and a.id_campo=b.id_campo\n" +
                "where a.id_rc = ? and a.estado=1 order by a.id_campo\n");
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
                condicion=condicion+obj[2]+" = '"+obj[0].toString()+"'";
            }

            /*Query deleteSelect = entityManager.createNativeQuery("DELETE FROM "+nombreTabla+" WHERE NOT("+ condicion +"));");
            deleteSelect.executeUpdate();*/

            String deleteSelect = "DELETE FROM "+nombreTabla+" WHERE NOT("+ condicion +"));";
            System.out.println("QUERY -> "+deleteSelect);
            jdbcTemplate.execute(deleteSelect);

        }

    }

    public void validationData(AccountingRoute data) {
        String nombreTabla = "PRECISO_TEMP_CONTABLES";
        Query querySelect = entityManager.createNativeQuery(
                "SELECT b.nombre as referencia, c.nombre as validacion, a.valor_validacion, a.valor_operacion, " +
                        "CASE a.operacion WHEN 'Suma' THEN '+' WHEN 'Resta' THEN '-' WHEN 'Multiplica' THEN '*' WHEN 'Divida' THEN '/' ELSE '' END as Operacion " +
                        "FROM PRECISO.dbo.preciso_validaciones_rc a " +
                        "LEFT JOIN PRECISO.dbo.preciso_campos_rc b ON a.id_rc = b.id_rc AND a.id_campo_referencia = b.id_campo " +
                        "LEFT JOIN PRECISO.dbo.preciso_campos_rc c ON a.id_rc = c.id_rc AND a.id_campo_validacion = c.id_campo " +
                        "WHERE a.id_rc = ? AND a.estado = 1");
        querySelect.setParameter(1, data.getId());
        List<Object[]> validacionLista = querySelect.getResultList();

        if (!validacionLista.isEmpty()) {
            for (Object[] obj : validacionLista) {
                String operacion = obj[4] != null ? obj[4].toString() : "";

                // Construir la consulta dependiendo de si hay operación o no
                String queryUpdate;
                if (!operacion.isEmpty()) {
                    queryUpdate = "UPDATE " + nombreTabla + " SET " +
                            obj[0].toString() + " = CAST(CASE WHEN "+obj[0].toString()+" LIKE '%.%' THEN TRY_CAST("+obj[0].toString()+" AS DECIMAL(38, 2)) " +
                            "ELSE TRY_CAST("+obj[0].toString()+" AS DECIMAL(38, 2)) /100.0 END "+ operacion + obj[3].toString() +" AS DECIMAL(38, 2)) ";
                    if(obj[1]!=null && !obj[1].toString().equalsIgnoreCase("") && obj[2]!=null)
                        queryUpdate = queryUpdate+"WHERE " + obj[1].toString() + " = '" + obj[2].toString() + "';";
                } else {
                    queryUpdate = "UPDATE " + nombreTabla + " SET " +
                            obj[0].toString() + " = '" + obj[3].toString() + "' " ;
                            if(obj[1]!=null && !obj[1].toString().equalsIgnoreCase("") && obj[2]!=null)
                                queryUpdate=queryUpdate+"WHERE " + obj[1].toString() + " = '" + obj[2].toString() + "';";
                }

                // Ejecutar la consulta
                System.out.println("QUERY -> "+queryUpdate);
                jdbcTemplate.execute(queryUpdate);
            }
        }
    }


    public void copyData(AccountingRoute data,String fecha){
        String nombreTabla = "PRECISO_TEMP_CONTABLES";
        List<CampoRC> lista =getCamposRC(data);
        String campos = lista.stream()
                .map(CampoRC::getNombre)
                .collect(Collectors.joining(","));

        String deleteSelect = "DELETE FROM preciso_rc_" + data.getId() + " WHERE periodo_preciso = '" + fecha + "' ; \n" +
                "INSERT INTO preciso_rc_" + data.getId() + " (" + campos + ",periodo_preciso" + ") SELECT " + campos + ",CAST('" + fecha + "' AS DATE) FROM " + nombreTabla;
        System.out.println("QUERY -> " + deleteSelect);
        jdbcTemplate.execute(deleteSelect);

        Query querySelect = entityManager.createNativeQuery( "SELECT TOP 2 * FROM preciso_rc_" + data.getId() + " WHERE periodo_preciso = '" + fecha + "' ;");
        List<Object> temporal = querySelect.getResultList();

        if(data.isSobreescribir() && temporal.size()>0)
        {
            String deleteAll = "DELETE FROM preciso_rc_" + data.getId() + " WHERE periodo_preciso != '" + fecha + "' ;";
            System.out.println("QUERY -> " + deleteAll);
            jdbcTemplate.execute(deleteAll);
        }
    }

    public void generarArchivoFormato(List<CampoRC> campos, String rutaArchivoFormato) throws IOException, PersistenceException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(rutaArchivoFormato))) {

            // Escribir encabezado del archivo de formato
            writer.write("13.0\n"); // Versión del archivo de formato
            writer.write(campos.size() + 1 + "\n"); // Número de campos

            int fieldIndex = 1;
            for (CampoRC campo : campos) {
                int longitudCampo;
                // Si la longitud es "MAX", usa 8000 como tamaño predeterminado para VARCHAR
                if ("MAX".equalsIgnoreCase(campo.getLongitud())) {
                    longitudCampo = 8000; // Longitud máxima para un campo VARCHAR no MAX
                } else {
                    longitudCampo = Integer.parseInt(campo.getLongitud());
                }
                // Generar la línea
                String line = String.format(
                        "%d\tSQLCHAR\t0\t%d\t\"\"\t%d\t%s\tLatin1_General_CI_AS",
                        fieldIndex,
                        longitudCampo,
                        fieldIndex,
                        campo.getNombre()
                );

                // Añadir salto de línea
                writer.write(line + "\n");

                fieldIndex++;
            }

            // Última línea con terminador de fila
            CampoRC ultimoCampo = campos.get(campos.size() - 1);
            int longitudUltimoCampo = "MAX".equalsIgnoreCase(ultimoCampo.getLongitud()) ? 8000 : Integer.parseInt(ultimoCampo.getLongitud());
            String ultimaLinea = String.format(
                    "%d\tSQLCHAR\t0\t%d\t\"\\r\\n\"\t%d\t%s\tLatin1_General_CI_AS",
                    fieldIndex,
                    longitudUltimoCampo,
                    fieldIndex,
                    ultimoCampo.getNombre()
            );
            writer.write(ultimaLinea + "\n");

        } catch (IOException e) {
            throw new IOException("Error al generar el archivo de formato.", e);
        } catch (NumberFormatException e) {
            throw new PersistenceException("Longitud de campo inválida en la lista de campos.", e);
        }
    }

    public List<AccountingRoute> findByFilter(String value, String filter) {
        List<AccountingRoute> list=new ArrayList<AccountingRoute>();
        switch (filter) {
            case "Código":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM preciso_rutas_contables as em " +
                        "WHERE em.id_rc LIKE ?", AccountingRoute.class);
                query.setParameter(1, value );
                list= query.getResultList();
                break;
            case "Nombre":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM preciso_rutas_contables as em " +
                        "WHERE em.nombre LIKE ?", AccountingRoute.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            case "Archivo":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM preciso_rutas_contables as em " +
                        "WHERE em.nombre_archivo LIKE ?", AccountingRoute.class);
                query2.setParameter(1, value);
                list= query2.getResultList();
                break;
            case "Estado":
                Boolean valor = true;
                if ("inactivo".equalsIgnoreCase(value)) valor = false;
                Query query3 = entityManager.createNativeQuery(
                        "SELECT em.* FROM preciso_rutas_contables as em WHERE em.activo = ?", AccountingRoute.class);
                query3.setParameter(1, valor);
                list= query3.getResultList();
                break;
            case "Ruta de Acceso":
                Query query4 = entityManager.createNativeQuery("SELECT em.* FROM preciso_rutas_contables as em " +
                        "WHERE em.ruta LIKE ?", AccountingRoute.class);
                query4.setParameter(1, value);
                list= query4.getResultList();
                break;
            case "Tipo de Archivo":
                Query query5 = entityManager.createNativeQuery("SELECT em.* FROM preciso_rutas_contables as em " +
                        "WHERE em.tipo_archivo LIKE ?", AccountingRoute.class);
                query5.setParameter(1, value);
                list= query5.getResultList();
                break;
            default:
                break;
        }
        return list;
    }


    public List<Object[]> findRutasBySFC(int SFCid) {
        Query query = entityManager.createNativeQuery(
                "SELECT id_rc, nombre FROM preciso_rutas_contables WHERE id_sf = :SFCid");
        query.setParameter("SFCid", SFCid);
        return query.getResultList();
    }

    public List<Object[]> findRutasByConcil(int SFCid) {
        Query query = entityManager.createNativeQuery(
                "SELECT id_rc, nombre FROM preciso_rutas_contables WHERE id_sf = :SFCid");
        query.setParameter("SFCid", SFCid);
        return query.getResultList();
    }
    public List<CondicionRC> findCondicionesRc(int SFCid) {
        Query query = entityManager.createNativeQuery(
                "select b.* from preciso_rutas_contables a, preciso_condiciones_rc b where a.id_rc = b.id_rc and a.id_rc = :SFCid ",CondicionRC.class);
        query.setParameter("SFCid", SFCid);
        return query.getResultList();
    }

    public List<ValidationRC> findValidacionesRc(int SFCid) {
        Query query = entityManager.createNativeQuery(
                "select b.* from preciso_rutas_contables a, preciso_validaciones_rc b where a.id_rc = b.id_rc and a.id_rc = :SFCid ",ValidationRC.class);
        query.setParameter("SFCid", SFCid);
        return query.getResultList();
    }

    public List<Object[]> findByEncabezados() {
        Query query = entityManager.createNativeQuery("SELECT a.id_rc,a.nombre,a.nombre_archivo,a.ruta,a.tipo_archivo, a.estado FROM preciso_rutas_contables a order by a.id_rc");
        return query.getResultList();
    }

    public List<Object[]> findByCampos() {
        Query query = entityManager.createNativeQuery("SELECT b.id_rc,b.nombre as n1,a.id_campo,a.nombre as n2, a.primario,a.tipo,a.longitud,a.visualizacion,a.separador,a.formato_fecha,a.formula,a.operacion,a.valor_operacion FROM preciso_campos_rc a\n" +
                "LEFT JOIN  preciso_rutas_contables b ON a.id_rc =b.id_rc order by b.id_rc,a.id_campo");
        return query.getResultList();
    }

    public List<Object[]> findByCamposSelect(int id) {
        Query query = entityManager.createNativeQuery("SELECT b.id_rc,b.nombre as n1,a.id_campo,a.nombre as n2, a.primario,a.tipo,a.longitud,a.visualizacion,a.separador,a.formato_fecha,a.formula,a.operacion,a.valor_operacion FROM preciso_campos_rc a\n" +
                "LEFT JOIN  preciso_rutas_contables b ON a.id_rc =b.id_rc where b.id_rc = ? order by b.id_rc,a.id_campo");
        query.setParameter(1,id);
        return query.getResultList();
    }

    public List<CampoRC> findByCamposSelectById(int id,String nombre) {
        Query query = entityManager.createNativeQuery("SELECT a.* FROM preciso_campos_rc a\n" +
                "LEFT JOIN  preciso_rutas_contables b ON a.id_rc =b.id_rc where b.id_rc = ? and a.nombre = ? ",CampoRC.class);
        query.setParameter(1,id);
        query.setParameter(2,nombre);
        return query.getResultList();
    }

    public List<Object[]> findByCondiciones() {
        Query query = entityManager.createNativeQuery("SELECT b.id_rc,b.nombre as n1,c.id_campo,c.nombre as n2,a.valor_condicion FROM preciso_condiciones_rc a\n" +
                "LEFT JOIN  preciso_rutas_contables b ON a.id_rc =b.id_rc\n" +
                "LEFT JOIN  preciso_campos_rc c ON a.id_rc =c.id_rc and a.id_campo = c.id_campo order by b.id_rc,a.id_campo");
        return query.getResultList();
    }

    public List<Object[]> findByValidaciones() {
        Query query = entityManager.createNativeQuery("SELECT b.id_rc,b.nombre as n1,a.id_campo_referencia,c.nombre as n2,a.id_campo_validacion,d.nombre as n3,a.valor_validacion,UPPER(a.operacion) AS OPE,a.valor_operacion FROM preciso_validaciones_rc a\n" +
                "LEFT JOIN  preciso_rutas_contables b ON a.id_rc =b.id_rc\n" +
                "LEFT JOIN  preciso_campos_rc c ON a.id_rc =c.id_rc and a.id_campo_referencia = c.id_campo\n" +
                "LEFT JOIN  preciso_campos_rc d ON a.id_rc =d.id_rc and a.id_campo_validacion = d.id_campo order by b.id_rc,a.id_campo_referencia");
        return query.getResultList();
    }

    public void updateLoads(AccountingRoute ac,String fecha){
        masterInventService.updateLoads(ac,fecha);
    }


    @Scheduled(cron = "0 0/30 * * * ?")
    public void processJob()  {

        List<AccountingRoute> list = findByJob();
        for (AccountingRoute ac :list) {
            LocalDateTime fechaHoy = LocalDateTime.now();
            fechaHoy = fechaHoy.minusDays(1);
            if(ac.getDiasRetardo()>1)
                fechaHoy = fechaHoy.minusDays(ac.getDiasRetardo()-1);
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String fecha = fechaHoy.format(formato);
            try {
                List<CondicionRC> crc = findCondicionesRc(ac.getId());
                List<ValidationRC> vrc = findValidacionesRc(ac.getId());
                createTableTemporal(ac);
                generarArchivoFormato(ac.getCampos(), rutaArchivoFormato);
                if (ac.getTipoArchivo().equalsIgnoreCase("XLS") || ac.getTipoArchivo().equalsIgnoreCase("XLSX"))
                    importXlsx(ac, rutaArchivoFormato, fecha, null);
                else
                    bulkImport(ac, rutaArchivoFormato, fecha, null);
                if (!crc.isEmpty())
                    conditionData(ac);
                if (vrc.size() != 0)
                    validationData(ac);
                copyData(ac, fecha);
                updateLoads(ac,fecha);
                if(findAllDataValidation(ac,fecha)) {
                    jobAutoService.loadLogCargue(null, ac, fecha, "Automático", "Exitoso", "");
                }
                else if(findAllDataTemporal(ac,fecha)) {
                    jobAutoService.loadLogCargue(null, ac, fecha, "Automático", "Fallido", "La ruta "+ac.getRuta()+" es inaccesible. (El sistema no puede encontrar el archivo especificado)");
                }
                else {
                    jobAutoService.loadLogCargue(null, ac, fecha, "Automático", "Fallido", "Valide el formato de los campos de tipo Float y Bigint");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                Throwable rootCause = e;
                while (rootCause.getCause() != null) {
                    rootCause = rootCause.getCause();
                }
                try {
                    jobAutoService.loadLogCargue(null, ac, fecha, "Automático", "Fallido", rootCause.getMessage());
                } catch (Exception logException) {
                    logException.printStackTrace(); // Log adicional si guardar el log también falla
                }
            }
        }
    }

    @Scheduled(cron = "0 45 23 * * ?")
    public void jobEjectutarDiariosFaltantes() {
        LocalDateTime fechaHoyTemp = LocalDateTime.now();
        fechaHoyTemp = fechaHoyTemp.minusDays(1);
        DateTimeFormatter formatoTemp = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String fechaTemp = fechaHoyTemp.format(formatoTemp);

        List<AccountingRoute> list = findByJobNotLoad(fechaTemp);
        for (AccountingRoute ac :list) {
            LocalDateTime fechaHoy = LocalDateTime.now();
            fechaHoy = fechaHoy.minusDays(1);
            if(ac.getDiasRetardo()>1)
                fechaHoy = fechaHoy.minusDays(ac.getDiasRetardo()-1);
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String fecha = fechaHoy.format(formato);
            try {
                List<CondicionRC> crc = findCondicionesRc(ac.getId());
                List<ValidationRC> vrc = findValidacionesRc(ac.getId());
                createTableTemporal(ac);
                generarArchivoFormato(ac.getCampos(), rutaArchivoFormato);
                if (ac.getTipoArchivo().equalsIgnoreCase("XLS") || ac.getTipoArchivo().equalsIgnoreCase("XLSX"))
                    importXlsx(ac, rutaArchivoFormato, fecha, null);
                else
                    bulkImport(ac, rutaArchivoFormato, fecha, null);
                if (!crc.isEmpty())
                    conditionData(ac);
                if (vrc.size() != 0)
                    validationData(ac);
                copyData(ac, fecha);
                updateLoads(ac,fecha);
                if(findAllDataValidation(ac,fecha)) {
                    jobAutoService.loadLogCargue(null, ac, fecha, "Automático", "Exitoso", "");
                }
                else if(findAllDataTemporal(ac,fecha)) {
                    jobAutoService.loadLogCargue(null, ac, fecha, "Automático", "Fallido", "La ruta "+ac.getRuta()+" es inaccesible. (El sistema no puede encontrar el archivo especificado)");
                }
                else {
                    jobAutoService.loadLogCargue(null, ac, fecha, "Automático", "Fallido", "Valide el formato de los campos de tipo Float y Bigint");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                Throwable rootCause = e;
                while (rootCause.getCause() != null) {
                    rootCause = rootCause.getCause();
                }
                try {
                    jobAutoService.loadLogCargue(null, ac, fecha, "Automático", "Fallido", rootCause.getMessage());
                } catch (Exception logException) {
                    logException.printStackTrace(); // Log adicional si guardar el log también falla
                }
            }
        }
    }

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Rutas Contables");
        insert.setFecha(today);
        insert.setInput("Campos Rutas");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public String generarCadenaDeCuentas(List<AccountConcil> cuentas) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < cuentas.size(); i++) {
            sb.append(cuentas.get(i).getValor()); // Asumiendo que getValor() devuelve un String o número
            if (i < cuentas.size() - 1) {
                sb.append(", "); // Agrega una coma solo si no es el último elemento
            }
        }

        return sb.toString();
    }

    public List<Object[]> resumeAR(Conciliation concil, String fechapp, EventType evento) {
        String campoCentro = concil.getCentro();
        String campoCuenta = concil.getCuenta();
        String campoDivisa = concil.getDivisa();
        String campoSaldo = concil.getSaldo();

        String nombreTabla = "preciso_rc_" + concil.getRutaContable().getId();

        List<AccountConcil> cuentas = concil.getArregloCuentas();
        String cuentasConcil = generarCadenaDeCuentas(cuentas); // Genera cadena "101, 102, 103"
        System.out.println(cuentasConcil);

        String fecha = "2024-12-02";

        // Construir el query
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT \n")
                .append("periodo_preciso AS FECHA_CONCILIACION, \n")
                .append(campoCentro).append(" AS CENTRO_CONTABLE, \n")
                .append("CAST(").append(campoCuenta).append(" AS BIGINT) AS CUENTA_CONTABLE, \n")
                .append(campoDivisa).append(" AS DIVISA_CUENTA, \n")
                .append("SUM(TRY_CAST(").append(campoSaldo).append(" AS DECIMAL(18, 2))) AS TOTAL_VALOR_CUENTA \n")
                .append("FROM ").append(nombreTabla).append("\n")
                .append("WHERE periodo_preciso = :fecha AND ")
                .append(campoCuenta).append(" IN (").append(cuentasConcil).append(") \n")
                .append("GROUP BY periodo_preciso, ")
                .append(campoCentro).append(", ")
                .append(campoCuenta).append(", ")
                .append(campoDivisa);

        // Crear la consulta
        Query querySelect = entityManager.createNativeQuery(queryBuilder.toString());
        querySelect.setParameter("fecha", fecha);

        return querySelect.getResultList();
    }
    public void leerArchivosMasivo(String[] ids, String fecha)  {
        for (String id :ids)
        {
            AccountingRoute ac = findById(Integer.parseInt(id));
            try {
                List<CondicionRC> crc = findCondicionesRc(ac.getId());
                List<ValidationRC> vrc = findValidacionesRc(ac.getId());
                createTableTemporal(ac);
                generarArchivoFormato(ac.getCampos(), rutaArchivoFormato);
                if (ac.getTipoArchivo().equalsIgnoreCase("XLS") || ac.getTipoArchivo().equalsIgnoreCase("XLSX"))
                    importXlsx(ac, rutaArchivoFormato, fecha, null);
                else
                    bulkImport(ac, rutaArchivoFormato, fecha, null);
                if (!crc.isEmpty())
                    conditionData(ac);
                if (vrc.size() != 0)
                    validationData(ac);
                copyData(ac, fecha);
                updateLoads(ac,fecha);
                if(findAllDataValidation(ac,fecha)) {
                    jobAutoService.loadLogCargue(null, ac, fecha, "Cargue Masivo", "Exitoso", "");
                }
                else if(findAllDataTemporal(ac,fecha)) {
                    jobAutoService.loadLogCargue(null, ac, fecha, "Cargue Masivo", "Fallido", "La ruta "+ac.getRuta()+" es inaccesible. (El sistema no puede encontrar el archivo especificado)");
                }
                else {
                    jobAutoService.loadLogCargue(null, ac, fecha, "Cargue Masivo", "Fallido", "Valide el formato de los campos de tipo Float y Bigint");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                Throwable rootCause = e;
                while (rootCause.getCause() != null) {
                    rootCause = rootCause.getCause();
                }
                try {
                    jobAutoService.loadLogCargue(null, ac, fecha, "Cargue Masivo", "Fallido", rootCause.getMessage());
                } catch (Exception logException) {
                    logException.printStackTrace(); // Log adicional si guardar el log también falla
                }
            }
        }
    }

    public List<Object[]> findAllData(AccountingRoute data, String fecha, String cadena, String campo) {
        String campos = data.getCampos().stream()
                .map(CampoRC::getNombre)
                //.limit(10)
                .collect(Collectors.joining(","));

        // Construir la consulta básica
        StringBuilder queryBuilder = new StringBuilder("SELECT TOP 10 " + campos + ", periodo_preciso " +
                "FROM preciso_rc_" + data.getId() + " WHERE periodo_preciso = :fecha");

        // Verificar si cadena no es nula o vacía
        if (cadena != null && !cadena.isEmpty()) {
            queryBuilder.append(" AND " + campo + " LIKE '"+cadena+"'");
        }

        // Crear la consulta
        Query querySelect = entityManager.createNativeQuery(queryBuilder.toString());
        querySelect.setParameter("fecha", fecha);

        // Si cadena no es nula, añadir el parámetro para LIKE
        //if (cadena != null && !cadena.isEmpty()) {
          //  querySelect.setParameter("cadena", cadena );
        //}

        return querySelect.getResultList();
    }

    public ArrayList<String[]> saveFileBD(InputStream file, User user,String id) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<>();
        if (file!=null)
        {
            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheet("Campos");
            Iterator<Row> rows = sheet.iterator();
            list=validarPlantilla(rows,id);
            if(list.get(0)[2].equals("SUCCESS"))
                loadAudit(user,"Cargue exitoso plantilla campos");
            else
                loadAudit(user,"Cargue Fallido plantilla campos");
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows, String id) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<CampoRC> toInsert = new ArrayList<>();
        String stateFinal = "SUCCESS";
        XSSFRow row;
        List<String> valoresTipo = new ArrayList<>(Arrays.asList("Varchar","Integer","Bigint","Float","Date","Time","DateTime","Bit"));
        List<String> valoresFormato = new ArrayList<>(Arrays.asList("YYYYMMDD","YYMMDD","DDMMMMYYYY","DDMMYYYY","DDMMYY","MMDDYY"));
        List<String> valoresOperacion = new ArrayList<>(Arrays.asList("Suma","Resta","Multiplica","Divida"));
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    int consecutivo=0;
                    DataFormatter formatter = new DataFormatter();
                    String cellCodContable = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellNomContable = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellCodCampo = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellNomCampo = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellPrimario = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellTipo = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellLongitud = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellVisualizacion = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellSeparador = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellFormato  = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellFormula  = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellOperacion = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellValorOperacion = formatter.formatCellValue(row.getCell(consecutivo++)).trim();

                    if (cellCodContable.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "El campo Código Contable no puede estar vacio.";
                        lista.add(log);
                    } else if (!cellCodContable.equalsIgnoreCase(id)) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "El campo Código Contable no corresponde a la ruta contable donde esta intentando realizar el cargue.";
                        lista.add(log);
                    }
                    if (cellNomCampo.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(3);
                        log[2] = "El campo Nombre Campo no puede estar vacio.";
                        lista.add(log);
                    }
                    if (!cellPrimario.equalsIgnoreCase("Si") && !cellPrimario.equalsIgnoreCase("No") ) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(4);
                        log[2] = "El campo Primario debe contener un Si o No.";
                        lista.add(log);
                    }
                    if (!valoresTipo.contains(cellTipo)) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(5);
                        log[2] = "El campo Tipo no se encuentra dentro del listado permitido";
                        lista.add(log);
                    }
                    if (cellLongitud.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(6);
                        log[2] = "El campo Longitud no puede estar vacio.";
                        lista.add(log);
                    }
                    else {
                        if(!cellLongitud.equalsIgnoreCase("MAX"))
                        {
                            try{
                                Integer.parseInt(cellLongitud);
                            }
                            catch (Exception e){
                                String[] log = new String[3];
                                log[0] = String.valueOf(row.getRowNum() + 1);
                                log[1] = CellReference.convertNumToColString(6);
                                log[2] = "El campo Longitud debe ser númerico o contenter la palabra MAX.";
                                lista.add(log);
                            }
                        }
                    }
                    if (!cellVisualizacion.equalsIgnoreCase("Si") && !cellVisualizacion.equalsIgnoreCase("No") ) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(7);
                        log[2] = "El campo Primario debe contener un Si o No.";
                        lista.add(log);
                    }
                    if (cellTipo.equalsIgnoreCase("Date") || cellTipo.equalsIgnoreCase("DateTime")) {
                        if (cellSeparador.length() == 0) {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(8);
                            log[2] = "El campo Separador no puede estar vacio.";
                            lista.add(log);
                        }
                        if (!valoresFormato.contains(cellFormato)) {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(9);
                            log[2] = "El campo Formato no se encuentra dentro del listado permitido";
                            lista.add(log);
                        }
                    }
                    if (!cellFormula.equalsIgnoreCase("Si") && !cellFormula.equalsIgnoreCase("No") ) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(10);
                        log[2] = "El campo Formula debe contener un Si o No.";
                        lista.add(log);
                    }

                    if (cellFormula.equalsIgnoreCase("Si") && !valoresOperacion.contains(cellOperacion)) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(11);
                        log[2] = "El campo Operacion no se encuentra dentro del listado permitido.";
                        lista.add(log);
                    }
                    if (cellFormula.equalsIgnoreCase("Si")&& cellValorOperacion.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(12);
                        log[2] = "El campo Valor Operacion no puede estar vacio.";
                        lista.add(log);
                    }


                    if (lista.isEmpty() && findByCamposSelectById(Integer.parseInt(id),cellNomCampo).isEmpty()) {
                        CampoRC data = new CampoRC();
                        data.setRutaContable(findById(Integer.parseInt(id)));
                        data.setVisualizacion(cellVisualizacion.equalsIgnoreCase("Si"));
                        data.setTipo(cellTipo);
                        data.setSeparador(cellSeparador);
                        data.setPrimario(cellPrimario.equalsIgnoreCase("Si"));
                        data.setNombre(cellNomCampo);
                        data.setLongitud(cellLongitud);
                        data.setIdioma("EspañolColombia");
                        data.setFormatoFecha(cellFormato);
                        data.setFormula(cellFormula.equalsIgnoreCase("Si"));
                        data.setOperacion(cellOperacion);
                        data.setValorOperacion(cellValorOperacion);
                        data.setEstado(true);
                        toInsert.add(data);
                    }
                }
            }
        }

        if (lista.size() != 0)
            stateFinal = "FAILED";
        String[] log2 = new String[3];
        log2[0] = String.valueOf((toInsert.size() * 11) - lista.size());
        log2[1] = String.valueOf(lista.size());
        log2[2] = stateFinal;
        lista.add(log2);
        String[] temp = lista.get(0);
        if (temp[2].equals("SUCCESS")) {
            campoRCRepository.saveAll(toInsert);
            campoRCService.recreateTable(findById(Integer.parseInt(id)));
        }
        toInsert.clear();
        return lista;
    }

}
