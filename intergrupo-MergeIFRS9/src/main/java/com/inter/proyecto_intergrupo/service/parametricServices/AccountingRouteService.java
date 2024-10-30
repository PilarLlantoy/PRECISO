package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.AccountingRouteRepository;
import com.inter.proyecto_intergrupo.repository.parametric.LogAccountingLoadRepository;
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
        Query queryDrop = entityManager.createNativeQuery("IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '"+(nombreTabla)+"' AND TABLE_SCHEMA = 'dbo') BEGIN DROP TABLE "+(nombreTabla) +" END;");
        queryDrop.executeUpdate();

        StringBuilder createTableQuery = new StringBuilder("CREATE TABLE ");
        createTableQuery.append(nombreTabla).append(" (");

        for (int i = 0; i < data.getCampos().size(); i++) {
            CampoRC column = data.getCampos().get(i);
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

    public String todayDateConvert(String formato,String fecha) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formato);
        if(fecha.isEmpty()) {
            return today.format(formatter);
        }
        else
        {
            LocalDate fechaCast = LocalDate.parse(fecha);
            return fechaCast.format(formatter);
        }
    }

    /*
    public void bulkImport(AccountingRoute data, String ruta,String fecha, String fuente) throws PersistenceException  {
        String nombreTabla = "PRECISO_TEMP_CONTABLES";
        String extension="";
        String delimitador=data.getDelimitador();

        if(data.getTipoArchivo().equals("XLS") || data.getTipoArchivo().equals("XLSX"))
            delimitador=";";

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

     */

    public void bulkImport(AccountingRoute data, String ruta, String fecha, String fuente) throws PersistenceException {
        String nombreTabla = "PRECISO_TEMP_CONTABLES";
        String extension = "";
        String delimitador = data.getDelimitador();

        if (data.getTipoArchivo().equals("XLS") || data.getTipoArchivo().equals("XLSX")) {
            delimitador = ";";
        }

        String complement = "FIELDTERMINATOR = '" + delimitador + "', ROWTERMINATOR = '\\n', FIRSTROW = " + data.getFilasOmitidas();
        if (data.getTipoArchivo().equals("XLS") || data.getTipoArchivo().equals("XLSX") || data.getTipoArchivo().equals("CSV") || data.getTipoArchivo().equals("TXT")) {
            extension = "." + data.getTipoArchivo();
        }
        if (delimitador.equalsIgnoreCase("")) {
            complement = "FORMATFILE = '" + ruta + "', ROWTERMINATOR = '\\r\\n', FIRSTROW = " + data.getFilasOmitidas();
        }

        String fichero = ensureTrailingSlash(data.getRuta()) + data.getNombreArchivo() + todayDateConvert(data.getFormatoFecha(), fecha) + data.getComplementoArchivo() + extension;
        if (fuente != null) {
            fichero = fuente;
        }

        Integer longitud = 0;
        for (CampoRC cam : data.getCampos()) {
            longitud += Integer.valueOf(cam.getLongitud());
        }
        if(delimitador ==";") longitud+=data.getCampos().size()-1;

        List<String> validLines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(fichero))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String truncatedLine = line.length() > longitud ? line.substring(0, longitud) : line;
                boolean validLine = true;
                int start = 0;
                for (CampoRC cam : data.getCampos()) {

                    int length = Integer.valueOf(cam.getLongitud());
                    String segment = truncatedLine.length() >= start + length ? truncatedLine.substring(start, start + length) : truncatedLine.substring(start);
                    boolean valido = isValidField(segment, cam.getTipo());

                    if (!valido) {
                        validLine = false;
                        break;
                    }
                    start += length;
                }

                if (validLine) {
                    validLines.add(truncatedLine);
                }
            }
        } catch (IOException e) {
            throw new PersistenceException("Error al leer el archivo: " + e.getMessage(), e);
        }

        String archivoTemporal = "D:\\archivo_temporal.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivoTemporal))) {
            for (String validLine : validLines) {
                writer.write(validLine);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new PersistenceException("Error al escribir el archivo temporal: " + e.getMessage(), e);
        }

        Query queryBulk = entityManager.createNativeQuery("BULK INSERT " + nombreTabla +
                " FROM '" + archivoTemporal +
                "' WITH (" + complement + ")");
        queryBulk.executeUpdate();
    }

    private boolean isValidField(String field, String tipoDato) {
        try {
            switch (tipoDato.toLowerCase()) {
                case "integer":
                    try {
                        // Primero intentamos como Integer
                        Integer.parseInt(field);
                    } catch (NumberFormatException e) {
                        // Si falla, intentamos como Long
                        Long.parseLong(field);
                    }
                    break;
                case "bigint":
                    Long.parseLong(field.trim());
                    break;
                case "float":
                    Float.parseFloat(field.trim());
                    break;
                case "varchar":
                    break;
                case "date":
                    java.sql.Date.valueOf(field.trim());
                    break;
                case "time":
                    java.sql.Time.valueOf(field.trim());
                    break;
                case "datetime":
                    java.sql.Timestamp.valueOf(field.trim());
                    break;
                case "bit":
                    if (!field.trim().equals("0") && !field.trim().equals("1")) {
                        throw new NumberFormatException();
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Tipo de dato no soportado: " + tipoDato);
            }
        } catch (Exception e) {
            return false;
        }
        return true;
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
                Query deleteSelect = entityManager.createNativeQuery("UPDATE "+nombreTabla+" SET "+obj[0].toString()+" = "+obj[0].toString()+obj[4].toString()+obj[3].toString()+" WHERE "+obj[1].toString()+"='"+obj[2].toString()+"';");
                deleteSelect.executeUpdate();
            }
        }

    }
    */
    public void validationData(AccountingRoute data) {

        // Obtener el valor actual del campo para la conversión
        // Paso 1: Obtener todos los valores que necesitan ser convertidos
        String sqlObtenerValores0 = "SELECT * FROM PRECISO_TEMP_CONTABLES";
        Query obtenerValoresQuery0 = entityManager.createNativeQuery(sqlObtenerValores0);
        List<Object[]> resultados0 = obtenerValoresQuery0.getResultList();


        String nombreTabla = "PRECISO_TEMP_CONTABLES";
        Query querySelect = entityManager.createNativeQuery("SELECT b.nombre as referencia, c.nombre as validacion, a.valor_validacion, a.valor_operacion, \n" +
                "CASE a.operacion when 'Suma' then '+' when 'Resta' then '-' when 'Multiplica' then '*' when 'Divida' then '/' END as Operacion\n" +
                "FROM PRECISO.dbo.preciso_validaciones_rc a \n" +
                "inner join PRECISO.dbo.preciso_campos_rc b on a.id_rc = b.id_rc and a.id_campo_referencia=b.id_campo \n" +
                "inner join PRECISO.dbo.preciso_campos_rc c on a.id_rc = c.id_rc and a.id_campo_validacion=c.id_campo \n" +
                "where a.id_rc = ? and a.estado=1");
        querySelect.setParameter(1, data.getId());
        List<Object[]> validacionLista = querySelect.getResultList();

        List<CampoRC> campos = data.getCampos();

        if (!validacionLista.isEmpty()) {
            for (Object[] obj : validacionLista) {

                String referencia = obj[0].toString(); // Nombre del campo de referencia
                String validacion = obj[1].toString(); // Nombre del campo de validación
                String valorValidacion = obj[2].toString(); // Valor de validación
                String valorOperacion = obj[3].toString(); // Valor de la operación
                String operacion = obj[4].toString(); // Operación (+, -, *, /)

                // Encontrar el tipo de dato del campo 'referencia'
                Optional<CampoRC> campoReferenciaOpt = campos.stream()
                        .filter(campo -> campo.getNombre().equals(referencia))
                        .findFirst();

                // Paso 1: Definir la consulta para modificar la columna `Valor` a VARCHAR(MAX)
                String sqlAlterTable = "ALTER TABLE PRECISO_TEMP_CONTABLES ALTER COLUMN "+referencia+" "+campoReferenciaOpt.get().getTipo()+"(MAX)";
                Query alterTableQuery = entityManager.createNativeQuery(sqlAlterTable);
                alterTableQuery.executeUpdate();

                // Paso 2: Definir la consulta para modificar la columna `Valor` a VARCHAR(MAX) para preciso_rc_id
                String sqlAlterTableRC = "ALTER TABLE preciso_rc_"+data.getId()+" ALTER COLUMN "+referencia+" "+campoReferenciaOpt.get().getTipo()+"(MAX)";
                Query alterTableQueryRC = entityManager.createNativeQuery(sqlAlterTableRC);
                alterTableQueryRC.executeUpdate();

                if (campoReferenciaOpt.isPresent()) {
                    String tipoCampo = campoReferenciaOpt.get().getTipo(); // Obtiene el tipo del campo
                    String sqlUpdate;

                    // Crear la consulta SQL según el tipo de dato
                    if ("VARCHAR".equalsIgnoreCase(tipoCampo)) {
                        // Para valores varchar, convierte temporalmente a double, realiza la operación, y convierte de nuevo a varchar
                        sqlUpdate = "UPDATE " + nombreTabla +
                                " SET " + referencia + " = CAST(TRY_CAST(" + referencia + " AS DECIMAL(38, 0)) " +
                                operacion + " " + valorOperacion + " AS VARCHAR) " +
                                " WHERE " + validacion + " = '" + valorValidacion + "';";
                    } else {
                        // Para valores numéricos (double, integer), realiza la operación directamente
                        sqlUpdate = "UPDATE " + nombreTabla +
                                " SET " + referencia + " = " + referencia + operacion + valorOperacion +
                                " WHERE " + validacion + " = '" + valorValidacion + "';";
                    }

                    // Ejecutar la consulta de actualización
                    Query deleteSelect = entityManager.createNativeQuery(sqlUpdate);
                    deleteSelect.executeUpdate();

                } else {
                    // Manejar el caso en que no se encuentra el campo
                    System.out.println("Campo " + referencia + " no encontrado en la lista de campos.");
                }


            }
        }
    }

    private String convertirANumeroCompleto(String valor) {
        try {
            // Convertir el valor a BigDecimal para eliminar la notación científica
            BigDecimal numeroCompleto = new BigDecimal(valor);
            return numeroCompleto.toPlainString(); // Retorna la representación en cadena sin notación científica
        } catch (NumberFormatException e) {
            // Manejar el caso donde la conversión falla
            System.err.println("Error al convertir el valor: " + valor);
            return valor; // Retorna el valor original en caso de error
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
            writer.write(campos.size()+1 + "\n"); // Número de campos

            int fieldIndex = 1;
            for (int i = 0; i < campos.size();i++) {
                // Cada línea sigue la estructura:
                // <FieldID> SQLCHAR 0 <LongitudCampo> "" <IndexCampo> <NombreCampo> Latin1_General_CI_AS
                String line = String.format(
                        "%d\tSQLCHAR\t0\t%d\t\"\"\t%d\t%s\tLatin1_General_CI_AS",
                        fieldIndex,
                        Integer.parseInt(campos.get(i).getLongitud()),
                        fieldIndex,
                        campos.get(i).getNombre()
                );

                // Añadir salto de línea
                writer.write(line + "\n");

                fieldIndex++;
            }

            // Si necesitas manejar el último campo con terminador de línea (por ejemplo, "\r\n")
            String ultimaLinea = String.format(
                    "%d\tSQLCHAR\t0\t%d\t\"\\r\\n\"\t%d\t%s\tLatin1_General_CI_AS",
                    fieldIndex,
                    Integer.parseInt(campos.get(campos.size() - 1).getLongitud()),
                    fieldIndex,
                    campos.get(campos.size() - 1).getNombre()
            );
            writer.write(ultimaLinea + "\n");

        } catch (IOException e) {
            throw new IOException("Error al generar el archivo de formato.", e);
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
