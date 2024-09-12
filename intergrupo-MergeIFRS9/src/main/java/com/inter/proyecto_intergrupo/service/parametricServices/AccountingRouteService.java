package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.AccountingRoute;
import com.inter.proyecto_intergrupo.model.parametric.CampoRC;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.AccountingRouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
        Query queryDrop = entityManager.createNativeQuery("IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '"+(data.getNombre()+"_TEMPORAL")+"' AND TABLE_SCHEMA = 'dbo') BEGIN DROP TABLE "+(data.getNombre()+"_TEMPORAL") +" END;");
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formato);
        return today.format(formatter);
    }

    public void bulkImport(AccountingRoute data){
        String extension=".txt";
        Query queryBulk = entityManager.createNativeQuery("BULK INSERT "+(data.getNombre()+"_TEMPORAL")+" FROM '"+data.getRuta()+"\\"+data.getNombreArchivo() + todayDateConvert(data.getFormatoFecha()) +data.getComplementoArchivo()+extension+"' WITH (FIELDTERMINATOR= '"+data.getDelimitador()+"',ROWTERMINATOR = '\\n', FIRSTROW = "+data.getFilasOmitidas()+")");
        queryBulk.executeUpdate();
    }



}
