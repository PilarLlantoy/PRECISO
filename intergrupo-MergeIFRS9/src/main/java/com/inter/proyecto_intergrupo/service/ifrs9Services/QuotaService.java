package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.Quotas;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
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
public class QuotaService {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;


    public List<Quotas> findAll(){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_cupos as em ", Quotas.class);
        return query.getResultList();
    }

    public List<Quotas> findQuotabyContratoCuenta(String contrato, String cuenta){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_cupos as em " +
                "WHERE em.contrato_origen = ? AND cuentas_puc = ?",Quotas.class);

        query.setParameter(1, contrato );
        query.setParameter(2, cuenta );
        return query.getResultList();
    }


    public void modifyQuota(Quotas toModify,String contrato, String cuenta){
        Quotas toInsert = new Quotas();
        toInsert.setCuentasPuc(toModify.getCuentasPuc());
        toInsert.setContratoOrigen(toModify.getContratoOrigen());
        toInsert.setContratoIfrs9(toModify.getContratoIfrs9());
        Query query = entityManager.createNativeQuery("UPDATE nexco_cupos SET contrato_ifrs9 = ? " +
                "WHERE contrato_origen = ? AND cuentas_puc = ? ", Quotas.class);
        query.setParameter(1,toInsert.getContratoIfrs9());
        query.setParameter(2, contrato );
        query.setParameter(3, cuenta );
        try {
                query.executeUpdate();
        }catch(Exception e){

        }
    }

    public void saveQuota(Quotas quotas){
        Query query = entityManager.createNativeQuery("INSERT INTO nexco_cupos (cuentas_puc,contrato_origen,contrato_ifrs9) VALUES (?,?,?)", Quotas.class);
        query.setParameter(1, quotas.getCuentasPuc());
        query.setParameter(2, quotas.getContratoOrigen() );
        query.setParameter(3, quotas.getContratoIfrs9());
        query.executeUpdate();
    }

    public void removeQuota(String contrato, String cuenta){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_cupos WHERE contrato_origen = ? AND cuentas_puc = ? ", Quotas.class);
        query.setParameter(1, contrato);
        query.setParameter(2, cuenta);
        query.executeUpdate();
    }

    public void clearQuota(User user){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_cupos", Quotas.class);
        query.executeUpdate();
    }

    public Page<Quotas> getAll(Pageable pageable){
        List<Quotas> list = findAll();
        int start = (int)pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<Quotas> pageSubsidiaries = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return pageSubsidiaries;
    }

    public List<Quotas> findByFilter(String value, String filter) {
        List<Quotas> list=new ArrayList<Quotas>();
        switch (filter)
        {
            case "Cuentas Puc":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_cupos as em " +
                        "WHERE em.cuentas_puc LIKE ?", Quotas.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Contrato Origen":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cupos as em " +
                        "WHERE em.contrato_origen LIKE ?", Quotas.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            case "Contrato ifrs9":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cupos as em " +
                        "WHERE em.contrato_ifrs9 LIKE ?", Quotas.class);
                query1.setParameter(1, value);

                list= query1.getResultList();
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
                list=getRows(rows1, user);
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Inserción archivo Cuenta y Subproductos");
                insert.setCentro(user.getCentro());
                insert.setComponente("Ifrs9");
                insert.setFecha(today);
                insert.setInput("Quotas");
                insert.setNombre(user.getNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }
            else{
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Fallo inserción archivo Cuenta y Subproductos");
                insert.setCentro(user.getCentro());
                insert.setComponente("Ifrs9");
                insert.setFecha(today);
                insert.setInput("Quotas");
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
                String cellCuentaPuc= formatter.formatCellValue(row.getCell(0));
                String cellContratoOrigen = formatter.formatCellValue(row.getCell(1));
                String cellContratoIFRS9= formatter.formatCellValue(row.getCell(2));
                log[0] = String.valueOf(row.getRowNum());
                if((cellCuentaPuc.isEmpty() || cellCuentaPuc.isBlank()) && (cellContratoIFRS9.isEmpty() || cellContratoIFRS9.isBlank()))
                {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = String.valueOf(row.getRowNum());
                    log[2] = "true";
                    break;
                } else if (cellCuentaPuc.isEmpty() || cellCuentaPuc.isBlank() || cellCuentaPuc.length() < 9 || cellCuentaPuc.length() > 18) {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = "1";
                    log[2] = "false";
                    break;
                } else if (!cellContratoOrigen.isEmpty() && !cellContratoOrigen.isBlank() && (cellContratoOrigen.length() < 17 || cellContratoOrigen.length() > 20)) {
                    log[1] = "2";
                    log[2] = "false";
                    break;
                } else if (cellContratoIFRS9.isEmpty() || cellContratoIFRS9.isBlank() || cellContratoIFRS9.length() < 4 || cellContratoIFRS9.length() > 20) {
                    log[1] = "2";
                    log[2] = "false";
                    break;
                } else {
                    try {
                        log[0] = String.valueOf(row.getRowNum());
                        Long cuenta = Long.parseLong(cellCuentaPuc);log[1]="1";
                        if (!(cellContratoOrigen.isEmpty() || cellContratoOrigen.isBlank())) {
                            Long.parseLong(cellContratoOrigen);
                            log[1]="2";
                        }
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

    public ArrayList getRows(Iterator<Row> rows, User user) {
        XSSFRow row;
        ArrayList lista= new ArrayList();
        clearQuota(user);
        int firstRow = 1;
        while (rows.hasNext())
        {
            String[] log=new String[3];
            log[2]="true";
            row = (XSSFRow) rows.next();
            if(firstRow!=1 && row.getCell(0)!=null)
            {
                DataFormatter formatter = new DataFormatter();
                String cellCuentaPuc= formatter.formatCellValue(row.getCell(0));
                String cellContratoOrigen = formatter.formatCellValue(row.getCell(1));
                String cellContratoIFRS9= formatter.formatCellValue(row.getCell(2));

                if((cellCuentaPuc.isEmpty() || cellCuentaPuc.isBlank()) && (cellContratoIFRS9.isEmpty() || cellContratoIFRS9.isBlank()))
                {
                    break;
                } else {
                    Quotas quotas = new Quotas();
                    quotas.setCuentasPuc(cellCuentaPuc);
                    quotas.setContratoOrigen(cellContratoOrigen);
                    quotas.setContratoIfrs9(cellContratoIFRS9);
                    saveQuota(quotas);
                    log[0] = cellCuentaPuc;
                    log[1] = "Registro actualizado exitosamente.";
                }
                lista.add(log);
            }
            else{
                firstRow=0;
            }
        }
        return lista;
    }
    
}
