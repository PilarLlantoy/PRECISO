package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.GenericAccount;
import com.inter.proyecto_intergrupo.model.parametric.GenericAccount;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.GenericAccountRepository;
import com.inter.proyecto_intergrupo.repository.parametric.GenericAccountRepository;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class GenericAccountService {
    
    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private final GenericAccountRepository genericAccountRepository;

    public GenericAccountService(GenericAccountRepository genericAccountRepository) {
        this.genericAccountRepository = genericAccountRepository;
    }


    public List<GenericAccount> findAll(){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuenta_generica as em ", GenericAccount.class);
        return query.getResultList();
    }

    public List<GenericAccount> findGenericAccountbyId(Integer id){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuenta_generica as em " +
                "WHERE em.id = ?",GenericAccount.class);

        query.setParameter(1, id);
        return query.getResultList();
    }


    public void modifyGenericAccount(GenericAccount toModify,Integer id){
        GenericAccount toInsert = new GenericAccount();
        toInsert.setEmpresa(toModify.getEmpresa());
        toInsert.setCuenta(toModify.getCuenta());
        toInsert.setCodCons(toModify.getCodCons());
        toInsert.setDivisa(toModify.getDivisa());
        toInsert.setConcepto(toModify.getConcepto());
        toInsert.setFecha(toModify.getFecha());
        toInsert.setObservaciones(toModify.getObservaciones());
        Query query = entityManager.createNativeQuery("UPDATE nexco_cuenta_generica SET cuenta = ? , empresa = ? , cod_cons = ?  , observaciones = ? , divisa = ? , concepto = ? , fecha = ? " +
                "WHERE id = ? ", GenericAccount.class);
        query.setParameter(1,toInsert.getCuenta());
        query.setParameter(2, toInsert.getEmpresa());
        query.setParameter(3, toInsert.getCodCons());
        query.setParameter(4,toInsert.getObservaciones());
        query.setParameter(5, toInsert.getDivisa());
        query.setParameter(6, toInsert.getConcepto());
        query.setParameter(7, toInsert.getFecha());
        query.setParameter(8, id );
        try {
            query.executeUpdate();
        }catch(Exception e){

        }
    }

    public void saveGenericAccount(GenericAccount GenericAccounts){
        Query query = entityManager.createNativeQuery("INSERT INTO nexco_cuenta_generica (cuenta,empresa,cod_cons,observaciones,divisa,concepto,fecha) VALUES (?,?,?,?,?,?,?)", GenericAccount.class);
        query.setParameter(1, GenericAccounts.getCuenta());
        query.setParameter(2, GenericAccounts.getEmpresa() );
        query.setParameter(3, GenericAccounts.getCodCons());
        query.setParameter(4, GenericAccounts.getObservaciones());
        query.setParameter(5, GenericAccounts.getDivisa());
        query.setParameter(6, GenericAccounts.getConcepto());
        query.setParameter(7, GenericAccounts.getFecha());
        query.executeUpdate();
    }

    public void removeGenericAccount(Integer id){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_cuenta_generica WHERE id = ? ", GenericAccount.class);
        query.setParameter(1, id);
        query.executeUpdate();
    }

    public void clearGenericAccount(User user){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_cuenta_generica", GenericAccount.class);
        query.executeUpdate();
    }

    public Page<GenericAccount> getAll(Pageable pageable){
        List<GenericAccount> list = findAll();
        int start = (int)pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<GenericAccount> pageGenericAccount = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return pageGenericAccount;
    }

    public List<GenericAccount> findByFilter(String value, String filter) {
        List<GenericAccount> list=new ArrayList<GenericAccount>();
        switch (filter)
        {
            case "Cuenta":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuenta_generica as em " +
                        "WHERE em.cuenta LIKE ?", GenericAccount.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Empresa":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuenta_generica as em " +
                        "WHERE em.empresa LIKE ?", GenericAccount.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            case "CodCons":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuenta_generica as em " +
                        "WHERE em.cod_cons LIKE ?", GenericAccount.class);
                query1.setParameter(1, value);

                list= query1.getResultList();
                break;
            case "Observaciones":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuenta_generica as em " +
                        "WHERE em.observaciones LIKE ?", GenericAccount.class);
                query2.setParameter(1, value);

                list= query2.getResultList();
                break;
            case "Divisa":
                Query query3 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuenta_generica as em " +
                        "WHERE em.divisa LIKE ?", GenericAccount.class);
                query3.setParameter(1, value);

                list= query3.getResultList();
                break;
            case "Concepto":
                Query query4 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuenta_generica as em " +
                        "WHERE em.concepto LIKE ?", GenericAccount.class);
                query4.setParameter(1, value);

                list= query4.getResultList();
                break;
            case "Fecha":
                Query query5 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuenta_generica as em " +
                        "WHERE em.fecha LIKE ?", GenericAccount.class);
                query5.setParameter(1, value);

                list= query5.getResultList();
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
                insert.setAccion("Inserción archivo Cuenta y Subproductos");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Cuenta y Subproductos");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }
            else{
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Fallo inserción archivo Cuenta y Subproductos");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Cuenta y Subproductos");
                insert.setNombre(user.getPrimerNombre());
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
                DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String cellCuenta = formatter.formatCellValue(row.getCell(0));
                String cellEmpresa = formatter.formatCellValue(row.getCell(1));
                String cellCodCons = formatter.formatCellValue(row.getCell(2));
                String cellObservaciones = formatter.formatCellValue(row.getCell(3));
                String cellDivisa = formatter.formatCellValue(row.getCell(4));
                String cellConcepto = formatter.formatCellValue(row.getCell(5));
                String cellFecha = formatter.formatCellValue(row.getCell(6));
                log[0] = String.valueOf(row.getRowNum());
                if ((cellCuenta.isEmpty() || cellCuenta.isBlank()) && (cellEmpresa.isEmpty() || cellEmpresa.isBlank())
                        && (cellCodCons.isEmpty() || cellCodCons.isBlank()) && (cellObservaciones.isEmpty() || cellObservaciones.isBlank())
                        && (cellDivisa.isEmpty() || cellDivisa.isBlank()) && (cellConcepto.isEmpty() || cellConcepto.isBlank())
                        && (cellFecha.isEmpty() || cellFecha.isBlank())) {
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
                } else if (cellConcepto.isEmpty() || cellConcepto.isBlank() || cellConcepto.length() > 50) {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = "5";
                    log[2] = "false";
                    break;
                } else if (cellFecha.isEmpty() || cellFecha.isBlank() || !validarFecha(cellFecha, sdf)) {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = "6";
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
                String cellConcepto = formatter.formatCellValue(row.getCell(5));
                String cellFecha = formatter.formatCellValue(row.getCell(6));
                if ((cellCuenta.isEmpty() || cellCuenta.isBlank()) && (cellEmpresa.isEmpty() || cellEmpresa.isBlank())
                        && (cellCodCons.isEmpty() || cellCodCons.isBlank()) && (cellObservaciones.isEmpty() || cellObservaciones.isBlank())
                        && (cellDivisa.isEmpty() || cellDivisa.isBlank()) && (cellConcepto.isEmpty() || cellConcepto.isBlank())
                        && (cellFecha.isEmpty() || cellFecha.isBlank())) {
                    log[0] = cellCuenta;
                    log[1] = "Fallo al ingresar registro";
                    break;
                } else {
                    GenericAccount genericAccount = new GenericAccount();
                    genericAccount.setCuenta(cellCuenta);
                    genericAccount.setEmpresa(cellEmpresa);
                    genericAccount.setCodCons(cellCodCons);
                    genericAccount.setObservaciones(cellObservaciones);
                    genericAccount.setDivisa(cellDivisa);
                    genericAccount.setConcepto(cellConcepto);
                    genericAccount.setFecha(cellFecha);
                    genericAccountRepository.save(genericAccount);
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

    private boolean validarFecha(String fecha, DateFormat sdf){
        sdf.setLenient(false);
        try {
            sdf.parse(fecha);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}