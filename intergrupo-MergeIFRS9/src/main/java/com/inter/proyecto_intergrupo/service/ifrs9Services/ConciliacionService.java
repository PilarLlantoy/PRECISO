package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.Deaccount;
import com.inter.proyecto_intergrupo.model.ifrs9.ApuntesRiesgos;
import com.inter.proyecto_intergrupo.model.ifrs9.Desconnv15;
import com.inter.proyecto_intergrupo.model.ifrs9.TemporalTable;
import com.inter.proyecto_intergrupo.model.temporal.SubsidiariesTemplateTemporal;
import com.inter.proyecto_intergrupo.repository.ifrs9.*;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import com.monitorjbl.xlsx.StreamingReader;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

@Service
@Transactional
public class ConciliacionService {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    CondetaRIRepository condetaRIRepository;

    @Autowired
    TemporalRepository temporalRepository;

    @Autowired
    ConciliacionService conciliacionService;

    @Autowired
    private ApuntesRiesgosRepository apuntesRiesgosRepository;

    public void getAccountsAjuste(String periodo){

        String periodo2 = periodo.replace("-","_");

        Query updateCentros = entityManager.createNativeQuery("update a\n" +
                "set a.centro = b.centro\n" +
                "from nexco_condetari a\n" +
                "inner join (SELECT distinct centro, contrato FROM [82.255.50.134].DB_FINAN_NUEVA.dbo.cont_h141mes_"+periodo2+") b\n" +
                "on a.contrato = b.contrato COLLATE SQL_Latin1_General_CP1_CI_AS and a.centro <> b.centro COLLATE SQL_Latin1_General_CP1_CI_AS\n" +
                "where SUBSTRING(a.fecha1, 1, 7) = ? and a.cuenta in (select distinct NUCTA from nexco_provisiones a\n" +
                "inner join (select NUCTA,CODICONS46 FROM CUENTAS_PUC WHERE EMPRESA = '0013') b\n" +
                "on a.cuenta_neocon = b.CODICONS46)\n" +
                ";");
        updateCentros.setParameter(1,periodo);
        updateCentros.executeUpdate();

        Query delete = entityManager.createNativeQuery("delete from nexco_diferencias_automaticas where periodo = ?;");
        delete.setParameter(1,periodo);
        delete.executeUpdate();

        Query query = entityManager.createNativeQuery("INSERT INTO nexco_diferencias_automaticas(centro, cuenta, valor_140, valor_condeta, diferencia, periodo) " +
            "select z.centro, z.cuenta, diff_140 diff_140, isnull(diff_con, 0) diff_con, diff_140-isnull(diff_con, 0) diff, ? from (\n" +
                "select centro, cuenta, sum(isnull(saldo_aplicativo, 0)-isnull(saldo_contable,0)) diff_140\n" +
                "from nexco_h140 \n" +
                "where cuenta in\n" +
                "(select distinct NUCTA from nexco_provisiones a\n" +
                "inner join (select NUCTA,CODICONS46 FROM CUENTAS_PUC WHERE EMPRESA = '0013') b\n" +
                "on a.cuenta_neocon = b.CODICONS46)\n" +
                "and SUBSTRING(fecha, 1, 7) = ? \n" +
                "and saldo_aplicativo-saldo_contable <> 0 \n" +
                "group by centro, cuenta) z\n" +
                "left join \n" +
                "(select c.centro, c.cuenta, c.valor_aplicativo-(c.valor_contable+isnull(d.importe, 0)) diff_con from \n" +
                "(select centro, cuenta, SUM(valor_aplicativo) valor_aplicativo, SUM(valor_contable) valor_contable \n" +
                "from nexco_condetari where SUBSTRING(fecha1, 1, 7) = ?\n" +
                "and cuenta in (select distinct NUCTA from nexco_provisiones a\n" +
                "inner join (select NUCTA,CODICONS46 FROM CUENTAS_PUC WHERE EMPRESA = '0013') b\n" +
                "on a.cuenta_neocon = b.CODICONS46)\n" +
                "group by centro, cuenta) c\n" +
                "left join (select right('0000'+centro, 4) centro, cuenta, sum(importe) importe from nexco_apuntes \n" +
                "where periodo = ? group by centro, cuenta) d\n" +
                "on c.centro = d.centro and c.cuenta = d.cuenta) y\n" +
                "on z.centro = y.centro and z.cuenta = y.cuenta\n" +
                "order by 1, 2;");
        query.setParameter(1,periodo);
        query.setParameter(2,periodo);
        query.setParameter(3,periodo);
        query.setParameter(4,periodo);
        query.executeUpdate();

    }

    public void getAccountsAjusteSinRiesgo(String periodo){

        String periodo2 = periodo.replace("-","_");

        Query updateCentros = entityManager.createNativeQuery("update a\n" +
                "set a.centro = b.centro\n" +
                "from nexco_condetari a\n" +
                "inner join (SELECT distinct centro, contrato FROM [82.255.50.134].DB_FINAN_NUEVA.dbo.cont_h141mes_"+periodo2+" where substring(nucta,1,1) <> '8') b\n" +
                "on a.contrato COLLATE Modern_Spanish_CI_AS  = b.contrato and a.centro COLLATE Modern_Spanish_CI_AS  <> b.centro \n" +
                "where SUBSTRING(a.fecha1, 1, 7) = ? and a.cuenta in (select distinct NUCTA from nexco_provisiones a\n" +
                "inner join (select NUCTA,CODICONS46 FROM CUENTAS_PUC WHERE EMPRESA = '0013') b\n" +
                "on a.cuenta_neocon = b.CODICONS46)\n" +
                ";");
        updateCentros.setParameter(1,periodo);
        updateCentros.executeUpdate();

        Query delete = entityManager.createNativeQuery("delete from nexco_diferencias_automaticas where periodo = ?;");
        delete.setParameter(1,periodo);
        delete.executeUpdate();

        Query query = entityManager.createNativeQuery("INSERT INTO nexco_diferencias_automaticas(centro, cuenta, valor_140, valor_condeta, diferencia, periodo) " +
                "select z.centro, z.cuenta, diff_140 diff_140, isnull(diff_con, 0) diff_con, diff_140-isnull(diff_con, 0) diff, ? periodo from (\n" +
                "select centro, cuenta, sum(isnull(saldo_aplicativo, 0)-isnull(saldo_contable,0)) diff_140\n" +
                "from nexco_h140 \n" +
                "where cuenta in\n" +
                "(select distinct NUCTA from nexco_provisiones a\n" +
                "\tinner join (select NUCTA,CODICONS46 FROM CUENTAS_PUC WHERE EMPRESA = '0013') b\n" +
                "on a.cuenta_neocon = b.CODICONS46)\n" +
                "and SUBSTRING(fecha, 1, 7) = ? \n" +
                "and saldo_aplicativo-saldo_contable <> 0 \n" +
                "group by centro, cuenta) z\n" +
                "left join \n" +
                "(select centro, cuenta, SUM(valor_diferencia) diff_con\n" +
                "from nexco_condetari where SUBSTRING(fecha1, 1, 7) = ? \n" +
                "and cuenta in (select distinct NUCTA from nexco_provisiones a\n" +
                "\tinner join (select NUCTA,CODICONS46 FROM CUENTAS_PUC WHERE EMPRESA = '0013') b\n" +
                "on a.cuenta_neocon = b.CODICONS46)\n" +
                "group by centro, cuenta) y\n" +
                "on z.centro = y.centro and z.cuenta = y.cuenta\n" +
                "order by 1, 2;");
        query.setParameter(1,periodo);
        query.setParameter(2,periodo);
        query.setParameter(3,periodo);
        query.executeUpdate();

        /*Query query1 = entityManager.createNativeQuery("update a set a.diferencia = 0 \n" +
                "from (SELECT centro, cuenta, valor_140, valor_condeta, diferencia FROM nexco_diferencias_automaticas WHERE periodo = ? and diferencia <> 0) a\n" +
                "inner join (SELECT centro,cuenta,diferencia FROM nexco_h140 WHERE SUBSTRING(fecha, 1, 7) = ? ) b on a.centro = b.centro and a.cuenta = b.cuenta where a.diferencia - b.diferencia > -5 and a.diferencia - b.diferencia < 5");
        query1.setParameter(1,periodo);
        query1.setParameter(2,periodo);
        query1.executeUpdate();*/

    }

    public List<Object[]> selDiffAutoMatch(String periodo){

        Query query1 = entityManager.createNativeQuery("SELECT centro, cuenta, valor_140, valor_condeta, diferencia FROM nexco_diferencias_automaticas WHERE periodo = ? and diferencia = 0;");
        query1.setParameter(1,periodo);

        return query1.getResultList();

    }

    public List<Object[]> selDiffAutoNoMatch(String periodo){

        Query query1 = entityManager.createNativeQuery("SELECT centro, cuenta, valor_140, valor_condeta, diferencia FROM nexco_diferencias_automaticas WHERE periodo = ? and diferencia <> 0;");
        query1.setParameter(1,periodo);

        return query1.getResultList();

    }

    public List<Object[]> getCondeta(String periodo){

        Query query1 = entityManager.createNativeQuery("SELECT top 1 fecha1 FROM nexco_condetari WHERE SUBSTRING(fecha1, 1, 7) = ? ;");
        query1.setParameter(1,periodo);

        return query1.getResultList();

    }

    public List<Object[]> get140(String periodo){

        Query query1 = entityManager.createNativeQuery("SELECT top 1 fecha FROM nexco_h140 WHERE SUBSTRING(fecha, 1, 7) = ?;");
        query1.setParameter(1,periodo);

        return query1.getResultList();

    }

    public List<Object[]> selDiffContratoSinAjuste(String periodo){

        Query query1 = entityManager.createNativeQuery("select a.centro, a.contrato, a.cuenta, sum(a.valor_contable) valor_contable, sum(a.valor_aplicativo) valor_aplicativo, sum(a.valor_diferencia) valor_diferencia from \n" +
                "(select * from nexco_condetari where SUBSTRING(fecha1, 1, 7) = ? and valor_diferencia not between -1000 and 1000) a\n" +
                "inner join \n" +
                "(select * from nexco_diferencias_automaticas where periodo = ? and valor_140 <> 0 and valor_condeta <> 0 and diferencia <> 0) b\n" +
                "on a.centro = b.centro and a.cuenta = b.cuenta\n" +
                "group by a.centro, a.contrato, a.cuenta\n" +
                "order by 1, 2, 3;");
        query1.setParameter(1,periodo);
        query1.setParameter(2,periodo);

        return query1.getResultList();

    }

    public List<Object[]> selDiffContratoAjuste(String periodo){

        Query query1 = entityManager.createNativeQuery("select a.centro, a.contrato, a.cuenta, sum(a.valor_contable) valor_contable, sum(isnull(a.importe, 0)) valor_apunte, sum(a.valor_aplicativo) valor_aplicativo, sum((a.valor_contable+isnull(a.importe, 0))-a.valor_aplicativo) valor_diferencia from \n" +
                "(select z.*, y.importe from (select * from nexco_condetari where SUBSTRING(fecha1, 1, 7) = ?) z\n" +
                "left join (select * from nexco_apuntes where periodo = ?) y\n" +
                "on z.centro = y.centro and z.contrato = y.contrato and z.cuenta = y.cuenta\n" +
                "where (valor_contable-valor_aplicativo) not between -1000 and 1000) a\n" +
                "inner join \n" +
                "(select * from nexco_diferencias_automaticas where periodo = ? and valor_140 <> 0 and valor_condeta <> 0 and diferencia <> 0) b\n" +
                "on a.centro = b.centro and a.cuenta = b.cuenta\n" +
                "group by a.centro, a.contrato, a.cuenta\n" +
                "order by 1, 2, 3;");

        query1.setParameter(1,periodo);
        query1.setParameter(2,periodo);
        query1.setParameter(3,periodo);

        return query1.getResultList();

    }

    public List<Object[]> selDiffCeroContrato(String periodo){

        Query query1 = entityManager.createNativeQuery("select a.centro, a.contrato, a.cuenta, sum(a.valor_contable) valor_contable, sum(a.valor_aplicativo) valor_aplicativo, sum(a.valor_diferencia) valor_diferencia from \n" +
                "(select * from nexco_condetari where SUBSTRING(fecha1, 1, 7) = ? and valor_diferencia not between -1000 and 1000) a\n" +
                "inner join \n" +
                "(select * from nexco_diferencias_automaticas where periodo = ? and valor_140 <> 0 and valor_condeta <> 0 and diferencia = 0) b\n" +
                "on a.centro = b.centro and a.cuenta = b.cuenta\n" +
                "group by a.centro, a.contrato, a.cuenta\n" +
                "order by 1, 2, 3;");
        query1.setParameter(1,periodo);
        query1.setParameter(2,periodo);

        return query1.getResultList();

    }

    public List<Deaccount> getDeaccount(String periodo){
        Query querySave = entityManager.createNativeQuery("SELECT * FROM nexco_diferencias where periodo = ?",Deaccount.class);
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
                    "(select * from nexco_condetari where SUBSTRING(fecha1, 1, 7) = ? and valor_diferencia not between -1 and 1) a\n" +
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

    public ArrayList<String[]> saveFileDiffBD(InputStream file, User user, String period) throws IOException {
        ArrayList<String[]> list = new ArrayList<String[]>();
        if (file != null) {
            Iterator<Row> rows;
            try {
                XSSFWorkbook wb = new XSSFWorkbook(file);
                XSSFSheet sheet = wb.getSheetAt(2);
                rows = sheet.iterator();
                list = validarPlantillaDiff(rows, user, period);
            }catch (Exception e){
                String[] error = new String[1];
                error[0] = "Fallo Estructura";
                list.add(error);
                e.printStackTrace();
            }
        }
        return list;
    }

    public ArrayList<String[]> validarPlantillaDiff(Iterator<Row> rows, User user, String period) {

        ArrayList<String[]> lista = new ArrayList();
        ArrayList<Deaccount> deacList = new ArrayList();
        XSSFRow row;
        int firstRow = 1;
        String stateFinal = "true";
        String stateFinalCon = "trueCon";

        ArrayList<SubsidiariesTemplateTemporal> toInsert = new ArrayList<>();

        Query deleteAp1 = entityManager.createNativeQuery("truncate table nexco_diferencias_temp;");
        deleteAp1.executeUpdate();

        while (rows.hasNext()) {
            //String[] log = new String[3];
            row = (XSSFRow) rows.next();
            if (firstRow != 1 && row != null) {
                DataFormatter formatter = new DataFormatter();

                String cellCentro = formatter.formatCellValue(row.getCell(0)).trim();
                String cellContrato = formatter.formatCellValue(row.getCell(1)).trim();
                String cellCuenta = formatter.formatCellValue(row.getCell(2)).trim();
                String cellValContable = formatter.formatCellValue(row.getCell(3)).trim();
                String cellValApp = formatter.formatCellValue(row.getCell(4)).trim();

                if (cellCentro.trim().length() != 4) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(0);
                    log1[2] = "El Centro debe estar diligenciado a 4 caracteres";
                    lista.add(log1);
                }
                if (cellCuenta.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(2);
                    log1[2] = "La Cuenta no puede estar vacía";
                    lista.add(log1);
                }
                if (cellCuenta.trim().length() > 20) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(2);
                    log1[2] = "La Cuenta no tiene la longitud permitida";
                    lista.add(log1);
                }
                if (cellContrato.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(1);
                    log1[2] = "El Contrato no puede estar vacío";
                    lista.add(log1);
                }
                if (cellValContable.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(3);
                    log1[2] = "El Importe no puede estar vacío";
                    lista.add(log1);
                }
                if (cellValApp.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(4);
                    log1[2] = "El Valor no puede estar vacío";
                    lista.add(log1);
                }

                Double valcont;
                Double valapp;

                try{
                    valcont = !cellValContable.isEmpty()?Double.parseDouble(cellValContable.trim().replace(".","").replace(",",".").replace("(","-").replace(")","")):.0;
                }catch (Exception e){
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(3);
                    log1[2] = "El Valor debe ser númerico";
                    lista.add(log1);
                    valcont = .0;
                }

                try{
                    valapp = !cellValApp.isEmpty()?Double.parseDouble(cellValApp.trim().replace(".","").replace(",",".").replace("(","-").replace(")","")):.0;
                }catch (Exception e){
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(4);
                    log1[2] = "El Valor debe ser númerico";
                    lista.add(log1);
                    valapp = .0;
                }

                Deaccount descon = new Deaccount();
                descon.setCentro(cellCentro.toString());
                descon.setContrato(cellContrato.toString());
                descon.setCuenta(cellCuenta.toString());
                descon.setValorContable(valcont);
                descon.setValorAplicativo(valapp);
                descon.setValorDiferencia(valapp-valcont);
                descon.setPeriodo(period);
                deacList.add(descon);

            } else {
                firstRow = 0;
            }
        }
        if(lista.isEmpty()) {

            for(Deaccount list: deacList) {

                Query insert = entityManager.createNativeQuery("insert into nexco_diferencias_temp (centro, contrato, cuenta, valor_contable, valor_aplicativo, valor_diferencia)\n" +
                        "values (?, ?, ?, ?, ?, ?);");
                insert.setParameter(1, list.getCentro());
                insert.setParameter(2, list.getContrato());
                insert.setParameter(3, list.getCuenta());
                insert.setParameter(4, list.getValorContable());
                insert.setParameter(5, list.getValorAplicativo());
                insert.setParameter(6, list.getValorDiferencia());
                insert.executeUpdate();

            }

            Query diff = entityManager.createNativeQuery("select trim(a.centro) centro, trim(a.cuenta) cuenta, 'La diferencia del centro y cuenta cargado ('+CONVERT(varchar, convert(numeric(18,2), isnull(valor_diferencia, 0)))+') no coincide con la diferencia del H140 ('+CONVERT(varchar, convert(numeric(18,2), valor_140))+')' from (select centro, cuenta, SUM(valor_140) valor_140\n" +
                    "from nexco_diferencias_automaticas where periodo = ? and diferencia <> 0 \n" +
                    "group by centro, cuenta) a\n" +
                    "left join \n" +
                    "(select centro, cuenta, sum(valor_diferencia) valor_diferencia\n" +
                    "from nexco_diferencias_temp \n" +
                    "group by centro, cuenta) b\n" +
                    "on a.centro = b.centro and a.cuenta = b.cuenta\n" +
                    "where (a.valor_140)-(isnull(valor_diferencia, 0)) not between -5 and 5 \n" +
                    ";");
            diff.setParameter(1, period);
            List<Object[]> diferencias = diff.getResultList();

            if(diferencias.isEmpty()){

                Query deleteAp = entityManager.createNativeQuery("delete from nexco_diferencias where periodo = ? and origen = 'CAR';");
                deleteAp.setParameter(1,period);
                deleteAp.executeUpdate();

                Query insert2 = entityManager.createNativeQuery("insert into nexco_diferencias (centro, contrato, cuenta, valor_contable, valor_aplicativo, valor_diferencia, periodo, origen)\n" +
                        "select centro, contrato, cuenta, sum(valor_contable) valor_contable, \n" +
                        "sum(valor_aplicativo) valor_aplicativo, sum(valor_diferencia) valor_diferencia, ?, 'CAR' \n" +
                        "from nexco_diferencias_temp \n" +
                        "group by centro, contrato, cuenta \n" +
                        ";");
                insert2.setParameter(1, period);
                insert2.executeUpdate();

                String[] log = new String[3];
                log[2] = stateFinal;
                lista.add(log);

            }else{

                for(Object[] list: diferencias) {

                    String[] log1 = new String[3];
                    log1[0] = list[0].toString();
                    log1[1] = list[1].toString();
                    log1[2] = list[2].toString();
                    lista.add(log1);

                }

                String[] log = new String[3];
                log[2] = stateFinalCon;
                lista.add(log);

            }

        }else{

            String[] log = new String[3];
            log[2] = stateFinal;
            lista.add(log);

        }

        return lista;
    }

    public ArrayList<String[]> saveFileApuntesBD(InputStream file, User user, String period) throws IOException {
        ArrayList<String[]> list = new ArrayList<String[]>();
        if (file != null) {
            Iterator<Row> rows;
            try {
                XSSFWorkbook wb = new XSSFWorkbook(file);
                XSSFSheet sheet = wb.getSheetAt(0);
                rows = sheet.iterator();
                list = validarPlantillaApuntes(rows, user, period);
            }catch (Exception e){
                String[] error = new String[1];
                error[0] = "Fallo Estructura";
                list.add(error);
                e.printStackTrace();
            }
        }
        return list;
    }

    public ArrayList<String[]> validarPlantillaApuntes(Iterator<Row> rows, User user, String period) {

        ArrayList<String[]> lista = new ArrayList();
        ArrayList<ApuntesRiesgos> apuntesList = new ArrayList();
        XSSFRow row;
        int firstRow = 1;
        String stateFinal = "true";

        ArrayList<SubsidiariesTemplateTemporal> toInsert = new ArrayList<>();


        Query deleteAp = entityManager.createNativeQuery("delete from nexco_apuntes where periodo = ? ;");
        deleteAp.setParameter(1,period);
        deleteAp.executeUpdate();

        while (rows.hasNext()) {
            //String[] log = new String[3];
            row = (XSSFRow) rows.next();
            if (firstRow != 1 && row != null) {
                DataFormatter formatter = new DataFormatter();

                String cellCentro = formatter.formatCellValue(row.getCell(0)).trim();
                String cellCuenta = formatter.formatCellValue(row.getCell(1)).trim();
                String cellDivisa = formatter.formatCellValue(row.getCell(2)).trim();
                String cellContrato = formatter.formatCellValue(row.getCell(3)).trim();
                String cellImporte = formatter.formatCellValue(row.getCell(5)).trim();

                if (cellCentro.trim().length() != 4) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(0);
                    log1[2] = "El Centro debe estar diligenciado a 4 caracteres";
                    lista.add(log1);
                }
                if (cellCuenta.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(1);
                    log1[2] = "La Cuenta no puede estar vacía";
                    lista.add(log1);
                }
                if (cellCuenta.trim().length() > 20) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(1);
                    log1[2] = "La Cuenta no tiene la longitud permitida";
                    lista.add(log1);
                }
                if (cellDivisa.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(2);
                    log1[2] = "La Divisa no puede estar vacía";
                    lista.add(log1);
                }
                if (cellDivisa.trim().length() > 3) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(2);
                    log1[2] = "La Divisa no tiene la longitud permitida";
                    lista.add(log1);
                }
                if (cellContrato.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(3);
                    log1[2] = "El Contrato no puede estar vacío";
                    lista.add(log1);
                }
                if (cellImporte.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(5);
                    log1[2] = "El Importe no puede estar vacío";
                    lista.add(log1);
                }

                ApuntesRiesgos apuntes = new ApuntesRiesgos();
                apuntes.setCentro(cellCentro.toString());
                apuntes.setContrato(cellContrato.toString());
                apuntes.setCuenta(cellCuenta.toString());
                apuntes.setDivisa(cellDivisa.toString());
                apuntes.setImporte(!cellImporte.isEmpty()?Double.parseDouble(cellImporte.trim().replace(".","").replace(",",".").replace("(","-").replace(")","")):0);
                apuntes.setPeriodo(period);
                apuntesList.add(apuntes);

            } else {
                firstRow = 0;
            }
        }
        if(lista.isEmpty()) {
            apuntesRiesgosRepository.saveAll(apuntesList);
        }

        String[] log = new String[3];
        log[2] = stateFinal;
        lista.add(log);

        log[2] = stateFinal;
        lista.add(log);

        return lista;
    }
}
