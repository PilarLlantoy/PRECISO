package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Reclassification;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.model.parametric.YntpSociety;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ReclassificationRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ThirdRepository;
import com.inter.proyecto_intergrupo.repository.parametric.YntpSocietyRepository;
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
public class ReclassificationService {

    @Autowired
    private ReclassificationRepository reclassificationRepository;

    @Autowired
    private AuditRepository auditRepository;

    @PersistenceContext
    EntityManager entityManager;

    public ReclassificationService(ReclassificationRepository reclassificationRepository) {
        this.reclassificationRepository = reclassificationRepository;
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
                insert.setAccion("Inserción archivo Reclasificaciones Local");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Reclasificaciones Local");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }else{
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Fallo Inserción archivo Reclasificaciones Local");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Reclasificaciones Local");
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
                String cellNit = formatter.formatCellValue(row.getCell(0));
                String cellCuentaLocal = formatter.formatCellValue(row.getCell(1));
                String cellCuentaReclasificacion = formatter.formatCellValue(row.getCell(2));
                log[0]=String.valueOf(row.getRowNum());
                if((cellNit.isEmpty() || cellNit.isBlank()) && (cellCuentaLocal.isEmpty() || cellCuentaLocal.isBlank()) && (cellCuentaReclasificacion.isEmpty() || cellCuentaReclasificacion.isBlank()))
                {
                    log[1]=String.valueOf(row.getRowNum());
                    log[2]="true";
                    break;
                }
                else if(cellNit.isEmpty() || cellNit.isBlank() ||cellNit.length()>14)
                {
                    log[1]="1";
                    log[2]="false";
                    break;
                }
                else if(cellCuentaLocal.isEmpty() || cellCuentaLocal.isBlank() ||cellCuentaLocal.length()>14)
                {
                    log[1]="2";
                    log[2]="false";
                    break;
                }
                else if(cellCuentaReclasificacion.isEmpty() || cellCuentaReclasificacion.isBlank() ||cellCuentaReclasificacion.length()>14)
                {
                    log[1]="3";
                    log[2]="false";
                    break;
                }
                else
                {
                    try
                    {
                        log[1]="1"; Long nit = Long.parseLong(cellNit);
                        log[1]="2"; Long cuenta = Long.parseLong(cellCuentaLocal);
                        log[1]="3"; Long reclasificacion = Long.parseLong(cellCuentaReclasificacion);
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
                String cellNit = formatter.formatCellValue(row.getCell(0));
                String cellCuentaLocal = formatter.formatCellValue(row.getCell(1));
                String cellCuentaReclasificacion = formatter.formatCellValue(row.getCell(2));
                log[0] = cellNit;
                if((cellNit.isEmpty() || cellNit.isBlank()) && (cellCuentaLocal.isEmpty() || cellCuentaLocal.isBlank()) && (cellCuentaReclasificacion.isEmpty() || cellCuentaReclasificacion.isBlank()))
                {
                    break;
                }
                else if(reclassificationRepository.findByNitContraparte(Long.parseLong(cellNit))==null)
                {
                    Reclassification reclassification = new Reclassification();
                    reclassification.setNitContraparte(Long.parseLong(cellNit));
                    reclassification.setCuentaLocal(cellCuentaLocal);
                    reclassification.setCuentaLocalReclasificada(cellCuentaReclasificacion);
                    reclassificationRepository.save(reclassification);
                    log[1] = "Registro insertado exitosamente.";
                    lista.add(log);
                }
                else
                {
                    log[1]="Fallo al ingresar registro, NIT existente";
                    lista.add(log);
                }
            }
            else{
                firstRow=0;
            }
        }
        return lista;
    }

    public List<Reclassification> findAll(){
        return reclassificationRepository.findAll();
    }

    public Reclassification findReclassificationByNit(Long id){
        return reclassificationRepository.findByNitContraparte(id);
    }

    public Reclassification modifyReclassification(Reclassification toModify,Long id, User user){
        Reclassification toInsert = new Reclassification();
        toInsert.setNitContraparte(toModify.getNitContraparte());
        toInsert.setCuentaLocal(toModify.getCuentaLocal());
        toInsert.setCuentaLocalReclasificada(toModify.getCuentaLocalReclasificada());
        if(toModify.getNitContraparte()!=id)
            reclassificationRepository.deleteById(id);
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Modificar registro tabla Reclasificaciones Local");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Reclasificaciones Local");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        return reclassificationRepository.save(toInsert);
    }

    public Reclassification saveReclassification(Reclassification reclassification, User user){
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Guardar registro tabla Reclasificaciones Local");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Reclasificaciones Local");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        return reclassificationRepository.save(reclassification);
    }

    public void removeReclassification(Long id, User user){
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Eliminar registro tabla Reclasificaciones Local");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Reclasificaciones Local");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        reclassificationRepository.deleteById(id);
    }

    public void clearReclassification(User user){
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Limpiar tabla Reclasificaciones Local");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Reclasificaciones Local");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        reclassificationRepository.deleteAll();
    }

    public Page<Reclassification> getAll(Pageable pageable){
        return reclassificationRepository.findAll(pageable);
    }

    public List<Reclassification> findByFilter(String value, String filter) {
        List<Reclassification> list=new ArrayList<Reclassification>();
        switch (filter)
        {
            case "NIT Contraparte":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_reclasificaciones as em " +
                        "WHERE em.nit_contraparte LIKE ?", Reclassification.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Cuenta Local":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_reclasificaciones as em " +
                        "WHERE em.cuenta_local LIKE ?", Reclassification.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            case "Cuenta Local Reclasificada":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_reclasificaciones as em " +
                        "WHERE em.cuenta_local_reclasificada LIKE ?", Reclassification.class);
                query1.setParameter(1, value);

                list= query1.getResultList();
                break;
            default:
                break;
        }
        return list;
    }

}
