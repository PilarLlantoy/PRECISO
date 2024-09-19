package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CampoRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ConciliationRouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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


    public void createTableTemporal(ConciliationRoute data, List<CampoRConcil> columns) {
        Query queryDrop = entityManager.createNativeQuery("IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '"+(data.getNombreArchivo()+"_TEMPORAL")+"' AND TABLE_SCHEMA = 'dbo') BEGIN DROP TABLE "+(data.getNombreArchivo()+"_TEMPORAL") +" END;");
        queryDrop.executeUpdate();

        StringBuilder createTableQuery = new StringBuilder("CREATE TABLE ");
        createTableQuery.append(data.getNombreArchivo()+"_TEMPORAL").append(" (");

        for (int i = 0; i < columns.size(); i++) {
            CampoRConcil column = columns.get(i);
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
            System.out.println("Tabla creada exitosamente: " + data.getNombreArchivo()+"TEMPORAL");
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

    public void bulkImport(ConciliationRoute data, String ruta){
        String extension=".txt";
        Query queryBulk = entityManager.createNativeQuery("BULK INSERT " + (data.getNombreArchivo() + "_TEMPORAL") +
                " FROM '" + data.getRuta() + "\\" + data.getNombreArchivo() + todayDateConvert(data.getFormatoFecha()) + extension +
                "' WITH (FORMATFILE = '" + ruta + "', FIRSTROW = " + data.getFilasOmitidas() + ")");
        queryBulk.executeUpdate();
    }

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
                Query deleteSelect = entityManager.createNativeQuery("UPDATE "+data.getNombreArchivo() + "_TEMPORAL SET "+obj[0].toString()+" = "+obj[0].toString()+obj[4].toString()+obj[3].toString()+" WHERE "+obj[1].toString()+"='"+obj[2].toString()+"';");
                deleteSelect.executeUpdate();
            }
        }

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

}
