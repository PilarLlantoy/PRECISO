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
                valoresExactosConComillas.add("'" + valor.replace(" ","").replace("\t","") + "'");
            }
            condiciones.add("CAST([" + campo + "] AS BIGINT) IN (" + String.join(", ", valoresExactosConComillas) + ")");
        }

        // Agregar patrones LIKE si existen (asegurando comillas simples)
        if (!resultado.get("patronesLike").isEmpty()) {
            String[] patrones = resultado.get("patronesLike").split(", ");
            List<String> likes = new ArrayList<>();
            for (String patron : patrones) {
                likes.add("[" + campo + "] LIKE '" + patron.replace(" ","") + "'");
            }
            condiciones.add(String.join(" OR ", likes));
        }

        // Unir ambas condiciones con OR si existen ambas
        return String.join(" OR ", condiciones);
    }



    public boolean generarConciliacion(Conciliation concil, String fecha, String fechaCont, int idCont) {
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
        String condicionCuentaConcil = construirCondicionSQL("CUENTA_CONTABLE_PRECISOKEY", resultado);
        String condicionCuentaContable = construirCondicionSQL(campoCuenta, resultado);

        if(condicionCuentaContable.isEmpty() || condicionCuentaConcil.isEmpty())
            return false;

        // Construcción de la consulta
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT \n")
                .append("t1.FECHA_PRECISOKEY, t1.CENTRO_CONTABLE_PRECISOKEY, t1.CUENTA_CONTABLE_PRECISOKEY, t1.DIVISA_CUENTA_PRECISOKEY, " +
                        "t1.TOTAL_VALOR_CUENTA_PRECISOKEY AS TOTAL_VALOR_CUENTA1_PRECISOKEY, " +
                        "COALESCE(t2.TOTAL_VALOR_CUENTA,0) AS TOTAL_VALOR_CUENTA2_PRECISOKEY, " +
                        "t1.TOTAL_VALOR_CUENTA_PRECISOKEY - COALESCE(t2.TOTAL_VALOR_CUENTA,0) AS TOTAL_PRECISOKEY\n")
                .append("FROM \n")
                .append("(SELECT [FECHA_CONCILIACION_PRECISOKEY] AS FECHA_PRECISOKEY, [CENTRO_CONTABLE_PRECISOKEY], [CUENTA_CONTABLE_PRECISOKEY], DIVISA_CUENTA_PRECISOKEY, SUM([TOTAL_VALOR_CUENTA_PRECISOKEY]) AS TOTAL_VALOR_CUENTA_PRECISOKEY\n")
                .append("FROM [" + nombreTablaConciliacion + "]\n")
                .append("WHERE [FECHA_CONCILIACION_PRECISOKEY] = '" + fecha + "' AND (" + condicionCuentaConcil + ")\n")
                .append("GROUP BY [FECHA_CONCILIACION_PRECISOKEY], [CENTRO_CONTABLE_PRECISOKEY], [CUENTA_CONTABLE_PRECISOKEY], [DIVISA_CUENTA_PRECISOKEY]\n")
                .append(") t1\n")
                .append("LEFT JOIN\n")
                .append("(SELECT periodo_preciso AS FECHA, [" + campoCentro + "] AS CENTRO_CONTABLE, [" + campoCuenta + "] AS CUENTA_CONTABLE, [" +
                        campoDivisa + "] AS DIVISA_CUENTA, SUM(TRY_CAST([" + campoSaldo + "] AS DECIMAL(18, 2))) AS TOTAL_VALOR_CUENTA\n")
                .append("FROM [" + nombreTablaContable + "]\n")
                .append("WHERE periodo_preciso = '" + fechaCont + "' AND (" + condicionCuentaContable + ")\n")
                .append("GROUP BY periodo_preciso, [" + campoCentro + "], [" + campoCuenta + "], [" + campoDivisa + "]\n")
                .append(") t2\n")
                .append("ON  t1.FECHA_PRECISOKEY = t2.FECHA AND t1.CENTRO_CONTABLE_PRECISOKEY = t2.CENTRO_CONTABLE AND CAST(t1.CUENTA_CONTABLE_PRECISOKEY AS BIGINT) = CAST(t2.CUENTA_CONTABLE AS BIGINT)AND t1.DIVISA_CUENTA_PRECISOKEY = t2.DIVISA_CUENTA");

        // HACER LA SELECCION
        Query querySelect = entityManager.createNativeQuery(queryBuilder.toString());
        List<Object[]> resultados = querySelect.getResultList();
        if(resultados.isEmpty())
            return false;
        llenadoConciliacion(concil, resultados, fecha);
        return true;
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

    public void generarTablaNovedades(List<ConciliationRoute> listRoutes, String fecha, EventType tipoEvento) {
        for (ConciliationRoute route:listRoutes){
            String tableUpdate = "preciso_ci_"+route.getConciliacion().getId()+"_"+route.getId();
            StringBuilder dataCampo = new StringBuilder();
            for (CampoRConcil campo:route.getCampos())
            {
                dataCampo.append("t2."+campo.getNombre()).append(" = t."+campo.getNombre()).append(" AND\n");
            }
            Query queryGenerateIncidents = entityManager.createNativeQuery("UPDATE t SET t.NOVEDADES_PRECISOKEY = \n" +
                    "(CASE WHEN coalesce(CUENTA_CONTABLE_1_PRECISOKEY,CUENTA_CONTABLE_2_PRECISOKEY) IS NULL THEN 'A' ELSE '' END) \n" +
                    /*"+(CASE WHEN EXISTS (SELECT 1 FROM "+tableUpdate+" t2 \n" +
                    "\tWHERE "+dataCampo+" t2.INVENTARIO_PRECISOKEY = t.INVENTARIO_PRECISOKEY \n" +
                    "\tAND t2.TIPO_EVENTO_PRECISOKEY = t.TIPO_EVENTO_PRECISOKEY \n" +
                    "\tAND t2.FECHA_CONCILIACION_PRECISOKEY = t.FECHA_CONCILIACION_PRECISOKEY \n" +
                    "\tAND t2.CDGO_MATRIZ_EVENTO_PRECISOKEY = t.CDGO_MATRIZ_EVENTO_PRECISOKEY \n" +
                    "\tAND t2.CENTRO_CONTABLE_PRECISOKEY = t.CENTRO_CONTABLE_PRECISOKEY \n" +
                    "\tAND t2.CUENTA_CONTABLE_1_PRECISOKEY = t.CUENTA_CONTABLE_1_PRECISOKEY \n" +
                    "\tAND t2.DIVISA_CUENTA_1_PRECISOKEY = t.DIVISA_CUENTA_1_PRECISOKEY \n" +
                    "\tAND t2.VALOR_CUENTA_1_PRECISOKEY = t.VALOR_CUENTA_1_PRECISOKEY \n" +
                    "\tAND t2.CUENTA_CONTABLE_2_PRECISOKEY = t.CUENTA_CONTABLE_2_PRECISOKEY \n" +
                    "\tAND t2.DIVISA_CUENTA_2_PRECISOKEY = t.DIVISA_CUENTA_2_PRECISOKEY \n" +
                    "\tAND t2.VALOR_CUENTA_2_PRECISOKEY = t.VALOR_CUENTA_2_PRECISOKEY \n" +
                    "\tAND t2.FECHA_CONCILIACION_PRECISOKEY like :fecha and t2.TIPO_EVENTO_PRECISOKEY = :evento \n" +
                    "\tGROUP BY t2.INVENTARIO_PRECISOKEY, t2.FECHA_CONCILIACION_PRECISOKEY, t2.TIPO_EVENTO_PRECISOKEY, t2.CDGO_MATRIZ_EVENTO_PRECISOKEY, t2.CENTRO_CONTABLE_PRECISOKEY, t2.CUENTA_CONTABLE_1_PRECISOKEY,t2.DIVISA_CUENTA_1_PRECISOKEY,t2.VALOR_CUENTA_1_PRECISOKEY\n" +
                    "\t,t2.CUENTA_CONTABLE_2_PRECISOKEY,t2.DIVISA_CUENTA_2_PRECISOKEY,t2.VALOR_CUENTA_2_PRECISOKEY HAVING COUNT(*) > 1) THEN 'D' ELSE '' END)\n" +*/
                    "FROM "+tableUpdate+" t WHERE t.FECHA_CONCILIACION_PRECISOKEY like :fecha and t.TIPO_EVENTO_PRECISOKEY = :evento ;");
            queryGenerateIncidents.setParameter("fecha",fecha+"%");
            queryGenerateIncidents.setParameter("evento",tipoEvento.getNombre());
            queryGenerateIncidents.executeUpdate();
        }
    }

    public List<Object[]> generarTablaCruceCompleto_x_Conciliacion(int concilId, String fecha, int tipoEventoId) {
        System.out.println("ESTAMOS RECREANDO LA TABLA PARA LA COMBINACION EN EL CRUCE");
        Conciliation conciliacion = findById(concilId);
        EventType tipoEvento = eventTypeService.findAllById(tipoEventoId);
        List<ConciliationRoute> listRoutes = conciliationRouteService.getRoutesByConciliation(concilId); // Obtener las rutas
        StringBuilder queryBuilder = new StringBuilder();

        // Nombre de la tabla que se va a crear dinámicamente
        System.out.println(concilId);
        String nombreTabla = "preciso_ci_" + concilId;

        // Crear la tabla solo si no existe
        queryBuilder.append("IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = '").append(nombreTabla).append("') ")
                .append("BEGIN ")
                .append("CREATE TABLE ").append(nombreTabla).append(" (")
                .append("INVENTARIO_PRECISOKEY VARCHAR(50), ")
                .append("ID_INVENTARIO_PRECISOKEY INT, ")
                .append("FECHA_CONCILIACION_PRECISOKEY DATE, ")
                .append("TIPO_EVENTO_PRECISOKEY VARCHAR(50), ")
                .append("CDGO_MATRIZ_EVENTO_PRECISOKEY VARCHAR(50), ")
                .append("CENTRO_CONTABLE_PRECISOKEY VARCHAR(50), ")
                .append("CUENTA_CONTABLE_PRECISOKEY VARCHAR(50), ")
                .append("DIVISA_CUENTA_PRECISOKEY VARCHAR(50), ")
                .append("TOTAL_VALOR_CUENTA_PRECISOKEY DECIMAL(18,2)); ")
                .append("END;");

        // Ejecutar la consulta para crear la tabla si no existe
        Query queryCreateTable = entityManager.createNativeQuery(queryBuilder.toString());
        queryCreateTable.executeUpdate();  // Esto crea la tabla si no existe

        // Limpiar el StringBuilder para la siguiente parte de la consulta
        queryBuilder.setLength(0);

        // Eliminar los registros que tienen la misma FECHA_CONCILIACION y el mismo INVENTARIO
        queryBuilder.append("DELETE FROM ").append(nombreTabla)
                .append(" WHERE FECHA_CONCILIACION_PRECISOKEY = '").append(fecha).append("' ")
                //.append("AND INVENTARIO = '").append(conciliacion.getNombre()).append("' ")
                .append("AND TIPO_EVENTO_PRECISOKEY = '").append(tipoEvento.getNombre()).append("';");

        // Ejecutar la consulta de eliminación
        Query queryDelete = entityManager.createNativeQuery(queryBuilder.toString());
        queryDelete.executeUpdate();  // Esto elimina los registros

        // Limpiar el StringBuilder para la siguiente parte de la consulta
        queryBuilder.setLength(0);

        // Recorremos las rutas para construir las consultas de inserción
        for (int i = 0; i < listRoutes.size(); i++) {
            ConciliationRoute ruta = listRoutes.get(i);
            String nombreTablaRuta = "preciso_ci_" + ruta.getConciliacion().getId() + "_" + ruta.getId();

            queryBuilder.append(" IF OBJECT_ID('" + nombreTablaRuta + "', 'U') IS NOT NULL BEGIN \n");

            // Construir la consulta de inserción para la ruta actual (para CUENTA_CONTABLE_1 y CUENTA_CONTABLE_2)
            queryBuilder.append("INSERT INTO ").append(nombreTabla).append(" (INVENTARIO_PRECISOKEY, ID_INVENTARIO_PRECISOKEY, FECHA_CONCILIACION_PRECISOKEY, CENTRO_CONTABLE_PRECISOKEY, TIPO_EVENTO_PRECISOKEY, CDGO_MATRIZ_EVENTO_PRECISOKEY, CUENTA_CONTABLE_PRECISOKEY, DIVISA_CUENTA_PRECISOKEY, TOTAL_VALOR_CUENTA_PRECISOKEY) ")
                    .append("SELECT INVENTARIO_PRECISOKEY, ID_INVENTARIO_PRECISOKEY, FECHA_CONCILIACION_PRECISOKEY, CENTRO_CONTABLE_PRECISOKEY, TIPO_EVENTO_PRECISOKEY, CDGO_MATRIZ_EVENTO_PRECISOKEY, CUENTA_CONTABLE_1_PRECISOKEY AS CUENTA_CONTABLE_PRECISOKEY, ")
                    .append("DIVISA_CUENTA_1_PRECISOKEY AS DIVISA_CUENTA_PRECISOKEY, SUM(VALOR_CUENTA_1_PRECISOKEY) AS TOTAL_VALOR_CUENTA_PRECISOKEY ")
                    .append("FROM ").append(nombreTablaRuta).append(" ")
                    .append("WHERE FECHA_CONCILIACION_PRECISOKEY = '").append(fecha).append("' AND CUENTA_CONTABLE_1_PRECISOKEY IS NOT NULL AND TIPO_EVENTO_PRECISOKEY = '"+tipoEvento.getNombre()+"' ")
                    .append("GROUP BY INVENTARIO_PRECISOKEY, ID_INVENTARIO_PRECISOKEY, FECHA_CONCILIACION_PRECISOKEY, CENTRO_CONTABLE_PRECISOKEY, TIPO_EVENTO_PRECISOKEY, CDGO_MATRIZ_EVENTO_PRECISOKEY, CUENTA_CONTABLE_1_PRECISOKEY, DIVISA_CUENTA_1_PRECISOKEY; ");

            queryBuilder.append("INSERT INTO ").append(nombreTabla).append(" (INVENTARIO_PRECISOKEY, ID_INVENTARIO_PRECISOKEY, FECHA_CONCILIACION_PRECISOKEY, CENTRO_CONTABLE_PRECISOKEY, TIPO_EVENTO_PRECISOKEY, CDGO_MATRIZ_EVENTO_PRECISOKEY, CUENTA_CONTABLE_PRECISOKEY, DIVISA_CUENTA_PRECISOKEY, TOTAL_VALOR_CUENTA_PRECISOKEY) ")
                    .append("SELECT INVENTARIO_PRECISOKEY, ID_INVENTARIO_PRECISOKEY, FECHA_CONCILIACION_PRECISOKEY, CENTRO_CONTABLE_PRECISOKEY, TIPO_EVENTO_PRECISOKEY, CDGO_MATRIZ_EVENTO_PRECISOKEY, CUENTA_CONTABLE_2_PRECISOKEY AS CUENTA_CONTABLE_PRECISOKEY, ")
                    .append("DIVISA_CUENTA_2_PRECISOKEY AS DIVISA_CUENTA_PRECISOKEY, SUM(VALOR_CUENTA_2_PRECISOKEY) AS TOTAL_VALOR_CUENTA_PRECISOKEY ")
                    .append("FROM ").append(nombreTablaRuta).append(" ")
                    .append("WHERE FECHA_CONCILIACION_PRECISOKEY = '").append(fecha).append("' AND CUENTA_CONTABLE_2_PRECISOKEY IS NOT NULL AND TIPO_EVENTO_PRECISOKEY = '"+tipoEvento.getNombre()+"' ")
                    .append("GROUP BY INVENTARIO_PRECISOKEY, ID_INVENTARIO_PRECISOKEY, FECHA_CONCILIACION_PRECISOKEY, CENTRO_CONTABLE_PRECISOKEY, TIPO_EVENTO_PRECISOKEY, CDGO_MATRIZ_EVENTO_PRECISOKEY, CUENTA_CONTABLE_2_PRECISOKEY, DIVISA_CUENTA_2_PRECISOKEY; ");

            queryBuilder.append(" END \n");
            // Ejecutar la consulta de inserción
            Query queryInsert = entityManager.createNativeQuery(queryBuilder.toString());
            queryInsert.executeUpdate();  // Inserta los datos en la tabla

            // Limpiar el StringBuilder para la siguiente iteración
            queryBuilder.setLength(0);
        }

        // Consultar los datos finales ordenados
        String finalQuery = "SELECT * FROM " + nombreTabla + " ORDER BY INVENTARIO_PRECISOKEY, ID_INVENTARIO_PRECISOKEY, FECHA_CONCILIACION_PRECISOKEY, CENTRO_CONTABLE_PRECISOKEY, TIPO_EVENTO_PRECISOKEY, CDGO_MATRIZ_EVENTO_PRECISOKEY, CUENTA_CONTABLE_PRECISOKEY, DIVISA_CUENTA_PRECISOKEY;";
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

    public List<Object[]> findAllLogByDate(String fecha) {
        Query query = entityManager.createNativeQuery(
                "WITH CTE AS (\n" +
                        "SELECT \n" +
                        "    id_lc,\n" +
                        "    estado_proceso,\n" +
                        "    fecha_proceso,\n" +
                        "    fecha_preciso,\n" +
                        "    novedad,\n" +
                        "    tipo_proceso,\n" +
                        "    usuario,\n" +
                        "    id_conciliacion,\n" +
                        "    COUNT(*) OVER (PARTITION BY fecha_proceso, id_conciliacion) AS total_intentos,\n" +
                        "    ROW_NUMBER() OVER (PARTITION BY fecha_proceso, id_conciliacion ORDER BY fecha_preciso DESC) AS row_num\n" +
                        "FROM \n" +
                        "    PRECISO.dbo.preciso_log_conciliacion\n" +
                        "WHERE fecha_proceso like :fechaVarP \n" +
                        ")\n" +
                        "SELECT a.id_lc,\n" +
                        "b.nombre, \n" +
                        "'' as nulo,\n" +
                        "'' as nulo1,\n" +
                        "ISNULL(a.fecha_proceso,cast( :fechaVar as date)) as fecha_proceso,\n" +
                        "ISNULL(a.novedad,'') as novedad,\n" +
                        "ISNULL(a.fecha_preciso,cast( :fechaVar as date)) as fecha_preciso,\n" +
                        "ISNULL(a.usuario,'Sin Ejecutar') as usuario,\n" +
                        "ISNULL(a.tipo_proceso,'') as tipo_proceso,\n" +
                        "ISNULL(a.estado_proceso,'Fallido') as estado_proceso,\n" +
                        "ISNULL(a.total_intentos,0) as total_intentos,\n" +
                        "b.id   \n" +
                        "FROM preciso_conciliaciones b\n" +
                        "left join (SELECT * FROM CTE WHERE row_num = 1 ) a on b.id =a.id_conciliacion\n" +
                        "ORDER BY \n" +
                        "b.id,a.fecha_proceso;");
        query.setParameter("fechaVar", fecha);
        query.setParameter("fechaVarP", fecha+"%");
        return query.getResultList();
    }

}
