package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Indicators;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.IndicatorsRepository;
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
public class IndicatorsService {

    @Autowired
    private IndicatorsRepository indicatorsRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    public IndicatorsService(IndicatorsRepository indicatorsRepository) {
        this.indicatorsRepository = indicatorsRepository;
    }

    public ArrayList<String[]> saveFileBD(InputStream  file, User user) throws IOException, InvalidFormatException {
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
                insert.setAccion("Inserción archivo Indicadores Intergrupo");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Indicadores Intergrupo");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }else{
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Fallo en inserción archivo Indicadores Intergrupo");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Indicadores Intergrupo");
                insert.setNombre(user.getPrimerNombre());
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
                DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
                String cellCuentaNeocon = formatter.formatCellValue(row.getCell(0));
                String cellSigno= formatter.formatCellValue(row.getCell(1));
                log[0]=String.valueOf(row.getRowNum());

                if((cellCuentaNeocon.isEmpty() || cellCuentaNeocon.isBlank()) && (cellSigno.isEmpty() || cellSigno.isBlank()))
                {
                    log[1]=String.valueOf(row.getRowNum());
                    log[2]="true";
                    break;
                }
                else if(cellCuentaNeocon.isEmpty() || cellCuentaNeocon.isBlank() ||cellCuentaNeocon.length()>255)
                {
                    log[1]="1";
                    log[2]="false";
                    break;
                }
                else if(cellSigno.isEmpty() || cellSigno.isBlank() ||(!cellSigno.trim().equals("+") && !cellSigno.trim().equals("-")))
                {
                    log[1]="2";
                    log[2]="false";
                    break;
                }
                else
                {
                    try
                    {
                        log[1]="1"; Long cuentaLocal = Long.parseLong(cellCuentaNeocon);
                        log[2]="true";
                    }
                    catch(Exception e){
                        log[2]="falseFormat";
                        lista.add(log);
                        return lista;
                    }
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
                String cellCuentaNeocon = formatter.formatCellValue(row.getCell(0));
                String cellSigno= formatter.formatCellValue(row.getCell(1));
                log[0] = cellCuentaNeocon;

                if((cellCuentaNeocon.isEmpty() || cellCuentaNeocon.isBlank()) && (cellSigno.isEmpty() || cellSigno.isBlank()))
                {
                    break;
                }
                else if(indicatorsRepository.findByCuentaNeocon(cellCuentaNeocon)==null) {
                    Indicators indicators = new Indicators();
                    indicators.setCuentaNeocon(cellCuentaNeocon);
                    indicators.setSigno(cellSigno);
                    indicatorsRepository.save(indicators);
                    log[1] = "Registro insertado exitosamente.";
                }
                else {
                    log[1] = "Cuenta Neocon ya se encuentra creada";
                }
                lista.add(log);

            }
            else{
                firstRow=0;
            }
        }
        return lista;
    }

    public List<Indicators> findAll(){
        return indicatorsRepository.findAll();
    }

    public Indicators findIndicatorsByCuentaNeocon(String id){
        return indicatorsRepository.findByCuentaNeocon(id);
    }

    public Indicators modifyIndicators(Indicators toModify,String id, User user){
        Indicators toInsert = new Indicators();
        toInsert.setCuentaNeocon(toModify.getCuentaNeocon());
        toInsert.setSigno(toModify.getSigno());
        if(toModify.getCuentaNeocon()!=id)
            indicatorsRepository.deleteById(id);
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Modificar registro tabla Indicadores Intergrupo");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Indicadores Intergrupo");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        return indicatorsRepository.save(toInsert);
    }

    public Indicators modifySign(Indicators toModify){
        if (toModify.getSigno().equals("+")){
            toModify.setSigno("-");
        }
        else{
            toModify.setSigno("+");
        }
        return indicatorsRepository.save(toModify);
    }

    public Indicators saveIndicators(Indicators indicators, User user){
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Guardar registro tabla Indicadores Intergrupo");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Indicadores Intergrupo");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        return indicatorsRepository.save(indicators);
    }

    public void removeIndicators(String id, User user){
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Eliminar registro tabla Indicadores Intergrupo");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Indicadores Intergrupo");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        indicatorsRepository.deleteById(id);
    }

    public void clearIndicators(User user){
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Limpiar tabla Indicadores Intergrupo");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Indicadores Intergrupo");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        indicatorsRepository.deleteAll();
    }

    public Page<Indicators> getAll(Pageable pageable){
        return indicatorsRepository.findAll(pageable);
    }

    public List<Indicators> findByFilter(String value, String filter) {
        List<Indicators> list=new ArrayList<Indicators>();
        switch (filter)
        {
            case "Cuenta Neocon":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_indicadores as em " +
                        "WHERE em.cuenta_neocon LIKE ?", Indicators.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Signo":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_indicadores as em " +
                        "WHERE em.signo LIKE ?", Indicators.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            default:
                break;
        }
        return list;
    }

}
