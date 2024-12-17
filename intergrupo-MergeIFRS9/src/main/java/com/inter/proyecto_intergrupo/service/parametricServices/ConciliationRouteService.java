package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ConciliationRouteRepository;
import com.inter.proyecto_intergrupo.repository.parametric.LogInventoryLoadRepository;
import org.apache.poi.ss.usermodel.*;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.inter.proyecto_intergrupo.controller.parametric.AccountingLoadController.rutaArchivoFormato;

@Service
@EnableTransactionManagement
public class ConciliationRouteService {

    @Autowired
    private final ConciliationRouteRepository conciliationRouteRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JobAutoService jobAutoService;

    @Autowired
    public ConciliationRouteService(ConciliationRouteRepository conciliationRouteRepository) {
        this.conciliationRouteRepository = conciliationRouteRepository;
    }

    @Autowired
    private LogInventoryLoadRepository logInventoryLoadRepository;


    public List<ConciliationRoute> findAllActive() {
        return conciliationRouteRepository.findByEstado(true);
    }

    public ConciliationRoute findById(int id){
        return conciliationRouteRepository.findAllById(id);
    }

    public List<ConciliationRoute> getRoutesByConciliation(int concil) {
        return conciliationRouteRepository.findByActivoAndConciliacionId(true, concil);
    }

    public ConciliationRoute findByName(String nombre){
        return conciliationRouteRepository.findAllByDetalle(nombre);
    }

    public ConciliationRoute modificar(ConciliationRoute croute){
        conciliationRouteRepository.save(croute);
       return croute;
    }

    public String encontrarUltimaFechaSubida(ConciliationRoute data) {
        Query querySelect = entityManager.createNativeQuery(
                "SELECT MAX(periodo_preciso) AS ultimo_periodo_preciso FROM preciso_rconcil_" + data.getId()
        );
        Object result = querySelect.getSingleResult();
        return result != null ? result.toString() : null;
    }

    public List<CampoRConcil> getCamposRcon(ConciliationRoute data) {
        Query querySelect = entityManager.createNativeQuery(
                "select b.* from preciso_rutas_conciliaciones a, preciso_campos_rconcil b where a.id = b.id_rconcil and a.id = " + data.getId(),CampoRConcil.class);
        return querySelect.getResultList();
    }

    public List<Object[]> findAllData(ConciliationRoute data, String fecha, String cadena, String campo) {
        String campos = data.getCampos().stream()
                .map(CampoRConcil::getNombre)
                .collect(Collectors.joining(","));

        // Construir la consulta básica
        StringBuilder queryBuilder = new StringBuilder("SELECT " + campos + ", periodo_preciso " +
                "FROM preciso_rconcil_" + data.getId() + " WHERE periodo_preciso = :fecha");

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

    public boolean findAllDataValidationA(ConciliationRoute data, String fecha) {
        StringBuilder queryBuilder = new StringBuilder("SELECT * " +
                "FROM preciso_rconcil_" + data.getId() + " WHERE periodo_preciso = :fecha");

        Query querySelect = entityManager.createNativeQuery(queryBuilder.toString());
        querySelect.setParameter("fecha", fecha);

        return !querySelect.getResultList().isEmpty();
    }

    public void createTableTemporal(ConciliationRoute data) {
        String nombreTabla = "PRECISO_TEMP_INVENTARIOS";
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

        List<CampoRConcil> listCampos = getCamposRcon(data);

        for (int i = 0; i < listCampos.size(); i++) {
            CampoRConcil column = listCampos.get(i);

            // Verificar si la columna es clave primaria y asignar tamaño limitado
            //if (column.getTipo().equalsIgnoreCase("VARCHAR") || column.getTipo().equalsIgnoreCase("DATE")){
                if (column.isPrimario()) {
                    createTableQuery.append(column.getNombre()).append(" VARCHAR(8000)");
                    primaryKeys.add(column.getNombre());
                } else {
                    createTableQuery.append(column.getNombre()).append(" VARCHAR(MAX)");
                }
           /* }
            else{
                if (column.isPrimario()) {
                    primaryKeys.add(column.getNombre());
                }
                createTableQuery.append(column.getNombre())
                        .append(" ")
                        .append(column.getTipo());
            }*/

            if (i < listCampos.size() - 1) {
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


    public void generarArchivoFormato(List<CampoRConcil> campos, String rutaArchivoFormato) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(rutaArchivoFormato))) {

            // Escribir encabezado del archivo de formato
            writer.write("13.0\n"); // Versión del archivo de formato
            writer.write(campos.size()+1 + "\n"); // Número de campos

            int fieldIndex = 1;
            for (CampoRConcil campo : campos) {
                // Cada línea sigue la estructura:
                // <FieldID> SQLCHAR 0 <LongitudCampo> "" <IndexCampo> <NombreCampo> Latin1_General_CI_AS
                String value="2147483646";
                if(campo.getLongitud()!=null && !campo.getLongitud().equalsIgnoreCase("MAX"))
                {
                    value=campo.getLongitud();
                }

                String line = String.format(
                        "%d\tSQLCHAR\t0\t%d\t\"\"\t%d\t%s\tLatin1_General_CI_AS",
                        fieldIndex,
                        Integer.parseInt(value),
                        fieldIndex,
                        campo.getNombre()
                );

                // Añadir salto de línea
                writer.write(line + "\n");

                fieldIndex++;
            }

            String value="2147483646";
            if(campos.get(campos.size() - 1).getLongitud()!=null && !campos.get(campos.size() - 1).getLongitud().equalsIgnoreCase("MAX"))
            {
                value=campos.get(campos.size() - 1).getLongitud();
            }

            // Si necesitas manejar el último campo con terminador de línea (por ejemplo, "\r\n")
            String ultimaLinea = String.format(
                    "%d\tSQLCHAR\t0\t%d\t\"\\r\\n\"\t%d\t%s\tLatin1_General_CI_AS",
                    fieldIndex,
                    Integer.parseInt(value),
                    fieldIndex,
                    campos.get(campos.size() - 1).getNombre()
            );
            writer.write(ultimaLinea + "\n");

        } catch (IOException e) {
            throw new IOException("Error al generar el archivo de formato.", e);
        }
    }

    public String ensureTrailingSlash(String path) {
        if (!path.endsWith("\\")) {
            path += "\\";
        }
        return path;
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

    public String todayDateConvert(String formato,String fecha,String idioma) {
        LocalDate fechaHoy = LocalDate.now();
        LocalDate today = fechaHoy.minusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formato,convertRegion(idioma));
        if(fecha.isEmpty()) {
            return today.format(formatter).replace(".","");
        }
        else
        {
            LocalDate fecha2 = LocalDate.parse(fecha);
            LocalDate fechaCast = fecha2.minusDays(1);
            return fechaCast.format(formatter).replace(".","");
        }
    }

    public List<ConciliationRoute> findByJob() {
        LocalTime now = LocalTime.now(); // Hora actual
        LocalTime thirtyMinutesBefore = now.minusMinutes(29); // Hora hace 30 minutos

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        String timeBefore = thirtyMinutesBefore.format(formatter);
        String timeNow = now.format(formatter);

        String sql = "SELECT * FROM preciso_rutas_conciliaciones WHERE activo = 1 AND hora_cargue BETWEEN '"+timeBefore+"' AND '"+timeNow+"'";

        Query querySelect = entityManager.createNativeQuery(sql, ConciliationRoute.class);
        return querySelect.getResultList();
    }

    public void importXlsx(ConciliationRoute data, String ruta,String fecha, String fuente) throws PersistenceException, IOException {
        String fichero=ensureTrailingSlash(data.getRuta()) + data.getNombreArchivo() + todayDateConvert(data.getFormatoFecha(),fecha,data.getIdiomaFecha()) +"."+ data.getTipoArchivo();
        if(fuente !=null)
            fichero=fuente;
        if (fichero != null && !fichero.isEmpty()) {
            StringBuilder sqlQueryBuilder = new StringBuilder("INSERT INTO PRECISO_TEMP_INVENTARIOS (");
            List<CampoRConcil> campos = getCamposRcon(data);
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
                    if (firstRow > data.getFilasOmitidas()) {
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
                                        //Double numericValue = cell.getNumericCellValue();
                                        String numericValue = "";
                                        if(!campos.get(i).getSeparador().equalsIgnoreCase("."))
                                            numericValue = formatter.formatCellValue(cell).replace(".","").replace(",",".");
                                        else
                                            numericValue = formatter.formatCellValue(cell).replace(",","");


                                        System.out.println(numericValue);
                                        // Verificar si tiene decimales adicionales y formatear dinámicamente
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

    public void bulkImport(ConciliationRoute data, String ruta,String fecha, String fuente) throws PersistenceException  {
        String nombreTabla = "PRECISO_TEMP_INVENTARIOS";
        String extension="";
        String delimitador=data.getDelimitador();

        if(data.getTipoArchivo().equals("XLS") || data.getTipoArchivo().equals("XLSX"))
            delimitador=";";

        String complement = "FIELDTERMINATOR = '"+delimitador+"', ROWTERMINATOR = '\\n', FIRSTROW = "+data.getFilasOmitidas()+1;

        if(data.getTipoArchivo().equals("XLS") || data.getTipoArchivo().equals("XLSX") || data.getTipoArchivo().equals("CSV") || data.getTipoArchivo().equals("TXT"))
            extension="."+data.getTipoArchivo();
        if(delimitador.equalsIgnoreCase(""))
            complement="FORMATFILE = '" + ruta + "', ROWTERMINATOR = '\\r\\n', FIRSTROW = " + data.getFilasOmitidas();

        String fechaConvert="";
        if(data.isSiglasFechas()==true)
            fechaConvert=todayDateConvert(data.getFormatoFecha(),fecha,data.getIdiomaFecha());
        String fichero=ensureTrailingSlash(data.getRuta()) + data.getNombreArchivo() + fechaConvert + extension;
        if(fuente !=null)
            fichero=fuente;

        String queryBulk = "BULK INSERT " + (nombreTabla) +
                " FROM '" + fichero +
                "' WITH ("+complement+ ")";
        System.out.println("QUERY -> "+queryBulk);
        jdbcTemplate.execute(queryBulk);

        String update="";
        String update1="";
        for (CampoRConcil campo:data.getCampos()) {
            if(!update.isEmpty() && campo.getTipo().equalsIgnoreCase("Float"))
                update=update+",";
            if(campo.getTipo().equalsIgnoreCase("Float") || (campo.getSeparador() == null && campo.getSeparador().equalsIgnoreCase("."))) {
                update = update + campo.getNombre() + " = REPLACE(TRIM(REPLACE(" + campo.getNombre() + ",' .00','0.00')),',','')";
            }
            else if(campo.getTipo().equalsIgnoreCase("Float") || campo.getSeparador().equalsIgnoreCase(",")) {
                update = update + campo.getNombre() + " = REPLACE(REPLACE(TRIM(REPLACE(" + campo.getNombre() + ",' ,00','0,00')),'.',''),',','.')";
            }
            if(!update1.isEmpty() && campo.getTipo().equalsIgnoreCase("Bit"))
                update1=update1+",";
            if(campo.getTipo().equalsIgnoreCase("Bit"))
                update1 = update1 + campo.getNombre() + " = REPLACE(REPLACE(TRIM(UPPER(" + campo.getNombre() + ")),'SI','1'),'NO','0')";
        }
        if(!update.isEmpty())
        {
            String queryUpdate = "UPDATE PRECISO_TEMP_INVENTARIOS SET " + update;
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

    public void validationData(ConciliationRoute data) {
        String nombreTabla = "PRECISO_TEMP_INVENTARIOS";
        Query querySelect = entityManager.createNativeQuery(
                "SELECT b.nombre as referencia, c.nombre as validacion, a.valor_validacion, a.valor_operacion, " +
                        "CASE a.operacion WHEN 'Suma' THEN '+' WHEN 'Resta' THEN '-' WHEN 'Multiplica' THEN '*' WHEN 'Divida' THEN '/' ELSE '' END as Operacion " +
                        "FROM PRECISO.dbo.preciso_validaciones_rconcil a " +
                        "INNER JOIN PRECISO.dbo.preciso_campos_rconcil b ON a.id_rc = b.id_rconcil AND a.id_campo_referencia = b.id_campo " +
                        "INNER JOIN PRECISO.dbo.preciso_campos_rconcil c ON a.id_rc = c.id_rconcil AND a.id_campo_validacion = c.id_campo " +
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
                            obj[0].toString() + " = CAST(TRY_CAST(" + obj[0].toString() + " AS DECIMAL(38, 0)) * 0.01 " +
                            operacion + obj[3].toString() + " AS VARCHAR) " +
                            "WHERE " + obj[1].toString() + " = '" + obj[2].toString() + "';";
                } else {
                    queryUpdate = "UPDATE " + nombreTabla + " SET " +
                            obj[0].toString() + " = '" + obj[3].toString() + "' " +
                            "WHERE " + obj[1].toString() + " = '" + obj[2].toString() + "';";
                }

                // Ejecutar la consulta
                /*Query deleteSelect = entityManager.createNativeQuery(queryUpdate);
                deleteSelect.executeUpdate();*/

                System.out.println("QUERY -> "+queryUpdate);
                jdbcTemplate.execute(queryUpdate);
            }
        }
    }



    public void copyData(ConciliationRoute data,String fecha){
        String nombreTabla = "PRECISO_TEMP_INVENTARIOS";
        String campos = getCamposRcon(data).stream()
                .map(CampoRConcil::getNombre)
                .collect(Collectors.joining(","));
        /*Query querySelect = entityManager.createNativeQuery("DELETE FROM preciso_rconcil_"+data.getId()+" WHERE periodo_preciso = '"+fecha+"' ; \n" +
                "INSERT INTO preciso_rconcil_"+data.getId()+" ("+campos+",periodo_preciso"+") SELECT "+campos+",CAST('"+fecha+"' AS DATE) FROM "+nombreTabla);
        querySelect.executeUpdate();*/

        String querySelect = "DELETE FROM preciso_rconcil_"+data.getId()+" WHERE periodo_preciso = '"+fecha+"' ; \n" +
                "INSERT INTO preciso_rconcil_"+data.getId()+" ("+campos+",periodo_preciso"+") SELECT "+campos+",CAST('"+fecha+"' AS DATE) FROM "+nombreTabla;
        System.out.println("QUERY -> "+querySelect);
        jdbcTemplate.execute(querySelect);
    }

    public List<Object> findTemporal(){
        List<Object> listTemp = new ArrayList<>();
        try {
            Query querySelect = entityManager.createNativeQuery("SELECT * FROM PRECISO_TEMP_INVENTARIOS ");
            listTemp = querySelect.getResultList();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return listTemp;
    }

    public void loadLogCargue(User user, ConciliationRoute ac, String fecha, String tipo, String estado, String mensaje)
    {
        LocalDate localDate = LocalDate.parse(fecha);
        Date fechaDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<Object> listTemp =findTemporal();
        Date today=new Date();
        LogInventoryLoad insert = new LogInventoryLoad();
        insert.setFechaCargue(fechaDate);
        insert.setFechaPreciso(today);
        insert.setCantidadRegistros((long) listTemp.size());
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
        insert.setIdCR(ac);
        logInventoryLoadRepository.save(insert);
    }

    public List<ConciliationRoute> findByFilter(String value, String filter) {
        List<ConciliationRoute> list=new ArrayList<ConciliationRoute>();
        switch (filter) {
            case "Conciliación":
                Query query = entityManager.createNativeQuery("SELECT a.* FROM preciso_rutas_conciliaciones a \n" +
                        "inner join preciso_conciliaciones b on a.id_conciliacion = b.id\n" +
                        "where b.nombre = ?", ConciliationRoute.class);
                query.setParameter(1, value );
                list= query.getResultList();
                break;
            case "Nombre":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM preciso_rutas_conciliaciones as em WHERE em.detalle = ?", ConciliationRoute.class);
                query1.setParameter(1, value );
                list= query1.getResultList();
                break;
            case "Archivo":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM preciso_rutas_conciliaciones as em WHERE em.nombre_archivo = ?", ConciliationRoute.class);
                query2.setParameter(1, value );
                list= query2.getResultList();
                break;
            case "Ruta de Acceso":
                Query query4 = entityManager.createNativeQuery("SELECT em.* FROM preciso_rutas_conciliaciones as em WHERE em.ruta = ?", ConciliationRoute.class);
                query4.setParameter(1, value );
                list= query4.getResultList();
                break;
            case "Tipo Archivo":
                Query query5 = entityManager.createNativeQuery("SELECT em.* FROM preciso_rutas_conciliaciones as em WHERE em.tipo_archivo = ?", ConciliationRoute.class);
                query5.setParameter(1, value );
                list= query5.getResultList();
                break;
            case "Estado":
                Boolean valor = true;
                if (value.substring(0,1).equalsIgnoreCase("i")) valor = false;
                Query query3 = entityManager.createNativeQuery(
                        "SELECT em.* FROM preciso_rutas_conciliaciones as em WHERE em.activo = ?", ConciliationRoute.class);
                query3.setParameter(1, valor);
                list= query3.getResultList();
                break;
            default:
                break;
        }
        return list;
    }

    public List <ConciliationRoute> findFicherosActivos(){
        return conciliationRouteRepository.findByEstadoAndFichero(true, true);
    }

    public List<Object[]> findRutasByConcil(int concilId) {
        Query query = entityManager.createNativeQuery(
                "SELECT id, detalle FROM preciso_rutas_conciliaciones WHERE id_conciliacion = :concilId");
        query.setParameter("concilId", concilId);
        return query.getResultList();
    }

    //PARA CARGUE DE INVENTARIOS

    public List<LogInventoryLoad> findAllLog(ConciliationRoute cr, String fecha) {
        LocalDate localDate = LocalDate.parse(fecha);
        Date fechaDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        return logInventoryLoadRepository.findAllByIdCRAndFechaCargueOrderByIdDesc(cr,fechaDate);
    }

    public List<Object[]> findAllLogByDate() {
        Query query = entityManager.createNativeQuery(
                "WITH CTE AS (\n" +
                        "    SELECT \n" +
                        "        [id_lci],\n" +
                        "        [cantidad_registros],\n" +
                        "        [estado_proceso],\n" +
                        "        [fecha_cargue],\n" +
                        "        [fecha_preciso],\n" +
                        "        [novedad],\n" +
                        "        [tipo_proceso],\n" +
                        "        [usuario],\n" +
                        "        [idcr],\n" +
                        "        COUNT(*) OVER (PARTITION BY [fecha_cargue], [idcr]) AS total_intentos,\n" +
                        "        ROW_NUMBER() OVER (PARTITION BY [fecha_cargue], [idcr] ORDER BY [fecha_preciso] DESC) AS row_num\n" +
                        "    FROM \n" +
                        "        [PRECISO].[dbo].[preciso_log_cargues_inventarios]\n" +
                        ")\n" +
                        "SELECT [id_lci],\n" +              //0
                        "    [cantidad_registros],\n" +     //1
                        "    [estado_proceso],\n" +         //2
                        "    [fecha_cargue],\n" +           //3
                        "    [fecha_preciso],\n" +          //4
                        "    [novedad],\n" +                //5
                        "    [tipo_proceso],\n" +           //6
                        "    [usuario],\n" +                //7
                        "    [idcr],\n" +                   //8
                        "    total_intentos\n" +           //9
                        "FROM CTE\n" +
                        "WHERE row_num = 1\n" +
                        "ORDER BY \n" +
                        "[fecha_cargue], [idcr];");
        return query.getResultList();
    }


    public List<Object[]> findAllData(ConciliationRoute data, String fecha) {
        List<Object[]> lista= new ArrayList<>();
        if(!data.getCampos().isEmpty()) {
            String campos = data.getCampos().stream()
                    .map(CampoRConcil::getNombre)
                    .collect(Collectors.joining(","));
            Query querySelect = entityManager.createNativeQuery("SELECT " + campos + ",periodo_preciso FROM preciso_rconcil_" + data.getId() + " WHERE periodo_preciso = '" + fecha + "' ");
            lista = querySelect.getResultList();
        }
        return lista;
    }

    public List<Object[]> processList(List<Object[]> aroutes, List<CampoRConcil> colAroutes) {
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
                    // Si no es de tipo "Float", dejamos el valor tal cual está
                    processedRow[i] = row[i];
                }
            }
            processedList.add(processedRow);
        }

        return processedList;
    }

    @Scheduled(cron = "0 0/30 * * * ?")
    public void jobLeerArchivos() {
        LocalDateTime fechaHoy = LocalDateTime.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String fecha = fechaHoy.format(formato);

        List<ConciliationRoute> list = findByJob();
        for (ConciliationRoute cr :list)
        {
            try {;
                createTableTemporal(cr);
                generarArchivoFormato(getCamposRcon(cr), rutaArchivoFormato);
                if(cr.getTipoArchivo().equalsIgnoreCase("XLS") || cr.getTipoArchivo().equalsIgnoreCase("XLSX"))
                    importXlsx(cr,rutaArchivoFormato,fecha,null);
                else
                    bulkImport(cr,rutaArchivoFormato,fecha,null);
                validationData(cr);
                copyData(cr,fecha);
                loadLogCargue(null,cr,fecha,"Automático","Exitoso","");
            }
            catch (Exception e) {
                e.printStackTrace();
                Throwable rootCause = e;
                while (rootCause.getCause() != null) {
                    rootCause = rootCause.getCause(); // Navega a la causa raíz
                }
                loadLogCargue(null,cr,fecha,"Automático","Fallido",rootCause.getMessage());
            }
        }
    }

}
