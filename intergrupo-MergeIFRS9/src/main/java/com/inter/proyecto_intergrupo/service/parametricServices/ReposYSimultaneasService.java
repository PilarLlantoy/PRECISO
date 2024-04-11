package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.ReposYSimultaneas;
import com.inter.proyecto_intergrupo.model.parametric.ReposYSimultaneas;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ReposYSimultaneasRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ReposYSimultaneasRepository;
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
public class ReposYSimultaneasService {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private final ReposYSimultaneasRepository reposYSimultaneasRepository;

    public ReposYSimultaneasService(ReposYSimultaneasRepository reposYSimultaneasRepository) {
        this.reposYSimultaneasRepository = reposYSimultaneasRepository;
    }


    public List<ReposYSimultaneas> findAll(){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_repos_simultaneas as em ", ReposYSimultaneas.class);
        return query.getResultList();
    }

    public List<ReposYSimultaneas> findReposYSimultaneasbyId(Integer id){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_repos_simultaneas as em " +
                "WHERE em.id = ?",ReposYSimultaneas.class);

        query.setParameter(1, id);
        return query.getResultList();
    }


    public void modifyReposYSimultaneas(ReposYSimultaneas toModify,Integer id){
        ReposYSimultaneas toInsert = new ReposYSimultaneas();
        toInsert.setCuenta(toModify.getCuenta());
        toInsert.setCodNombre(toModify.getCodNombre());
        toInsert.setCodigo(toModify.getCodigo());
        toInsert.setDescripcion(toModify.getDescripcion());
        toInsert.setCuentaPyG(toModify.getCuentaPyG());
        toInsert.setDescripcionCuentaPyG(toModify.getDescripcionCuentaPyG());
        Query query = entityManager.createNativeQuery("UPDATE nexco_repos_simultaneas SET cuenta = ? , cod_nombre = ? , codigo = ?  , descripcion = ? , cuenta_pyg = ? , descripcion_cuenta_pyg = ? " +
                "WHERE id = ? ", ReposYSimultaneas.class);
        query.setParameter(1,toInsert.getCuenta());
        query.setParameter(2, toInsert.getCodNombre());
        query.setParameter(3, toInsert.getCodigo());
        query.setParameter(4,toInsert.getDescripcion());
        query.setParameter(5, toInsert.getCuentaPyG());
        query.setParameter(6, toInsert.getDescripcionCuentaPyG());
        query.setParameter(7, id );
        try {
            query.executeUpdate();
        }catch(Exception e){

        }
    }

    public void saveReposYSimultaneas(ReposYSimultaneas ReposYSimultaneass){
        Query query = entityManager.createNativeQuery("INSERT INTO nexco_repos_simultaneas (cuenta,cod_nombre,codigo,descripcion,cuenta_pyg,descripcion_cuenta_pyg) VALUES (?,?,?,?,?,?)", ReposYSimultaneas.class);
        query.setParameter(1, ReposYSimultaneass.getCuenta());
        query.setParameter(2, ReposYSimultaneass.getCodNombre() );
        query.setParameter(3, ReposYSimultaneass.getCodigo());
        query.setParameter(4, ReposYSimultaneass.getDescripcion());
        query.setParameter(5, ReposYSimultaneass.getCuentaPyG());
        query.setParameter(6, ReposYSimultaneass.getDescripcionCuentaPyG());
        query.executeUpdate();
    }

    public void removeReposYSimultaneas(Integer id){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_repos_simultaneas WHERE id = ? ", ReposYSimultaneas.class);
        query.setParameter(1, id);
        query.executeUpdate();
    }

    public void clearReposYSimultaneas(User user){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_repos_simultaneas", ReposYSimultaneas.class);
        query.executeUpdate();
    }

    public Page<ReposYSimultaneas> getAll(Pageable pageable){
        List<ReposYSimultaneas> list = findAll();
        int start = (int)pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<ReposYSimultaneas> pageReposYSimultaneas = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return pageReposYSimultaneas;
    }

    public List<ReposYSimultaneas> findByFilter(String value, String filter) {
        List<ReposYSimultaneas> list=new ArrayList<ReposYSimultaneas>();
        switch (filter)
        {
            case "Cuenta":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_repos_simultaneas as em " +
                        "WHERE em.cuenta LIKE ?", ReposYSimultaneas.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "CodNombre":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_repos_simultaneas as em " +
                        "WHERE em.cod_nombre LIKE ?", ReposYSimultaneas.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            case "Codigo":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_repos_simultaneas as em " +
                        "WHERE em.codigo LIKE ?", ReposYSimultaneas.class);
                query1.setParameter(1, value);

                list= query1.getResultList();
                break;
            case "Descripcion":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_repos_simultaneas as em " +
                        "WHERE em.descripcion LIKE ?", ReposYSimultaneas.class);
                query2.setParameter(1, value);

                list= query2.getResultList();
                break;
            case "CuentaPyG":
                Query query3 = entityManager.createNativeQuery("SELECT em.* FROM nexco_repos_simultaneas as em " +
                        "WHERE em.cuenta_pyg LIKE ?", ReposYSimultaneas.class);
                query3.setParameter(1, value);

                list= query3.getResultList();
                break;
            case "DescripcionCuentaPyG":
                Query query4 = entityManager.createNativeQuery("SELECT em.* FROM nexco_repos_simultaneas as em " +
                        "WHERE em.descripcion_cuenta_pyg LIKE ?", ReposYSimultaneas.class);
                query4.setParameter(1, value);

                list= query4.getResultList();
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
                insert.setAccion("Inserción archivo RyS");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("RyS");
                insert.setNombre(user.getNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }
            else{
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Fallo inserción archivo RyS");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("RyS");
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
                String cellCodNombre = formatter.formatCellValue(row.getCell(1));
                String cellCodigo = formatter.formatCellValue(row.getCell(2));
                String cellDescripcion = formatter.formatCellValue(row.getCell(3));
                String cellCuentaPyG = formatter.formatCellValue(row.getCell(4));
                String cellDescripcionPyG = formatter.formatCellValue(row.getCell(5));
                log[0] = String.valueOf(row.getRowNum());
                if ((cellCuenta.isEmpty() || cellCuenta.isBlank()) && (cellCodNombre.isEmpty() || cellCodNombre.isBlank())
                        && (cellCodigo.isEmpty() || cellCodigo.isBlank()) && (cellDescripcion.isEmpty() || cellDescripcion.isBlank())
                        && (cellCuentaPyG.isEmpty() || cellCuentaPyG.isBlank()) && (cellDescripcionPyG.isEmpty() || cellDescripcionPyG.isBlank())) {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = String.valueOf(row.getRowNum());
                    log[2] = "true";
                    break;
                } else if (cellCuenta.isEmpty() || cellCuenta.isBlank() || cellCuenta.length() > 18 || cellCuenta.length() < 4) {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = "1";
                    log[2] = "false";
                    break;
                } else if (cellCodNombre.isEmpty() || cellCodNombre.isBlank() || cellCodNombre.length() > 50) {
                    log[1] = "2";
                    log[2] = "false";
                    break;
                } else if (cellCodigo.isEmpty() || cellCodigo.isBlank() || cellCodigo.length() > 50) {
                    log[1] = "3";
                    log[2] = "false";
                    break;
                } else if (cellDescripcion.isEmpty() || cellDescripcion.isBlank() || cellDescripcion.length() > 50) {
                    log[1] = "4";
                    log[2] = "false";
                    break;
                } else if (cellCuentaPyG.isEmpty() || cellCuentaPyG.isBlank() || cellCuentaPyG.length() > 18 || cellCuentaPyG.length() < 4) {
                    log[1] = "5";
                    log[2] = "false";
                    break;
                } else if (cellDescripcionPyG.isEmpty() || cellDescripcionPyG.isBlank() || cellDescripcionPyG.length() > 50) {
                    log[1] = "6";
                    log[2] = "false";
                    break;
                } else {
                    try {
                        log[0] = String.valueOf(row.getRowNum());
                        Long cuenta = Long.parseLong(cellCuenta);log[1]="1";
                        Long codigo = Long.parseLong(cellCodigo);log[1]="3";
                        Long cuentaPyG = Long.parseLong(cellCuentaPyG);log[1]="5";
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
                String cellCodNombre = formatter.formatCellValue(row.getCell(1));
                String cellCodigo = formatter.formatCellValue(row.getCell(2));
                String cellDescripcion = formatter.formatCellValue(row.getCell(3));
                String cellCuentaPyG = formatter.formatCellValue(row.getCell(4));
                String cellDescripcionPyG = formatter.formatCellValue(row.getCell(5));
                if ((cellCuenta.isEmpty() || cellCuenta.isBlank()) && (cellCodNombre.isEmpty() || cellCodNombre.isBlank())
                        && (cellCodigo.isEmpty() || cellCodigo.isBlank()) && (cellDescripcion.isEmpty() || cellDescripcion.isBlank())
                        && (cellCuentaPyG.isEmpty() || cellCuentaPyG.isBlank()) && (cellDescripcionPyG.isEmpty() || cellDescripcionPyG.isBlank())) {
                    log[0] = cellCuenta;
                    log[1] = "Fallo al ingresar registro";
                    break;
                } else {
                    ReposYSimultaneas reposYSimultaneas = new ReposYSimultaneas();
                    reposYSimultaneas.setCuenta(cellCuenta);
                    reposYSimultaneas.setCodNombre(cellCodNombre);
                    reposYSimultaneas.setCodigo(cellCodigo);
                    reposYSimultaneas.setDescripcion(cellDescripcion);
                    reposYSimultaneas.setCuentaPyG(cellCuentaPyG);
                    reposYSimultaneas.setDescripcionCuentaPyG(cellDescripcionPyG);
                    reposYSimultaneasRepository.save(reposYSimultaneas);
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