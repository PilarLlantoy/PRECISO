package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.ApuntesRiesgos;
import com.inter.proyecto_intergrupo.model.ifrs9.Deaccount;
import com.inter.proyecto_intergrupo.model.ifrs9.ManualAdjustments;
import com.inter.proyecto_intergrupo.model.ifrs9.ValQueryEEFF;
import com.inter.proyecto_intergrupo.model.temporal.SubsidiariesTemplateTemporal;
import com.inter.proyecto_intergrupo.repository.ifrs9.ApuntesRiesgosRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.CondetaRIRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.ManualAdjustmentsRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.TemporalRepository;
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
public class ManualAdjustmentsService {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private ManualAdjustmentsRepository manualAdjustmentsRepository;


    public List<ManualAdjustments> getManuals(String periodo){
        Query querySave = entityManager.createNativeQuery("SELECT * FROM nexco_ajustes_manuales where periodo = ?", ManualAdjustments.class);
        querySave.setParameter(1, periodo);
        return querySave.getResultList();
    }

    public List<ManualAdjustments> getManualsDate(String periodo){
        Query querySave = entityManager.createNativeQuery("SELECT * FROM nexco_ajustes_manuales where periodo = ?", ManualAdjustments.class);
        querySave.setParameter(1, periodo);
        return querySave.getResultList();
    }

    public void insertDeaccount(String periodo){
        try{

            Query queryDelete = entityManager.createNativeQuery("delete from nexco_diferencias where periodo = ? and origen = 'CAL';");
            queryDelete.setParameter(1,periodo);
            queryDelete.executeUpdate();

            Query querySave = entityManager.createNativeQuery("insert into nexco_diferencias (centro, contrato, cuenta, valor_contable, valor_aplicativo, valor_diferencia, periodo, origen)\n" +
                    "select a.centro, a.contrato, a.cuenta, sum(a.valor_contable) valor_contable, sum(a.valor_aplicativo) valor_aplicativo, sum(a.valor_diferencia) valor_diferencia, ?, 'CAL' from \n" +
                    "(select * from nexco_condetari where SUBSTRING(fecha1, 1, 7) = ? and valor_diferencia not between -1000 and 1000) a\n" +
                    "inner join \n" +
                    "(select * from nexco_diferencias_automaticas where periodo = ? and valor_140 <> 0 and valor_condeta <> 0 and diferencia = 0) b\n" +
                    "on a.centro = b.centro and a.cuenta = b.cuenta\n" +
                    "group by a.centro, a.contrato, a.cuenta\n" +
                    ";");
            querySave.setParameter(1,periodo);
            querySave.setParameter(2,periodo);
            querySave.setParameter(3,periodo);
            querySave.executeUpdate();

        } catch(Exception e){
            e.printStackTrace();
        }

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
        ArrayList<ManualAdjustments> manualList = new ArrayList();
        XSSFRow row;
        int firstRow = 1;
        String stateFinal = "true";

        ArrayList<SubsidiariesTemplateTemporal> toInsert = new ArrayList<>();

        while (rows.hasNext()) {
            //String[] log = new String[3];
            row = (XSSFRow) rows.next();
            if (firstRow != 1 && row != null) {
                DataFormatter formatter = new DataFormatter();

                String cellCodicons = formatter.formatCellValue(row.getCell(0)).trim();
                String cellDivisa = formatter.formatCellValue(row.getCell(1)).trim();
                String cellSaldo = formatter.formatCellValue(row.getCell(2)).trim();
                String cellFuente = formatter.formatCellValue(row.getCell(3)).trim();
                String cellObservacion = formatter.formatCellValue(row.getCell(4)).trim();

                Query querySel = entityManager.createNativeQuery("SELECT puc.cuenta FROM nexco_cuentas_neocon as puc WHERE puc.cuenta = ?;");
                querySel.setParameter(1,cellCodicons);
                List<Object[]> valCodicons = querySel.getResultList();

                Query querySel1 = entityManager.createNativeQuery("select distinct divisa_neocon from nexco_divisas where divisa_neocon = ?;");
                querySel1.setParameter(1,cellDivisa);
                List<Object[]> valDivisa = querySel1.getResultList();

                if (valCodicons.isEmpty()) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(0);
                    log1[2] = "El Código de Consolidación no se encuentra en el PUC Plan 00";
                    lista.add(log1);
                }
                if (valDivisa.isEmpty()) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(1);
                    log1[2] = "La Divisa no se encuentra en la parametría de Transformación de Divisas Neocon";
                    lista.add(log1);
                }
                if(!cellFuente.equals("PROV")) {
                    if(!cellFuente.equals("REC")) {
                        String[] log1 = new String[3];
                        log1[0] = String.valueOf(row.getRowNum() + 1);
                        log1[1] = CellReference.convertNumToColString(3);
                        log1[2] = "La Fuente debe ser PROV o REC";
                        lista.add(log1);
                    }
                }

                Double valAj;

                try{
                    valAj = !cellSaldo.isEmpty()?Double.parseDouble(cellSaldo.trim().replace(".","").replace(",",".").replace("(","-").replace(")","")):.0;
                }catch (Exception e){
                    e.printStackTrace();
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(2);
                    log1[2] = "El Valor debe ser númerico";
                    lista.add(log1);
                    valAj = .0;
                }

                ManualAdjustments manuals = new ManualAdjustments();
                manuals.setCodicons(cellCodicons.toString());
                manuals.setDivisaEspana(cellDivisa.toString());
                manuals.setSaldo(valAj);
                manuals.setFuente(cellFuente);
                manuals.setPeriodo(period);
                manuals.setObservacion(cellObservacion.toString());
                manualList.add(manuals);

            } else {
                firstRow = 0;
            }
        }
        if(lista.isEmpty()) {

            Query deleteAp = entityManager.createNativeQuery("delete from nexco_ajustes_manuales where periodo = ?;");
            deleteAp.setParameter(1,period);
            deleteAp.executeUpdate();

            manualAdjustmentsRepository.saveAll(manualList);
        }

        String[] log = new String[3];
        log[2] = stateFinal;
        lista.add(log);

        log[2] = stateFinal;
        lista.add(log);

        return lista;
    }

    public void clearManuals(User user, String periodo){
        Query query = entityManager.createNativeQuery("delete from nexco_ajustes_manuales where periodo = ?;", ManualAdjustments.class);
        query.setParameter(1, periodo);
        query.executeUpdate();
    }
}
