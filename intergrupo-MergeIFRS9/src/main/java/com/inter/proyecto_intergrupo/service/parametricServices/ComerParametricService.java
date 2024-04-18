package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.ComerParametric;
import com.inter.proyecto_intergrupo.model.parametric.Provisions;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
@Transactional
public class ComerParametricService {

    @Autowired
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    public ArrayList<String[]> saveFileBD(InputStream file, User user) throws IOException, InvalidFormatException {
        ArrayList<String[]> list = new ArrayList<String[]>();
        if (file != null) {
            Iterator<Row> rows = null;
            Iterator<Row> rows1 = null;

            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            rows = sheet.iterator();
            rows1 = sheet.iterator();
            list = validarPlantilla(rows);
            String[] temporal = list.get(0);
            if (temporal[2].equals("true")) {
                list = getRows(rows1, user);
                Date today=new Date();
                Audit insert = new Audit();
                insert.setAccion("Inserción archivo Metodología Comercializadora");
                insert.setCentro(user.getCentro());
                insert.setComponente("Metodología Comercializadora");
                insert.setFecha(today);
                insert.setInput("Metodología Comercializadora");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }
            else
            {
                Date today=new Date();
                Audit insert = new Audit();
                insert.setAccion("Fallo Inserción archivo Metodología Comercializadora");
                insert.setCentro(user.getCentro());
                insert.setComponente("Metodología Comercializadora");
                insert.setFecha(today);
                insert.setInput("Metodología Comercializadora");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);

            }
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows) {
        ArrayList<String[]> lista = new ArrayList();
        XSSFRow row;
        String stateFinal = "true";

        while (rows.hasNext())
        {
            String[] log = new String[3];
            String[] logTry = new String[3];
            row = (XSSFRow) rows.next();
            if (row.getRowNum()>0)
            {
                DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
                String cellCuentaLocal = formatter.formatCellValue(row.getCell(0)).trim();
                /*String cellClase = formatter.formatCellValue(row.getCell(1)).trim();
                String cellNombreClase = formatter.formatCellValue(row.getCell(2)).trim();
                String cellDoc = formatter.formatCellValue(row.getCell(3)).trim();
                String cellProIva = formatter.formatCellValue(row.getCell(4)).trim();
                String cellImporte = formatter.formatCellValue(row.getCell(5)).trim();*/

                log[0] = String.valueOf(row.getRowNum() + 1);
                logTry[0] = String.valueOf(row.getRowNum() + 1);

                try{
                    Long.parseLong(cellCuentaLocal);
                }catch(Exception e){
                    logTry[1] = CellReference.convertNumToColString(0)+" - (1)";
                    logTry[2] = "Tipo de Dato Inválido";
                    lista.add(logTry);
                }

                if (cellCuentaLocal.trim().length() == 0) {
                    log[1] = CellReference.convertNumToColString(0)+" - (1)";
                    log[2] = "Cuenta Local Vacía";
                    lista.add(log);
                }
            }
        }
        if(lista.size()==0)
        {
            String[] log = new String[3];
            log[2] = stateFinal;
            lista.add(log);
        }
        return lista;
    }

    public ArrayList<String[]> getRows(Iterator<Row> rows, User user)
    {
        XSSFRow row;
        ArrayList<String[]> lista = new ArrayList<String[]>();
        clearTable(user);
        while (rows.hasNext())
        {
            String[] log = new String[3];
            log[2] = "true";
            row = (XSSFRow) rows.next();
            if (row.getRowNum()>0)
            {
                DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
                String cellCuentaLocal = formatter.formatCellValue(row.getCell(0)).trim();
                String cellClase = formatter.formatCellValue(row.getCell(1)).trim();
                String cellNombreClase = formatter.formatCellValue(row.getCell(2)).trim();
                String cellDoc = formatter.formatCellValue(row.getCell(3)).trim();
                String cellProIva = formatter.formatCellValue(row.getCell(4)).trim();
                String cellImporte = formatter.formatCellValue(row.getCell(5)).trim();

                log[0] = cellCuentaLocal + " - "+cellClase;

                ComerParametric comer = new ComerParametric();
                comer.setCuentaLocal(cellCuentaLocal);
                comer.setClase(cellClase);
                comer.setNombreClase(cellNombreClase);
                if(cellDoc.length()==0)
                {
                    comer.setDocCompr("GENERAL");
                }
                else
                {
                    comer.setDocCompr(cellDoc);
                }
                comer.setProIva(cellProIva);
                comer.setImporte(cellImporte);
                insertData(comer);
                log[1] = "Registro insertado exitosamente.";
                lista.add(log);
            }
        }
        return lista;
    }

    public void insertData(ComerParametric comer) {
        Query toInsert = entityManager.createNativeQuery("INSERT INTO nexco_parametrica_metodo_comer (cuenta_local,clase,nombre_clase,doc_compr,prorrata_iva,tipo_importe) " +
                "VALUES (?,?,?,?,?,?)");
        toInsert.setParameter(1, comer.getCuentaLocal());
        toInsert.setParameter(2, comer.getClase());
        toInsert.setParameter(3, comer.getNombreClase());
        toInsert.setParameter(4, comer.getDocCompr());
        toInsert.setParameter(5, comer.getProIva());
        toInsert.setParameter(6, comer.getImporte());
        toInsert.executeUpdate();
    }

    public void clearTable(User user) {
        Query clear = entityManager.createNativeQuery("DELETE FROM nexco_parametrica_metodo_comer");
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion("Eliminación archivo Metodología Comercializadora");
        insert.setCentro(user.getCentro());
        insert.setComponente("Metodología Comercializadora");
        insert.setFecha(today);
        insert.setInput("Metodología Comercializadora");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        clear.executeUpdate();

    }

    public ArrayList<ComerParametric> getAll() {
        Query getInfo = entityManager.createNativeQuery("SELECT * FROM nexco_parametrica_metodo_comer", ComerParametric.class);

        ArrayList<ComerParametric> result;

        if (getInfo.getResultList().isEmpty()) {
            result = new ArrayList<>();
        } else {
            result = (ArrayList<ComerParametric>) getInfo.getResultList();
        }

        return result;
    }

    public List<ComerParametric> findByFilter(String value, String filter)
    {
        List<ComerParametric> list=new ArrayList<ComerParametric>();
        switch (filter)
        {
            case "Cuenta Local":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_parametrica_metodo_comer as em " +
                        "WHERE em.cuenta_local LIKE ?", ComerParametric.class);
                query.setParameter(1, value );
                list= query.getResultList();

                break;
            case "Clase":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_parametrica_metodo_comer as em " +
                        "WHERE em.clase LIKE ?", ComerParametric.class);
                query0.setParameter(1, value);
                list= query0.getResultList();
                break;
            case "Nombre clase":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_parametrica_metodo_comer as em " +
                        "WHERE em.nombre_clase LIKE ?", ComerParametric.class);
                query1.setParameter(1, value);
                list= query1.getResultList();
                break;
            case "Doc.compr.":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_parametrica_metodo_comer as em " +
                        "WHERE em.doc_compr LIKE ?", ComerParametric.class);
                query2.setParameter(1, value);
                list= query2.getResultList();
                break;
            case "Prorrata de iva":
                Query query3 = entityManager.createNativeQuery("SELECT em.* FROM nexco_parametrica_metodo_comer as em " +
                        "WHERE em.prorrata_iva LIKE ?", ComerParametric.class);
                query3.setParameter(1, value);
                list= query3.getResultList();
                break;
            case "Tipo de importe":
                Query query4 = entityManager.createNativeQuery("SELECT em.* FROM nexco_parametrica_metodo_comer as em " +
                        "WHERE em.tipo_importe LIKE ?", ComerParametric.class);
                query4.setParameter(1, value);
                list= query4.getResultList();
                break;
            default:
                break;
        }
        return list;
    }
}