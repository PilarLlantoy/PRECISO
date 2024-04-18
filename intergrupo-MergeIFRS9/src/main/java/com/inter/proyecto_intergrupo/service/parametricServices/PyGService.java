package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.AvalTypes;
import com.inter.proyecto_intergrupo.model.parametric.PyG;
import com.inter.proyecto_intergrupo.model.parametric.SegmentDecisionTree;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.SegmentDecisionTreeRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
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
public class PyGService {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private final SegmentDecisionTreeRepository segmentDecisionTreeRepository;

    public PyGService(SegmentDecisionTreeRepository segmentDecisionTreeRepository) {
        this.segmentDecisionTreeRepository = segmentDecisionTreeRepository;
    }

    public List<PyG> findAll(){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_parametria_pyg as em ", PyG.class);
        return query.getResultList();
    }

    public List<PyG> findPyGbyId(Integer id){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_parametria_pyg as em " +
                "WHERE em.id = ?",PyG.class);
        query.setParameter(1, id);
        return query.getResultList();
    }


    public void modifyPyG(PyG toModify,Integer id){

        Query query = entityManager.createNativeQuery("UPDATE nexco_parametria_pyg SET anio=?, descripcion=?, cuenta=?, tipo=?, stage=?, divisa=?, valor=?, naturaleza=?, cuenta_h=? " +
                "WHERE id = ? ", PyG.class);
        query.setParameter(1, toModify.getAnio());
        query.setParameter(2, toModify.getDescripcion());
        query.setParameter(3, toModify.getCuenta());
        query.setParameter(4, toModify.getTipo());
        query.setParameter(5, toModify.getStage());
        query.setParameter(6, toModify.getDivisa());
        query.setParameter(7, toModify.getValor());
        query.setParameter(8, toModify.getNaturaleza());
        query.setParameter(9, toModify.getCuentaH());
        query.setParameter(10, id);
        query.executeUpdate();

        try {
            query.executeUpdate();
        }catch(Exception e){

        }
    }

    public void savePyG(PyG pyg){
        Query query = entityManager.createNativeQuery("INSERT INTO nexco_parametria_pyg (anio, descripcion, cuenta, tipo, stage, divisa, valor, naturaleza, cuenta_h) " +
                "VALUES (?,?,?,?,?,?,?,?,?)", PyG.class);
        query.setParameter(1, pyg.getAnio());
        query.setParameter(2, pyg.getDescripcion());
        query.setParameter(3, pyg.getCuenta());
        query.setParameter(4, pyg.getTipo());
        query.setParameter(5, pyg.getStage());
        query.setParameter(6, pyg.getDivisa());
        query.setParameter(7, pyg.getValor());
        query.setParameter(8, pyg.getNaturaleza());
        query.setParameter(9, pyg.getCuentaH());

        query.executeUpdate();
    }

    public void removePyG(Integer id){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_parametria_pyg WHERE id = ? ", PyG.class);
        query.setParameter(1, id);
        query.executeUpdate();
    }

    public void clearPyG(User user){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_parametria_pyg", PyG.class);
        query.executeUpdate();
    }

    public Page<PyG> getAll(Pageable pageable){
        List<PyG> list = findAll();
        int start = (int)pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<PyG> pagePyG = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return pagePyG;
    }

    public List<PyG> findByFilter(String value, String filter) {
        List<PyG> list=new ArrayList<PyG>();
        switch (filter)
        {
            case "Año":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_parametria_pyg as em " +
                        "WHERE em.anio LIKE ?", PyG.class);
                query.setParameter(1, value);
                list= query.getResultList();
                break;

            case "Descripción":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_parametria_pyg as em " +
                        "WHERE em.descripcion LIKE ?", PyG.class);
                query0.setParameter(1, value);
                list= query0.getResultList();
                break;

            case "Cuenta D":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_parametria_pyg as em " +
                        "WHERE em.cuenta LIKE ?", PyG.class);
                query1.setParameter(1, value);
                list= query1.getResultList();
                break;

            case "Cuenta H":
                Query query11 = entityManager.createNativeQuery("SELECT em.* FROM nexco_parametria_pyg as em " +
                        "WHERE em.cuenta_h LIKE ?", PyG.class);
                query11.setParameter(1, value);
                list= query11.getResultList();
                break;

            case "Nombre Cuenta D":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_parametria_pyg as em " +
                        "WHERE em.nombre_cuenta LIKE ?", PyG.class);
                query2.setParameter(1, value );
                list= query2.getResultList();
                break;

            case "Nombre Cuenta H":
                Query query22 = entityManager.createNativeQuery("SELECT em.* FROM nexco_parametria_pyg as em " +
                        "WHERE em.nombre_cuenta LIKE ?", PyG.class);
                query22.setParameter(1, value );
                list= query22.getResultList();
                break;

            case "Tipo":
                Query query3 = entityManager.createNativeQuery("SELECT em.* FROM nexco_parametria_pyg as em " +
                        "WHERE em.tipo LIKE ?", PyG.class);
                query3.setParameter(1, value);
                list= query3.getResultList();
                break;

            case "Stage":
                Query query4 = entityManager.createNativeQuery("SELECT em.* FROM nexco_parametria_pyg as em " +
                        "WHERE em.stage LIKE ?", PyG.class);
                query4.setParameter(1, value);
                list= query4.getResultList();
                break;

            case "Divisa":
                Query query5 = entityManager.createNativeQuery("SELECT em.* FROM nexco_parametria_pyg as em " +
                        "WHERE em.divisa LIKE ?", PyG.class);
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
            clearPyG(user);
            list=validarPlantilla(rows);
            String[] temporal= list.get(0);
            if(temporal[2].equals("true"))
            {
                list=getRows(rows1);
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Inserción archivo PyG");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("PyG");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }
            else{
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Fallo inserción archivo PyG");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("PyG");
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

        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (firstRow == 2) {
                DataFormatter formatter = new DataFormatter();
                String cellAnio = formatter.formatCellValue(row.getCell(0));
                String cellDescripcion = formatter.formatCellValue(row.getCell(1));
                String cellCuenta = formatter.formatCellValue(row.getCell(2));
                //String cellNombreCuenta = formatter.formatCellValue(row.getCell(3));
                String cellCuentaH = formatter.formatCellValue(row.getCell(4));
                //String cellNombreCuentaH = formatter.formatCellValue(row.getCell(5));
                String cellTipo = formatter.formatCellValue(row.getCell(6));
                String cellStage = formatter.formatCellValue(row.getCell(7));
                String cellDivisa = formatter.formatCellValue(row.getCell(8));
                String cellValor = formatter.formatCellValue(row.getCell(9)).replace(",","").replace("(","-").replace(")","");
                String cellNaturaleza = formatter.formatCellValue(row.getCell(10));

                if ((cellAnio.isEmpty() || cellAnio.isBlank()) && (cellDescripcion.isEmpty() || cellDescripcion.isBlank())
                        && (cellCuenta.isEmpty() || cellCuenta.isBlank()) && (cellTipo.isEmpty() || cellTipo.isBlank())
                        && (cellStage.isEmpty() || cellStage.isBlank()) && (cellDivisa.isEmpty() || cellDivisa.isBlank())
                        && (cellValor.isEmpty() || cellValor.isBlank()) && (cellNaturaleza.isEmpty() || cellNaturaleza.isBlank())) {

                    String[] log = new String[4];
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = String.valueOf(row.getRowNum());
                    log[2] = "true";
                    lista.add(log);

                }
                if (cellAnio.isEmpty() || cellAnio.isBlank() || cellAnio.length() != 4) {
                    String[] log = new String[4];
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = CellReference.convertNumToColString(0);
                    log[2] = "false";
                    log[3] = "El Año no es válido";
                    lista.add(log);

                }
                if (cellDescripcion.isEmpty() || cellDescripcion.isBlank() || cellDescripcion.length() > 50) {
                    String[] log = new String[4];
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = CellReference.convertNumToColString(1);
                    log[2] = "false";
                    log[3] = "La descripción no es válida";
                    lista.add(log);

                }
                if (cellCuenta.isEmpty() || cellCuenta.isBlank() || cellCuenta.length() > 10) {
                    String[] log = new String[4];
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = CellReference.convertNumToColString(2);
                    log[2] = "false";
                    log[3] = "La Cuenta D no es válida";
                    lista.add(log);

                }
                if (cellCuentaH.isEmpty() || cellCuentaH.isBlank() || cellCuentaH.length() > 10) {
                    String[] log = new String[4];
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = CellReference.convertNumToColString(4);
                    log[2] = "false";
                    log[3] = "La Cuenta H no es válida";
                    lista.add(log);

                }
                if (cellTipo.isEmpty() || cellTipo.isBlank() || cellTipo.length() > 256) {
                    String[] log = new String[4];
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = CellReference.convertNumToColString(6);
                    log[2] = "false";
                    log[3] = "El Tipo no es válido";
                    lista.add(log);

                }
                if (cellStage.isEmpty() || cellStage.isBlank() || cellStage.length() > 2) {
                    String[] log = new String[4];
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = CellReference.convertNumToColString(7);
                    log[2] = "false";
                    log[3] = "El Stage no es válido";
                    lista.add(log);

                }
                if (cellDivisa.isEmpty() || cellDivisa.isBlank() || cellDivisa.length() != 3) {
                    String[] log = new String[4];
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = CellReference.convertNumToColString(8);
                    log[2] = "false";
                    log[3] = "La Divisa no es válida";
                    lista.add(log);

                }
                if (cellNaturaleza.isEmpty() || cellNaturaleza.isBlank() || cellNaturaleza.length() != 1) {
                    String[] log = new String[4];
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = CellReference.convertNumToColString(10);
                    log[2] = "false";
                    log[3] = "La Naturaleza no es válida";
                    lista.add(log);

                }

                try {
                    String[] log = new String[4];
                    log[0] = String.valueOf(row.getRowNum());
                    Double valor = Double.parseDouble(cellValor);

                } catch (Exception e) {
                    String[] log = new String[4];
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = CellReference.convertNumToColString(9);
                    log[2] = "falseFormat";
                    log[3] = "El Valor no tiene formato númerico";
                    lista.add(log);
                    return lista;
                }

            } else {
                firstRow ++;
            }
        }

        String[] log = new String[4];
        log[0] = "0";
        log[1] = "0";
        log[2] = "true";
        log[3] = "Ejecuciòn finalizada";
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

                String cellAnio = formatter.formatCellValue(row.getCell(0));
                String cellDescripcion = formatter.formatCellValue(row.getCell(1));
                String cellCuenta = formatter.formatCellValue(row.getCell(2));
                //String cellNombreCuenta = formatter.formatCellValue(row.getCell(3));
                String cellCuentaH = formatter.formatCellValue(row.getCell(4));
                //String cellNombreCuenta = formatter.formatCellValue(row.getCell(5));
                String cellTipo = formatter.formatCellValue(row.getCell(6));
                String cellStage = formatter.formatCellValue(row.getCell(7));
                String cellDivisa = formatter.formatCellValue(row.getCell(8));
                String cellValor = formatter.formatCellValue(row.getCell(9)).replace(",","").replace("(","-").replace(")","");
                String cellNaturaleza = formatter.formatCellValue(row.getCell(10));

                if ((cellAnio.isEmpty() || cellAnio.isBlank()) && (cellDescripcion.isEmpty() || cellDescripcion.isBlank())
                        && (cellCuenta.isEmpty() || cellCuenta.isBlank()) && (cellTipo.isEmpty() || cellTipo.isBlank())
                        && (cellStage.isEmpty() || cellStage.isBlank()) && (cellDivisa.isEmpty() || cellDivisa.isBlank())
                        && (cellValor.isEmpty() || cellValor.isBlank()) && (cellNaturaleza.isEmpty() || cellNaturaleza.isBlank())) {
                    log[0] = cellCuenta;
                    log[1] = "Fallo al ingresar registro";
                    break;
                } else {

                    Query queryValidate = entityManager.createNativeQuery("SELECT em.* FROM nexco_parametria_pyg as em WHERE em.cuenta = ? AND em.tipo = ? and em.stage = ? and em.divisa = ? and em.cuenta_h = ?", PyG.class);
                    queryValidate.setParameter(1, cellAnio);
                    queryValidate.setParameter(2, cellCuenta);
                    queryValidate.setParameter(3, cellTipo);
                    queryValidate.setParameter(4, cellStage);
                    queryValidate.setParameter(5, cellCuentaH);

                    if(queryValidate.getResultList().isEmpty()) {

                        Query query = entityManager.createNativeQuery("INSERT INTO nexco_parametria_pyg(anio, descripcion, cuenta, tipo, stage, divisa, valor, naturaleza, cuenta_h)" +
                                " VALUES (?,?,?,?,?,?,?,?,?)", PyG.class);
                        query.setParameter(1, cellAnio);
                        query.setParameter(2, cellDescripcion);
                        query.setParameter(3, cellCuenta);
                        query.setParameter(4, cellTipo);
                        query.setParameter(5, cellStage);
                        query.setParameter(6, cellDivisa);
                        query.setParameter(7, cellValor);
                        query.setParameter(8, cellNaturaleza);
                        query.setParameter(9, cellCuentaH);

                        query.executeUpdate();



                        log[0] = cellCuenta;
                        log[1] = "Registro insertado exitosamente";
                        log[2] = "true";

                    } else
                    {
                        Query query = entityManager.createNativeQuery("UPDATE nexco_parametria_pyg SET descripcion=?,divisa=?,valor=?,naturaleza=? " +
                                "WHERE anio=? AND cuenta=? AND tipo=? AND stage=? and cuenta_h=? ", PyG.class);
                        query.setParameter(1, cellDescripcion);
                        query.setParameter(2, cellDivisa);
                        query.setParameter(3, cellValor);
                        query.setParameter(4, cellNaturaleza);
                        query.setParameter(5, cellAnio);
                        query.setParameter(6, cellCuenta);
                        query.setParameter(7, cellTipo);
                        query.setParameter(8, cellStage);
                        query.setParameter(9, cellCuentaH);
                        query.executeUpdate();

                        log[0] = cellCuenta;
                        log[1] = "Registro actualizado exitosamente";
                        log[2] = "true";
                    }

                }
                lista.add(log);
            } else {
                firstRow ++;
            }
        }

        Query query2 = entityManager.createNativeQuery("UPDATE nexco_parametria_pyg \n" +
                "SET nombre_cuenta = b.descripcion\n" +
                "FROM nexco_parametria_pyg a, nexco_cuentas_neocon b\n" +
                "WHERE a.cuenta = b.cuenta and a.nombre_cuenta IS NULL");
        query2.executeUpdate();

        Query query3 = entityManager.createNativeQuery("UPDATE nexco_parametria_pyg \n" +
                "SET nombre_cuenta_h = b.descripcion\n" +
                "FROM nexco_parametria_pyg a, nexco_cuentas_neocon b\n" +
                "WHERE a.cuenta_h = b.cuenta and a.nombre_cuenta_h IS NULL");
        query3.executeUpdate();

        return lista;
    }

}