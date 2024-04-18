package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.ControlPanel;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.Eeff;
import com.inter.proyecto_intergrupo.model.parametric.AccountAndByProduct;
import com.inter.proyecto_intergrupo.model.parametric.AccountAndByProduct;
import com.inter.proyecto_intergrupo.model.parametric.UserAccount;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.admin.UserRepository;
import com.inter.proyecto_intergrupo.repository.parametric.AccountAndByProductRepository;
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
public class AccountAndByProductService {


    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private final AccountAndByProductRepository accountAndByProductRepository;

    public AccountAndByProductService(AccountAndByProductRepository accountAndByProductRepository) {
        this.accountAndByProductRepository = accountAndByProductRepository;
    }

    public List<AccountAndByProduct> findAll(){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuenta_subproducto_local as em ", AccountAndByProduct.class);
        return query.getResultList();
    }

    public List<AccountAndByProduct> findAccountAndByProductbyId(Integer id){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuenta_subproducto_local as em " +
                "WHERE em.id = ?",AccountAndByProduct.class);

        query.setParameter(1, id);
        return query.getResultList();
    }


    public void modifyAccountAndByProduct(AccountAndByProduct toModify,Integer id){
        AccountAndByProduct toInsert = new AccountAndByProduct();
        toInsert.setCuenta(toModify.getCuenta());
        toInsert.setSubproducto(toModify.getSubproducto());
        Query query = entityManager.createNativeQuery("UPDATE nexco_cuenta_subproducto_local SET cuenta = ? , subproducto = ? " +
                "WHERE id = ? ", AccountAndByProduct.class);
        query.setParameter(1,toInsert.getCuenta());
        query.setParameter(2, toInsert.getSubproducto());
        query.setParameter(3, id );
        try {
            query.executeUpdate();
        }catch(Exception e){

        }
    }

    public void saveAccountAndByProduct(AccountAndByProduct AccountAndByProducts){
        Query query = entityManager.createNativeQuery("INSERT INTO nexco_cuenta_subproducto_local (cuenta,subproducto) VALUES (?,?)", AccountAndByProduct.class);
        query.setParameter(1, AccountAndByProducts.getCuenta());
        query.setParameter(2, AccountAndByProducts.getSubproducto() );
        query.executeUpdate();
    }

    public void removeAccountAndByProduct(Integer id){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_cuenta_subproducto_local WHERE id = ? ", AccountAndByProduct.class);
        query.setParameter(1, id);
        query.executeUpdate();
    }

    public void clearAccountAndByProduct(User user){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_cuenta_subproducto_local", AccountAndByProduct.class);
        query.executeUpdate();
    }

    public Page<AccountAndByProduct> getAll(Pageable pageable){
        List<AccountAndByProduct> list = findAll();
        int start = (int)pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<AccountAndByProduct> pageAccountAndByProduct = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return pageAccountAndByProduct;
    }

    public List<AccountAndByProduct> findByFilter(String value, String filter) {
        List<AccountAndByProduct> list=new ArrayList<AccountAndByProduct>();
        switch (filter)
        {
            case "Cuenta":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuenta_subproducto_local as em " +
                        "WHERE em.cuenta LIKE ?", AccountAndByProduct.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Subproducto":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuenta_subproducto_local as em " +
                        "WHERE em.subproducto LIKE ?", AccountAndByProduct.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
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
                clearAccountAndByProduct(user);
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
                String cellCuenta = formatter.formatCellValue(row.getCell(0));
                String cellSubproducto = formatter.formatCellValue(row.getCell(1));
                log[0] = String.valueOf(row.getRowNum());
                if ((cellCuenta.isEmpty() || cellCuenta.isBlank()) && (cellSubproducto.isEmpty() || cellSubproducto.isBlank())) {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = String.valueOf(row.getRowNum());
                    log[2] = "true";
                    break;
                } else if (cellCuenta.isEmpty() || cellCuenta.isBlank() || cellCuenta.length() > 18 || cellCuenta.length() < 4) {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = "1";
                    log[2] = "false";
                    break;
                } else if (cellSubproducto.isEmpty() || cellSubproducto.isBlank() || cellSubproducto.length() > 50) {
                    log[1] = "2";
                    log[2] = "false";
                    break;
                } else {
                    try {
                        log[0] = String.valueOf(row.getRowNum());
                        Long cuenta = Long.parseLong(cellCuenta);log[1]="1";
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
                String cellSubproducto = formatter.formatCellValue(row.getCell(1));
                if ((cellCuenta.isEmpty() || cellCuenta.isBlank()) && (cellSubproducto.isEmpty() || cellSubproducto.isBlank())) {
                    log[0] = cellCuenta;
                    log[1] = "Fallo al ingresar registro";
                    break;
                } else {
                    AccountAndByProduct accountAndByProduct = new AccountAndByProduct();
                    accountAndByProduct.setCuenta(cellCuenta);
                    accountAndByProduct.setSubproducto(cellSubproducto);
                    accountAndByProductRepository.save(accountAndByProduct);
                    log[0] = cellCuenta;
                    log[1] = "Registro insertado exitosamente";
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
