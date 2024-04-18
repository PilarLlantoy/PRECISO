package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.ProvisionsAndProduct;
import com.inter.proyecto_intergrupo.model.parametric.ProvisionsAndProduct;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ProvisionsAndProductRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ProvisionsAndProductRepository;
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
public class ProvisionsAndProductService {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private final ProvisionsAndProductRepository provisionsAndProductRepository;

    public ProvisionsAndProductService(ProvisionsAndProductRepository provisionsAndProductRepository) {
        this.provisionsAndProductRepository = provisionsAndProductRepository;
    }


    public List<ProvisionsAndProduct> findAll(){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_provisiones_producto as em ", ProvisionsAndProduct.class);
        return query.getResultList();
    }

    public List<ProvisionsAndProduct> findProvisionsAndProductbyCuenta(String cuenta){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_provisiones_producto as em " +
                "WHERE em.cuenta = ?",ProvisionsAndProduct.class);

        query.setParameter(1, cuenta);
        return query.getResultList();
    }


    public void modifyProvisionsAndProduct(ProvisionsAndProduct toModify,String cuenta){
        ProvisionsAndProduct toInsert = new ProvisionsAndProduct();
        toInsert.setInstrumento(toModify.getInstrumento());
        toInsert.setJerarquia(toModify.getJerarquia());
        toInsert.setDescripcion(toModify.getDescripcion());
        toInsert.setCuenta(toModify.getCuenta());
        toInsert.setMinimo(toModify.getMinimo());
        toInsert.setPerimetroIFRS9(toModify.getPerimetroIFRS9());
        toInsert.setStagesSpain(toModify.getStagesSpain());
        toInsert.setProductoSpain(toModify.getProductoSpain());
        toInsert.setSectorSpain(toModify.getSectorSpain());
        toInsert.setSigno(toModify.getSigno());

        Query query = entityManager.createNativeQuery("UPDATE nexco_provisiones_producto SET instrumento = ? , jerarquia = ? , descripcion = ?  , cuenta = ? , minimo = ? , perimetro_ifrs9 = ? , stages_spain = ?  , producto_spain = ? , sector_spain = ? , signo = ? " +
                "WHERE cuenta = ? ", ProvisionsAndProduct.class);
        query.setParameter(1, toInsert.getInstrumento());
        query.setParameter(2, toInsert.getJerarquia());
        query.setParameter(3, toInsert.getDescripcion());
        query.setParameter(4, toInsert.getCuenta());
        query.setParameter(5, toInsert.getMinimo());
        query.setParameter(6, toInsert.getPerimetroIFRS9());
        query.setParameter(7, toInsert.getStagesSpain());
        query.setParameter(8, toInsert.getProductoSpain());
        query.setParameter(9, toInsert.getSectorSpain());
        query.setParameter(10, toInsert.getSigno());
        query.setParameter(11, cuenta);
        try {
            query.executeUpdate();
        }catch(Exception e){

        }
    }

    public void saveProvisionsAndProduct(ProvisionsAndProduct ProvisionsAndProducts){
        Query query = entityManager.createNativeQuery("INSERT INTO nexco_provisiones_producto (instrumento,jerarquia,descripcion,cuenta,minimo,perimetro_ifrs9,stages_spain,producto_spain,sector_spain,signo) VALUES (?,?,?,?,?,?,?,?,?,?)", ProvisionsAndProduct.class);
        query.setParameter(1, ProvisionsAndProducts.getInstrumento());
        query.setParameter(2, ProvisionsAndProducts.getJerarquia());
        query.setParameter(3, ProvisionsAndProducts.getDescripcion());
        query.setParameter(4, ProvisionsAndProducts.getCuenta());
        query.setParameter(5, ProvisionsAndProducts.getMinimo());
        query.setParameter(6, ProvisionsAndProducts.getPerimetroIFRS9());
        query.setParameter(7, ProvisionsAndProducts.getStagesSpain());
        query.setParameter(8, ProvisionsAndProducts.getProductoSpain());
        query.setParameter(9, ProvisionsAndProducts.getSectorSpain());
        query.setParameter(10, ProvisionsAndProducts.getSigno());
        query.executeUpdate();
    }

    public void removeProvisionsAndProduct(String cuenta){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_provisiones_producto WHERE cuenta = ? ", ProvisionsAndProduct.class);
        query.setParameter(1, cuenta);
        query.executeUpdate();
    }

    public void clearProvisionsAndProduct(User user){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_provisiones_producto", ProvisionsAndProduct.class);
        query.executeUpdate();
    }

    public Page<ProvisionsAndProduct> getAll(Pageable pageable){
        List<ProvisionsAndProduct> list = findAll();
        int start = (int)pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<ProvisionsAndProduct> pageProvisionsAndProduct = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return pageProvisionsAndProduct;
    }

    public List<ProvisionsAndProduct> findByFilter(String value, String filter) {
        List<ProvisionsAndProduct> list=new ArrayList<ProvisionsAndProduct>();
        switch (filter)
        {

            case "Instrumento":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_provisiones_producto as em " +
                        "WHERE em.instrumento LIKE ?", ProvisionsAndProduct.class);
                query.setParameter(1, value);
                list= query.getResultList();
                break;

            case "Jerarquia":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_provisiones_producto as em " +
                        "WHERE em.jerarquia LIKE ?", ProvisionsAndProduct.class);
                query0.setParameter(1, value);
                list= query0.getResultList();
                break;

            case "Descripcion":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_provisiones_producto as em " +
                        "WHERE em.descripcion LIKE ?", ProvisionsAndProduct.class);
                query1.setParameter(1, value);
                list= query1.getResultList();
                break;

            case "Cuenta":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_provisiones_producto as em " +
                        "WHERE em.cuenta LIKE ?", ProvisionsAndProduct.class);
                query2.setParameter(1, value );
                list= query2.getResultList();
                break;

            case "Minimo":
                Query query3 = entityManager.createNativeQuery("SELECT em.* FROM nexco_provisiones_producto as em " +
                        "WHERE em.minimo LIKE ?", ProvisionsAndProduct.class);
                query3.setParameter(1, value);
                list= query3.getResultList();
                break;

            case "PerimetroIFRS9":
                Query query4 = entityManager.createNativeQuery("SELECT em.* FROM nexco_provisiones_producto as em " +
                        "WHERE em.perimetro_ifrs9 LIKE ?", ProvisionsAndProduct.class);
                query4.setParameter(1, value);
                list= query4.getResultList();
                break;

            case "StagesSpain":
                Query query5 = entityManager.createNativeQuery("SELECT em.* FROM nexco_provisiones_producto as em " +
                        "WHERE em.stages_spain LIKE ?", ProvisionsAndProduct.class);
                query5.setParameter(1, value);
                list= query5.getResultList();
                break;

            case "ProductoSpain":
                Query query6 = entityManager.createNativeQuery("SELECT em.* FROM nexco_provisiones_producto as em " +
                        "WHERE em.producto_spain LIKE ?", ProvisionsAndProduct.class);
                query6.setParameter(1, value);
                list= query6.getResultList();
                break;

            case "SectorSpain":
                Query query7 = entityManager.createNativeQuery("SELECT em.* FROM nexco_provisiones_producto as em " +
                        "WHERE em.sector_spain LIKE ?", ProvisionsAndProduct.class);
                query7.setParameter(1, value);
                list= query7.getResultList();
                break;

            case "Signo":
                Query query8 = entityManager.createNativeQuery("SELECT em.* FROM nexco_provisiones_producto as em " +
                        "WHERE em.signo LIKE ?", ProvisionsAndProduct.class);
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
            list=validarPlantilla(rows);
            String[] temporal= list.get(0);
            if(temporal[2].equals("true"))
            {
                list=getRows(rows1);
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Inserción archivo Provisiones y Producto");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Provisiones y Producto");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }
            else{
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Fallo inserción archivo Provisiones y Producto");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Provisiones y Producto");
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
                String cellCuenta = formatter.formatCellValue(row.getCell(0));
                String cellInstrumento = formatter.formatCellValue(row.getCell(1));
                String cellJerarquia = formatter.formatCellValue(row.getCell(2));
                String cellDescripcion = formatter.formatCellValue(row.getCell(3));
                String cellMinimo = formatter.formatCellValue(row.getCell(4));
                String cellPerimetro = formatter.formatCellValue(row.getCell(5));
                String cellStage = formatter.formatCellValue(row.getCell(6));
                String cellProducto = formatter.formatCellValue(row.getCell(7));
                String cellSector = formatter.formatCellValue(row.getCell(8));
                String cellSigno = formatter.formatCellValue(row.getCell(9));
                log[0] = String.valueOf(row.getRowNum());
                if ((cellCuenta.isEmpty() || cellCuenta.isBlank()) && (cellInstrumento.isEmpty() || cellInstrumento.isBlank())
                        && (cellJerarquia.isEmpty() || cellJerarquia.isBlank()) && (cellDescripcion.isEmpty() || cellDescripcion.isBlank())
                        && (cellMinimo.isEmpty() || cellMinimo.isBlank()) && (cellPerimetro.isEmpty() || cellPerimetro.isBlank())
                        && (cellStage.isEmpty() || cellStage.isBlank()) && (cellProducto.isEmpty() || cellProducto.isBlank())
                        && (cellSector.isEmpty() || cellSector.isBlank()) && (cellSigno.isEmpty() || cellSigno.isBlank())) {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = String.valueOf(row.getRowNum());
                    log[2] = "true";
                    break;
                } else if (cellCuenta.isEmpty() || cellCuenta.isBlank() || cellCuenta.length() > 18 || cellCuenta.length() < 4) {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = "1";
                    log[2] = "false";
                    break;
                } else if (cellInstrumento.isEmpty() || cellInstrumento.isBlank() || cellInstrumento.length() > 50) {
                    log[1] = "2";
                    log[2] = "false";
                    break;
                } else if (cellJerarquia.isEmpty() || cellJerarquia.isBlank() || cellJerarquia.length() > 50) {
                    log[1] = "3";
                    log[2] = "false";
                    break;
                } else if (cellDescripcion.isEmpty() || cellDescripcion.isBlank() || cellDescripcion.length() > 50) {
                    log[1] = "4";
                    log[2] = "false";
                    break;
                } else if (cellMinimo.isEmpty() || cellMinimo.isBlank() || cellMinimo.length() > 50) {
                    log[1] = "5";
                    log[2] = "false";
                    break;
                } else if (cellPerimetro.isEmpty() || cellPerimetro.isBlank()) {
                    log[1] = "6";
                    log[2] = "false";
                    break;
                } else if (cellStage.isEmpty() || cellStage.isBlank() || cellStage.length() > 50) {
                    log[1] = "7";
                    log[2] = "false";
                    break;
                } else if (cellProducto.isEmpty() || cellProducto.isBlank() || cellProducto.length() > 50) {
                    log[1] = "8";
                    log[2] = "false";
                    break;
                } else if (cellSector.isEmpty() || cellSector.isBlank() || cellSector.length() > 50) {
                    log[1] = "9";
                    log[2] = "false";
                    break;
                } else if (cellSigno.isEmpty() || cellSigno.isBlank() || cellSigno.length() != 1) {
                    log[1] = "10";
                    log[2] = "false";
                    break;
                } else {
                    try {
                        log[0] = String.valueOf(row.getRowNum());
                        Long cuenta = Long.parseLong(cellCuenta);log[1]="1";
                        boolean perimetro = Boolean.parseBoolean(cellPerimetro);log[1]="6";
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
                String cellInstrumento = formatter.formatCellValue(row.getCell(1));
                String cellJerarquia = formatter.formatCellValue(row.getCell(2));
                String cellDescripcion = formatter.formatCellValue(row.getCell(3));
                String cellMinimo = formatter.formatCellValue(row.getCell(4));
                String cellPerimetro = formatter.formatCellValue(row.getCell(5));
                String cellStage = formatter.formatCellValue(row.getCell(6));
                String cellProducto = formatter.formatCellValue(row.getCell(7));
                String cellSector = formatter.formatCellValue(row.getCell(8));
                String cellSigno = formatter.formatCellValue(row.getCell(9));
                if ((cellCuenta.isEmpty() || cellCuenta.isBlank()) && (cellInstrumento.isEmpty() || cellInstrumento.isBlank())
                        && (cellJerarquia.isEmpty() || cellJerarquia.isBlank()) && (cellDescripcion.isEmpty() || cellDescripcion.isBlank())
                        && (cellMinimo.isEmpty() || cellMinimo.isBlank()) && (cellPerimetro.isEmpty() || cellPerimetro.isBlank())
                        && (cellStage.isEmpty() || cellStage.isBlank()) && (cellProducto.isEmpty() || cellProducto.isBlank())
                        && (cellSector.isEmpty() || cellSector.isBlank()) && (cellSigno.isEmpty() || cellSigno.isBlank())) {
                    log[0] = cellCuenta;
                    log[1] = "Fallo al ingresar registro";
                    break;
                } else {
                    ProvisionsAndProduct provisionsAndProduct = new ProvisionsAndProduct();
                    provisionsAndProduct.setCuenta(cellCuenta);
                    provisionsAndProduct.setInstrumento(cellInstrumento);
                    provisionsAndProduct.setJerarquia(cellJerarquia);
                    provisionsAndProduct.setMinimo(cellMinimo);
                    provisionsAndProduct.setPerimetroIFRS9(Boolean.parseBoolean(cellPerimetro));
                    provisionsAndProduct.setSectorSpain(cellSector);
                    provisionsAndProduct.setStagesSpain(cellStage);
                    provisionsAndProduct.setProductoSpain(cellProducto);
                    provisionsAndProduct.setSigno(cellSigno);
                    provisionsAndProduct.setDescripcion(cellDescripcion);
                    provisionsAndProductRepository.save(provisionsAndProduct);
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
