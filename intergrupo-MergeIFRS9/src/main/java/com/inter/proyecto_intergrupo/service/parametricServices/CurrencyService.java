package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Currency;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CurrencyRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
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
import java.util.*;

@Service
@Transactional
public class CurrencyService {

    @Autowired
    private final CurrencyRepository currencyRepository;

    @Autowired
    private AuditRepository auditRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    public CurrencyService(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
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
                insert.setAccion("Inserción archivo Transformación de Divisas");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Transformación de Divisas");
                insert.setNombre(user.getNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }else {
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Fallo inserción archivo Transformación de Divisas");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Transformación de Divisas");
                insert.setNombre(user.getNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows) {
        ArrayList lista= new ArrayList();
        XSSFRow row;
        int firstRow=1;
        String[] log=new String[3];
        log[0]="0";
        log[1]="0";
        log[2]="false";
        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(firstRow!=1)
            {
                DataFormatter formatter = new DataFormatter();
                String cellCodigoDivisa = formatter.formatCellValue(row.getCell(0));
                String cellNombreDivisa = formatter.formatCellValue(row.getCell(1));
                String cellNeocon = formatter.formatCellValue(row.getCell(2));

                log[0]=String.valueOf(row.getRowNum());
                if((cellCodigoDivisa.isEmpty() || cellCodigoDivisa.isBlank()) && (cellNombreDivisa.isEmpty() || cellNombreDivisa.isBlank()) && (cellNeocon.isEmpty() || cellNeocon.isBlank()))
                {
                    log[2]="true";
                    break;
                }
                else if(cellCodigoDivisa.isEmpty() || cellCodigoDivisa.isBlank() ||cellCodigoDivisa.trim().length()!=3)
                {
                    log[1]="1";
                    log[2]="false";
                    break;
                }
                else if(cellNombreDivisa.isEmpty() || cellNombreDivisa.isBlank() ||cellNombreDivisa.length()>254)
                {
                    log[1]="2";
                    log[2]="false";
                    break;
                }
                else if(cellNeocon.isEmpty() || cellNeocon.isBlank() ||cellNeocon.length()>254)
                {
                    log[1]="3";
                    log[2]="false";
                    break;
                }
                else{
                    log[2]="true";
                }
            }
            else
            {
                firstRow=0;
            }
        }
        lista.add(log);
        return lista;
    }

    public ArrayList getRows(Iterator<Row> rows) {
        XSSFRow row;
        Date today=new Date();
        ArrayList lista= new ArrayList();
        int firstRow=1;
        while (rows.hasNext())
        {
            String[] log=new String[3];
            log[2]="true";
            row = (XSSFRow) rows.next();

            if(firstRow!=1 && row.getCell(0)!=null)
            {
                DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
                String cellCodigoDivisa = formatter.formatCellValue(row.getCell(0));
                String cellNombreDivisa = formatter.formatCellValue(row.getCell(1));
                String cellNeocon = formatter.formatCellValue(row.getCell(2));
                if((cellCodigoDivisa.isEmpty() || cellCodigoDivisa.isBlank()) && (cellNombreDivisa.isEmpty() || cellNombreDivisa.isBlank()) && (cellNeocon.isEmpty() || cellNeocon.isBlank()))
                {
                    break;
                }
                else if(currencyRepository.findAllById(cellCodigoDivisa)!=null)
                {
                    Currency currency = currencyRepository.findAllById(cellCodigoDivisa);
                    currency.setId(cellCodigoDivisa);
                    currency.setNombre(cellNombreDivisa);
                    currency.setDivisaNeocon(cellNeocon);
                    currencyRepository.save(currency);
                    log[0] = String.valueOf(currency.getId());
                    log[1] = "Registro actualizado exitosamente.";
                }
                else{
                    log[0]=cellCodigoDivisa;
                    log[1]="Fallo al actualizar el registro, No se encontro la divisa";
                }
                lista.add(log);
            }
            else{
                firstRow=0;
            }
        }
        return lista;
    }

    public List<Currency> findAll(){return currencyRepository.findAll();}
    public Currency findCurrency(String divisa){
        return currencyRepository.findAllById(divisa);
    }


    public Currency findCurrencyById(String id){
        return currencyRepository.findAllById(id);
    }

    public void modifyCurrencies(Currency toModify,String id, User user){

        Currency toInsert = new Currency();
        toInsert.setId(toModify.getId().toUpperCase(Locale.ROOT));
        toInsert.setNombre(toModify.getNombre().toUpperCase(Locale.ROOT));
        toInsert.setDivisaNeocon(toModify.getDivisaNeocon().toUpperCase(Locale.ROOT));
        Query query = entityManager.createNativeQuery("UPDATE nexco_divisas SET id_divisa = ? , nombre_divisa = ? , divisa_neocon = ? " +
                "WHERE id_divisa = ?", Currency.class);
        query.setParameter(1, toInsert.getId() );
        query.setParameter(2, toInsert.getNombre());
        query.setParameter(3, toInsert.getDivisaNeocon());
        query.setParameter(4, id);
        query.executeUpdate();
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Modificacion registro archivo transformación de Divisas");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Histórico Cruce Filiales");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);

    }

    public Currency saveCurrency(Currency currency, User user){
        currency.setId(currency.getId().toUpperCase(Locale.ROOT));
        currency.setNombre(currency.getNombre().toUpperCase(Locale.ROOT));
        currency.setDivisaNeocon(currency.getDivisaNeocon().toUpperCase(Locale.ROOT));
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Insertar registro archivo transformación de Divisas");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Histórico Cruce Filiales");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        return currencyRepository.save(currency);
    }

    public void removeCurrency(String id, User user){
        Query query = entityManager.createNativeQuery("DELETE from nexco_divisas " +
                "WHERE id_divisa = ?", Currency.class);
        query.setParameter(1, id );
        query.executeUpdate();
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Eliminar registro archivo transformación de Divisas");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Histórico Cruce Filiales");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);

    }

    public void clearCurrency(User user){
        //currencyRepository.deleteAll();
        Query query = entityManager.createNativeQuery("DELETE from nexco_divisas", Currency.class);
        query.executeUpdate();
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Limpiar archivo transformación de Divisas");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Histórico Cruce Filiales");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public Page<Currency> getAll(Pageable pageable){
        return currencyRepository.findAll(pageable);
    }

    public List<Currency> findByFilter(String value, String filter) {
        List<Currency> list=new ArrayList<Currency>();
        switch (filter)
        {
            case "Código Divisa":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_divisas as em " +
                        "WHERE em.id_divisa LIKE ?", Currency.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Nombre Divisa":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_divisas as em " +
                        "WHERE em.nombre_divisa LIKE ?", Currency.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            case "Divisa Neocon":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_divisas as em " +
                        "WHERE em.divisa_neocon LIKE ?", Currency.class);
                query1.setParameter(1, value);

                list= query1.getResultList();
                break;
            default:
                break;
        }
        return list;
    }
}
