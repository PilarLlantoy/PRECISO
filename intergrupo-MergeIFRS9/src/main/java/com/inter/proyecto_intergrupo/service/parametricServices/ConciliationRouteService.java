package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ConciliationRouteRepository;
import com.inter.proyecto_intergrupo.repository.parametric.LogInventoryLoadRepository;
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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConciliationRouteService {

    @Autowired
    private final ConciliationRouteRepository conciliationRouteRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

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

/*
    public void createTableTemporal(ConciliationRoute data) {
        String nombreTabla = "PRECISO_TEMP_INVENTARIOS";
        Query queryDrop = entityManager.createNativeQuery("IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '"+(nombreTabla)+"' AND TABLE_SCHEMA = 'dbo') BEGIN DROP TABLE "+(nombreTabla) +" END;");
        queryDrop.executeUpdate();

        StringBuilder createTableQuery = new StringBuilder("CREATE TABLE ");
        createTableQuery.append(nombreTabla).append(" (");

        for (int i = 0; i < data.getCampos().size(); i++) {
            CampoRConcil column = data.getCampos().get(i);
            createTableQuery.append(column.getNombre())
                    .append(" ")
                    .append(column.getTipo());

            if (column.getTipo().equalsIgnoreCase("VARCHAR")) {
                createTableQuery.append("(").append(column.getLongitud()).append(")");
            }

            if (i < data.getCampos().size() - 1) {
                createTableQuery.append(", ");
            }
        }

        createTableQuery.append(");");

        try {
            entityManager.createNativeQuery(createTableQuery.toString()).executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
     */

    public void createTableTemporal(ConciliationRoute data) {
        String nombreTabla = "PRECISO_TEMP_INVENTARIOS";
        Query queryDrop = entityManager.createNativeQuery(
                "IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '" + nombreTabla + "' AND TABLE_SCHEMA = 'dbo') " +
                        "BEGIN DROP TABLE " + nombreTabla + " END;");
        queryDrop.executeUpdate();

        StringBuilder createTableQuery = new StringBuilder("CREATE TABLE ");
        createTableQuery.append(nombreTabla).append(" (");

        List<String> primaryKeys = new ArrayList<>(); // Almacenar nombres de columnas que son claves primarias

        for (int i = 0; i < data.getCampos().size(); i++) {
            CampoRConcil column = data.getCampos().get(i);

            // Verificar si la columna es clave primaria y asignar tamaño limitado
            if (column.getTipo().equalsIgnoreCase("VARCHAR")){
                if (column.isPrimario()) {
                    createTableQuery.append(column.getNombre()).append(" VARCHAR(255)");
                    primaryKeys.add(column.getNombre());
                } else {
                    createTableQuery.append(column.getNombre()).append(" VARCHAR(MAX)");
                }
            }
            else{
                if (column.isPrimario()) {
                    primaryKeys.add(column.getNombre());
                }
                createTableQuery.append(column.getNombre())
                        .append(" ")
                        .append(column.getTipo());
            }

            if (i < data.getCampos().size() - 1) {
                createTableQuery.append(", ");
            }
        }

        // Si existen claves primarias, agregarlas a la consulta
        if (!primaryKeys.isEmpty()) {
            createTableQuery.append(", PRIMARY KEY (");
            createTableQuery.append(String.join(", ", primaryKeys)); // Agregar las columnas de clave primaria
            createTableQuery.append(")");
        }

        createTableQuery.append(");");

        try {
            entityManager.createNativeQuery(createTableQuery.toString()).executeUpdate();
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

    public String todayDateConvert(String formato,String fecha) {
        System.out.println(formato+" - "+fecha);
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formato);
        if(fecha.isEmpty()) {
            return today.format(formatter).replace(".","");
        }
        else
        {
            LocalDate fechaCast = LocalDate.parse(fecha);
            return fechaCast.format(formatter).replace(".","");
        }
    }

    /*
    public void bulkImport(ConciliationRoute data, String ruta,String fecha, String fuente) throws PersistenceException {
        String nombreTabla = "PRECISO_TEMP_INVENTARIOS";
        String extension="";
        String delimitador=data.getDelimitador();

        if(data.getTipoArchivo().equals("XLS") || data.getTipoArchivo().equals("XLSX"))
            delimitador=";";

        String complement = "FIELDTERMINATOR = '"+delimitador+"', ROWTERMINATOR = '\\n', FIRSTROW = "+data.getFilasOmitidas();

        if(data.getTipoArchivo().equals("XLS") || data.getTipoArchivo().equals("XLSX") || data.getTipoArchivo().equals("CSV") || data.getTipoArchivo().equals("TXT"))
            extension="."+data.getTipoArchivo();
        if(delimitador.equalsIgnoreCase(""))
            complement="FORMATFILE = '" + ruta + "', ROWTERMINATOR = '\\r\\n', FIRSTROW = " + data.getFilasOmitidas();

        String fichero=ensureTrailingSlash(data.getRuta()) + data.getNombreArchivo() + todayDateConvert(data.getFormatoFecha(),fecha)+ extension;
        if(fuente !=null)
            fichero=fuente;

        Query queryBulk = entityManager.createNativeQuery("BULK INSERT " + (nombreTabla) +
                " FROM '" + fichero +
                "' WITH ("+complement+ ")");
        queryBulk.executeUpdate();
    }
     */

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
        String fichero=ensureTrailingSlash(data.getRuta()) + data.getNombreArchivo() + todayDateConvert(data.getFormatoFecha(),fecha) +"."+ data.getTipoArchivo();
        if(fuente !=null)
            fichero=fuente;
        if (fichero != null && !fichero.isEmpty()) {
            StringBuilder sqlQueryBuilder = new StringBuilder("INSERT INTO PRECISO_TEMP_INVENTARIOS (");
            List<CampoRConcil> campos = data.getCampos();
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
                        Query query = entityManager.createNativeQuery(sqlQuery);
                        DataFormatter formatter = new DataFormatter();
                        for (int i = 0; i < campos.size(); i++) {
                            Cell cell = row.getCell(i);
                            Object value = null;
                            if (campos.get(i).getTipo().equalsIgnoreCase("Float")) {
                                value = cell != null ? formatter.formatCellValue(cell).replace(".", "").replace(",", ".") : null;
                            } else if (campos.get(i).getTipo().equalsIgnoreCase("Date") || campos.get(i).getTipo().equalsIgnoreCase("Datetime")) {
                                String fechaLeida = cell != null ? formatter.formatCellValue(cell) : null;
                                if (!fechaLeida.matches("\\d{2}/\\d{2}/\\d{4}")) {
                                    // Por ejemplo, convierte de `1/18/99` a `18/01/1999` si es necesario
                                    System.out.println("INSPECCIONES");
                                    System.out.println(fechaLeida);
                                    System.out.println(campos.get(i).getFormatoFecha());
                                    fechaLeida = convertirFormatoExcel(fechaLeida,campos.get(i).getFormatoFecha());
                                }
                                value = fechaLeida != null ?  formatoFecha(fechaLeida, campos.get(i).getFormatoFecha(), campos.get(i).getSeparador()) : null;
                            } else {
                                value = cell != null ? formatter.formatCellValue(cell) : null;
                            }

                            query.setParameter(i + 1, value);
                            System.out.println(i + " -> " + value);
                        }
                        query.executeUpdate();
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

        String complement = "FIELDTERMINATOR = '"+delimitador+"', ROWTERMINATOR = '\\n', FIRSTROW = "+data.getFilasOmitidas();

        if(data.getTipoArchivo().equals("XLS") || data.getTipoArchivo().equals("XLSX") || data.getTipoArchivo().equals("CSV") || data.getTipoArchivo().equals("TXT"))
            extension="."+data.getTipoArchivo();
        if(delimitador.equalsIgnoreCase(""))
            complement="FORMATFILE = '" + ruta + "', ROWTERMINATOR = '\\r\\n', FIRSTROW = " + data.getFilasOmitidas();

        String fechaConvert="";
        if(data.isSiglasFechas()==true)
            fechaConvert=todayDateConvert(data.getFormatoFecha(),fecha);
        String fichero=ensureTrailingSlash(data.getRuta()) + data.getNombreArchivo() + fechaConvert + extension;
        if(fuente !=null)
            fichero=fuente;

        Query queryBulk = entityManager.createNativeQuery("BULK INSERT " + (nombreTabla) +
                " FROM '" + fichero +
                "' WITH ("+complement+ ")");
        queryBulk.executeUpdate();
    }

    /*
    public void validationData(ConciliationRoute data){
        Query querySelect = entityManager.createNativeQuery("SELECT b.nombre as referencia, c.nombre as validacion, a.valor_validacion, a.valor_operacion, \n" +
                "CASE a.operacion when 'Suma' then '+' when 'Resta' then '-' when 'Multiplica' then '*' when 'Divida' then '/' END as Operacion\n" +
                "FROM PRECISO.dbo.preciso_validaciones_rconcil a \n" +
                "inner join PRECISO.dbo.preciso_campos_rconcil b on a.id_rc = b.id_rconcil and a.id_campo_referencia=b.id_campo \n" +
                "inner join PRECISO.dbo.preciso_campos_rconcil c on a.id_rc = c.id_rconcil and a.id_campo_validacion=c.id_campo \n" +
                "where a.id_rc = ? and a.estado=1");
        querySelect.setParameter(1,data.getId());
        List<Object[]> validacionLista = querySelect.getResultList();
        if(!validacionLista.isEmpty()){
            for(Object[] obj : validacionLista){
                Query deleteSelect = entityManager.createNativeQuery("UPDATE "+data.getNombreArchivo() + "_TEMPORAL SET "
                        +obj[0].toString()+" = "+obj[0].toString()+obj[4].toString()+obj[3].toString()+" WHERE "+obj[1].toString()+"='"+obj[2].toString()+"';");
                deleteSelect.executeUpdate();
            }
        }
    }
     */

    /*
    public void validationData(ConciliationRoute data){
        String nombreTabla = "PRECISO_TEMP_INVENTARIOS";
        Query querySelect = entityManager.createNativeQuery("SELECT b.nombre as referencia, c.nombre as validacion, a.valor_validacion, a.valor_operacion, \n" +
                "CASE a.operacion when 'Suma' then '+' when 'Resta' then '-' when 'Multiplica' then '*' when 'Divida' then '/' END as Operacion\n" +
                "FROM PRECISO.dbo.preciso_validaciones_rconcil a \n" +
                "inner join PRECISO.dbo.preciso_campos_rconcil b on a.id_rc = b.id_rconcil and a.id_campo_referencia=b.id_campo \n" +
                "inner join PRECISO.dbo.preciso_campos_rconcil c on a.id_rc = c.id_rconcil and a.id_campo_validacion=c.id_campo \n" +
                "where a.id_rc = ? and a.estado=1");
        querySelect.setParameter(1,data.getId());
        List<Object[]> validacionLista = querySelect.getResultList();
        if(!validacionLista.isEmpty()){
            for(Object[] obj : validacionLista){
                Query deleteSelect = entityManager.createNativeQuery("UPDATE "+nombreTabla+" SET " +
                        obj[0].toString()+" = CAST(TRY_CAST("+ obj[0].toString() + " AS DECIMAL(38, 0))*0.01 " +
                        obj[4].toString() + obj[3].toString()+" AS VARCHAR) WHERE "+obj[1].toString()+"='"+obj[2].toString()+"';");
                deleteSelect.executeUpdate();
            }
        }

    }

     */

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
                Query deleteSelect = entityManager.createNativeQuery(queryUpdate);
                deleteSelect.executeUpdate();
            }
        }
    }



    public void copyData(ConciliationRoute data,String fecha){
        String nombreTabla = "PRECISO_TEMP_INVENTARIOS";
        String campos = data.getCampos().stream()
                .map(CampoRConcil::getNombre)
                .collect(Collectors.joining(","));
        Query querySelect = entityManager.createNativeQuery("DELETE FROM preciso_rconcil_"+data.getId()+" WHERE periodo_preciso = '"+fecha+"' ; \n" +
                "INSERT INTO preciso_rconcil_"+data.getId()+" ("+campos+",periodo_preciso"+") SELECT "+campos+",CAST('"+fecha+"' AS DATE) FROM "+nombreTabla);
        querySelect.executeUpdate();
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
        String campos = data.getCampos().stream()
                .map(CampoRConcil::getNombre)
                .collect(Collectors.joining(","));
        Query querySelect = entityManager.createNativeQuery("SELECT "+campos+",periodo_preciso FROM preciso_rconcil_"+data.getId()+" WHERE periodo_preciso = '"+fecha+"' ");
        return querySelect.getResultList();
    }

}
