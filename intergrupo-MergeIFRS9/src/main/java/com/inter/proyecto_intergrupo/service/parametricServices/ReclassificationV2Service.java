package com.inter.proyecto_intergrupo.service.parametricServices;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.ReclassificationIntergroup;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
public class ReclassificationV2Service {

    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    public List<ReclassificationIntergroup> findAll() {
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_reclasificacion_intergrupo_v2 as em ", ReclassificationIntergroup.class);
        return query.getResultList();
    }

    public Page getAll(Pageable pageable) {
        List<ReclassificationIntergroup> list = findAll();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<ReclassificationIntergroup> pageAval = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return pageAval;
    }

    public List<ReclassificationIntergroup> findRec(String idRec) {
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_reclasificacion_intergrupo_v2 as em " +
                "WHERE em.id = ?", ReclassificationIntergroup.class);
        query.setParameter(1, idRec);

        return query.getResultList();
    }

    public void saveRecla(ReclassificationIntergroup recla) {
        Query query = entityManager.createNativeQuery("INSERT INTO nexco_reclasificacion_intergrupo_v2(concepto, codicons, tipo_sociedad, segmento, producto, tipo, stage, cuenta, cuenta_contrapartida)" +
                "VALUES (?,?,?,?,?,?,?,?,?)", ReclassificationIntergroup.class);
        query.setParameter(1, recla.getConcepto());
        query.setParameter(2, recla.getCodicons());
        query.setParameter(3, recla.getTipoSociedad());
        query.setParameter(4, recla.getSegmento());
        query.setParameter(5, recla.getProducto());
        query.setParameter(6, recla.getTipo());
        query.setParameter(7, recla.getStage());
        query.setParameter(8, recla.getCuenta());
        query.setParameter(9, recla.getCuentaContrapartida());
        query.executeUpdate();
    }

    public void removeRec(String id) {
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_reclasificacion_intergrupo_v2 WHERE id = ?");
        query.setParameter(1, id);
        query.executeUpdate();
    }

    public void modifyRec(ReclassificationIntergroup rec, String id) {
        ReclassificationIntergroup toModify = new ReclassificationIntergroup();
        toModify.setConcepto(rec.getConcepto());
        toModify.setCodicons(rec.getCodicons());
        toModify.setTipoSociedad(rec.getTipoSociedad());
        toModify.setSegmento(rec.getSegmento());
        toModify.setProducto(rec.getProducto());
        toModify.setTipo(rec.getTipo());
        toModify.setStage(rec.getStage());
        toModify.setCuenta(rec.getCuenta());
        toModify.setCuentaContrapartida(rec.getCuentaContrapartida());
        Query query = entityManager.createNativeQuery("UPDATE nexco_reclasificacion_intergrupo_v2 \n" +
                "SET concepto = ?, codicons = ?, tipo_sociedad = ?, segmento = ?, producto = ?, tipo = ?, stage = ?, cuenta = ?, cuenta_contrapartida = ? \n" +
                "where id = ?", ReclassificationIntergroup.class);
        query.setParameter(1,toModify.getConcepto());
        query.setParameter(2,toModify.getCodicons());
        query.setParameter(3,toModify.getTipoSociedad());
        query.setParameter(4,toModify.getSegmento());
        query.setParameter(5,toModify.getProducto());
        query.setParameter(6,toModify.getTipo());
        query.setParameter(7,toModify.getStage());
        query.setParameter(8,toModify.getCuenta());
        query.setParameter(9,toModify.getCuentaContrapartida());
        query.setParameter(10,id);
        query.executeUpdate();

    }

    public ArrayList<String[]> saveFileBD(InputStream file, User user) throws IOException, InvalidFormatException {
        ArrayList<String[]> list = new ArrayList<String[]>();
        if (file != null) {
            Iterator<Row> rows = null;
            Iterator<Row> rows1 = null;

            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            rows = sheet.iterator();
            rows1 = sheet.iterator();
            list = validateTemplate(rows);
            String[] temporal = list.get(0);
            if (temporal[2].equals("true")) {
                list = getRows(rows1);
                Date today=new Date();
                Audit insert = new Audit();
                insert.setAccion("Inserción Documento Reclasificaciones Intergrupo V2");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Reclasificaciones Intergrupo V2");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }else
            {
                Date today=new Date();
                Audit insert = new Audit();
                insert.setAccion("Error en inserción Documento Reclasificaciones Intergrupo V2");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Reclasificaciones Intergrupo V2");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }
        }
        return list;
    }

    public ArrayList<String[]> validateTemplate(Iterator<Row> rows) {
        ArrayList lista = new ArrayList();
        XSSFRow row;
        int firstRow = 1;
        String[] log = new String[3];
        log[0] = "0";
        log[1] = "0";
        log[2] = "false";
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (firstRow != 1) {
                log[0] = String.valueOf(row.getRowNum());
                DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
                String cellConcepto = formatter.formatCellValue(row.getCell(0));
                String cellCodicons = formatter.formatCellValue(row.getCell(1));
                String cellSociedad = formatter.formatCellValue(row.getCell(2));
                String cellSegmento = formatter.formatCellValue(row.getCell(3));
                String cellProducto = formatter.formatCellValue(row.getCell(4));
                String cellTipo = formatter.formatCellValue(row.getCell(5));
                String cellStage = formatter.formatCellValue(row.getCell(6));
                String cellCuenta = formatter.formatCellValue(row.getCell(7));
                String cellCuentaContra = formatter.formatCellValue(row.getCell(8));

                Query query1 = entityManager.createNativeQuery("select * from cuentas_puc where NUCTA = ? and CODICONS46 = ? and EMPRESA = '0013'");
                query1.setParameter(1, cellCuenta);
                query1.setParameter(2, cellCodicons);



                if ((cellConcepto.isEmpty() || cellConcepto.isBlank()) && (cellCodicons.isEmpty() || cellCodicons.isBlank()) && (cellSociedad.isEmpty() || cellSociedad.isBlank()) &&
                        (cellSegmento.isEmpty() || cellSegmento.isBlank()) && (cellStage.isEmpty() || cellStage.isBlank()) && (cellCuenta.isEmpty() || cellCuenta.isBlank())) {
                    log[1] = String.valueOf(row.getRowNum());
                    log[2] = "true";
                    break;
                } else if (cellConcepto.isEmpty() || cellConcepto.isBlank() || cellConcepto.length() > 30) {
                    log[1] = "1";
                    log[2] = "false";
                    break;
                } else if (cellCodicons.isEmpty() || cellCodicons.isBlank() || cellCodicons.length() > 10) {
                    log[1] = "2";
                    log[2] = "false";
                    break;
                } else if (cellSociedad.isEmpty() || cellSociedad.isBlank() || cellSociedad.length() > 100) {
                    log[1] = "3";
                    log[2] = "false";
                    break;
                } else if (cellSegmento.isEmpty() || cellSegmento.isBlank() || cellSegmento.length() > 10) {
                    log[1] = "4";
                    log[2] = "false";
                    break;
                } else if (cellStage.isEmpty() || cellStage.isBlank() ||  cellStage.length() > 30) {
                    log[1] = "7";
                    log[2] = "false";
                    break;
                } else if (cellCuenta.isEmpty() || cellCuenta.isBlank() ||  cellCuenta.length() > 30) {
                    log[1] = "8";
                    log[2] = "false";
                    break;
                } else if (query1.getResultList().isEmpty()) {
                    log[1] = "8";
                    log[2] = "falseAccount";
                    break;
                } else {
                    try {
                        log[1] = "8";
                        Long.parseLong(cellCuenta);
                        log[2] = "true";
                    } catch (Exception e) {
                        log[2] = "falseFormat";
                        lista.add(log);
                        return lista;
                    }
                }
            } else {
                firstRow++;
            }
            lista.add(log);
        }
        return lista;
    }

    public ArrayList getRows(Iterator<Row> rows) {
        XSSFRow row;
        Date today = new Date();
        ArrayList lista = new ArrayList();
        int firstRow = 1;

        while (rows.hasNext()) {
            String[] log = new String[4];
            log[2] = "true";
            row = (XSSFRow) rows.next();

            if(firstRow > 1) {
                DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
                String cellConcepto = formatter.formatCellValue(row.getCell(0));
                String cellCodicons = formatter.formatCellValue(row.getCell(1));
                String cellSociedad = formatter.formatCellValue(row.getCell(2));
                String cellSegmento = formatter.formatCellValue(row.getCell(3));
                String cellProducto = formatter.formatCellValue(row.getCell(4));
                String cellTipo = formatter.formatCellValue(row.getCell(5));
                String cellStage = formatter.formatCellValue(row.getCell(6));
                String cellCuenta = formatter.formatCellValue(row.getCell(7));
                String cellCuentaContra = formatter.formatCellValue(row.getCell(8));

                if((cellConcepto.isEmpty() || cellConcepto.isBlank()) && (cellCodicons.isEmpty() || cellCodicons.isBlank()) && (cellSociedad.isEmpty() || cellSociedad.isBlank()) &&
                        (cellSegmento.isEmpty() || cellSegmento.isBlank()) && (cellStage.isEmpty() || cellStage.isBlank()) && (cellCuenta.isEmpty() || cellCuenta.isBlank())) {
                    break;
                }

                    ReclassificationIntergroup rec = new ReclassificationIntergroup();
                    rec.setConcepto(cellConcepto);
                    rec.setCodicons(cellCodicons);
                    rec.setTipoSociedad(cellSociedad);
                    rec.setSegmento(cellSegmento);
                    rec.setProducto(cellProducto);
                    rec.setTipo(cellTipo);
                    rec.setStage(cellStage);
                    rec.setCuenta(cellCuenta);
                    rec.setCuentaContrapartida(cellCuentaContra);

                    Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_reclasificacion_intergrupo_v2 as em \n" +
                            "WHERE em.concepto = ? and em.codicons = ? AND em.segmento = ? and em.producto = ? and tipo = ? and stage = ?", ReclassificationIntergroup.class);
                    query1.setParameter(1, cellConcepto);
                    query1.setParameter(2, cellCodicons);
                    query1.setParameter(3, cellSegmento);
                    query1.setParameter(4, cellProducto);
                    query1.setParameter(5, cellTipo);
                    query1.setParameter(6, cellStage);

                    logger.warn(query1.getResultList().isEmpty());

                    if(query1.getResultList().isEmpty()) {
                        Query query = entityManager.createNativeQuery("INSERT INTO nexco_reclasificacion_intergrupo_v2(concepto, codicons, tipo_sociedad, segmento, producto, tipo, stage, cuenta, cuenta_contrapartida)" +
                                "VALUES (?,?,?,?,?,?,?,?,?)", ReclassificationIntergroup.class);
                        query.setParameter(1, rec.getConcepto());
                        query.setParameter(2, rec.getCodicons());
                        query.setParameter(3, rec.getTipoSociedad());
                        query.setParameter(4, rec.getSegmento());
                        query.setParameter(5, rec.getProducto());
                        query.setParameter(6, rec.getTipo());
                        query.setParameter(7, rec.getStage());
                        query.setParameter(8, rec.getCuenta());
                        query.setParameter(9, rec.getCuentaContrapartida());
                        query.executeUpdate();
                        log[1] = "Registro insertado exitosamente.";
                    } else {
                        Query query = entityManager.createNativeQuery("UPDATE nexco_reclasificacion_intergrupo_v2 \n" +
                                "SET tipo_sociedad = ?, cuenta = ?, cuenta_contrapartida = ? \n" +
                                "WHERE concepto = ? and codicons = ? AND segmento = ? and producto = ? and tipo = ? and stage = ?", ReclassificationIntergroup.class);
                        query.setParameter(1, rec.getTipoSociedad());
                        query.setParameter(2, rec.getCuenta());
                        query.setParameter(3, rec.getCuentaContrapartida());
                        query.setParameter(4, rec.getConcepto());
                        query.setParameter(5, rec.getCodicons());
                        query.setParameter(6, rec.getSegmento());
                        query.setParameter(7, rec.getProducto());
                        query.setParameter(8, rec.getTipo());
                        query.setParameter(9, rec.getStage());
                        query.executeUpdate();
                        log[1] = "Registro actualizado exitosamente.";
                    }

                    log[0] = cellCodicons+" - "+cellSegmento;
                    lista.add(log);


            } else {
                firstRow++;
            }
        }
        return lista;
    }

    public List<ReclassificationIntergroup> findByFilter(String value, String filter) {
        List<ReclassificationIntergroup> list = new ArrayList<ReclassificationIntergroup>();
        switch (filter) {
            case "Concepto":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_reclasificacion_intergrupo_v2 as em " +
                        "WHERE em.concepto = ? ", ReclassificationIntergroup.class);
                query.setParameter(1, value);

                list = query.getResultList();

                break;
            case "Código Consolidación":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_reclasificacion_intergrupo_v2 as em " +
                        "WHERE em.codicons LIKE ?", ReclassificationIntergroup.class);
                query1.setParameter(1, value);

                list = query1.getResultList();
                break;
            case "Tipo Sociedad":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_reclasificacion_intergrupo_v2 as em " +
                        "WHERE em.tipo_sociedad LIKE ?", ReclassificationIntergroup.class);
                query2.setParameter(1, value);

                list = query2.getResultList();
                break;
            case "Segmento":
                Query query3 = entityManager.createNativeQuery("SELECT em.* FROM nexco_reclasificacion_intergrupo_v2 as em " +
                        "WHERE em.segmento LIKE ?", ReclassificationIntergroup.class);
                query3.setParameter(1, value);

                list = query3.getResultList();
                break;
            case "Producto":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_reclasificacion_intergrupo_v2 as em " +
                        "WHERE em.producto LIKE ?", ReclassificationIntergroup.class);
                query0.setParameter(1, value);

                list = query0.getResultList();
                break;
            case "Tipo":
                Query query4 = entityManager.createNativeQuery("SELECT em.* FROM nexco_reclasificacion_intergrupo_v2 as em " +
                        "WHERE em.tipo LIKE ?", ReclassificationIntergroup.class);
                query4.setParameter(1, value);

                list = query4.getResultList();
                break;
            case "Stage":
                Query query5 = entityManager.createNativeQuery("SELECT em.* FROM nexco_reclasificacion_intergrupo_v2 as em " +
                        "WHERE em.stage LIKE ?", ReclassificationIntergroup.class);
                query5.setParameter(1, value);

                list = query5.getResultList();
                break;
            case "Cuenta":
                Query query6 = entityManager.createNativeQuery("SELECT em.* FROM nexco_reclasificacion_intergrupo_v2 as em " +
                        "WHERE em.cuenta LIKE ?", ReclassificationIntergroup.class);
                query6.setParameter(1, value);

                list = query6.getResultList();
                break;
            case "Cuenta Contrapartida":
                Query query7 = entityManager.createNativeQuery("SELECT em.* FROM nexco_reclasificacion_intergrupo_v2 as em " +
                        "WHERE em.cuenta_contrapartida LIKE ?", ReclassificationIntergroup.class);
                query7.setParameter(1, value);

                list = query7.getResultList();
                break;
            default:
                break;
        }

        return list;
    }
}
