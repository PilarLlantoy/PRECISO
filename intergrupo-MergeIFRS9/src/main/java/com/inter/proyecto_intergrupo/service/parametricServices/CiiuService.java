package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.ControlPanel;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.Eeff;
import com.inter.proyecto_intergrupo.model.parametric.Ciiu;
import com.inter.proyecto_intergrupo.model.parametric.UserAccount;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.admin.UserRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CiiuRepository;
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
public class CiiuService {


    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private final CiiuRepository ciiuRepository;

    public CiiuService(CiiuRepository ciiuRepository) {
        this.ciiuRepository = ciiuRepository;
    }

    public List<Ciiu> findAll(){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_ciiu as em ", Ciiu.class);
        return query.getResultList();
    }

    public List<Ciiu> findCiiubyId(Integer id){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_ciiu as em " +
                "WHERE em.id = ?",Ciiu.class);

        query.setParameter(1, id);
        return query.getResultList();
    }


    public void modifyCiiu(Ciiu toModify,Integer id){
        Ciiu toInsert = new Ciiu();
        toInsert.setCiiu(toModify.getCiiu());
        toInsert.setDescripcion(toModify.getDescripcion());
        Query query = entityManager.createNativeQuery("UPDATE nexco_ciiu SET ciiu = ? , descripcion = ? " +
                "WHERE id = ? ", Ciiu.class);
        query.setParameter(1,toInsert.getCiiu());
        query.setParameter(2, toInsert.getDescripcion());
        query.setParameter(3, id );
        try {
            query.executeUpdate();
        }catch(Exception e){

        }
    }

    public void saveCiiu(Ciiu ciiu){
        Query query = entityManager.createNativeQuery("INSERT INTO nexco_ciiu (ciiu,descripcion) VALUES (?,?)", Ciiu.class);
        query.setParameter(1, ciiu.getCiiu());
        query.setParameter(2, ciiu.getDescripcion() );
        query.executeUpdate();
    }

    public void removeCiiu(Integer id){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_ciiu WHERE id = ? ", Ciiu.class);
        query.setParameter(1, id);
        query.executeUpdate();
    }

    public void clearCiiu(User user){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_ciiu", Ciiu.class);
        query.executeUpdate();
    }

    public Page<Ciiu> getAll(Pageable pageable){
        List<Ciiu> list = findAll();
        int start = (int)pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<Ciiu> pageCiiu = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return pageCiiu;
    }

    public List<Ciiu> findByFilter(String value, String filter) {
        List<Ciiu> list=new ArrayList<Ciiu>();
        switch (filter)
        {
            case "Ciiu":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_ciiu as em " +
                        "WHERE em.ciiu LIKE ?", Ciiu.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Descripcion":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_ciiu as em " +
                        "WHERE em.descripcion LIKE ?", Ciiu.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            default:
                break;
        }
        return list;
    }


    public ArrayList<String[]> saveFileBD(InputStream file, User user) throws IOException, InvalidFormatException {
        ArrayList<String[]> list= new ArrayList<>();
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
                insert.setAccion("Inserción archivo CIIU");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("CIIU");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }
            else{
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Fallo inserción archivo CIIU");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("CIIU");
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
                String cellCiius = formatter.formatCellValue(row.getCell(0));
                String cellDescripcion = formatter.formatCellValue(row.getCell(1));
                log[0] = String.valueOf(row.getRowNum());
                if ((cellCiius.isEmpty() || cellCiius.isBlank()) && (cellDescripcion.isEmpty() || cellDescripcion.isBlank())) {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = String.valueOf(row.getRowNum());
                    log[2] = "true";
                    break;
                } else if (cellCiius.isEmpty() || cellCiius.isBlank() || cellCiius.length() > 50) {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = "1";
                    log[2] = "false";
                    break;
                } else if (cellDescripcion.isEmpty() || cellDescripcion.isBlank() || cellDescripcion.length() > 100) {
                    log[1] = "2";
                    log[2] = "false";
                    break;
                } else {
                    try {
                        log[0] = String.valueOf(row.getRowNum());
                        Long ciius = Long.parseLong(cellCiius);log[1]="1";
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
                String cellCiius = formatter.formatCellValue(row.getCell(0));
                String cellDescripcion = formatter.formatCellValue(row.getCell(1));
                if ((cellCiius.isEmpty() || cellCiius.isBlank()) && (cellDescripcion.isEmpty() || cellDescripcion.isBlank())) {
                    log[0] = cellCiius;
                    log[1] = "Fallo al ingresar registro";
                    break;
                } else {
                    if(findByFilter(cellCiius,"Ciius").size() == 0){
                        Ciiu ciiu = new Ciiu();
                        ciiu.setCiiu(cellCiius);
                        ciiu.setDescripcion(cellDescripcion);
                        ciiuRepository.save(ciiu);
                        log[0] = cellCiius;
                        log[1] = "Registro actualizado exitosamente";
                        log[2] = "true";
                    }
                    else{
                        log[0] = cellCiius;
                        log[1] = "Registro duplicado";
                    }
                }
                lista.add(log);
            } else {
                firstRow ++;
            }
        }
        return lista;
    }

}