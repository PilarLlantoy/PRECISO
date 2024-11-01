package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ConciliationRouteRepository;
import com.inter.proyecto_intergrupo.repository.parametric.LogInventoryLoadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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

    public ConciliationRoute findByName(String nombre){
        return conciliationRouteRepository.findAllByDetalle(nombre);
    }

    public ConciliationRoute modificar(ConciliationRoute croute){
        conciliationRouteRepository.save(croute);
       return croute;
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
                String line = String.format(
                        "%d\tSQLCHAR\t0\t%d\t\"\"\t%d\t%s\tLatin1_General_CI_AS",
                        fieldIndex,
                        Integer.parseInt(campo.getLongitud()),
                        fieldIndex,
                        campo.getNombre()
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

    public String ensureTrailingSlash(String path) {
        if (!path.endsWith("\\")) {
            path += "\\";
        }
        return path;
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

        String fichero=ensureTrailingSlash(data.getRuta()) + data.getNombreArchivo() + todayDateConvert(data.getFormatoFecha(),fecha)+ extension;
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
            case "Estado":
                Boolean valor = true;
                if ("inactivo".equalsIgnoreCase(value)) valor = false;
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
