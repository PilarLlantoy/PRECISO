package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.AccountingRoute;
import com.inter.proyecto_intergrupo.model.parametric.CampoRC;
import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.AccountingRouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

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
    public AccountingRouteService(AccountingRouteRepository accountingRouteRepository) {
        this.accountingRouteRepository = accountingRouteRepository;
    }

    public List <AccountingRoute> findAll(){return accountingRouteRepository.findAllByOrderByNombreAsc();}
    public List<AccountingRoute> findAllActive() {
        return accountingRouteRepository.findByEstado(true);
    }

    public AccountingRoute findById(int id){
        return accountingRouteRepository.findAllById(id);
    }

    public AccountingRoute findByName(String nombre){
        return accountingRouteRepository.findAllByNombre(nombre);
    }

    public AccountingRoute modificar(AccountingRoute conciliacion){
        accountingRouteRepository.save(conciliacion);
       return conciliacion;
    }

   public void createTableTemporal(AccountingRoute data, List<CampoRC> columns) {
        Query queryDrop = entityManager.createNativeQuery("IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '"+(data.getNombreArchivo()+"_TEMPORAL")+"' AND TABLE_SCHEMA = 'dbo') BEGIN DROP TABLE "+(data.getNombreArchivo()+"_TEMPORAL") +" END;");
        queryDrop.executeUpdate();

        StringBuilder createTableQuery = new StringBuilder("CREATE TABLE ");
        createTableQuery.append(data.getNombreArchivo()+"_TEMPORAL").append(" (");

        for (int i = 0; i < columns.size(); i++) {
            CampoRC column = columns.get(i);
            createTableQuery.append(column.getNombre())
                    .append(" ")
                    .append(column.getTipo());

            if (column.getTipo().equalsIgnoreCase("VARCHAR")) {
                createTableQuery.append("(").append(column.getLongitud()).append(")");
            }

            if (i < columns.size() - 1) {
                createTableQuery.append(", ");
            }
        }

        createTableQuery.append(");");

        try {
            entityManager.createNativeQuery(createTableQuery.toString()).executeUpdate();
            System.out.println("Tabla creada exitosamente: " + data.getNombre());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String todayDateConvert(String formato) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");

        System.out.println("FORMATOOOO "+formato +" "+ today.format(formatter));
        return today.format(formatter);
    }

    public void bulkImport(AccountingRoute data, String ruta){
        String extension=".txt";
        Query queryBulk = entityManager.createNativeQuery("BULK INSERT " + (data.getNombreArchivo() + "_TEMPORAL") +
                " FROM '" + data.getRuta() + "\\" + data.getNombreArchivo() + todayDateConvert(data.getFormatoFecha()) +
                data.getComplementoArchivo() + extension +
                "' WITH (FORMATFILE = '" + ruta + "', FIRSTROW = " + data.getFilasOmitidas() + ")");
        queryBulk.executeUpdate();
    }

    public void conditionData(AccountingRoute data){
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

            Query deleteSelect = entityManager.createNativeQuery("DELETE FROM "+data.getNombreArchivo() + "_TEMPORAL WHERE NOT("+ condicion +"));");
            deleteSelect.executeUpdate();

        }

    }

    public void validationData(AccountingRoute data){
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
                Query deleteSelect = entityManager.createNativeQuery("UPDATE "+data.getNombreArchivo() + "_TEMPORAL SET "+obj[0].toString()+" = "+obj[0].toString()+obj[4].toString()+obj[3].toString()+" WHERE "+obj[1].toString()+"='"+obj[2].toString()+"';");
                deleteSelect.executeUpdate();
            }
        }

    }

    public void generarArchivoFormato(List<CampoRC> campos, String rutaArchivoFormato) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(rutaArchivoFormato))) {

            // Escribir encabezado del archivo de formato
            writer.write("13.0\n"); // Versión del archivo de formato
            writer.write(campos.size()+1 + "\n"); // Número de campos

            int fieldIndex = 1;
            for (CampoRC campo : campos) {
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

}
