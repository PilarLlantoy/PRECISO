package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.AccountingRouteRepository;
import com.inter.proyecto_intergrupo.repository.parametric.LogAccountingLoadRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
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


@Service
@Transactional
public class AccountingRouteService {

    @Autowired
    private final AccountingRouteRepository accountingRouteRepository;

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

    public List <AccountingRoute> findAll(){return accountingRouteRepository.findAllByOrderByNombreAsc();}

    public List<AccountingRoute> findAllActive() {
        return accountingRouteRepository.findByActivo(true);
    }

    public AccountingRoute findById(int id){
        return accountingRouteRepository.findAllById(id);
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

    public List<LogAccountingLoad> findAllLogByDate() {
        //LocalDate localDate = LocalDate.parse(fecha);
        //Date fechaDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        //return logInventoryLoadRepository.findAllByFechaCargueOrderByIdDesc(fechaDate);
        return logAccountingLoadRepository.findAllByOrderByFechaCargueAsc();
    }

    public List<Object[]> findAllData(AccountingRoute data, String fecha, String cadena, String campo) {
        String campos = data.getCampos().stream()
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
        Query queryDrop = entityManager.createNativeQuery(
                "IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '" + nombreTabla + "' AND TABLE_SCHEMA = 'dbo') " +
                        "BEGIN DROP TABLE " + nombreTabla + " END;");
        queryDrop.executeUpdate();

        StringBuilder createTableQuery = new StringBuilder("CREATE TABLE ");
        createTableQuery.append(nombreTabla).append(" (");

        List<String> primaryKeys = new ArrayList<>(); // Almacenar nombres de columnas que son claves primarias

        for (int i = 0; i < data.getCampos().size(); i++) {
            CampoRC column = data.getCampos().get(i);

            // Verificar si la columna es clave primaria y asignar tamaño limitado
            if (column.getTipo().equalsIgnoreCase("VARCHAR")){
                if (column.isPrimario()) {
                    createTableQuery.append(column.getNombre()).append(" VARCHAR(8000)");
                    primaryKeys.add(column.getNombre());
                } else {
                    createTableQuery.append(column.getNombre()).append(" VARCHAR(MAX)");
                }
            }
            else{
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



    public String todayDateConvert(String formato,String fecha) {
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

    public void importXlsx(AccountingRoute data, String ruta,String fecha, String fuente) throws PersistenceException, IOException {
        String fichero=ensureTrailingSlash(data.getRuta()) + data.getNombreArchivo() + todayDateConvert(data.getFormatoFecha(),fecha) + data.getComplementoArchivo() +"."+ data.getTipoArchivo();
        if(fuente !=null)
            fichero=fuente;
        if (fichero != null && !fichero.isEmpty()) {
            StringBuilder sqlQueryBuilder = new StringBuilder("INSERT INTO PRECISO_TEMP_CONTABLES (");
            List<CampoRC> campos = data.getCampos();
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
                        Query query = entityManager.createNativeQuery(sqlQuery);
                        for (int i = 0; i < campos.size(); i++) {
                            Cell cell = row.getCell(i);
                            Object value = null;
                            DataFormatter formatter = new DataFormatter();
                            value = cell != null ? formatter.formatCellValue(cell) : null;
                            query.setParameter(i + 1, value);
                        }
                        query.executeUpdate();
                    } else {
                        firstRow++;
                    }
                }
            }
        }
    }

    public void bulkImport(AccountingRoute data, String ruta,String fecha, String fuente) throws PersistenceException  {
        String nombreTabla = "PRECISO_TEMP_CONTABLES";
        String extension="";
        String delimitador=data.getDelimitador();

        /*if(data.getTipoArchivo().equals("XLS") || data.getTipoArchivo().equals("XLSX"))
            delimitador=";";*/

        String complement = "FIELDTERMINATOR = '"+delimitador+"', ROWTERMINATOR = '\\n', FIRSTROW = "+data.getFilasOmitidas();

        if(data.getTipoArchivo().equals("XLS") || data.getTipoArchivo().equals("XLSX") || data.getTipoArchivo().equals("CSV") || data.getTipoArchivo().equals("TXT"))
            extension="."+data.getTipoArchivo();
        if(delimitador.equalsIgnoreCase(""))
            complement="FORMATFILE = '" + ruta + "', ROWTERMINATOR = '\\r\\n', FIRSTROW = " + data.getFilasOmitidas();

        String fichero=ensureTrailingSlash(data.getRuta()) + data.getNombreArchivo() + todayDateConvert(data.getFormatoFecha(),fecha) + data.getComplementoArchivo() + extension;
        if(fuente !=null)
            fichero=fuente;

        Query queryBulk = entityManager.createNativeQuery("BULK INSERT " + (nombreTabla) +
                " FROM '" + fichero +
                "' WITH ("+complement+ ")");
        queryBulk.executeUpdate();
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

            Query deleteSelect = entityManager.createNativeQuery("DELETE FROM "+nombreTabla+" WHERE NOT("+ condicion +"));");
            deleteSelect.executeUpdate();

        }

    }

    /*public void validationData(AccountingRoute data){
        String nombreTabla = "PRECISO_TEMP_CONTABLES";
        Query querySelect = entityManager.createNativeQuery("SELECT b.nombre as referencia, c.nombre as validacion, a.valor_validacion, a.valor_operacion, \n" +
                "CASE a.operacion when 'Suma' then '+' when 'Resta' then '-' when 'Multiplica' then '*' when 'Divida' then '/' END as Operacion\n" +
                "FROM PRECISO.dbo.preciso_validaciones_rc a \n" +
                "inner join PRECISO.dbo.preciso_campos_rc b on a.id_rc = b.id_rc and a.id_campo_referencia=b.id_campo \n" +
                "inner join PRECISO.dbo.preciso_campos_rc c on a.id_rc = c.id_rc and a.id_campo_validacion=c.id_campo \n" +
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

    }*/

    public void validationData(AccountingRoute data) {
        String nombreTabla = "PRECISO_TEMP_CONTABLES";
        Query querySelect = entityManager.createNativeQuery(
                "SELECT b.nombre as referencia, c.nombre as validacion, a.valor_validacion, a.valor_operacion, " +
                        "CASE a.operacion WHEN 'Suma' THEN '+' WHEN 'Resta' THEN '-' WHEN 'Multiplica' THEN '*' WHEN 'Divida' THEN '/' ELSE '' END as Operacion " +
                        "FROM PRECISO.dbo.preciso_validaciones_rc a " +
                        "INNER JOIN PRECISO.dbo.preciso_campos_rc b ON a.id_rc = b.id_rc AND a.id_campo_referencia = b.id_campo " +
                        "INNER JOIN PRECISO.dbo.preciso_campos_rc c ON a.id_rc = c.id_rc AND a.id_campo_validacion = c.id_campo " +
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


    public void copyData(AccountingRoute data,String fecha){
        String nombreTabla = "PRECISO_TEMP_CONTABLES";
        String campos = data.getCampos().stream()
                .map(CampoRC::getNombre)
                .collect(Collectors.joining(","));
        System.out.println("DELETE FROM preciso_rc_" + data.getId() + " WHERE periodo_preciso = '" + fecha + "' ; \n" +
                "INSERT INTO preciso_rc_" + data.getId() + " (" + campos + ",periodo_preciso" +
                ") SELECT " + campos + ",CAST('" + fecha + "' AS DATE) FROM " + nombreTabla);

        Query querySelect = entityManager.createNativeQuery("DELETE FROM preciso_rc_"+data.getId()+" WHERE periodo_preciso = '"+fecha+"' ; \n" +
                "INSERT INTO preciso_rc_"+data.getId()+" ("+campos+",periodo_preciso"+") SELECT "+campos+",CAST('"+fecha+"' AS DATE) FROM "+nombreTabla);
        querySelect.executeUpdate();
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

    public List<Object> findTemporal(){
        List<Object> listTemp = new ArrayList<>();
        try {
            Query querySelect = entityManager.createNativeQuery("SELECT * FROM PRECISO_TEMP_CONTABLES ");
            listTemp = querySelect.getResultList();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return listTemp;
    }

    public void loadLogCargue(User user,AccountingRoute ac, String fecha, String tipo, String estado, String mensaje)
    {
        LocalDate localDate = LocalDate.parse(fecha);
        Date fechaDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<Object> listTemp =findTemporal();
        Date today=new Date();
        LogAccountingLoad insert = new LogAccountingLoad();
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
        insert.setIdRc(ac);
        logAccountingLoadRepository.save(insert);
    }

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Data Quality");
        insert.setFecha(today);
        insert.setInput("Reglas DQ");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

}
