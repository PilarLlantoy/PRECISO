package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.ChangeCurrency;
import com.inter.proyecto_intergrupo.model.parametric.GarantBank;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CountryRepository;
import com.inter.proyecto_intergrupo.repository.parametric.GarantBankRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class ChangeCurrencyService {

    @Autowired
    private AuditRepository auditRepository;

    @PersistenceContext
    EntityManager entityManager;

    public ChangeCurrencyService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public ArrayList<String[]> saveFileBD(InputStream  file,User user) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<String[]>();
        if (file!=null)
        {
            Iterator<Row> rows = null;
            Iterator<Row> rows1 = null;

            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            rows = sheet.iterator();
            rows1 = sheet.iterator();
            list=validarPlantilla(rows,user);
            String[] temporal= list.get(0);
            if(temporal[2].equals("0"))
            {
                getRows(rows1,user);
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Inserción archivo Cambio Valor Divisa");
                insert.setCentro(user.getCentro());
                insert.setComponente("Paramétricas");
                insert.setFecha(today);
                insert.setInput("Cambio Valor Divisa");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }else{
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Falla inserción archivo Cambio Valor Divisa");
                insert.setCentro(user.getCentro());
                insert.setComponente("Paramétricas");
                insert.setFecha(today);
                insert.setInput("Cambio Valor Divisa");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows, User user) {
        ArrayList lista= new ArrayList();
        XSSFRow row;
        int fail=0;
        int success =0;
        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(row.getRowNum()>0)
            {
                String[] log=new String[4];
                log[2]="true";

                DataFormatter formatter = new DataFormatter();
                String cellFecha = formatter.formatCellValue(row.getCell(0)).trim();
                String cellDivisa = formatter.formatCellValue(row.getCell(1)).trim();
                String cellValor = formatter.formatCellValue(row.getCell(2)).trim();

                log[0]=String.valueOf(row.getRowNum());

                if(cellFecha.length()==0 || cellFecha.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(0)+" - (1)";
                    log[2]="false";
                    log[3]="Falló Fecha Vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellDivisa.length()!=3)
                {
                    log[1]=CellReference.convertNumToColString(1)+" - (2)";
                    log[2]="false";
                    log[3]="Falló Divisa Vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellValor.length()==0 || cellValor.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(2)+" - (3)";
                    log[2]="false";
                    log[3]="Falló Valor Vacío";
                    fail++;
                    lista.add(log);
                }
                else if(log[2].equals("true")){
                    success++;
                }
            }
        }
        String[] logFinal=new String[4];
        logFinal[0]="CAMBIO VALOR DIVISA";
        logFinal[1]=String.valueOf(success);
        logFinal[2]=String.valueOf(fail);
        logFinal[3]="true";
        lista.add(logFinal);

        if(fail>0)
        {
            Date today=new Date();
            Audit insert = new Audit();
            insert.setAccion("Fallá carga masiva apartado cambio valor divisa");
            insert.setCentro(user.getCentro());
            insert.setComponente("PARAMÉTRICAS");
            insert.setFecha(today);
            insert.setInput("CAMBIO VALOR DIVISA");
            insert.setNombre(user.getPrimerNombre());
            insert.setUsuario(user.getUsuario());
            auditRepository.save(insert);
        }
        return lista;
    }

    public void getRows(Iterator<Row> rows,User user) {
        XSSFRow row;
        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(row.getRowNum()>0)
            {
                DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
                String cellFecha = formatter.formatCellValue(row.getCell(0)).trim();
                String cellDivisa = formatter.formatCellValue(row.getCell(1)).trim();

                XSSFCell cell1= row.getCell(2);
                cell1.setCellType(CellType.STRING);
                String cellValor = formatter.formatCellValue(cell1).replace(" ", "");

                Date fechaDateC = new Date();
                Calendar calendar = Calendar.getInstance();
                String[] loadDate = new String[5];

                try
                {
                    SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
                    formato.applyPattern("yyyy-MM-dd");
                    fechaDateC = formato.parse(cellFecha+"-01");
                    loadDate=cellFecha.split("-");
                }
                catch(Exception e)
                {

                }

                List<ChangeCurrency> insert = new ArrayList<ChangeCurrency>();
                try{
                    Query query = entityManager.createNativeQuery("delete from nexco_divisas_valor where MONTH(fecha) = ? AND YEAR(fecha) = ? AND divisa = ?", ChangeCurrency.class);
                    query.setParameter(1,loadDate[1]);
                    query.setParameter(2,loadDate[0]);
                    query.setParameter(3,cellDivisa);
                    query.executeUpdate();

                }catch (Exception e){
                    e.printStackTrace();
                }

                ChangeCurrency changeCurrency = new ChangeCurrency();
                changeCurrency.setFecha(fechaDateC);
                changeCurrency.setDivisa(cellDivisa);
                changeCurrency.setValor(Double.parseDouble(cellValor));

                insertChangeCurrency(changeCurrency);
            }
        }
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion("Carga masiva apartado cambio valor divisa realizada exitosamente");
        insert.setCentro(user.getCentro());
        insert.setComponente("PARAMÉTRICAS");
        insert.setFecha(today);
        insert.setInput("CAMBIO VALOR DIVISA");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public List<ChangeCurrency> findAll(){
        Query query = entityManager.createNativeQuery("select * from nexco_divisas_valor ORDER BY fecha desc, divisa ", ChangeCurrency.class);
        return query.getResultList();
    }

    public List<ChangeCurrency> findChangeCurrencyByDivisaYFecha(String year,String month, String divisa){
        Query query = entityManager.createNativeQuery("select * from nexco_divisas_valor where MONTH(fecha) = ? AND YEAR(fecha) = ? AND divisa = ?", ChangeCurrency.class);
        query.setParameter(1,month);
        query.setParameter(2,year);
        query.setParameter(3,divisa);
        return query.getResultList();
    }

    public void modifyChangeCurrency(ChangeCurrency toModify,String id, User user,String fecha){
        ChangeCurrency toInsert = new ChangeCurrency();
        toInsert.setFecha(toModify.getFecha());
        toInsert.setDivisa(toModify.getDivisa());
        toInsert.setValor(toModify.getValor());
        //if(toModify.getFecha()!=fecha ||toModify.getDivisa()!=id)
        {
            Query query = entityManager.createNativeQuery("delete from nexco_divisas_valor where fecha = ? AND divisa = ?", ChangeCurrency.class);
            query.setParameter(1,fecha);
            query.setParameter(2,id);
            query.executeUpdate();
        }
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Modificacion registro tabla Cambio Valor Divisas");
        insert.setCentro(user.getCentro());
        insert.setComponente("PARAMÉTRICAS");
        insert.setFecha(today);
        insert.setInput("CAMBIO VALOR DIVISA");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);

        saveChangeCurrency(toInsert);

    }

    public void saveChangeCurrency(ChangeCurrency toInsert){
        Query query = entityManager.createNativeQuery("INSERT INTO nexco_divisas_valor (fecha, divisa, valor) VALUES (?,?,?)", ChangeCurrency.class);
        query.setParameter(1,toInsert.getFecha());
        query.setParameter(2,toInsert.getDivisa());
        query.setParameter(3,toInsert.getValor());
        query.executeUpdate();
    }

    public void removeChangeCurrency(Date fecha,String divisa, User user){

        Query query = entityManager.createNativeQuery("delete from nexco_divisas_valor where fecha = ? AND divisa = ?", ChangeCurrency.class);
        query.setParameter(1,fecha);
        query.setParameter(2,divisa);
        query.executeUpdate();

        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Eliminar registro tabla Cambio Valor Divisa");
        insert.setCentro(user.getCentro());
        insert.setComponente("PARAMÉTRICAS");
        insert.setFecha(today);
        insert.setInput("CAMBIO VALOR DIVISA");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public boolean insertChangeCurrency(ChangeCurrency toInsert){

        boolean state = false;
        Query verify = entityManager.createNativeQuery("SELECT * FROM nexco_divisas_valor WHERE fecha = ? AND divisa = ?");
        verify.setParameter(1,toInsert.getFecha());
        verify.setParameter(2,toInsert.getDivisa());

        if(verify.getResultList().isEmpty()){
            try {
                saveChangeCurrency(toInsert);
                state = true;
            } catch (Exception e){
                e.printStackTrace();
            }

        }

        return state;
    }

    public void clearChangeCurrency(User user){
        Query query = entityManager.createNativeQuery("delete from nexco_divisas_valor", ChangeCurrency.class);
        query.executeUpdate();

        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Limpiar tabla Cambio Valor Divisa");
        insert.setCentro(user.getCentro());
        insert.setComponente("PARAMÉTRICAS");
        insert.setFecha(today);
        insert.setInput("CAMBIO VALOR DIVISA");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public List<ChangeCurrency> findByFilter(String value, String filter) {
        List<ChangeCurrency> list=new ArrayList<ChangeCurrency>();
        switch (filter)
        {
            case "Fecha":
                if(value.contains("-") && !value.contains("%"))
                {
                    String[] v = value.split("-");
                    Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_divisas_valor as em " +
                            "WHERE YEAR(em.fecha) LIKE ? AND MONTH(em.fecha) LIKE ? ORDER BY fecha desc, divisa", ChangeCurrency.class);
                    query.setParameter(1, v[0]);
                    if(!v[1].equals("10"))
                        query.setParameter(2, v[1].replace("0",""));
                    else
                        query.setParameter(2, v[1]);
                    list= query.getResultList();
                }
                else {
                    Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_divisas_valor as em " +
                            "WHERE em.fecha LIKE ? ORDER BY fecha desc, divisa", ChangeCurrency.class);
                    query.setParameter(1, value);
                    list= query.getResultList();
                }

                break;
            case "Divisa":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_divisas_valor as em " +
                        "WHERE em.divisa LIKE ? ORDER BY fecha desc, divisa", ChangeCurrency.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            case "Valor":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_divisas_valor as em " +
                        "WHERE em.valor LIKE ? ORDER BY fecha desc, divisa", ChangeCurrency.class);
                query1.setParameter(1, value);

                list= query1.getResultList();
                break;
            default:
                break;
        }

        return list;
    }
}
