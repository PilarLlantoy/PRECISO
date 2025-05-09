package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CampoRCRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CampoRConcilRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ConciliationRouteRepository;
import com.inter.proyecto_intergrupo.repository.parametric.LogInventoryLoadRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import java.io.*;
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
    private CampoRConcilRepository campoRConcilRepository;

    @Autowired
    private CampoRConcilService campoRConcilService;

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

    public List<ConciliationRoute> findAllActiveOrder() {
        Query querySelect = entityManager.createNativeQuery("select * from preciso_rutas_conciliaciones where estado = 1 order by id_conciliacion,id",ConciliationRoute.class);
        return querySelect.getResultList();
    }

    public ConciliationRoute findById(int id){
        return conciliationRouteRepository.findAllById(id);
    }

    public List<ConciliationRoute> getRoutesByConciliation(int concil) {
        return conciliationRouteRepository.findByActivoAndConciliacionId(true, concil);
    }

    public List<Object[]> findByEncabezados() {
        Query query = entityManager.createNativeQuery("SELECT a.id_conciliacion,b.detalle,a.id,a.detalle as d1,a.nombre_archivo,a.ruta,a.tipo_archivo, a.estado FROM preciso_rutas_conciliaciones a\n" +
                "LEFT JOIN preciso_conciliaciones b on a.id_conciliacion=b.id order by a.id_conciliacion, a.id");
        return query.getResultList();
    }

    public List<Object[]> findByCampos() {
        Query query = entityManager.createNativeQuery("SELECT c.id,c.detalle,b.id as id2,b.detalle as n1,a.id_campo,a.nombre as n2, a.primario,a.tipo,a.longitud,a.conciliacion,a.nulo_moneda,a.separador,a.formato_fecha,a.idioma FROM preciso_campos_rconcil a\n" +
                "LEFT JOIN  preciso_rutas_conciliaciones b ON a.id_rconcil =b.id\n" +
                "LEFT JOIN preciso_conciliaciones c on b.id_conciliacion=c.id order by c.id,b.id");
        return query.getResultList();
    }

    public List<Object[]> findByCamposSelect(int id) {
        Query query = entityManager.createNativeQuery("SELECT c.id,c.detalle,b.id as id2,b.detalle as n1,a.id_campo,a.nombre as n2, a.primario,a.tipo,a.longitud,a.conciliacion,a.nulo_moneda,a.separador,a.formato_fecha,a.idioma FROM preciso_campos_rconcil a\n" +
                "LEFT JOIN  preciso_rutas_conciliaciones b ON a.id_rconcil =b.id\n" +
                "LEFT JOIN preciso_conciliaciones c on b.id_conciliacion=c.id where b.id = ? order by c.id,b.id");
        query.setParameter(1,id);
        return query.getResultList();
    }

    public List<CampoRConcil> findByCamposSelectById(int id,String nombre) {
        Query query = entityManager.createNativeQuery("SELECT a.* FROM preciso_campos_rconcil a\n" +
                "LEFT JOIN  preciso_rutas_conciliaciones b ON a.id_rconcil =b.id where b.id = ? and a.nombre = ? ",CampoRConcil.class);
        query.setParameter(1,id);
        query.setParameter(2,nombre);
        return query.getResultList();
    }

    public List<Object[]> findByValidaciones() {
        Query query = entityManager.createNativeQuery("SELECT c.id,c.detalle,b.id as id2,b.detalle as n1,a.id_campo_referencia,d.nombre as n2,a.id_campo_validacion,e.nombre as n3,a.valor_validacion,UPPER(a.operacion) AS OPE,a.valor_operacion,a.formula FROM preciso_validaciones_rconcil a\n" +
                "LEFT JOIN  preciso_rutas_conciliaciones b ON a.id_rc =b.id\n" +
                "LEFT JOIN preciso_conciliaciones c on b.id_conciliacion=c.id \n" +
                "LEFT JOIN preciso_campos_rconcil d on a.id_rc=d.id_rconcil and a.id_campo_referencia = d.id_campo\n" +
                "LEFT JOIN preciso_campos_rconcil e on a.id_rc=e.id_rconcil and a.id_campo_validacion = e.id_campo\n" +
                "order by c.id,b.id_conciliacion");
        return query.getResultList();
    }

    public ConciliationRoute findByName(String nombre){
        return conciliationRouteRepository.findAllByDetalle(nombre);
    }

    public ConciliationRoute modificar(ConciliationRoute croute){
        conciliationRouteRepository.save(croute);
       return croute;
    }

    public String encontrarUltimaFechaSubida(AccountingRoute data) {
        Query querySelect = entityManager.createNativeQuery(
                "SELECT MAX(periodo_preciso) AS ultimo_periodo_preciso FROM preciso_rc_" + data.getId()
        );
        Object result = querySelect.getSingleResult();
        return result != null ? result.toString() : null;
    }

    public List<CampoRConcil> getCamposRcon(ConciliationRoute data) {
        Query querySelect = entityManager.createNativeQuery(
                "select b.* from preciso_rutas_conciliaciones a, preciso_campos_rconcil b where a.id = b.id_rconcil and a.id = " + data.getId(),CampoRConcil.class);
        return querySelect.getResultList();
    }

    public boolean findAllDataValidationA(ConciliationRoute data, String fecha) {
        StringBuilder queryBuilder = new StringBuilder("SELECT * " +
                "FROM preciso_rconcil_" + data.getId() + " WHERE periodo_preciso = :fecha");

        Query querySelect = entityManager.createNativeQuery(queryBuilder.toString());
        querySelect.setParameter("fecha", fecha);

        return !querySelect.getResultList().isEmpty();
    }

    public boolean findAllDataTemporalA(ConciliationRoute data, String fecha) {
        StringBuilder queryBuilder = new StringBuilder("SELECT * " +
                "FROM PRECISO_TEMP_INVENTARIOS ");

        Query querySelect = entityManager.createNativeQuery(queryBuilder.toString());

        return querySelect.getResultList().isEmpty();
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

    public String todayDateConvert(String formato,String fecha,String idioma,ConciliationRoute data) {
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

    public List<ConciliationRoute> findByJobNotLoad(String fecha) {
        String sql = "SELECT a.* FROM (select * from preciso_rutas_conciliaciones WHERE activo = 1) a\n" +
                "left join (select idcr,estado_proceso from preciso_log_cargues_inventarios where fecha_cargue like '"+fecha+"%' group by idcr,estado_proceso) b on a.id = b.idcr\n" +
                "where b.idcr is null ";
        Query querySelect = entityManager.createNativeQuery(sql, ConciliationRoute.class);
        return querySelect.getResultList();
    }

    public void importXlsx(ConciliationRoute data, String ruta,String fecha, String fuente) throws PersistenceException, IOException {
        String fichero=ensureTrailingSlash(data.getRuta()) + data.getNombreArchivo() +"."+ data.getTipoArchivo();
        if(data.isSiglasFechas()){
            fichero=ensureTrailingSlash(data.getRuta()) + data.getNombreArchivo() + todayDateConvert(data.getFormatoFecha(),fecha,data.getIdiomaFecha(),data) +"."+ data.getTipoArchivo();
        }

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
                                        if(cell.getCellType() == CellType.NUMERIC)
                                        {
                                            double numericValue = cell.getNumericCellValue();
                                            value = new BigDecimal(Double.toString(numericValue));
                                        }
                                        else
                                        {
                                            String numericValue = "";
                                            if (!campos.get(i).getSeparador().equalsIgnoreCase("."))
                                                numericValue = formatter.formatCellValue(cell).replace(".", "").replace(",", ".");
                                            else
                                                numericValue = formatter.formatCellValue(cell).replace(",", "");
                                            System.out.println(numericValue);
                                            value = Double.parseDouble(numericValue);
                                        }
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

        int cantidad=data.getFilasOmitidas()+1;

        String complement = "FIELDTERMINATOR = '"+delimitador+"', ROWTERMINATOR = '\\n', FIRSTROW = "+cantidad;

        if(data.getTipoArchivo().equals("XLS") || data.getTipoArchivo().equals("XLSX") || data.getTipoArchivo().equals("CSV") || data.getTipoArchivo().equals("TXT"))
            extension="."+data.getTipoArchivo();
        if(delimitador.equalsIgnoreCase(""))
            complement="FORMATFILE = '" + ruta + "', ROWTERMINATOR = '\\r\\n', FIRSTROW = " + data.getFilasOmitidas();

        String fechaConvert="";
        if(data.isSiglasFechas()==true)
            fechaConvert=todayDateConvert(data.getFormatoFecha(),fecha,data.getIdiomaFecha(),data);
        String fichero=ensureTrailingSlash(data.getRuta()) + data.getNombreArchivo() + fechaConvert + extension;
        if(fuente !=null)
            fichero=fuente;

        String queryBulk = "BULK INSERT " + (nombreTabla) +
                " FROM '" + fichero +
                "' WITH ("+complement+ ")";
        System.out.println("QUERY -> "+queryBulk);
        jdbcTemplate.execute(queryBulk);

        List<CampoRConcil> lista =getCamposRcon(data);

        String update="";
        String update1="";
        for (CampoRConcil campo:lista) {
            if(!update.isEmpty() && campo.getTipo().equalsIgnoreCase("Float"))
                update=update+",";
            if(campo.getTipo().equalsIgnoreCase("Float") && (campo.getSeparador() == null || ( campo.getSeparador() != null && (campo.getSeparador().equalsIgnoreCase(".")||campo.getSeparador().equalsIgnoreCase(""))))) {
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
            String queryUpdate = "UPDATE PRECISO_TEMP_INVENTARIOS SET " + update;
            System.out.println("QUERY -> "+queryUpdate);
            jdbcTemplate.execute(queryUpdate);
        }

        if(!update1.isEmpty())
        {
            String queryUpdate1 = "UPDATE PRECISO_TEMP_INVENTARIOS SET " + update1;
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
                            obj[0].toString() + " = CAST(CASE WHEN "+obj[0].toString()+" LIKE '%.%' THEN TRY_CAST("+obj[0].toString()+" AS DECIMAL(38, 2)) " +
                            "ELSE TRY_CAST("+obj[0].toString()+" AS DECIMAL(38, 2)) /100.0 END "+ operacion + obj[3].toString() +" AS DECIMAL(38, 2)) ";
                    if(obj[1]!=null && !obj[1].toString().equalsIgnoreCase("") && obj[2]!=null)
                        queryUpdate = queryUpdate+"WHERE " + obj[1].toString() + " = '" + obj[2].toString() + "';";
                } else {
                    queryUpdate = "UPDATE " + nombreTabla + " SET " +
                            obj[0].toString() + " = '" + obj[3].toString() + "' ";
                    if(obj[1]!=null && !obj[1].toString().equalsIgnoreCase("") && obj[2]!=null)
                        queryUpdate = queryUpdate+"WHERE " + obj[1].toString() + " = '" + obj[2].toString() + "';";
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
                        "where b.nombre = ? order by a.id_conciliacion,a.id", ConciliationRoute.class);
                query.setParameter(1, value );
                list= query.getResultList();
                break;
            case "Nombre":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM preciso_rutas_conciliaciones as em WHERE em.detalle = ? order by em.id_conciliacion,em.id", ConciliationRoute.class);
                query1.setParameter(1, value );
                list= query1.getResultList();
                break;
            case "Archivo":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM preciso_rutas_conciliaciones as em WHERE em.nombre_archivo = ? order by em.id_conciliacion,em.id", ConciliationRoute.class);
                query2.setParameter(1, value );
                list= query2.getResultList();
                break;
            case "Ruta de Acceso":
                Query query4 = entityManager.createNativeQuery("SELECT em.* FROM preciso_rutas_conciliaciones as em WHERE em.ruta = ? order by em.id_conciliacion,em.id", ConciliationRoute.class);
                query4.setParameter(1, value );
                list= query4.getResultList();
                break;
            case "Tipo Archivo":
                Query query5 = entityManager.createNativeQuery("SELECT em.* FROM preciso_rutas_conciliaciones as em WHERE em.tipo_archivo = ? order by em.id_conciliacion,em.id", ConciliationRoute.class);
                query5.setParameter(1, value );
                list= query5.getResultList();
                break;
            case "Estado":
                Boolean valor = true;
                if (value.substring(0,1).equalsIgnoreCase("i")) valor = false;
                Query query3 = entityManager.createNativeQuery(
                        "SELECT em.* FROM preciso_rutas_conciliaciones as em WHERE em.activo = ?  order by em.id_conciliacion,em.id", ConciliationRoute.class);
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

    public List<ConciliationRoute> findByConcil(int concilId) {
        Query query = entityManager.createNativeQuery(
                "SELECT * FROM preciso_rutas_conciliaciones WHERE id_conciliacion = :concilId", ConciliationRoute.class);
        query.setParameter("concilId", concilId);
        return query.getResultList();
    }

    //PARA CARGUE DE INVENTARIOS

    public List<LogInventoryLoad> findAllLog(ConciliationRoute cr, String fecha) {
        LocalDate localDate = LocalDate.parse(fecha);
        Date fechaDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        return logInventoryLoadRepository.findAllByIdCRAndFechaCargueOrderByIdDesc(cr,fechaDate);
    }

    public List<Object[]> findAllLogByDate(String fecha) {
        Query query = entityManager.createNativeQuery(
                "WITH CTE AS (\n" +
                        "SELECT \n" +
                        "[id_lci],\n" +
                        "[cantidad_registros],\n" +
                        "[estado_proceso],\n" +
                        "[fecha_cargue],\n" +
                        "[fecha_preciso],\n" +
                        "[novedad],\n" +
                        "[tipo_proceso],\n" +
                        "[usuario],\n" +
                        "[idcr],\n" +
                        "COUNT(*) OVER (PARTITION BY [fecha_cargue], [idcr]) AS total_intentos,\n" +
                        "ROW_NUMBER() OVER (PARTITION BY [fecha_cargue], [idcr] ORDER BY [fecha_preciso] DESC) AS row_num\n" +
                        "FROM \n" +
                        "[PRECISO].[dbo].[preciso_log_cargues_inventarios]\n" +
                        "WHERE fecha_cargue like :fechaValP \n" +
                        ")\n" +
                        "SELECT a.id_lci,\n" +
                        "ISNULL(a.cantidad_registros,0) as cantidad_registros, \n" +
                        "ISNULL(a.estado_proceso,'Fallido') as estado_proceso, \n" +
                        "ISNULL(a.fecha_cargue,cast( :fechaVal as date)) as fecha_cargue, \n" +
                        "ISNULL(a.fecha_preciso,cast( :fechaVal as date)) as fecha_preciso,\n" +
                        "ISNULL(a.novedad,'') as novedad,\n" +
                        "ISNULL(a.tipo_proceso,'') as tipo_proceso,\n" +
                        "ISNULL(a.usuario,'Sin Ejecutar') as usuario, \n" +
                        "b.id,\n" +
                        "ISNULL(a.total_intentos,0) as total_intentos\n" +
                        "FROM preciso_rutas_conciliaciones b\n" +
                        "left join (SELECT * FROM CTE WHERE row_num = 1 ) a on b.id =a.idcr\n" +
                        "ORDER BY \n" +
                        "b.id, a.fecha_cargue;\n");
        query.setParameter("fechaVal", fecha);
        query.setParameter("fechaValP", fecha+"%");
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
        List<ConciliationRoute> list = findByJob();
        for (ConciliationRoute cr :list)
        {
            LocalDateTime fechaHoy = LocalDateTime.now();
            fechaHoy = fechaHoy.minusDays(1);
            if(cr.getDiasRetardo()>1)
                fechaHoy = fechaHoy.minusDays(cr.getDiasRetardo()-1);
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String fecha = fechaHoy.format(formato);

            try {
                createTableTemporal(cr);
                generarArchivoFormato(getCamposRcon(cr), rutaArchivoFormato);
                if(cr.getTipoArchivo().equalsIgnoreCase("XLS") || cr.getTipoArchivo().equalsIgnoreCase("XLSX"))
                    importXlsx(cr,rutaArchivoFormato,fecha,null);
                else
                    bulkImport(cr,rutaArchivoFormato,fecha,null);
                validationData(cr);
                copyData(cr,fecha);

                if(findAllDataValidationA(cr,fecha)) {
                    loadLogCargue(null, cr, fecha, "Automático", "Exitoso", "");
                }
                else if(findAllDataTemporalA(cr,fecha)) {
                    loadLogCargue(null, cr, fecha, "Automático", "Fallido", "La ruta "+cr.getRuta()+" es inaccesible. (El sistema no puede encontrar el archivo especificado)");
                }
                else {
                    loadLogCargue(null, cr, fecha, "Automático", "Fallido", "Valide el formato de los campos de tipo Float y Bigint");
                }
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

    @Scheduled(cron = "0 45 23 * * ?")
    public void jobEjectutarDiariosFaltantes() {
        LocalDateTime fechaHoyTemp = LocalDateTime.now();
        fechaHoyTemp = fechaHoyTemp.minusDays(1);
        DateTimeFormatter formatoTemp = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String fechaTemp = fechaHoyTemp.format(formatoTemp);

        List<ConciliationRoute> list = findByJobNotLoad(fechaTemp);
        for (ConciliationRoute cr :list)
        {
            LocalDateTime fechaHoy = LocalDateTime.now();
            fechaHoy = fechaHoy.minusDays(1);
            if(cr.getDiasRetardo()>1)
                fechaHoy = fechaHoy.minusDays(cr.getDiasRetardo()-1);
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String fecha = fechaHoy.format(formato);

            try {
                createTableTemporal(cr);
                generarArchivoFormato(getCamposRcon(cr), rutaArchivoFormato);
                if(cr.getTipoArchivo().equalsIgnoreCase("XLS") || cr.getTipoArchivo().equalsIgnoreCase("XLSX"))
                    importXlsx(cr,rutaArchivoFormato,fecha,null);
                else
                    bulkImport(cr,rutaArchivoFormato,fecha,null);
                validationData(cr);
                copyData(cr,fecha);

                if(findAllDataValidationA(cr,fecha)) {
                    loadLogCargue(null, cr, fecha, "Automático", "Exitoso", "");
                }
                else if(findAllDataTemporalA(cr,fecha)) {
                    loadLogCargue(null, cr, fecha, "Automático", "Fallido", "La ruta "+cr.getRuta()+" es inaccesible. (El sistema no puede encontrar el archivo especificado)");
                }
                else {
                    loadLogCargue(null, cr, fecha, "Automático", "Fallido", "Valide el formato de los campos de tipo Float y Bigint");
                }
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

    public void leerArchivosMasivo(String[] ids,String fecha) {

        for (String id :ids)
        {
            ConciliationRoute cr = findById(Integer.parseInt(id));
            try {
                createTableTemporal(cr);
                generarArchivoFormato(getCamposRcon(cr), rutaArchivoFormato);
                if(cr.getTipoArchivo().equalsIgnoreCase("XLS") || cr.getTipoArchivo().equalsIgnoreCase("XLSX"))
                    importXlsx(cr,rutaArchivoFormato,fecha,null);
                else
                    bulkImport(cr,rutaArchivoFormato,fecha,null);
                validationData(cr);
                copyData(cr,fecha);

                if(findAllDataValidationA(cr,fecha)) {
                    loadLogCargue(null, cr, fecha, "Cargue Masivo", "Exitoso", "");
                }
                else if(findAllDataTemporalA(cr,fecha)) {
                    loadLogCargue(null, cr, fecha, "Cargue Masivo", "Fallido", "La ruta "+cr.getRuta()+" es inaccesible. (El sistema no puede encontrar el archivo especificado)");
                }
                else {
                    loadLogCargue(null, cr, fecha, "Cargue Masivo", "Fallido", "Valide el formato de los campos de tipo Float y Bigint");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                Throwable rootCause = e;
                while (rootCause.getCause() != null) {
                    rootCause = rootCause.getCause(); // Navega a la causa raíz
                }
                loadLogCargue(null,cr,fecha,"Cargue Masivo","Fallido",rootCause.getMessage());
            }
        }
    }

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Rutas Conciliaciones");
        insert.setFecha(today);
        insert.setInput("Campos Rutas");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }
    public ArrayList<String[]> saveFileBD(InputStream file, User user, String id) throws IOException, InvalidFormatException {
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
        ArrayList<CampoRConcil> toInsert = new ArrayList<>();
        String stateFinal = "SUCCESS";
        XSSFRow row;
        List<String> valoresTipo = new ArrayList<>(Arrays.asList("Varchar","Integer","Bigint","Float","Date","Time","DateTime","Bit"));
        List<String> valoresFormato = new ArrayList<>(Arrays.asList("YYYYMMDD","YYMMDD","DDMMMMYYYY","DDMMYYYY","DDMMYY","MMDDYY"));
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    int consecutivo=0;
                    DataFormatter formatter = new DataFormatter();
                    String cellCodConciliacion = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellNomConiliacion = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellCodInventario= formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellNomInventario = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellCodCampo = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellNomCampo = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellPrimario = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellTipo = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellLongitud = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellVisualizacion = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellNulo = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellSeparador = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellFormato  = formatter.formatCellValue(row.getCell(consecutivo++)).trim();

                    if (cellCodInventario.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "El campo Código Contable no puede estar vacio.";
                        lista.add(log);
                    } else if (!cellCodInventario.equalsIgnoreCase(id)) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "El campo Código Contable no corresponde a la ruta contable donde esta intentando realizar el cargue.";
                        lista.add(log);
                    }
                    if (cellNomCampo.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(5);
                        log[2] = "El campo Nombre Campo no puede estar vacio.";
                        lista.add(log);
                    }
                    if (!cellPrimario.equalsIgnoreCase("Si") && !cellPrimario.equalsIgnoreCase("No") ) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(6);
                        log[2] = "El campo Primario debe contener un Si o No.";
                        lista.add(log);
                    }
                    if (!valoresTipo.contains(cellTipo)) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(7);
                        log[2] = "El campo Tipo no se encuentra dentro del listado permitido";
                        lista.add(log);
                    }
                    if (cellLongitud.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(8);
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
                                log[1] = CellReference.convertNumToColString(8);
                                log[2] = "El campo Longitud debe ser númerico o contenter la palabra MAX.";
                                lista.add(log);
                            }
                        }
                    }
                    if (!cellVisualizacion.equalsIgnoreCase("Si") && !cellVisualizacion.equalsIgnoreCase("No") ) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(9);
                        log[2] = "El campo Primario debe contener un Si o No.";
                        lista.add(log);
                    }
                    if (!cellNulo.equalsIgnoreCase("Si") && !cellNulo.equalsIgnoreCase("No") ) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(10);
                        log[2] = "El campo Primario debe contener un Si o No.";
                        lista.add(log);
                    }
                    if (cellTipo.equalsIgnoreCase("Date") || cellTipo.equalsIgnoreCase("DateTime")) {
                        if (cellSeparador.length() == 0) {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(11);
                            log[2] = "El campo Separador no puede estar vacio.";
                            lista.add(log);
                        }
                        if (!valoresFormato.contains(cellFormato)) {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(12);
                            log[2] = "El campo Formato no se encuentra dentro del listado permitido";
                            lista.add(log);
                        }
                    }


                    if (lista.isEmpty() && findByCamposSelectById(Integer.parseInt(id),cellNomCampo).isEmpty()) {
                        CampoRConcil data = new CampoRConcil();
                        data.setRutaConciliacion(findById(Integer.parseInt(id)));
                        data.setConciliacion(cellVisualizacion.equalsIgnoreCase("Si"));
                        data.setTipo(cellTipo);
                        data.setSeparador(cellSeparador);
                        data.setPrimario(cellPrimario.equalsIgnoreCase("Si"));
                        data.setNombre(cellNomCampo);
                        data.setLongitud(cellLongitud);
                        data.setIdioma("EspañolColombia");
                        data.setFormatoFecha(cellFormato);
                        data.setEstado(true);
                        toInsert.add(data);
                    }
                }
            }
        }

        if (lista.size() != 0)
            stateFinal = "FAILED";
        String[] log2 = new String[3];
        log2[0] = String.valueOf((toInsert.size() * 12) - lista.size());
        log2[1] = String.valueOf(lista.size());
        log2[2] = stateFinal;
        lista.add(log2);
        String[] temp = lista.get(0);
        if (temp[2].equals("SUCCESS")) {
            campoRConcilRepository.saveAll(toInsert);
            campoRConcilService.recreateTable(findById(Integer.parseInt(id)));
        }
        toInsert.clear();
        return lista;
    }

}
