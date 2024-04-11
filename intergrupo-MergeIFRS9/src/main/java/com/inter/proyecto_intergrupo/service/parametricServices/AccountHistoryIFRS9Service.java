package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.AccountHistoryIFRS9;
import com.inter.proyecto_intergrupo.model.parametric.AccountHistoryIFRS9;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.AccountAndByProductRepository;
import com.inter.proyecto_intergrupo.repository.parametric.AccountHistoryIFRS9Repository;
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
public class AccountHistoryIFRS9Service {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private final AccountHistoryIFRS9Repository accountHistoryIFRS9Repository;

    public AccountHistoryIFRS9Service(AccountHistoryIFRS9Repository accountHistoryIFRS9Repository) {
        this.accountHistoryIFRS9Repository = accountHistoryIFRS9Repository;
    }


    public List<AccountHistoryIFRS9> findAll(){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_historico_cuentas_ifrs9 as em ", AccountHistoryIFRS9.class);
        return query.getResultList();
    }

    public List<AccountHistoryIFRS9> findAccountHistoryIFRS9byId(Integer id){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_historico_cuentas_ifrs9 as em " +
                "WHERE em.id = ?",AccountHistoryIFRS9.class);
        query.setParameter(1, id);
        return query.getResultList();
    }


    public void modifyAccountHistoryIFRS9(AccountHistoryIFRS9 toModify,Integer id){
        AccountHistoryIFRS9 toInsert = new AccountHistoryIFRS9();
        toInsert.setEmpresa(toModify.getEmpresa());
        toInsert.setCuenta(toModify.getCuenta());
        toInsert.setCodCons(toModify.getCodCons());
        toInsert.setDivisa(toModify.getDivisa());
        toInsert.setObservaciones(toModify.getObservaciones());
        Query query = entityManager.createNativeQuery("UPDATE nexco_historico_cuentas_ifrs9 SET cuenta = ? , empresa = ? , cod_cons = ?  , observaciones = ? , divisa = ? " +
                "WHERE id = ? ", AccountHistoryIFRS9.class);
        query.setParameter(1,toInsert.getCuenta());
        query.setParameter(2, toInsert.getEmpresa());
        query.setParameter(3, toInsert.getCodCons());
        query.setParameter(4,toInsert.getObservaciones());
        query.setParameter(5, toInsert.getDivisa());
        query.setParameter(6, id);
        try {
            query.executeUpdate();
        }catch(Exception e){

        }
    }

    public void saveAccountHistoryIFRS9(AccountHistoryIFRS9 AccountHistoryIFRS9s){
        Query query = entityManager.createNativeQuery("INSERT INTO nexco_historico_cuentas_ifrs9 (cuenta,empresa,cod_cons,observaciones,divisa) VALUES (?,?,?,?,?)", AccountHistoryIFRS9.class);
        query.setParameter(1, AccountHistoryIFRS9s.getCuenta());
        query.setParameter(2, AccountHistoryIFRS9s.getEmpresa() );
        query.setParameter(3, AccountHistoryIFRS9s.getCodCons());
        query.setParameter(4, AccountHistoryIFRS9s.getObservaciones());
        query.setParameter(5, AccountHistoryIFRS9s.getDivisa());
        query.executeUpdate();
    }

    public void removeAccountHistoryIFRS9(Integer id){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_historico_cuentas_ifrs9 WHERE id = ? ", AccountHistoryIFRS9.class);
        query.setParameter(1, id);
        query.executeUpdate();
    }

    public void clearAccountHistoryIFRS9(User user){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_historico_cuentas_ifrs9", AccountHistoryIFRS9.class);
        query.executeUpdate();
    }

    public Page<AccountHistoryIFRS9> getAll(Pageable pageable){
        List<AccountHistoryIFRS9> list = findAll();
        int start = (int)pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<AccountHistoryIFRS9> pageAccountHistoryIFRS9 = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return pageAccountHistoryIFRS9;
    }

    public List<AccountHistoryIFRS9> findByFilter(String value, String filter) {
        List<AccountHistoryIFRS9> list=new ArrayList<AccountHistoryIFRS9>();
        switch (filter)
        {
            case "Cuenta":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_historico_cuentas_ifrs9 as em " +
                        "WHERE em.cuenta LIKE ?", AccountHistoryIFRS9.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Empresa":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_historico_cuentas_ifrs9 as em " +
                        "WHERE em.empresa LIKE ?", AccountHistoryIFRS9.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            case "CodCons":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_historico_cuentas_ifrs9 as em " +
                        "WHERE em.cod_cons LIKE ?", AccountHistoryIFRS9.class);
                query1.setParameter(1, value);

                list= query1.getResultList();
                break;
            case "Observaciones":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_historico_cuentas_ifrs9 as em " +
                        "WHERE em.observaciones LIKE ?", AccountHistoryIFRS9.class);
                query2.setParameter(1, value);

                list= query2.getResultList();
                break;
            case "Divisa":
                Query query3 = entityManager.createNativeQuery("SELECT em.* FROM nexco_historico_cuentas_ifrs9 as em " +
                        "WHERE em.divisa LIKE ?", AccountHistoryIFRS9.class);
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
                insert.setAccion("Inserción archivo Historico Cuentas IFRS9");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Historico Cuentas IFRS9");
                insert.setNombre(user.getNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }
            else{
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Fallo inserción archivo Historico Cuentas IFRS9");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Historico Cuentas IFRS9");
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
                String cellEmpresa = formatter.formatCellValue(row.getCell(1));
                String cellCodCons = formatter.formatCellValue(row.getCell(2));
                String cellObservaciones = formatter.formatCellValue(row.getCell(3));
                String cellDivisa = formatter.formatCellValue(row.getCell(4));
                log[0] = String.valueOf(row.getRowNum());
                if ((cellCuenta.isEmpty() || cellCuenta.isBlank()) && (cellEmpresa.isEmpty() || cellEmpresa.isBlank())
                        && (cellCodCons.isEmpty() || cellCodCons.isBlank()) && (cellObservaciones.isEmpty() || cellObservaciones.isBlank())
                        && (cellDivisa.isEmpty() || cellDivisa.isBlank())) {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = String.valueOf(row.getRowNum());
                    log[2] = "true";
                    break;
                } else if (cellCuenta.isEmpty() || cellCuenta.isBlank() || cellCuenta.length() < 4 || cellCuenta.length() > 18) {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = "1";
                    log[2] = "false";
                    break;
                } else if (cellEmpresa.isEmpty() || cellEmpresa.isBlank() || cellEmpresa.length() > 50) {
                    log[1] = "2";
                    log[2] = "false";
                    break;
                } else if (cellCodCons.isEmpty() || cellCodCons.isBlank() || cellCodCons.length() != 5) {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = "3";
                    log[2] = "false";
                    break;
                } else if (cellObservaciones.isEmpty() || cellObservaciones.isBlank() || cellObservaciones.length() > 50) {
                    log[1] = "4";
                    log[2] = "false";
                    break;
                } else if (cellDivisa.isEmpty() || cellDivisa.isBlank() || cellDivisa.length() != 3) {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = "5";
                    log[2] = "false";
                    break;
                } else {
                    try {
                        log[0] = String.valueOf(row.getRowNum());
                        Long cuenta = Long.parseLong(cellCuenta);log[1]="1";
                        Long codCons = Long.parseLong(cellCodCons);log[1]="3";
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
                String cellEmpresa = formatter.formatCellValue(row.getCell(1));
                String cellCodCons = formatter.formatCellValue(row.getCell(2));
                String cellObservaciones = formatter.formatCellValue(row.getCell(3));
                String cellDivisa = formatter.formatCellValue(row.getCell(4));
                if ((cellCuenta.isEmpty() || cellCuenta.isBlank()) && (cellEmpresa.isEmpty() || cellEmpresa.isBlank())
                        && (cellCodCons.isEmpty() || cellCodCons.isBlank()) && (cellObservaciones.isEmpty() || cellObservaciones.isBlank())
                        && (cellDivisa.isEmpty() || cellDivisa.isBlank())) {
                    log[0] = cellCuenta;
                    log[1] = "Fallo al ingresar registro";
                    break;
                } else {
                    AccountHistoryIFRS9 accountHistoryIFRS9 = new AccountHistoryIFRS9();
                    accountHistoryIFRS9.setCuenta(cellCuenta);
                    accountHistoryIFRS9.setEmpresa(cellEmpresa);
                    accountHistoryIFRS9.setCodCons(cellCodCons);
                    accountHistoryIFRS9.setObservaciones(cellObservaciones);
                    accountHistoryIFRS9.setDivisa(cellDivisa);
                    accountHistoryIFRS9Repository.save(accountHistoryIFRS9);
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
