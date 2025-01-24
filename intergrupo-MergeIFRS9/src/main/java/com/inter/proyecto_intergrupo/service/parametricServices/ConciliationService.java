package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.admin.UserConciliationRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ConciliationRepository;
import com.inter.proyecto_intergrupo.repository.parametric.LogAccountingLoadRepository;
import com.inter.proyecto_intergrupo.repository.parametric.LogConciliationRepository;
import com.inter.proyecto_intergrupo.repository.parametric.LogInformationCrossingRepository;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConciliationService {

    @Autowired
    private final ConciliationRepository conciliationRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private UserConciliationRepository userConciliationRepository;

    @Autowired
    private EventTypeService eventTypeService;

    @Autowired
    private UserService userService;

    @Autowired
    private ConciliationRouteService conciliationRouteService;

    @Autowired
    private LogConciliationRepository logConciliationRepository;

    @Autowired
    public ConciliationService(ConciliationRepository conciliationRepository) {
        this.conciliationRepository = conciliationRepository;
    }

    public List <Conciliation> findAll(){return conciliationRepository.findAllByOrderByIdAsc();}
    public List<Conciliation> findAllActive() {
        return conciliationRepository.findByActivo(true);
    }

    public Conciliation findById(int id){
        return conciliationRepository.findAllById(id);
    }

    public Conciliation findByName(String nombre){
        return conciliationRepository.findAllByNombre(nombre);
    }

    public Conciliation modificarConciliacion(Conciliation conciliacion){
        conciliationRepository.save(conciliacion);
       return conciliacion;
    }

    public List<Conciliation> findByFilter(String value, String filter) {
        List<Conciliation> list=new ArrayList<Conciliation>();
        switch (filter) {
            case "Estado":
                Boolean valor = true;
                if ("inactivo".equalsIgnoreCase(value)) {
                    valor = false;
                }
                Query quer = entityManager.createNativeQuery(
                        "SELECT em.* FROM preciso_conciliaciones as em WHERE em.activo = ?", Conciliation.class);
                quer.setParameter(1, valor);
                list = quer.getResultList();
                break;
            case "Nombre":
                String sql = "SELECT em.* FROM preciso_conciliaciones as em WHERE em.nombre LIKE ? ";
                Query query0 = entityManager.createNativeQuery(sql, Conciliation.class);
                String likeValue = "%" + value + "%";
                query0.setParameter(1, likeValue);
                list = query0.getResultList();
                break;
            case "Sistema Fuente":
                String sql1 = "SELECT nv.* FROM preciso_conciliaciones as nv \n" +
                        "left join preciso_sistema_fuente b\n" +
                        "on nv.id_sf = b.id_sf WHERE  b.nombre_sf LIKE ?";
                Query query1 = entityManager.createNativeQuery(sql1, Conciliation.class);
                String likeValue2 = "%" + value + "%";
                query1.setParameter(1, likeValue2);
                list = query1.getResultList();
                break;
            case "Fuente Contable":
                String sql2 = "SELECT nv.* FROM preciso_conciliaciones as nv \n" +
                        "left join preciso_sistema_fuente b\n" +
                        "on nv.id_sfc = b.id_sf WHERE  b.nombre_sf LIKE ?";
                Query query2 = entityManager.createNativeQuery(sql2, Conciliation.class);
                String likeValue3 = "%" + value + "%";
                query2.setParameter(1, likeValue3);
                list = query2.getResultList();
                break;
            default:
                break;
        }

        return list;
    }
/*
    public void clearConciliacion(User user){
        //currencyRepository.deleteAll();
        Query query = entityManager.createNativeQuery("DELETE from preciso_paises", Country.class);
        query.executeUpdate();
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Limpiar tabla Paises");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Tabla Paises");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }
*/


    @Transactional
    public void generarRelacionUserConciliation(String usuario, List<String> titulares, List<String> backups) {
        try {
            User user = userService.findById(Integer.parseInt(usuario));
            userConciliationRepository.deleteByUsuarioId(Integer.parseInt(usuario));
            relacionPorCargo(user, titulares, UserConciliation.RoleConciliation.TITULAR);
            relacionPorCargo(user, backups, UserConciliation.RoleConciliation.BACKUP);
        }catch (Error e){
            e.printStackTrace();
        }

    }

    public void relacionPorCargo(User user, List<String> conciliations, UserConciliation.RoleConciliation role) {
        try {
            for (String concil : conciliations) {
                Conciliation conciliacion = conciliationRepository.findAllById(Integer.valueOf(concil));
                // Obtener las relaciones existentes de la conciliación
                List<UserConciliation> existingRoles = conciliacion.getUserConciliations()
                        .stream()
                        .filter(uc -> uc.getRol() == role)
                        .collect(Collectors.toList());

                // Validar si ya existe un usuario asignado como titular o backup
                if (role == UserConciliation.RoleConciliation.TITULAR && !existingRoles.isEmpty()) {
                    throw new IllegalStateException("La conciliación con ID " + conciliacion.getId() + " ya tiene un usuario titular asignado.");
                }

                if (role == UserConciliation.RoleConciliation.BACKUP && !existingRoles.isEmpty()) {
                    throw new IllegalStateException("La conciliación con ID " + conciliacion.getId() + " ya tiene un usuario backup asignado.");
                }

                // Crear la nueva relación usuario-conciliación
                UserConciliation userConciliation = UserConciliation.builder()
                        .usuario(user)
                        .conciliacion(conciliacion)
                        .rol(role)
                        .build();

                // Agregar la relación en ambas entidades
                conciliacion.getUserConciliations().add(userConciliation);
                user.getUserConciliations().add(userConciliation);

                // Guardar la relación en la base de datos
                userConciliationRepository.save(userConciliation);
                conciliationRepository.save(conciliacion);
            }
        }catch (Error e){
            e.printStackTrace();
        }

    }

    public static Map<String, String> separarCuentas(List<AccountConcil> cuentas) {
        List<String> valoresExactos = new ArrayList<>();
        List<String> patronesLike = new ArrayList<>();

        for (int i = 0; i < cuentas.size(); i++) {

            if (cuentas.get(i).getValor().contains("%"))
                patronesLike.add(cuentas.get(i).getValor());
            else
                valoresExactos.add(cuentas.get(i).getValor());
        }

        // Construir las cadenas formateadas
        String valoresExactosStr = String.join(", ", valoresExactos);
        String patronesLikeStr = String.join(", ", patronesLike);

        Map<String, String> resultado = new HashMap<>();
        resultado.put("valoresExactos", valoresExactosStr);
        resultado.put("patronesLike", patronesLikeStr);

        return resultado;
    }

    private String construirCondicionSQL(String campo, Map<String, String> resultado) {
        List<String> condiciones = new ArrayList<>();

        // Agregar valores exactos si existen (asegurando comillas simples)
        if (!resultado.get("valoresExactos").isEmpty()) {
            String[] valoresExactosArray = resultado.get("valoresExactos").split(", ");
            List<String> valoresExactosConComillas = new ArrayList<>();
            for (String valor : valoresExactosArray) {
                valoresExactosConComillas.add("'" + valor + "'");
            }
            condiciones.add("CAST([" + campo + "] AS BIGINT) IN (" + String.join(", ", valoresExactosConComillas) + ")");
        }

        // Agregar patrones LIKE si existen (asegurando comillas simples)
        if (!resultado.get("patronesLike").isEmpty()) {
            String[] patrones = resultado.get("patronesLike").split(", ");
            List<String> likes = new ArrayList<>();
            for (String patron : patrones) {
                likes.add("[" + campo + "] LIKE '" + patron + "'");
            }
            condiciones.add(String.join(" OR ", likes));
        }

        // Unir ambas condiciones con OR si existen ambas
        return String.join(" OR ", condiciones);
    }



    public void generarConciliacion(Conciliation concil, String fecha, String fechaCont, int idCont) {
        String campoCentro = concil.getCentro();
        String campoCuenta = concil.getCuenta();
        String campoDivisa = concil.getDivisa();
        String campoSaldo = concil.getSaldo();

        String nombreTablaContable = "preciso_rc_" + idCont;
        String nombreTablaConciliacion = "preciso_ci_" + concil.getId();

        List<AccountConcil> cuentas = concil.getArregloCuentas();

        // Separar valores exactos y patrones
        Map<String, String> resultado = separarCuentas(cuentas);

        System.out.println("Valores exactos: " + resultado.get("valoresExactos"));
        System.out.println("Patrones: " + resultado.get("patronesLike"));

        // Construcción de las condiciones dinámicas
        String condicionCuentaConcil = construirCondicionSQL("CUENTA_CONTABLE", resultado);
        String condicionCuentaContable = construirCondicionSQL(campoCuenta, resultado);

        // Construcción de la consulta
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT \n")
                .append("t1.FECHA, t1.CENTRO_CONTABLE, t1.CUENTA_CONTABLE, t1.DIVISA_CUENTA, " +
                        "t1.TOTAL_VALOR_CUENTA AS total_valor_cuenta1, " +
                        "COALESCE(t2.TOTAL_VALOR_CUENTA,0) AS total_valor_cuenta2, " +
                        "t1.TOTAL_VALOR_CUENTA - COALESCE(t2.TOTAL_VALOR_CUENTA,0) AS TOTAL\n")
                .append("FROM \n")
                .append("(SELECT [FECHA_CONCILIACION] AS FECHA, [CENTRO_CONTABLE], [CUENTA_CONTABLE], DIVISA_CUENTA, SUM([TOTAL_VALOR_CUENTA]) AS TOTAL_VALOR_CUENTA\n")
                .append("FROM [" + nombreTablaConciliacion + "]\n")
                .append("WHERE [FECHA_CONCILIACION] = '" + fecha + "' AND (" + condicionCuentaConcil + ")\n")
                .append("GROUP BY [FECHA_CONCILIACION], [CENTRO_CONTABLE], [CUENTA_CONTABLE], [DIVISA_CUENTA]\n")
                .append(") t1\n")
                .append("LEFT JOIN\n")
                .append("(SELECT periodo_preciso AS FECHA, [" + campoCentro + "] AS CENTRO_CONTABLE, [" + campoCuenta + "] AS CUENTA_CONTABLE, [" +
                        campoDivisa + "] AS DIVISA_CUENTA, SUM(TRY_CAST([" + campoSaldo + "] AS DECIMAL(18, 2))) AS TOTAL_VALOR_CUENTA\n")
                .append("FROM [" + nombreTablaContable + "]\n")
                .append("WHERE periodo_preciso = '" + fechaCont + "' AND (" + condicionCuentaContable + ")\n")
                .append("GROUP BY periodo_preciso, [" + campoCentro + "], [" + campoCuenta + "], [" + campoDivisa + "]\n")
                .append(") t2\n")
                .append("ON  t1.FECHA = t2.FECHA AND t1.CENTRO_CONTABLE = t2.CENTRO_CONTABLE AND CAST(t1.CUENTA_CONTABLE AS BIGINT) = CAST(t2.CUENTA_CONTABLE AS BIGINT)AND t1.DIVISA_CUENTA = t2.DIVISA_CUENTA");

        // HACER LA SELECCION
        Query querySelect = entityManager.createNativeQuery(queryBuilder.toString());
        List<Object[]> resultados = querySelect.getResultList();
        llenadoConciliacion(concil, resultados, fecha);
    }


    public void llenadoConciliacion(Conciliation concil, List<Object[]> resultados, String fecha){

        // CREAR LA TABLA PARA EL RESUMEN DE CONCILIACION
        //------------------------------------------------------------
        StringBuilder queryBuilder = new StringBuilder();
        String nombreTablaConciliacion = "preciso_conciliacion_" + concil.getId();

        queryBuilder = new StringBuilder();
        queryBuilder.setLength(0);

        queryBuilder.append("IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = '").append(nombreTablaConciliacion).append("') ")
                .append("BEGIN ")
                .append("CREATE TABLE ").append(nombreTablaConciliacion).append(" (")
                .append("FECHA VARCHAR(50), CENTRO_CONTABLE VARCHAR(50), CUENTA_CONTABLE VARCHAR(50), DIVISA_CUENTA VARCHAR(50), ")
                .append("total_valor_cuenta1 DECIMAL(18,2), total_valor_cuenta2 DECIMAL(18,2), TOTAL DECIMAL(18,2)); ")
                .append("END;");


        // Ejecutar la consulta para crear la tabla si no existe
        Query queryCreateTable = entityManager.createNativeQuery(queryBuilder.toString());
        queryCreateTable.executeUpdate();  // Esto crea la tabla si no existe

        // Eliminar los registros que tienen la misma FECHA_CONCILIACION y el mismo INVENTARIO
        queryBuilder = new StringBuilder();
        queryBuilder.append("DELETE FROM ").append(nombreTablaConciliacion)
                .append(" WHERE FECHA = '").append(fecha).append("';");

        // Ejecutar la consulta de eliminación
        Query queryDelete = entityManager.createNativeQuery(queryBuilder.toString());
        queryDelete.executeUpdate();  // Esto elimina los registros


        // Insertar los resultados en la tabla de conciliación
        queryBuilder = new StringBuilder();
        queryBuilder.setLength(0);
        queryBuilder.append("INSERT INTO ").append(nombreTablaConciliacion).append(" (FECHA, CENTRO_CONTABLE, CUENTA_CONTABLE, DIVISA_CUENTA, total_valor_cuenta1, total_valor_cuenta2, TOTAL) VALUES ");

        for (int i = 0; i < resultados.size(); i++) {
            Object[] resultado = resultados.get(i);
            queryBuilder.append("(")
                    .append("'").append(resultado[0]).append("', ")  // FECHA
                    .append("'").append(resultado[1]).append("', ")  // CENTRO_CONTABLE
                    .append("'").append(resultado[2]).append("', ")  // CUENTA_CONTABLE
                    .append("'").append(resultado[3]).append("', ")  // DIVISA_CUENTA
                    .append(resultado[4]).append(", ")               // total_valor_cuenta1
                    .append(resultado[5]).append(", ")               // total_valor_cuenta2
                    .append(resultado[6]);                           // TOTAL

            if (i < resultados.size() - 1) {
                queryBuilder.append("), ");
            } else {
                queryBuilder.append(");");
            }
        }

        // Ejecutar la consulta de inserción
        Query queryInsert = entityManager.createNativeQuery(queryBuilder.toString());
        queryInsert.executeUpdate();  // Esto inserta los registros en la tabla
    }

    public List<Object[]> generarTablaCruceCompleto_x_Conciliacion(int concilId, String fecha, int tipoEventoId) {
        Conciliation conciliacion = findById(concilId);
        EventType tipoEvento = eventTypeService.findAllById(tipoEventoId);
        List<ConciliationRoute> listRoutes = conciliationRouteService.getRoutesByConciliation(concilId); // Obtener las rutas
        StringBuilder queryBuilder = new StringBuilder();

        // Nombre de la tabla que se va a crear dinámicamente
        String nombreTabla = "preciso_ci_" + concilId;

        // Crear la tabla solo si no existe
        queryBuilder.append("IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = '").append(nombreTabla).append("') ")
                .append("BEGIN ")
                .append("CREATE TABLE ").append(nombreTabla).append(" (")
                .append("INVENTARIO VARCHAR(50), ")
                .append("ID_INVENTARIO INT, ")
                .append("FECHA_CONCILIACION DATE, ")
                .append("TIPO_EVENTO VARCHAR(50), ")
                .append("CDGO_MATRIZ_EVENTO VARCHAR(50), ")
                .append("CENTRO_CONTABLE VARCHAR(50), ")
                .append("CUENTA_CONTABLE VARCHAR(50), ")
                .append("DIVISA_CUENTA VARCHAR(50), ")
                .append("TOTAL_VALOR_CUENTA DECIMAL(18,2)); ")
                .append("END;");

        // Ejecutar la consulta para crear la tabla si no existe
        Query queryCreateTable = entityManager.createNativeQuery(queryBuilder.toString());
        queryCreateTable.executeUpdate();  // Esto crea la tabla si no existe

        // Limpiar el StringBuilder para la siguiente parte de la consulta
        queryBuilder.setLength(0);

        // Eliminar los registros que tienen la misma FECHA_CONCILIACION y el mismo INVENTARIO
        queryBuilder.append("DELETE FROM ").append(nombreTabla)
                .append(" WHERE FECHA_CONCILIACION = '").append(fecha).append("' ")
                .append("AND INVENTARIO = '").append(conciliacion.getNombre()).append("' ")
                .append("AND TIPO_EVENTO = '").append(tipoEvento.getNombre()).append("';");

        // Ejecutar la consulta de eliminación
        Query queryDelete = entityManager.createNativeQuery(queryBuilder.toString());
        queryDelete.executeUpdate();  // Esto elimina los registros

        // Limpiar el StringBuilder para la siguiente parte de la consulta
        queryBuilder.setLength(0);

        // Recorremos las rutas para construir las consultas de inserción
        for (int i = 0; i < listRoutes.size(); i++) {
            ConciliationRoute ruta = listRoutes.get(i);
            String nombreTablaRuta = "preciso_ci_" + concilId + "_" + ruta.getId();

            // Construir la consulta de inserción para la ruta actual (para CUENTA_CONTABLE_1 y CUENTA_CONTABLE_2)
            queryBuilder.append("INSERT INTO ").append(nombreTabla).append(" (INVENTARIO, ID_INVENTARIO, FECHA_CONCILIACION, CENTRO_CONTABLE, TIPO_EVENTO, CDGO_MATRIZ_EVENTO, CUENTA_CONTABLE, DIVISA_CUENTA, TOTAL_VALOR_CUENTA) ")
                    .append("SELECT INVENTARIO, ID_INVENTARIO, FECHA_CONCILIACION, CENTRO_CONTABLE, TIPO_EVENTO, CDGO_MATRIZ_EVENTO, CUENTA_CONTABLE_1 AS CUENTA_CONTABLE, ")
                    .append("DIVISA_CUENTA_1 AS DIVISA_CUENTA, SUM(VALOR_CUENTA_1) AS TOTAL_VALOR_CUENTA ")
                    .append("FROM ").append(nombreTablaRuta).append(" ")
                    .append("WHERE FECHA_CONCILIACION = '").append(fecha).append("' ")
                    .append("GROUP BY INVENTARIO, ID_INVENTARIO, FECHA_CONCILIACION, CENTRO_CONTABLE, TIPO_EVENTO, CDGO_MATRIZ_EVENTO, CUENTA_CONTABLE_1, DIVISA_CUENTA_1; ");

            queryBuilder.append("INSERT INTO ").append(nombreTabla).append(" (INVENTARIO, ID_INVENTARIO, FECHA_CONCILIACION, CENTRO_CONTABLE, TIPO_EVENTO, CDGO_MATRIZ_EVENTO, CUENTA_CONTABLE, DIVISA_CUENTA, TOTAL_VALOR_CUENTA) ")
                    .append("SELECT INVENTARIO, ID_INVENTARIO, FECHA_CONCILIACION, CENTRO_CONTABLE, TIPO_EVENTO, CDGO_MATRIZ_EVENTO, CUENTA_CONTABLE_2 AS CUENTA_CONTABLE, ")
                    .append("DIVISA_CUENTA_2 AS DIVISA_CUENTA, SUM(VALOR_CUENTA_2) AS TOTAL_VALOR_CUENTA ")
                    .append("FROM ").append(nombreTablaRuta).append(" ")
                    .append("WHERE FECHA_CONCILIACION = '").append(fecha).append("' ")
                    .append("GROUP BY INVENTARIO, ID_INVENTARIO, FECHA_CONCILIACION, CENTRO_CONTABLE, TIPO_EVENTO, CDGO_MATRIZ_EVENTO, CUENTA_CONTABLE_2, DIVISA_CUENTA_2; ");

            // Ejecutar la consulta de inserción
            Query queryInsert = entityManager.createNativeQuery(queryBuilder.toString());
            queryInsert.executeUpdate();  // Inserta los datos en la tabla

            // Limpiar el StringBuilder para la siguiente iteración
            queryBuilder.setLength(0);
        }

        // Consultar los datos finales ordenados
        String finalQuery = "SELECT * FROM " + nombreTabla + " ORDER BY INVENTARIO, ID_INVENTARIO, FECHA_CONCILIACION, CENTRO_CONTABLE, TIPO_EVENTO, CDGO_MATRIZ_EVENTO, CUENTA_CONTABLE, DIVISA_CUENTA;";
        Query querySelect = entityManager.createNativeQuery(finalQuery);

        // Devolver los resultados (deberías ajustar según tu necesidad)
        return querySelect.getResultList();
    }

    public List<Object[]> findAllData(Conciliation concil, String fecha) {

        try {
            // Construir la consulta básica
            StringBuilder queryBuilder = new StringBuilder("IF EXISTS (SELECT 1 " +
                    "FROM INFORMATION_SCHEMA.TABLES " +
                    "WHERE TABLE_NAME = 'preciso_conciliacion_"+concil.getId()+"') " +
                    "BEGIN " +
                    "SELECT [FECHA], [CENTRO_CONTABLE], [CUENTA_CONTABLE], " +
                    "[DIVISA_CUENTA], [total_valor_cuenta1], [total_valor_cuenta2], [TOTAL] " +
                    "FROM preciso_conciliacion_" + concil.getId() + " WHERE FECHA = :fecha " +
                    "ORDER BY [FECHA], [CENTRO_CONTABLE], [CUENTA_CONTABLE], [DIVISA_CUENTA]" +
                    "END\n" +
                    "ELSE\n" +
                    "BEGIN\n" +
                    "    SELECT NULL AS FECHA_CONCILIACION \n" +
                    "    WHERE 1 = 0; \n" +
                    "END");

            // Crear la consulta
            Query querySelect = entityManager.createNativeQuery(queryBuilder.toString());
            querySelect.setParameter("fecha", fecha);

            return querySelect.getResultList();
        }
        catch (Exception e)
        {
            return new ArrayList<>();
        }
    }

    public List<Object[]> processList(List<Object[]> datos, List<String> colAroutes) {
        List<Object[]> processedList = new ArrayList<>();
        for (Object[] row : datos) {
            Object[] processedRow = new Object[row.length];

            for (int i = 0; i < row.length; i++) {
                // Verificar si la columna es "SALDO INVENTARIO"
                if (colAroutes.get(i).equals("SALDO INVENTARIO") || colAroutes.get(i).equals("SALDO CONTABLE") || colAroutes.get(i).equals("TOTAL") ) {
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


    public List<LogConciliation> findAllLog(Conciliation concil, String fecha) {
        LocalDate localDate = LocalDate.parse(fecha);
        Date fechaDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        //return logConciliationRepository.findAllByIdConciliacionAndFechaProceso(concil,fechaDate);
        return logConciliationRepository.findAllByIdConciliacionAndFechaProcesoOrderByIdDesc(concil,fechaDate);
    }

    public void loadLogConciliation(User user,int concil,String fecha, String estado, String mensaje)
    {
        LocalDate localDate = LocalDate.parse(fecha);
        Date fechaDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date today=new Date();
        LogConciliation insert = new LogConciliation();

        insert.setFechaInventario(fechaDate);
        insert.setFechaProceso(fechaDate);

        insert.setFechaPreciso(today);
        if(user!=null)
            insert.setUsuario(user.getUsuario());
        else
            insert.setUsuario("Automático");

        insert.setNovedad(mensaje);
        insert.setEstadoProceso(estado);
        if(user!=null)
            insert.setUsuario(user.getUsuario());
        else
            insert.setUsuario("Automático");

        Conciliation conciliation = findById(concil);
        insert.setIdConciliacion(conciliation);

        logConciliationRepository.save(insert);
    }


    public List<Object[]> findFechaCont(int concilID, String fecha) {
        fecha += "  00:00:00.0000000";
        Query query = entityManager.createNativeQuery(
                "SELECT fecha_cargue_contable, codigo_cargue_contable FROM " +
                        "preciso_maestro_inventarios WHERE codigo_conciliacion = :concilID and fecha_conciliacion = '" + fecha + "'");
        query.setParameter("concilID", concilID);
        return query.getResultList();
    }



}
