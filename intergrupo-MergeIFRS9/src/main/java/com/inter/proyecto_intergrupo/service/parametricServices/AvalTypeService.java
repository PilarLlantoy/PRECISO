package com.inter.proyecto_intergrupo.service.parametricServices;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.AvalTypes;
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
public class AvalTypeService {

    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    public List<AvalTypes> findAll() {
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_tipo_aval as em ", AvalTypes.class);
        return query.getResultList();
    }

    public List<Object[]> findAllDistinct() {
        Query query = entityManager.createNativeQuery("SELECT distinct em.aval_origen, em.id_tipo_aval FROM nexco_tipo_aval as em order by 2 asc");
        return query.getResultList();
    }

    public Page getAll(Pageable pageable) {
        List<AvalTypes> list = findAll();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<AvalTypes> pageAval = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return pageAval;
    }

    public List<AvalTypes> findAval(String avalOrigen, String cuenta13) {
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_tipo_aval as em " +
                "WHERE em.aval_origen = ? AND em.cuenta_contable_13 = ?", AvalTypes.class);
        query.setParameter(1, avalOrigen);
        query.setParameter(2, cuenta13);

        return query.getResultList();
    }

    public void saveAval(AvalTypes avalTypes) {
        Query query = entityManager.createNativeQuery("INSERT INTO nexco_tipo_aval(id_tipo_aval,aval_origen,tipo_archivo,cuenta_contable_13,cuenta_contable_60,contrapartida_generica)" +
                "VALUES (?,?,?,?,?,?)", AvalTypes.class);
        query.setParameter(1, avalTypes.getTipoAval());
        query.setParameter(2, avalTypes.getAvalOrigen());
        query.setParameter(3, avalTypes.getTipoArchivo());
        query.setParameter(4, avalTypes.getCuentaContable13());
        query.setParameter(5, avalTypes.getCuentaContable60());
        query.setParameter(6, avalTypes.getContraGenerica());
        query.executeUpdate();
    }

    public void removeAval(String avalOrigen, String cuenta13) {
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_tipo_aval WHERE aval_origen = ? AND cuenta_contable_13 = ?");
        query.setParameter(1, avalOrigen);
        query.setParameter(2, cuenta13);
        query.executeUpdate();
    }

    public void clearAval() {
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_tipo_aval");
        query.executeUpdate();
    }

    public void modifyAval(AvalTypes avalTypes, String avalOrigen, String cuenta13) {
        AvalTypes toModify = new AvalTypes();
        toModify.setAvalOrigen(avalTypes.getAvalOrigen());
        toModify.setTipoAval(avalTypes.getTipoAval());
        toModify.setContraGenerica(avalTypes.getContraGenerica());
        toModify.setCuentaContable13(avalTypes.getCuentaContable13());
        toModify.setCuentaContable60(avalTypes.getCuentaContable60());
        toModify.setTipoArchivo(avalTypes.getTipoArchivo());
        if (toModify.getAvalOrigen().equals(avalOrigen) && toModify.getCuentaContable13().equals(cuenta13))
            removeAval(avalOrigen, cuenta13);
        Query query = entityManager.createNativeQuery("INSERT INTO [dbo].[nexco_tipo_aval]([cuenta_contable_13],[aval_origen],[contrapartida_generica],[cuenta_contable_60],[tipo_archivo],[id_tipo_aval])VALUES(?,?,?,?,?,?)", AvalTypes.class);
        query.setParameter(1, toModify.getCuentaContable13());
        query.setParameter(2, toModify.getAvalOrigen());
        query.setParameter(3, toModify.getContraGenerica());
        query.setParameter(4, toModify.getCuentaContable60());
        query.setParameter(5, toModify.getTipoArchivo());
        query.setParameter(6,toModify.getTipoAval() );
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
                insert.setAccion("Inserción Documento Tipos de Aval");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Tipos de Aval");
                insert.setNombre(user.getNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }else
            {
                Date today=new Date();
                Audit insert = new Audit();
                insert.setAccion("Error en inserción Documento Tipos de Aval");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Tipos de Aval");
                insert.setNombre(user.getNombre());
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
                String cellAvalOrigen = formatter.formatCellValue(row.getCell(0));
                String cellCuenta13 = formatter.formatCellValue(row.getCell(2));
                String cellCuenta60 = formatter.formatCellValue(row.getCell(3));
                String cellTipoArchivo = formatter.formatCellValue(row.getCell(4));
                String cellContrapartida = formatter.formatCellValue(row.getCell(5));

                if ((cellAvalOrigen.isEmpty() || cellAvalOrigen.isBlank()) && (cellContrapartida.isEmpty() || cellContrapartida.isBlank()) && (cellCuenta13.isEmpty() || cellCuenta13.isBlank()) &&
                        (cellCuenta60.isEmpty() || cellCuenta60.isBlank()) && (cellTipoArchivo.isEmpty() || cellTipoArchivo.isBlank())) {
                    log[1] = String.valueOf(row.getRowNum());
                    log[2] = "true";
                    break;
                } else if (cellAvalOrigen.isEmpty() || cellAvalOrigen.isBlank() || cellAvalOrigen.length() > 254 || assignTipoAval(cellAvalOrigen).equals("Invalid")) {
                    log[1] = "1";
                    log[2] = "false";
                    break;
                } else if (cellTipoArchivo.isEmpty() || cellTipoArchivo.isBlank() || cellTipoArchivo.length() > 254) {
                    log[1] = "3";
                    log[2] = "false";
                    break;
                } else if (cellCuenta13.isEmpty() || cellCuenta13.isBlank() || cellCuenta13.length() < 4 || cellCuenta13.length() > 15) {
                    log[1] = "4";
                    log[2] = "false";
                    break;
                } else if (cellCuenta60.isEmpty() || cellCuenta60.isBlank() || cellCuenta60.length() < 4 || cellCuenta60.length() > 15) {
                    log[1] = "5";
                    log[2] = "false";
                    break;
                } else if (cellContrapartida.isEmpty() || cellContrapartida.isBlank() ||  cellContrapartida.length() > 15 || cellContrapartida.length() < 4) {
                    log[1] = "6";
                    log[2] = "false";
                    break;
                } else {
                    try {
                        log[1] = "2";
                        Long.parseLong(cellContrapartida);
                        log[1] = "3";
                        Long.parseLong(cellCuenta13);
                        log[1] = "4";
                        Long.parseLong(cellCuenta60);
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

    public String assignTipoAval(String aval) {
        ArrayList<String> tecnico = new ArrayList<>(Arrays.asList("tec", "téc", "tecn", "técnico", "tecnico", "tecnic"));
        ArrayList<String> finan = new ArrayList<>(Arrays.asList("fin", "financiero", "finan"));
        ArrayList<String> comer = new ArrayList<>(Arrays.asList("comer", "comercial", "com"));
        ArrayList<String> contra = new ArrayList<>(Arrays.asList("contragarantia comercial"));
        String res = "Invalid";
        if (tecnico.contains(aval.toLowerCase())) {
            res = "1";
        } else if (finan.contains(aval.toLowerCase())) {
            res = "2";
        } else if (comer.contains(aval.toLowerCase())) {
            res = "3";
        }else if (contra.contains(aval.toLowerCase())) {
            res = "4";
        }

        return res;
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
                String cellAvalOrigen = formatter.formatCellValue(row.getCell(0));
                String cellCuenta13 = formatter.formatCellValue(row.getCell(2));
                String cellCuenta60 = formatter.formatCellValue(row.getCell(3));
                String cellTipoArchivo = formatter.formatCellValue(row.getCell(4));
                String cellContrapartida = formatter.formatCellValue(row.getCell(5));
                String res = assignTipoAval(cellAvalOrigen);

                if ((cellAvalOrigen.isEmpty() || cellAvalOrigen.isBlank()) && (cellContrapartida.isEmpty() || cellContrapartida.isBlank()) && (cellCuenta13.isEmpty() || cellCuenta13.isBlank()) &&
                        (cellCuenta60.isEmpty() || cellCuenta60.isBlank()) && (cellTipoArchivo.isEmpty() || cellTipoArchivo.isBlank())) {
                    break;
                }

                if(res.equals("Invalid")) {
                    log[1] = "El aval origen no ha sido encontrado";
                    log[0] = cellAvalOrigen;
                    lista.add(log);
                } else{
                    AvalTypes avalTypes = new AvalTypes();
                    avalTypes.setAvalOrigen(cellAvalOrigen);
                    avalTypes.setTipoAval(Integer.parseInt(res));
                    avalTypes.setTipoArchivo(cellTipoArchivo);
                    avalTypes.setCuentaContable13(cellCuenta13);
                    avalTypes.setCuentaContable60(cellCuenta60);
                    avalTypes.setContraGenerica(cellContrapartida);

                    Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_tipo_aval as em WHERE em.aval_origen = ? AND em.cuenta_contable_13 = ?", AvalTypes.class);
                    query1.setParameter(1, cellAvalOrigen );
                    query1.setParameter(2, cellCuenta13 );

                    logger.warn(query1.getResultList().isEmpty());

                    if(query1.getResultList().isEmpty()) {
                        Query query = entityManager.createNativeQuery("INSERT INTO nexco_tipo_aval(aval_origen,id_tipo_aval,tipo_archivo,cuenta_contable_13,cuenta_contable_60,contrapartida_generica)" +
                                " VALUES (?,?,?,?,?,?)", AvalTypes.class);
                        query.setParameter(1, avalTypes.getAvalOrigen());
                        query.setParameter(2, avalTypes.getTipoAval());
                        query.setParameter(3, avalTypes.getTipoArchivo());
                        query.setParameter(4, avalTypes.getCuentaContable13());
                        query.setParameter(5, avalTypes.getCuentaContable60());
                        query.setParameter(6, avalTypes.getContraGenerica());
                        query.executeUpdate();
                        log[1] = "Registro insertado exitosamente.";
                    } else
                    {
                        Query query = entityManager.createNativeQuery("UPDATE nexco_tipo_aval SET aval_origen=?,id_tipo_aval=?, tipo_archivo=?, cuenta_contable_13=?,cuenta_contable_60=?,contrapartida_generica=? " +
                                "WHERE aval_origen=? AND cuenta_contable_13=?", AvalTypes.class);
                        query.setParameter(1, avalTypes.getAvalOrigen());
                        query.setParameter(2, avalTypes.getTipoAval());
                        query.setParameter(3, avalTypes.getTipoArchivo());
                        query.setParameter(4, avalTypes.getCuentaContable13());
                        query.setParameter(5, avalTypes.getCuentaContable60());
                        query.setParameter(6, avalTypes.getContraGenerica());
                        query.setParameter(7, cellAvalOrigen);
                        query.setParameter(8, cellCuenta13);
                        query.executeUpdate();
                        log[1] = "Registro actualizado exitosamente.";
                    }

                    log[0] = cellAvalOrigen;
                    lista.add(log);
                }

            } else {
                firstRow++;
            }
        }
        return lista;
    }

    public List<AvalTypes> findByFilter(String value, String filter) {
        List<AvalTypes> list = new ArrayList<AvalTypes>();
        switch (filter) {
            case "Tipo de aval Origen":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_tipo_aval as em " +
                        "WHERE em.aval_origen = ? ", AvalTypes.class);
                query.setParameter(1, value);

                list = query.getResultList();

                break;
            case "Cuenta Contable 13":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_tipo_aval as em " +
                        "WHERE em.cuenta_contable_13 LIKE ?", AvalTypes.class);
                query1.setParameter(1, value);

                list = query1.getResultList();
                break;
            case "Cuenta Contable 60":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_tipo_aval as em " +
                        "WHERE em.cuenta_contable_60 LIKE ?", AvalTypes.class);
                query2.setParameter(1, value);

                list = query2.getResultList();
                break;
            case "Tipo de Archivo":
                Query query3 = entityManager.createNativeQuery("SELECT em.* FROM nexco_tipo_aval as em " +
                        "WHERE em.tipo_archivo LIKE ?", AvalTypes.class);
                query3.setParameter(1, value);

                list = query3.getResultList();
                break;
            case "Contrapartida Genérica":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_tipo_aval as em " +
                        "WHERE em.contrapartida_generica LIKE ?", AvalTypes.class);
                query0.setParameter(1, value);

                list = query0.getResultList();
                break;
            case "Tipo de aval":
                Query query4 = entityManager.createNativeQuery("SELECT em.* FROM nexco_tipo_aval as em " +
                        "WHERE em.id_tipo_aval LIKE ?", AvalTypes.class);
                query4.setParameter(1, value);

                list = query4.getResultList();
                break;
            default:
                break;
        }

        return list;
    }
}
