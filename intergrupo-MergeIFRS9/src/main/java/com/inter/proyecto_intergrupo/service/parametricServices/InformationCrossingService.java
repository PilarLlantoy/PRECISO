package com.inter.proyecto_intergrupo.service.parametricServices;
import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.AccountingRouteRepository;
import com.inter.proyecto_intergrupo.repository.parametric.LogAccountingLoadRepository;
import com.inter.proyecto_intergrupo.repository.parametric.LogInformationCrossingRepository;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
@Service
@Transactional
public class InformationCrossingService {
    @Autowired
    private final LogInformationCrossingRepository logInformationCrossingRepository;
    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    private AuditRepository auditRepository;
    @Autowired
    private LogAccountingLoadRepository logAccountingLoadRepository;

    @Autowired
    private ConciliationService conciliationService;

    @Autowired
    private EventTypeService eventTypeService;


    @Autowired
    public InformationCrossingService(LogInformationCrossingRepository logInformationCrossingRepository) {
        this.logInformationCrossingRepository = logInformationCrossingRepository;
    }
    public List<LogInformationCrossing> findAllLog(Conciliation concil, String fecha, EventType evento) {
        LocalDate localDate = LocalDate.parse(fecha);
        Date fechaDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        return logInformationCrossingRepository.findAllByIdConciliacionAndFechaProcesoAndIdEventoOrderByIdDesc(concil,fechaDate, evento);
    }

    public void loadLogInformationCrossing(User user,int concil, int event, String fecha, String tipo, String estado, String mensaje)
    {
        LocalDate localDate = LocalDate.parse(fecha);
        Date fechaDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date today=new Date();
        LogInformationCrossing insert = new LogInformationCrossing();

        insert.setFechaProceso(fechaDate);
        insert.setFechaPreciso(today);
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
        Conciliation conciliation = conciliationService.findById(concil);
        insert.setIdConciliacion(conciliation);
        EventType evento = eventTypeService.findAllById(event);
        insert.setIdEvento(evento);
        logInformationCrossingRepository.save(insert);
    }

    public void recreateTable(ConciliationRoute data, int idConcil) {
        StringBuilder createTableQuery = new StringBuilder("CREATE TABLE ");
        String tableName = "preciso_ci_" + idConcil + "_" + data.getId();
        createTableQuery.append(tableName).append(" (");

        // Paso 1: Agregar los campos existentes de la tabla original
        for (int i = 0; i < data.getCampos().size(); i++) {
            CampoRConcil column = data.getCampos().get(i);
            createTableQuery.append(column.getNombre())
                    .append(" ")
                    .append(column.getTipo());

            if (column.getTipo().equalsIgnoreCase("VARCHAR")) {
                createTableQuery.append("(MAX)"); // Longitud de MAX para VARCHAR
            }

            if (i < data.getCampos().size() - 1) {
                createTableQuery.append(", ");
            }
        }

        // Paso 2: Agregar los nuevos campos específicos
        if (!data.getCampos().isEmpty()) {
            createTableQuery.append(", ");
        }

        createTableQuery.append("INVENTARIO VARCHAR(MAX), ")
                .append("ID_INVENTARIO INT, ")
                .append("FECHA_CONCILIACION DATE, ")
                .append("TIPO_EVENTO VARCHAR(MAX), ")
                .append("CDGO_MATRIZ_EVENTO INT, ")
                .append("CENTRO_CONTABLE VARCHAR(MAX), ")
                .append("CUENTA_CONTABLE_1 VARCHAR(MAX), ")
                .append("DIVISA_CUENTA_1 VARCHAR(MAX), ")
                .append("VALOR_CUENTA_1 FLOAT, ")
                .append("CUENTA_CONTABLE_2 VARCHAR(MAX), ")
                .append("DIVISA_CUENTA_2 VARCHAR(MAX), ")
                .append("VALOR_CUENTA_2 FLOAT");

        // *** Cerrar la definición de la tabla ***
        createTableQuery.append(");"); // Cerramos correctamente con paréntesis y punto y coma

        // Validar si la tabla ya existe y eliminarla
        String dropTableQuery = "IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '"
                + tableName + "' AND TABLE_SCHEMA = 'dbo') " +
                "BEGIN DROP TABLE " + tableName + "; END;";

        // Ejecutar las consultas
        Query dropTable = entityManager.createNativeQuery(dropTableQuery);
        dropTable.executeUpdate();

        Query createTable = entityManager.createNativeQuery(createTableQuery.toString());
        createTable.executeUpdate();
    }


    public void rellenarTablaCruce(ConciliationRoute data, int idConcil){
        // Paso 3: Insertar registros desde "preciso_rc_<data.getId()>" a la nueva tabla
        String tableName = "preciso_ci_"+idConcil+"_"+data.getId();
        String sourceTableName = "preciso_rconcil_" + data.getId();
        StringBuilder insertDataQuery = new StringBuilder("INSERT INTO ").append(tableName).append(" (");

        // Agregar solo los nombres de los campos existentes en la tabla original
        for (int i = 0; i < data.getCampos().size(); i++) {
            CampoRConcil column = data.getCampos().get(i);
            insertDataQuery.append(column.getNombre());

            if (i < data.getCampos().size() - 1) {
                insertDataQuery.append(", ");
            }
        }

        insertDataQuery.append(") SELECT ");

        // Agregar los campos correspondientes en el SELECT
        for (int i = 0; i < data.getCampos().size(); i++) {
            CampoRConcil column = data.getCampos().get(i);
            insertDataQuery.append(column.getNombre());

            if (i < data.getCampos().size() - 1) {
                insertDataQuery.append(", ");
            }
        }

        insertDataQuery.append(" FROM ").append(sourceTableName).append(";");

        // Ejecutar la consulta para insertar los datos
        Query insertData = entityManager.createNativeQuery(insertDataQuery.toString());
        insertData.executeUpdate();
    }

    public void completarTablaCruce(ConciliationRoute data, int idConcil, String fecha, EventType tipoEvento, EventMatrix matriz) {
        // Crear la consulta SQL para insertar los valores
        String tableName = "preciso_ci_"+idConcil+"_"+data.getId();
        Query updateQuery = entityManager.createNativeQuery("UPDATE " +tableName+
                " SET INVENTARIO = ?, " +
                "    ID_INVENTARIO = ?, " +
                "    FECHA_CONCILIACION = ?, " +
                "    TIPO_EVENTO = ?, " +
                "    CDGO_MATRIZ_EVENTO = ?, " +
                "    CENTRO_CONTABLE = ? " +
                ""); // Reemplaza <CONDICION> con la condición adecuada para identificar los registros a actualizar

        updateQuery.setParameter(1,data.getDetalle());
        updateQuery.setParameter(2,data.getId());
        updateQuery.setParameter(3,fecha);
        updateQuery.setParameter(4,tipoEvento.getNombre());
        updateQuery.setParameter(5,matriz.getId());
        updateQuery.setParameter(6,matriz.getCentroContable());
        updateQuery.executeUpdate();
    }

}