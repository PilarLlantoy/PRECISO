package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.AdjustmentsHom;
import com.inter.proyecto_intergrupo.model.ifrs9.ManualAdjustments;
import com.inter.proyecto_intergrupo.model.temporal.SubsidiariesTemplateTemporal;
import com.inter.proyecto_intergrupo.repository.ifrs9.AdjustmentsHomRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.ManualAdjustmentsRepository;
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
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@Transactional
public class AdjustmentsHomService {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AdjustmentsHomRepository adjustmentsRepository;


    public List<AdjustmentsHom> getManuals(String periodo){
        Query querySave = entityManager.createNativeQuery("SELECT * FROM nexco_ajustes_hom where periodo = ?", AdjustmentsHom.class);
        querySave.setParameter(1, periodo);
        return querySave.getResultList();
    }

    public List<AdjustmentsHom> getManualsDate(String periodo){
        Query querySave = entityManager.createNativeQuery("SELECT * FROM nexco_ajustes_hom where periodo = ?", AdjustmentsHom.class);
        querySave.setParameter(1, periodo);
        return querySave.getResultList();
    }

    public ArrayList<String[]> saveFileBD(InputStream file, User user, String period) throws IOException {
        ArrayList<String[]> list = new ArrayList<String[]>();
        if (file != null) {
            Iterator<Row> rows;
            try {
                XSSFWorkbook wb = new XSSFWorkbook(file);
                XSSFSheet sheet = wb.getSheetAt(0);
                rows = sheet.iterator();
                list = validarPlantilla(rows, user, period);
            }catch (Exception e){
                String[] error = new String[1];
                error[0] = "Fallo Estructura";
                list.add(error);
                e.printStackTrace();
            }
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows, User user, String period) {

        ArrayList<String[]> lista = new ArrayList();
        ArrayList<AdjustmentsHom> manualList = new ArrayList();
        XSSFRow row;
        int firstRow = 5;
        String stateFinal = "true";

        while (rows.hasNext()) {
            //String[] log = new String[3];
            row = (XSSFRow) rows.next();
            if (firstRow == 0 && row != null) {
                DataFormatter formatter = new DataFormatter();

                String cellTipoAsiento = formatter.formatCellValue(row.getCell(3)).trim();
                String cellDesAsiento = formatter.formatCellValue(row.getCell(5)).trim();
                String cellCodicons = formatter.formatCellValue(row.getCell(10)).trim();
                String cellDebe1 = formatter.formatCellValue(row.getCell(13)).trim();
                String cellHaber1 = formatter.formatCellValue(row.getCell(14)).trim();
                String cellDebe2 = formatter.formatCellValue(row.getCell(15)).trim();
                String cellHaber2 = formatter.formatCellValue(row.getCell(16)).trim();
                String cellDivisa = formatter.formatCellValue(row.getCell(18)).trim();
                String cellSociedadIc = formatter.formatCellValue(row.getCell(19)).trim();
                String cellDesIc = formatter.formatCellValue(row.getCell(20)).trim();

                Query querySel = entityManager.createNativeQuery("select cuenta from nexco_cuentas_neocon where cuenta = ?;");
                querySel.setParameter(1,cellCodicons);
                List<Object[]> valCodicons = querySel.getResultList();

                Query querySel1 = entityManager.createNativeQuery("select distinct divisa_neocon from nexco_divisas where divisa_neocon = ?;");
                querySel1.setParameter(1,cellDivisa);
                List<Object[]> valDivisa = querySel1.getResultList();
                if (valCodicons.isEmpty()) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(0);
                    log1[2] = "El Código de Consolidación no se encuentra en el PUC";
                    lista.add(log1);
                }
                if (valDivisa.isEmpty()) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(1);
                    log1[2] = "La Divisa no se encuentra en la parametría de Transformación de Divisas Neocon";
                    lista.add(log1);
                }
                Double debe1;
                Double debe2;
                Double haber1;
                Double haber2;

                try{
                    debe1 = !cellDebe1.isEmpty()&&cellDebe1!=""?Double.parseDouble(cellDebe1.trim().replace(",","").replace(".", "")):.0;
                }catch (Exception e){
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(13);
                    log1[2] = "El Valor debe ser númerico";
                    lista.add(log1);

                    debe1 = .0;
                }
                try{
                    haber1 = !cellHaber1.isEmpty()&&cellHaber1!=""?Double.parseDouble(cellHaber1.trim().replace(",","").replace(".", "")):.0;
                }catch (Exception e){
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(14);
                    log1[2] = "El Valor debe ser númerico";
                    lista.add(log1);

                    haber1 = .0;
                }
                try{
                    debe2 = !cellDebe2.isEmpty()&&cellDebe2!=""?Double.parseDouble(cellDebe2.trim().replace(",","").replace(".", "")):.0;
                }catch (Exception e){
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(15);
                    log1[2] = "El Valor debe ser númerico";
                    lista.add(log1);

                    debe2 = .0;
                }
                try{
                    haber2 = !cellHaber2.isEmpty()&&cellHaber2!=""?Double.parseDouble(cellHaber2.trim().replace(",","").replace(".", "")):.0;
                }catch (Exception e){
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(16);
                    log1[2] = "El Valor debe ser númerico";
                    lista.add(log1);

                    haber2 = .0;
                }

                AdjustmentsHom manuals = new AdjustmentsHom();
                manuals.setTipoAsiento(cellTipoAsiento.toString());
                manuals.setDescripcionAsiento(cellDesAsiento.toString());
                manuals.setCodicons(cellCodicons.toString());
                manuals.setDivisa(cellDivisa.toString());
                manuals.setSociedadIc(cellSociedadIc.toString());
                manuals.setDescripcionIc(cellDesIc.toString());
                manuals.setSaldoDebe1(debe1);
                manuals.setSaldoDebe2(debe2);
                manuals.setSaldoHaber1(haber1);
                manuals.setSaldoHaber2(haber2);
                manuals.setPeriodo(period);
                manualList.add(manuals);

            } else {
                firstRow--;
            }
        }
        if(lista.isEmpty()) {

            Query deleteAp = entityManager.createNativeQuery("delete from nexco_ajustes_hom where periodo = ?;");
            deleteAp.setParameter(1,period);
            deleteAp.executeUpdate();

            adjustmentsRepository.saveAll(manualList);
        }

        String[] log = new String[3];
        log[2] = stateFinal;
        lista.add(log);

        log[2] = stateFinal;
        lista.add(log);

        return lista;
    }

    public void clearManuals(User user, String periodo){
        Query query = entityManager.createNativeQuery("delete from nexco_ajustes_hom where periodo = ?;", AdjustmentsHom.class);
        query.setParameter(1, periodo);
        query.executeUpdate();
    }
}
