package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.SegmentDecisionTree;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.SegmentDecisionTreeRepository;
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
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
@Transactional
public class SegmentDecisionTreeService {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private final SegmentDecisionTreeRepository segmentDecisionTreeRepository;

    public SegmentDecisionTreeService(SegmentDecisionTreeRepository segmentDecisionTreeRepository) {
        this.segmentDecisionTreeRepository = segmentDecisionTreeRepository;
    }

    public List<SegmentDecisionTree> findAll(){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_arbol_decision_segmento as em ", SegmentDecisionTree.class);
        return query.getResultList();
    }

    public List<SegmentDecisionTree> findSegmentDecisionTreebyId(Integer id){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_arbol_decision_segmento as em " +
                "WHERE em.id = ?",SegmentDecisionTree.class);

        query.setParameter(1, id);
        return query.getResultList();
    }


    public void modifySegmentDecisionTree(SegmentDecisionTree toModify,Integer id){
        SegmentDecisionTree toInsert = new SegmentDecisionTree();
        toInsert.setCodigoIFRS9(toModify.getCodigoIFRS9());
        toInsert.setDescripcionSectorizacion(toModify.getDescripcionSectorizacion());
        toInsert.setCorasuOp(toModify.getCorasuOp());
        toInsert.setCorasu(toModify.getCorasu());
        toInsert.setSubCorasuOp(toModify.getSubCorasuOp());
        toInsert.setSubCorasu(toModify.getSubCorasu());
        toInsert.setCiiuOp(toModify.getCiiuOp());
        toInsert.setCiiu(toModify.getCiiu());
        toInsert.setNumeroEmpleadosOp(toModify.getNumeroEmpleadosOp());
        toInsert.setNumeroEmpleados(toModify.getNumeroEmpleados());
        toInsert.setTotalActivosOp(toModify.getTotalActivosOp());
        toInsert.setTotalActivos(toModify.getTotalActivos());
        toInsert.setTotalVentasOp(toModify.getTotalVentasOp());
        toInsert.setTotalVentas(toModify.getTotalVentas());
        toInsert.setVerificacionContratos(toModify.getVerificacionContratos());
        toInsert.setOtrosCriterios(toModify.getOtrosCriterios());
        Query query = entityManager.createNativeQuery("UPDATE nexco_arbol_decision_segmento SET codigo_ifrs9 = ? , descripcion_sectorizacion = ? , corasu_op = ? , corasu = ? , sub_corasu_op = ? , sub_corasu = ? , ciiu_op = ? , ciiu = ? , numero_empleados_op = ? , numero_empleados = ? , total_activos_op = ? , total_activos = ?  , total_ventas_op = ?  , total_ventas = ? , verificacion_contratos = ? , otros_criterios = ? " +
                "WHERE id = ? ", SegmentDecisionTree.class);
        query.setParameter(1, toInsert.getCodigoIFRS9());
        query.setParameter(2, toInsert.getDescripcionSectorizacion());
        query.setParameter(3, toInsert.getCorasuOp());
        query.setParameter(4, toInsert.getCorasu());
        query.setParameter(5, toInsert.getSubCorasuOp());
        query.setParameter(6, toInsert.getSubCorasu());
        query.setParameter(7, toInsert.getCiiuOp());
        query.setParameter(8, toInsert.getCiiu());
        query.setParameter(9, toInsert.getNumeroEmpleadosOp());
        query.setParameter(10, toInsert.getNumeroEmpleados());
        query.setParameter(11, toInsert.getTotalActivosOp());
        query.setParameter(12, toInsert.getTotalActivos());
        query.setParameter(13, toInsert.getTotalVentasOp());
        query.setParameter(14, toInsert.getTotalVentas());
        query.setParameter(15, toInsert.getVerificacionContratos());
        query.setParameter(16, toInsert.getOtrosCriterios());
        query.setParameter(17, id);
        try {
            query.executeUpdate();
        }catch(Exception e){

        }
    }

    public void saveSegmentDecisionTree(SegmentDecisionTree SegmentDecisionTree){
        Query query = entityManager.createNativeQuery("INSERT INTO nexco_arbol_decision_segmento (codigo_ifrs9,descripcion_sectorizacion,corasu_op,corasu,sub_corasu_op,sub_corasu,ciiu_op,ciiu,numero_empleados_op,numero_empleados,total_activos_op,total_activos,total_ventas_op,total_ventas,verificacion_contratos,otros_criterios) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", SegmentDecisionTree.class);
        query.setParameter(1, SegmentDecisionTree.getCodigoIFRS9());
        query.setParameter(2, SegmentDecisionTree.getDescripcionSectorizacion());
        query.setParameter(3, SegmentDecisionTree.getCorasuOp());
        query.setParameter(4, SegmentDecisionTree.getCorasu());
        query.setParameter(5, SegmentDecisionTree.getSubCorasuOp());
        query.setParameter(6, SegmentDecisionTree.getSubCorasu());
        query.setParameter(7, SegmentDecisionTree.getCiiuOp());
        query.setParameter(8, SegmentDecisionTree.getCiiu());
        query.setParameter(9, SegmentDecisionTree.getNumeroEmpleadosOp());
        query.setParameter(10, SegmentDecisionTree.getNumeroEmpleados());
        query.setParameter(11, SegmentDecisionTree.getTotalActivosOp());
        query.setParameter(12, SegmentDecisionTree.getTotalActivos());
        query.setParameter(13, SegmentDecisionTree.getTotalVentasOp());
        query.setParameter(14, SegmentDecisionTree.getTotalVentas());
        query.setParameter(15, SegmentDecisionTree.getVerificacionContratos());
        query.setParameter(16, SegmentDecisionTree.getOtrosCriterios());
        query.executeUpdate();
    }

    public void removeSegmentDecisionTree(Integer id){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_arbol_decision_segmento WHERE id = ? ", SegmentDecisionTree.class);
        query.setParameter(1, id);
        query.executeUpdate();
    }

    public void clearSegmentDecisionTree(User user){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_arbol_decision_segmento", SegmentDecisionTree.class);
        query.executeUpdate();
    }

    public Page<SegmentDecisionTree> getAll(Pageable pageable){
        List<SegmentDecisionTree> list = findAll();
        int start = (int)pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<SegmentDecisionTree> pageSegmentDecisionTree = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return pageSegmentDecisionTree;
    }

    public List<SegmentDecisionTree> findByFilter(String value, String filter) {
        List<SegmentDecisionTree> list=new ArrayList<SegmentDecisionTree>();
        switch (filter)
        {

            case "CodigoIFRS9":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_arbol_decision_segmento as em " +
                        "WHERE em.codigo_ifrs9 LIKE ?", SegmentDecisionTree.class);
                query.setParameter(1, value);
                list= query.getResultList();
                break;

            case "DescripcionSectorizacion":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_arbol_decision_segmento as em " +
                        "WHERE em.descripcion_sectorizacion LIKE ?", SegmentDecisionTree.class);
                query0.setParameter(1, value);
                list= query0.getResultList();
                break;

            case "Corasu":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_arbol_decision_segmento as em " +
                        "WHERE em.corasu LIKE ?", SegmentDecisionTree.class);
                query1.setParameter(1, value);
                list= query1.getResultList();
                break;

            case "SubCorasu":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_arbol_decision_segmento as em " +
                        "WHERE em.sub_corasu LIKE ?", SegmentDecisionTree.class);
                query2.setParameter(1, value );
                list= query2.getResultList();
                break;

            case "CIIU":
                Query query3 = entityManager.createNativeQuery("SELECT em.* FROM nexco_arbol_decision_segmento as em " +
                        "WHERE em.ciiu LIKE ?", SegmentDecisionTree.class);
                query3.setParameter(1, value);
                list= query3.getResultList();
                break;

            case "NumeroEmpleados":
                Query query4 = entityManager.createNativeQuery("SELECT em.* FROM nexco_arbol_decision_segmento as em " +
                        "WHERE em.numero_empleados LIKE ?", SegmentDecisionTree.class);
                query4.setParameter(1, value);
                list= query4.getResultList();
                break;

            case "TotalActivos":
                Query query5 = entityManager.createNativeQuery("SELECT em.* FROM nexco_arbol_decision_segmento as em " +
                        "WHERE em.total_activos LIKE ?", SegmentDecisionTree.class);
                query5.setParameter(1, value);
                list= query5.getResultList();
                break;

            case "TotalVentas":
                Query query6 = entityManager.createNativeQuery("SELECT em.* FROM nexco_arbol_decision_segmento as em " +
                        "WHERE em.total_ventas LIKE ?", SegmentDecisionTree.class);
                query6.setParameter(1, value);
                list= query6.getResultList();
                break;

            case "VerificacionContratos":
                Query query7 = entityManager.createNativeQuery("SELECT em.* FROM nexco_arbol_decision_segmento as em " +
                        "WHERE em.verificacion_contratos LIKE ?", SegmentDecisionTree.class);
                query7.setParameter(1, value);
                list= query7.getResultList();
                break;

            case "OtrosCriterios":
                Query query8 = entityManager.createNativeQuery("SELECT em.* FROM nexco_arbol_decision_segmento as em " +
                        "WHERE em.otros_criterios LIKE ?", SegmentDecisionTree.class);
                query8.setParameter(1, value);
                list= query8.getResultList();
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
            clearSegmentDecisionTree(user);
            list=validarPlantilla(rows);
            String[] temporal= list.get(0);
            if(temporal[2].equals("true"))
            {
                list=getRows(rows1);
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Inserción archivo Arbol de Decision Segmento");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Arbol de Decision Segmento");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }
            else{
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Fallo inserción archivo Arbol de Decision Segmento");
                insert.setCentro(user.getCentro());
                insert.setComponente("IFRS9");
                insert.setFecha(today);
                insert.setInput("Arbol de Decision Segmento");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);

            }
        }
        return list;
    }
//codigo_ifrs9,descripcion_sectorizacion,corasu,sub_corasu,ciiu,numero_empleados,total_activos,total_ventas,verificacion_contratos,otros_criterios
    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows) {
        ArrayList lista = new ArrayList();
        XSSFRow row;
        int firstRow = 1;
        String[] log = new String[3];
        log[0] = "0";
        log[1] = "0";
        log[2] = "false";
        ArrayList<String> valoresOp = new ArrayList<String>(Arrays.asList("=","<>",">","<","IN","NOT IN"));
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (firstRow == 2) {
                DataFormatter formatter = new DataFormatter();
                String cellCodigoIFRS9 = formatter.formatCellValue(row.getCell(0));
                String cellDescripcion = formatter.formatCellValue(row.getCell(1));
                String cellCorasuOp = formatter.formatCellValue(row.getCell(2));
                String cellCorasu = formatter.formatCellValue(row.getCell(3));
                String cellSubCorasuOp = formatter.formatCellValue(row.getCell(4));
                String cellSubCorasu = formatter.formatCellValue(row.getCell(5));
                String cellCIIUOp = formatter.formatCellValue(row.getCell(6));
                String cellCIIU = formatter.formatCellValue(row.getCell(7));
                String cellNumeroEmpleadosOp = formatter.formatCellValue(row.getCell(8));
                String cellNumeroEmpleados = formatter.formatCellValue(row.getCell(9));
                String cellTotalActivosOp = formatter.formatCellValue(row.getCell(10));
                String cellTotalActivos = formatter.formatCellValue(row.getCell(11));
                String cellTotalVentasOp = formatter.formatCellValue(row.getCell(12));
                String cellTotalVentas = formatter.formatCellValue(row.getCell(13));
                String cellVerificacionContratos = formatter.formatCellValue(row.getCell(14));
                String cellOtrosCriterios = formatter.formatCellValue(row.getCell(15));
                log[0] = String.valueOf(row.getRowNum());
                if ((cellCodigoIFRS9.isEmpty() || cellCodigoIFRS9.isBlank()) && (cellDescripcion.isEmpty() || cellDescripcion.isBlank())
                        && (cellCorasuOp.isEmpty() || cellCorasuOp.isBlank()) && (cellCorasu.isEmpty() || cellCorasu.isBlank())
                        && (cellSubCorasuOp.isEmpty() || cellSubCorasuOp.isBlank()) && (cellSubCorasu.isEmpty() || cellSubCorasu.isBlank())
                        && (cellCIIUOp.isEmpty() || cellCIIUOp.isBlank()) && (cellCIIU.isEmpty() || cellCIIU.isBlank())
                        && (cellNumeroEmpleadosOp.isEmpty() || cellNumeroEmpleadosOp.isBlank()) && (cellNumeroEmpleados.isEmpty() || cellNumeroEmpleados.isBlank())
                        && (cellTotalActivosOp.isEmpty() || cellTotalActivosOp.isBlank()) && (cellTotalActivos.isEmpty() || cellTotalActivos.isBlank())
                        && (cellTotalVentasOp.isEmpty() || cellTotalVentasOp.isBlank()) && (cellTotalVentas.isEmpty() || cellTotalVentas.isBlank())
                        && (cellVerificacionContratos.isEmpty() || cellVerificacionContratos.isBlank()) && (cellOtrosCriterios.isEmpty() || cellOtrosCriterios.isBlank())) {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = String.valueOf(row.getRowNum());
                    log[2] = "true";
                    break;
                } else if (cellCodigoIFRS9.isEmpty() || cellCodigoIFRS9.isBlank() || cellCodigoIFRS9.length() != 3) {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = "1";
                    log[2] = "false";
                    break;
                } else if (cellDescripcion.isEmpty() || cellDescripcion.isBlank() || cellDescripcion.length() > 50) {
                    log[1] = "2";
                    log[2] = "false";
                    break;
                } else if (!(cellCorasuOp.isEmpty() || cellCorasuOp.isBlank()) && !(valoresOp.contains(cellCorasuOp))) {
                    log[1] = "3";
                    log[2] = "false";
                    break;
                } else if (!(cellCorasu.isEmpty() || cellCorasu.isBlank()) && (cellCorasu.length() > 50)) {
                    log[1] = "4";
                    log[2] = "false";
                    break;
                } else if (!(cellSubCorasuOp.isEmpty() || cellSubCorasuOp.isBlank()) && !(valoresOp.contains(cellSubCorasuOp))) {
                    log[1] = "5";
                    log[2] = "false";
                    break;
                } else if (!(cellSubCorasu.isEmpty() || cellSubCorasu.isBlank()) && (cellSubCorasu.length() > 50)) {
                    log[1] = "6";
                    log[2] = "false";
                    break;
                } else if (!(cellCIIUOp.isEmpty() || cellCIIUOp.isBlank()) && !(valoresOp.contains(cellCIIUOp))) {
                    log[1] = "7";
                    log[2] = "false";
                    break;
                } else if (!(cellCIIU.isEmpty() || cellCIIU.isBlank()) && (cellCIIU.length() > 50)) {
                    log[1] = "8";
                    log[2] = "false";
                    break;
                } else if (!(cellNumeroEmpleadosOp.isEmpty() || cellNumeroEmpleadosOp.isBlank()) && !(valoresOp.contains(cellNumeroEmpleadosOp))) {
                    log[1] = "9";
                    log[2] = "false";
                    break;
                } else if (!(cellNumeroEmpleados.isEmpty() || cellNumeroEmpleados.isBlank()) && (cellNumeroEmpleados.length() > 50)) {
                    log[1] = "10";
                    log[2] = "false";
                    break;
                } else if (!(cellTotalActivosOp.isEmpty() || cellTotalActivosOp.isBlank()) && !(valoresOp.contains(cellTotalActivosOp))) {
                    log[1] = "11";
                    log[2] = "false";
                    break;
                } else if (!(cellTotalActivos.isEmpty() || cellTotalActivos.isBlank()) && (cellTotalActivos.length() > 50)) {
                    log[1] = "12";
                    log[2] = "false";
                    break;
                } else if (!(cellTotalVentasOp.isEmpty() || cellTotalVentasOp.isBlank()) && !(valoresOp.contains(cellTotalVentasOp))) {
                    log[1] = "13";
                    log[2] = "false";
                    break;
                } else if (!(cellTotalVentas.isEmpty() || cellTotalVentas.isBlank()) && (cellTotalVentas.length() > 50)) {
                    log[1] = "14";
                    log[2] = "false";
                    break;
                } else if (!(cellVerificacionContratos.isEmpty() || cellVerificacionContratos.isBlank()) && (cellVerificacionContratos.length() > 50)) {
                    log[1] = "15";
                    log[2] = "false";
                    break;
                } else if (!(cellOtrosCriterios.isEmpty() || cellOtrosCriterios.isBlank()) && (cellOtrosCriterios.length() > 50)) {
                    log[1] = "16";
                    log[2] = "false";
                    break;
                } else {
                    try {
                        log[0] = String.valueOf(row.getRowNum());
                        Long codigoIFRS9 = Long.parseLong(cellCodigoIFRS9);log[1]="1";
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
                String cellCodigoIFRS9 = formatter.formatCellValue(row.getCell(0));
                String cellDescripcion = formatter.formatCellValue(row.getCell(1));
                String cellCorasuOp = formatter.formatCellValue(row.getCell(2));
                String cellCorasu = formatter.formatCellValue(row.getCell(3));
                String cellSubCorasuOp = formatter.formatCellValue(row.getCell(4));
                String cellSubCorasu = formatter.formatCellValue(row.getCell(5));
                String cellCIIUOp = formatter.formatCellValue(row.getCell(6));
                String cellCIIU = formatter.formatCellValue(row.getCell(7));
                String cellNumeroEmpleadosOp = formatter.formatCellValue(row.getCell(8));
                String cellNumeroEmpleados = formatter.formatCellValue(row.getCell(9));
                String cellTotalActivosOp = formatter.formatCellValue(row.getCell(10));
                String cellTotalActivos = formatter.formatCellValue(row.getCell(11));
                String cellTotalVentasOp = formatter.formatCellValue(row.getCell(12));
                String cellTotalVentas = formatter.formatCellValue(row.getCell(13));
                String cellVerificacionContratos = formatter.formatCellValue(row.getCell(14));
                String cellOtrosCriterios = formatter.formatCellValue(row.getCell(15));
                if ((cellCodigoIFRS9.isEmpty() || cellCodigoIFRS9.isBlank()) && (cellDescripcion.isEmpty() || cellDescripcion.isBlank())
                        && (cellCorasuOp.isEmpty() || cellCorasuOp.isBlank()) && (cellCorasu.isEmpty() || cellCorasu.isBlank())
                        && (cellSubCorasuOp.isEmpty() || cellSubCorasuOp.isBlank()) && (cellSubCorasu.isEmpty() || cellSubCorasu.isBlank())
                        && (cellCIIUOp.isEmpty() || cellCIIUOp.isBlank()) && (cellCIIU.isEmpty() || cellCIIU.isBlank())
                        && (cellNumeroEmpleadosOp.isEmpty() || cellNumeroEmpleadosOp.isBlank()) && (cellNumeroEmpleados.isEmpty() || cellNumeroEmpleados.isBlank())
                        && (cellTotalActivosOp.isEmpty() || cellTotalActivosOp.isBlank()) && (cellTotalActivos.isEmpty() || cellTotalActivos.isBlank())
                        && (cellTotalVentasOp.isEmpty() || cellTotalVentasOp.isBlank()) && (cellTotalVentas.isEmpty() || cellTotalVentas.isBlank())
                        && (cellVerificacionContratos.isEmpty() || cellVerificacionContratos.isBlank()) && (cellOtrosCriterios.isEmpty() || cellOtrosCriterios.isBlank())) {
                    log[0] = cellCodigoIFRS9;
                    log[1] = "Fallo al ingresar registro";
                    break;
                } else {
                    SegmentDecisionTree segmentDecisionTree = new SegmentDecisionTree();
                    segmentDecisionTree.setCodigoIFRS9(cellCodigoIFRS9);
                    segmentDecisionTree.setDescripcionSectorizacion(cellDescripcion);
                    segmentDecisionTree.setCorasuOp(cellCorasuOp);
                    segmentDecisionTree.setCorasu(cellCorasu);
                    segmentDecisionTree.setSubCorasuOp(cellSubCorasuOp);
                    segmentDecisionTree.setSubCorasu(cellSubCorasu);
                    segmentDecisionTree.setCiiuOp(cellCIIUOp);
                    segmentDecisionTree.setCiiu(cellCIIU);
                    segmentDecisionTree.setNumeroEmpleadosOp(cellNumeroEmpleadosOp);
                    segmentDecisionTree.setNumeroEmpleados(cellNumeroEmpleados);
                    segmentDecisionTree.setTotalActivosOp(cellTotalActivosOp);
                    segmentDecisionTree.setTotalActivos(cellTotalActivos);
                    segmentDecisionTree.setTotalVentasOp(cellTotalVentasOp);
                    segmentDecisionTree.setTotalVentas(cellTotalVentas);
                    segmentDecisionTree.setVerificacionContratos(cellVerificacionContratos);
                    segmentDecisionTree.setOtrosCriterios(cellOtrosCriterios);
                    segmentDecisionTreeRepository.save(segmentDecisionTree);
                    log[0] = cellCodigoIFRS9;
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