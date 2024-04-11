package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.CounterpartyGenericContracts;
import com.inter.proyecto_intergrupo.model.parametric.CounterpartyGenericContracts;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CounterpartyGenericContractsRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CounterpartyGenericContractsRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
@Transactional
public class CounterpartyGenericContractsService {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private final CounterpartyGenericContractsRepository counterpartyGenericContractsRepository;

    public CounterpartyGenericContractsService(CounterpartyGenericContractsRepository counterpartyGenericContractsRepository) {
        this.counterpartyGenericContractsRepository = counterpartyGenericContractsRepository;
    }


    public List<CounterpartyGenericContracts> findAll(){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_counterparty_generic_contracts as em ", CounterpartyGenericContracts.class);
        return query.getResultList();
    }

    public List<CounterpartyGenericContracts> findCounterpartyGenericContractsbyId(Integer id){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_counterparty_generic_contracts as em " +
                "WHERE em.id = ?",CounterpartyGenericContracts.class);
        query.setParameter(1, id);
        return query.getResultList();
    }

    /*
    //NUCTA => cuenta DECTA => descripcion
    public String getDECTACuentasPUC(String cuenta){
        Query neoconQuery = entityManager.createNativeQuery("SELECT DECTA FROM CUENTAS_PUC WHERE NUCTA = ?");
        neoconQuery.setParameter(1,cuenta);
        List neoconResult = neoconQuery.getResultList();

        if(neoconResult.isEmpty()){
            return "";
        }
        else return neoconResult.get(0).toString();
    }
    */


    public void modifyCounterpartyGenericContracts(CounterpartyGenericContracts toModify,Integer id){
        CounterpartyGenericContracts toInsert = new CounterpartyGenericContracts();
        toInsert.setCuenta(toModify.getCuenta());
        toInsert.setConcepto(toModify.getConcepto());
        toInsert.setContrato(toModify.getContrato());
        toInsert.setFuenteInformacion(toModify.getFuenteInformacion());
        toInsert.setSaldo(toModify.getSaldo());
        Query query = entityManager.createNativeQuery("UPDATE nexco_counterparty_generic_contracts SET cuenta = ? , concepto = ? , contrato = ?  , fuente_informacion = ?, saldo = ? " +
                "WHERE id = ? ", CounterpartyGenericContracts.class);
        query.setParameter(1, toInsert.getCuenta());
        query.setParameter(2, toInsert.getConcepto());
        query.setParameter(3, toInsert.getContrato());
        query.setParameter(4, toInsert.getFuenteInformacion());
        query.setParameter(5, toInsert.getSaldo());
        query.setParameter(6, id);
        try {
            query.executeUpdate();
        }catch(Exception e){

        }
    }

    public void saveCounterpartyGenericContracts(CounterpartyGenericContracts CounterpartyGenericContractss){
        Query query = entityManager.createNativeQuery("INSERT INTO nexco_counterparty_generic_contracts (cuenta,concepto,contrato,fuente_informacion,saldo) VALUES (?,?,?,?,?)", CounterpartyGenericContracts.class);
        query.setParameter(1, CounterpartyGenericContractss.getCuenta());
        query.setParameter(2, CounterpartyGenericContractss.getConcepto());
        query.setParameter(3, CounterpartyGenericContractss.getContrato());
        query.setParameter(4, CounterpartyGenericContractss.getFuenteInformacion());
        query.setParameter(5, CounterpartyGenericContractss.getSaldo());
        query.executeUpdate();
    }

    public void removeCounterpartyGenericContracts(Integer id){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_counterparty_generic_contracts WHERE id = ? ", CounterpartyGenericContracts.class);
        query.setParameter(1, id);
        query.executeUpdate();
    }

    public void clearCounterpartyGenericContracts(User user){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_counterparty_generic_contracts", CounterpartyGenericContracts.class);
        query.executeUpdate();
    }

    public Page<CounterpartyGenericContracts> getAll(Pageable pageable){
        List<CounterpartyGenericContracts> list = findAll();
        int start = (int)pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<CounterpartyGenericContracts> pageCounterpartyGenericContracts = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return pageCounterpartyGenericContracts;
    }

    public List<CounterpartyGenericContracts> findByFilter(String value, String filter) {
        List<CounterpartyGenericContracts> list=new ArrayList<CounterpartyGenericContracts>();
        switch (filter)
        {
            case "Cuenta":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_counterparty_generic_contracts as em " +
                        "WHERE em.cuenta LIKE ?", CounterpartyGenericContracts.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Concepto":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_counterparty_generic_contracts as em " +
                        "WHERE em.concepto LIKE ?", CounterpartyGenericContracts.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            case "Contrato":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_counterparty_generic_contracts as em " +
                        "WHERE em.contrato LIKE ?", CounterpartyGenericContracts.class);
                query1.setParameter(1, value);

                list= query1.getResultList();
                break;
            case "FuenteInformacion":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_counterparty_generic_contracts as em " +
                        "WHERE em.fuente_informacion LIKE ?", CounterpartyGenericContracts.class);
                query2.setParameter(1, value);

                list= query2.getResultList();
                break;
            case "Saldo":
                Query query3 = entityManager.createNativeQuery("SELECT em.* FROM nexco_counterparty_generic_contracts as em " +
                        "WHERE em.saldo LIKE ?", CounterpartyGenericContracts.class);
                query3.setParameter(1, value);

                list= query3.getResultList();
                break;
            default:
                break;
        }
        return list;
    }

    public ArrayList<String[]> saveFileBD(InputStream file, User user) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<String[]>();
        if (file!=null)
        {
            Iterator<Row> rows = null;
            Iterator<Row> rows1 = null;

            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            rows = sheet.iterator();
            rows1 = sheet.iterator();
            list=validarPlantilla(rows);
            String[] temporal= list.get(0);
            if(temporal[2].equals("true"))
            {
                list=getRows(rows1);
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Inserción archivo Contrapartida y Contratos Genericos");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Contrapartida y Contratos Genericos");
                insert.setNombre(user.getNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }
            else{
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Fallo inserción archivo Contrapartida y Contratos Genericos");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Contrapartida y Contratos Genericos");
                insert.setNombre(user.getNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);

            }
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows) {
        ArrayList lista = new ArrayList();
        XSSFRow row;
        int firstRow = 1;
        String[] log = new String[3];
        log[0] = "0";
        log[1] = "0";
        log[2] = "false";
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (firstRow == 2) {
                DataFormatter formatter = new DataFormatter();
                String cellCuenta = formatter.formatCellValue(row.getCell(0));
                String cellConcepto = formatter.formatCellValue(row.getCell(1));
                String cellContrato = formatter.formatCellValue(row.getCell(2));
                String cellFuenteInformacion = formatter.formatCellValue(row.getCell(3));
                String cellSaldo = formatter.formatCellValue(row.getCell(4));
                log[0] = String.valueOf(row.getRowNum());
                if ((cellCuenta.isEmpty() || cellCuenta.isBlank()) && (cellConcepto.isEmpty() || cellConcepto.isBlank())
                        && (cellContrato.isEmpty() || cellContrato.isBlank()) && (cellFuenteInformacion.isEmpty() || cellFuenteInformacion.isBlank())
                        && (cellSaldo.isEmpty() || cellSaldo.isBlank())) {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = String.valueOf(row.getRowNum());
                    log[2] = "true";
                    break;
                } else if (cellCuenta.isEmpty() || cellCuenta.isBlank() || cellCuenta.length() < 4 || cellCuenta.length() > 18) {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = "1";
                    log[2] = "false";
                    break;
                } else if (cellConcepto.isEmpty() || cellConcepto.isBlank() || cellConcepto.length() > 50) {
                    log[1] = "2";
                    log[2] = "false";
                    break;
                } else if (cellContrato.isEmpty() || cellContrato.isBlank() || cellContrato.length() != 18) {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = "3";
                    log[2] = "false";
                    break;
                } else if (cellFuenteInformacion.isEmpty() || cellFuenteInformacion.isBlank() || cellFuenteInformacion.length() > 50) {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = "4";
                    log[2] = "false";
                    break;
                } else if (cellSaldo.isEmpty() || cellSaldo.isBlank() || (!cellSaldo.equals("-1") && !cellSaldo.equals("1"))) {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = "5";
                    log[2] = "false";
                    break;
                } else {
                    try {
                        log[0] = String.valueOf(row.getRowNum());
                        Long cuenta = Long.parseLong(cellCuenta);log[1]="1";
                        Long contrato = Long.parseLong(cellContrato);log[1]="3";
                        Integer saldo = Integer.parseInt(cellSaldo);log[1]="5";
                        log[1] = "1";
                        log[2] = "true";
                    } catch (Exception e) {
                        log[2] = "falseFormat";
                        lista.add(log);
                        return lista;
                    }
                }
            } else {
                firstRow ++;
            }
        }
        lista.add(log);
        return lista;
    }


    public ArrayList getRows(Iterator<Row> rows) {
        XSSFRow row;
        ArrayList lista = new ArrayList();
        int firstRow = 1;
        while (rows.hasNext()) {
            String[] log = new String[3];
            log[2] = "false";
            row = (XSSFRow) rows.next();

            if (firstRow == 2 && row.getCell(0) != null) {
                DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
                String cellCuenta = formatter.formatCellValue(row.getCell(0));
                String cellConcepto = formatter.formatCellValue(row.getCell(1));
                String cellContrato = formatter.formatCellValue(row.getCell(2));
                String cellFuenteInformacion = formatter.formatCellValue(row.getCell(3));
                String cellSaldo = formatter.formatCellValue(row.getCell(4));
                if ((cellCuenta.isEmpty() || cellCuenta.isBlank()) && (cellConcepto.isEmpty() || cellConcepto.isBlank())
                        && (cellContrato.isEmpty() || cellContrato.isBlank()) && (cellFuenteInformacion.isEmpty() || cellFuenteInformacion.isBlank())
                        && (cellSaldo.isEmpty() || cellSaldo.isBlank())) {
                    log[0] = cellCuenta;
                    log[1] = "Fallo al ingresar registro";
                    break;
                } else {
                    CounterpartyGenericContracts counterpartyGenericContracts = new CounterpartyGenericContracts();
                    counterpartyGenericContracts.setCuenta(cellCuenta);
                    counterpartyGenericContracts.setConcepto(cellConcepto);
                    counterpartyGenericContracts.setContrato(cellContrato);
                    counterpartyGenericContracts.setFuenteInformacion(cellFuenteInformacion);
                    counterpartyGenericContracts.setSaldo(Integer.parseInt(cellSaldo));
                    counterpartyGenericContractsRepository.save(counterpartyGenericContracts);
                    log[0] = cellCuenta;
                    log[1] = "Registro actualizado exitosamente";
                    log[2] = "true";
                }
                lista.add(log);
            } else {
                firstRow ++;
            }
        }
        return lista;
    }


}