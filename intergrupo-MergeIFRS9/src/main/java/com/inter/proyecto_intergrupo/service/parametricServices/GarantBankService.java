package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.GarantBank;
import com.inter.proyecto_intergrupo.model.parametric.YntpSociety;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.GarantBankRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CountryRepository;
import com.inter.proyecto_intergrupo.repository.parametric.YntpSocietyRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
@Transactional
public class GarantBankService {

    @Autowired
    private GarantBankRepository garantBankRepository;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private CountryRepository countryRepository;

    @PersistenceContext
    EntityManager entityManager;

    public GarantBankService(GarantBankRepository garantBankRepository, AuditRepository auditRepository) {
        this.garantBankRepository = garantBankRepository;
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
                insert.setAccion("Inserción archivo Banco garante");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Banco garante");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }else{
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Falla inserción archivo Banco garante");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Banco garante");
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
                String cellNit = formatter.formatCellValue(row.getCell(0)).trim();
                String cellNombreBanco = formatter.formatCellValue(row.getCell(1)).trim();
                String cellNombreSimilar = formatter.formatCellValue(row.getCell(2)).trim();
                String cellPais = formatter.formatCellValue(row.getCell(3)).trim();

                log[0]=String.valueOf(row.getRowNum());

                if(cellNit.length()==0 || cellNit.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(0)+" - (1)";
                    log[2]="false";
                    log[3]="Falló Nit Vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellNombreBanco.length()==0 || cellNombreBanco.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(1)+" - (2)";
                    log[2]="false";
                    log[3]="Falló Nombre Banco Vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellNombreSimilar.length()==0 ||cellNombreSimilar.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(2)+" - (3)";
                    log[2]="false";
                    log[3]="Falló Nombre Similar Vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellPais.length()==0 ||cellPais.length()>255 || countryRepository.findAllById(Integer.valueOf(cellPais))==null)
                {
                    log[1]=CellReference.convertNumToColString(3)+" - (4)";
                    log[2]="false";
                    log[3]="Falló País vacío o no se encuentra en la parametría de Países";
                    fail++;
                    lista.add(log);
                }
                else if(log[2].equals("true")){
                    success++;
                }
            }
        }
        String[] logFinal=new String[4];
        logFinal[0]="BANCO GARANTE";
        logFinal[1]=String.valueOf(success);
        logFinal[2]=String.valueOf(fail);
        logFinal[3]="true";
        lista.add(logFinal);

        if(fail>0)
        {
            Date today=new Date();
            Audit insert = new Audit();
            insert.setAccion("Fallá carga masiva apartado banco garante");
            insert.setCentro(user.getCentro());
            insert.setComponente("PARAMÉTRICAS");
            insert.setFecha(today);
            insert.setInput("BANCO GARANTE");
            insert.setNombre(user.getPrimerNombre());
            insert.setUsuario(user.getUsuario());
            auditRepository.save(insert);
        }
        return lista;
    }

    public void getRows(Iterator<Row> rows,User user) {
        XSSFRow row;
        int firstRow=1;
        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(row.getRowNum()>0)
            {
                DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
                String cellNit = formatter.formatCellValue(row.getCell(0)).trim();
                String cellNombreBanco = formatter.formatCellValue(row.getCell(1)).trim();
                String cellNombreSimilar = formatter.formatCellValue(row.getCell(2)).trim();
                String cellPais = formatter.formatCellValue(row.getCell(3)).trim();

                List<GarantBank> insert = new ArrayList<>();
                try{
                    insert=garantBankRepository.findAllByNit(cellNit);
                    if(insert.size()>0)
                    {
                        garantBankRepository.delete(insert.get(0));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

                GarantBank garantBank = new GarantBank();
                garantBank.setNit(cellNit);
                garantBank.setNombreBancoReal(cellNombreBanco);
                garantBank.setNombreSimilar(cellNombreSimilar);
                garantBank.setPais(cellPais);
                garantBankRepository.save(garantBank);

            }
        }
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion("Carga masiva apartado banco garante realizada exitosamente");
        insert.setCentro(user.getCentro());
        insert.setComponente("PARAMÉTRICAS");
        insert.setFecha(today);
        insert.setInput("BANCO GARANTE");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public List<GarantBank> findAll(){
        return garantBankRepository.findAll();
    }

    public GarantBank findGarantBankByNombreSmiliar(String nombre){
        return garantBankRepository.findByNombreSimilar(nombre);
    }

    public GarantBank modifyGarantBank(GarantBank toModify,String id, User user,String pais){
        GarantBank toInsert = new GarantBank();
        toInsert.setNit(toModify.getNit());
        toInsert.setNombreBancoReal(toModify.getNombreBancoReal());
        toInsert.setNombreSimilar(toModify.getNombreSimilar());
        toInsert.setPais(pais);
        if(toModify.getNombreSimilar()!=id)
            garantBankRepository.deleteById(id);
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Modificacion registro tabla Banco garante");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Banco garante");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        return garantBankRepository.save(toInsert);
    }

    public GarantBank saveGarantBank(GarantBank garantBank){
        return garantBankRepository.save(garantBank);
    }

    public void removeGarantBank(String id, User user){
        garantBankRepository.deleteById(id);
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Eliminar registro tabla Banco garante");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Banco garante");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public boolean insertGarantBank(GarantBank toInsert){

        boolean state = false;

        Query verify = entityManager.createNativeQuery("SELECT * FROM nexco_banco_garante WHERE nombre_similar = ?");
        verify.setParameter(1,toInsert.getNombreSimilar());

        if(verify.getResultList().isEmpty()){
            try {
                Query insertGB = entityManager.createNativeQuery("INSERT INTO nexco_banco_garante (nombre_similar,nit,nombre_banco_real,pais) VALUES (?,?,?,?)");
                insertGB.setParameter(1, toInsert.getNombreSimilar());
                insertGB.setParameter(2, toInsert.getNit());
                insertGB.setParameter(3, toInsert.getNombreBancoReal());
                insertGB.setParameter(4, toInsert.getPais());

                insertGB.executeUpdate();
                state = true;
            } catch (Exception e){
                e.printStackTrace();
            }

        }

        return state;
    }

    public void clearGarantBank(User user){
        garantBankRepository.deleteAll();
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Limpiar tabla Banco garante");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Banco garante");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public Page<GarantBank> getAll(Pageable pageable){
        return garantBankRepository.findAll(pageable);
    }

    public List<GarantBank> findByFilter(String value, String filter) {
        List<GarantBank> list=new ArrayList<GarantBank>();
        switch (filter)
        {
            case "NIT":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_banco_garante as em " +
                        "WHERE em.nit LIKE ?", GarantBank.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Nombre Banco Real":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_banco_garante as em " +
                        "WHERE em.nombre_banco_real LIKE ?", GarantBank.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            case "Nombre Similar":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_banco_garante as em " +
                        "WHERE em.nombre_similar LIKE ?", GarantBank.class);
                query1.setParameter(1, value);

                list= query1.getResultList();
                break;
            case "País":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_banco_garante as em " +
                        "WHERE em.pais LIKE ?", GarantBank.class);
                query2.setParameter(1, value);

                list= query2.getResultList();
                break;
            default:
                break;
        }

        return list;
    }

}
