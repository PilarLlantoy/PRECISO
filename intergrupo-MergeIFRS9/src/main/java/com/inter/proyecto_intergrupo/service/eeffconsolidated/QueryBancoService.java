package com.inter.proyecto_intergrupo.service.eeffconsolidated;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.*;
import com.inter.proyecto_intergrupo.model.information.OnePercentDates;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.QueryBancoAjusteRepository;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.QueryBancoRepository;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.*;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
@Transactional
public class QueryBancoService {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private  QueryBancoRepository queryBancoRepository;

    @Autowired
    private QueryBancoAjusteRepository queryBancoAjusteRepository;

    @Autowired
    private AuditRepository auditRepository;


    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    public QueryBancoService(QueryBancoRepository queryBancoRepository) {
        this.queryBancoRepository = queryBancoRepository;
    }

    public Page getAll(Pageable pageable, String periodo) {
        List<QueryBanco> list = getEeffConsolidatedDataByPeriod(periodo);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<QueryBanco> pageAval = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return pageAval;
    }

    public boolean procesarArchivoTXT(InputStream fileContent, String periodo) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileContent))) {
            String line;
            int count = 0;
            List<QueryBanco> listBank = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (count > 0) {
                    listBank.add(procesarLineaTXT(line, periodo));
                }
                count++;
            }


            if (validarFecha(listBank, periodo)) {

                Query delete1 = entityManager.createNativeQuery("delete from nexco_query_banco where FECONT = ?");
                delete1.setParameter(1, listBank.get(0).getFECONT());
                delete1.executeUpdate();

                Query delete = entityManager.createNativeQuery("delete from nexco_query_banco_def where FECONT = ?");
                delete.setParameter(1, listBank.get(0).getFECONT());
                delete.executeUpdate();

                insertQuery(listBank);
                insertQueryDuplicated(listBank);

                Query delete2 = entityManager.createNativeQuery("delete from nexco_query_banco_def where FECONT = ? and empresa <> '0013' ");
                delete2.setParameter(1, listBank.get(0).getFECONT());
                delete2.executeUpdate();


                Query update1 = entityManager.createNativeQuery("update b set b.nombre_cuenta = a.DERECTA, b.naturaleza = case when a.incie = '-' then 'C' when a.incie = '+' then 'D' END, b.indic = a.indic from (select * from cuentas_puc where empresa='0013') as a, (select * from nexco_query_banco_def where periodo = ?) as b where a.nucta = b.nucta");
                update1.setParameter(1, listBank.get(0).getPeriodo());
                update1.executeUpdate();

                Query update = entityManager.createNativeQuery("update b set b.nombre_cuenta = a.DERECTA, b.naturaleza = case when a.incie = '-' then 'C' when a.incie = '+' then 'D' END, b.indic = a.indic from (select * from cuentas_puc where empresa='0013') as a, (select * from nexco_query_banco where periodo = ?) as b where a.nucta = b.nucta");
                update.setParameter(1, listBank.get(0).getPeriodo());
                update.executeUpdate();

                return true;

            } else {

                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    private QueryBanco procesarLineaTXT(String line , String periodo) {
        try {

            String[] campos = line.split("\\s+");

            QueryBanco queryBanco = new QueryBanco();
            queryBanco.setEmpresa(campos[1]);
            queryBanco.setNUCTA(campos[2]);
            queryBanco.setFECONT(campos[3]);
            if (campos[4].matches(".*[A-Z].*")){
                queryBanco.setCODDIV(campos[4]);
                queryBanco.setSALMES(new BigDecimal(campos[5].replace(",", "")));
                queryBanco.setSALMESD(new BigDecimal(campos[6].replace(",", "")));
                queryBanco.setSALMED(new BigDecimal(campos[7].replace(",", "")));
                queryBanco.setSALMEDD(new BigDecimal(campos[8].replace(",", "")));
                queryBanco.setCODIGEST(campos[9]);
                queryBanco.setCODICONS(campos[10]);
                queryBanco.setFECHPROCE(java.sql.Date.valueOf(campos[11]));
                queryBanco.setMoneda("ME");
                queryBanco.setPeriodo(periodo);
            } else {
                queryBanco.setMoneda("ML");
                queryBanco.setCODDIV("COP");
                queryBanco.setSALMES(new BigDecimal(campos[4].replace(",", "")));
                queryBanco.setSALMESD(new BigDecimal(campos[5].replace(",", "")));
                queryBanco.setSALMED(new BigDecimal(campos[6].replace(",", "")));
                queryBanco.setSALMEDD(new BigDecimal(campos[7].replace(",", "")));
                queryBanco.setCODIGEST(campos[8]);
                queryBanco.setCODICONS(campos[9]);
                queryBanco.setFECHPROCE(java.sql.Date.valueOf(campos[10]));
                queryBanco.setPeriodo(periodo);
            }
            return queryBanco;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void insertQuery(List<QueryBanco> temporal) {

        jdbcTemplate.batchUpdate(
                "INSERT INTO nexco_query_banco(empresa,NUCTA,FECONT,CODDIV,SALMES,SALMESD,SALMED,SALMEDD,CODIGEST,CODICONS,FECHPROCE,MONEDA,PERIODO) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, temporal.get(i).getEmpresa());
                        ps.setString(2, temporal.get(i).getNUCTA());
                        ps.setString(3, temporal.get(i).getFECONT());
                        ps.setString(4, temporal.get(i).getCODDIV());
                        ps.setBigDecimal(5, temporal.get(i).getSALMES());
                        ps.setBigDecimal(6, temporal.get(i).getSALMESD());
                        ps.setBigDecimal(7, temporal.get(i).getSALMED());
                        ps.setBigDecimal(8, temporal.get(i).getSALMEDD());
                        ps.setString(9, temporal.get(i).getCODIGEST());
                        ps.setString(10, temporal.get(i).getCODICONS());
                        ps.setString(11, temporal.get(i).getFECHPROCE().toString());
                        ps.setString(12, temporal.get(i).getMoneda());
                        ps.setString(13, temporal.get(i).getPeriodo());

                    }
                    public int getBatchSize() {
                        return temporal.size();
                    }});
    }

    public void insertQueryDuplicated(List<QueryBanco> temporal) {

        jdbcTemplate.batchUpdate(
                "INSERT INTO nexco_query_banco_def(empresa,NUCTA,FECONT,CODDIV,SALMES,SALMESD,SALMED,SALMEDD,CODIGEST,CODICONS,FECHPROCE,MONEDA,PERIODO) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, temporal.get(i).getEmpresa());
                        ps.setString(2, temporal.get(i).getNUCTA());
                        ps.setString(3, temporal.get(i).getFECONT());
                        ps.setString(4, temporal.get(i).getCODDIV());
                        ps.setBigDecimal(5, temporal.get(i).getSALMES());
                        ps.setBigDecimal(6, temporal.get(i).getSALMESD());
                        ps.setBigDecimal(7, temporal.get(i).getSALMED());
                        ps.setBigDecimal(8, temporal.get(i).getSALMEDD());
                        ps.setString(9, temporal.get(i).getCODIGEST());
                        ps.setString(10, temporal.get(i).getCODICONS());
                        ps.setString(11, temporal.get(i).getFECHPROCE().toString());
                        ps.setString(12, temporal.get(i).getMoneda());
                        ps.setString(13, temporal.get(i).getPeriodo());

                    }
                    public int getBatchSize() {
                        return temporal.size();
                    }});
    }

    public boolean validarFecha(List<QueryBanco> listBank, String periodo) {
        for (QueryBanco queryBanco : listBank) {
            String fecont = queryBanco.getFECONT();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date date = dateFormat.parse(fecont);

                SimpleDateFormat yearMonthFormat = new SimpleDateFormat("yyyy-MM");
                String fecontYearMonth = yearMonthFormat.format(date);

                System.out.println(fecont);
                System.out.println(periodo);

                if (fecontYearMonth.equals(periodo)) {
                    return true;

                }
            } catch (ParseException e) {

                e.printStackTrace();
            }
        }
        return false;
    }


    public List<QueryBanco> getEeffConsolidatedDataByPeriod(String periodo) {
        return queryBancoRepository.findByPeriodoAndEmpresa(periodo, "0013");
    }

    public void descargarEeffBanco(HttpServletResponse response, String periodo) throws IOException {


        List<QueryBanco> eeffDataBanco = getEeffConsolidatedDataByPeriod(periodo);

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=CopiaEeffBanco_" + periodo + ".xlsx");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("EEFFBanco");

        Row headerRow = sheet.createRow(0);
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        headerRow.createCell(0).setCellValue("Empresa");
        headerRow.createCell(1).setCellValue("Nucta");
        headerRow.createCell(2).setCellValue("Fecont");
        headerRow.createCell(3).setCellValue("Coddiv");
        headerRow.createCell(4).setCellValue("Salmes");
        headerRow.createCell(5).setCellValue("Salmesd");
        headerRow.createCell(6).setCellValue("Salmed");
        headerRow.createCell(7).setCellValue("Salmedd");
        headerRow.createCell(8).setCellValue("Codigest");
        headerRow.createCell(9).setCellValue("Codicons");
        headerRow.createCell(10).setCellValue("Fech Proce");
        headerRow.createCell(11).setCellValue("Periodo");

        int rowNum = 1;
        for (QueryBanco eeff : eeffDataBanco) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("00548");
            row.createCell(1).setCellValue(eeff.getNUCTA());
            row.createCell(2).setCellValue(eeff.getFECONT());
            row.createCell(3).setCellValue(eeff.getCODDIV());

            row.createCell(4).setCellValue(eeff.getSALMES().doubleValue() *-1);
            row.getCell(4).setCellStyle(style);

            row.createCell(5).setCellValue(eeff.getSALMESD().doubleValue());
            row.getCell(5).setCellStyle(style);

            row.createCell(6).setCellValue(eeff.getSALMED().doubleValue());
            row.getCell(6).setCellStyle(style);

            row.createCell(7).setCellValue(eeff.getSALMEDD().doubleValue());
            row.getCell(7).setCellStyle(style);

            row.createCell(8).setCellValue(eeff.getCODIGEST());
            row.createCell(9).setCellValue(eeff.getCODICONS());

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            row.createCell(10).setCellValue(dateFormat.format(eeff.getFECHPROCE()));

            row.createCell(11).setCellValue(eeff.getPeriodo());

        }
        workbook.write(response.getOutputStream());
        workbook.close();
    }
    public List<PlantillaBancoModel> getEeffConsolidatedAjusteDataByPeriod(String periodo) {
        return queryBancoAjusteRepository.findByPeriodo(periodo);
    }
    public void descargarEeffBancoPlantilla(HttpServletResponse response, String periodo) throws IOException {


        List<PlantillaBancoModel> eeffDataBanco = getEeffConsolidatedAjusteDataByPeriod(periodo);

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=AjusteEeffBanco_" + periodo + ".xlsx");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("AjustePlantillaBanco");

        Row headerRow = sheet.createRow(0);
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        headerRow.createCell(0).setCellValue("Cuenta");
        headerRow.createCell(1).setCellValue("Divisa");
        headerRow.createCell(2).setCellValue("Saldo");

        int rowNum = 1;
        for (PlantillaBancoModel eeff : eeffDataBanco) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(eeff.getCuenta());
            row.createCell(1).setCellValue(eeff.getDivisa());
            row.createCell(2).setCellValue(eeff.getSaldo());
            row.getCell(2).setCellStyle(style);
        }
        workbook.write(response.getOutputStream());
        workbook.close();
    }


    public ArrayList<String[]> validarPlantillaBanco(Iterator<Row> rows, String periodo) {
        ArrayList<String[]> lista = new ArrayList();
        XSSFRow row;
        String stateFinal = "SUCCESS";
        ArrayList<PlantillaBancoModel> toInsert = new ArrayList<>();
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                DataFormatter formatter = new DataFormatter();
                String cellcuenta = formatter.formatCellValue(row.getCell(0));
                String celldivisa = formatter.formatCellValue(row.getCell(1));
                String cellsaldo = formatter.formatCellValue(row.getCell(2));

                XSSFCell cell0= row.getCell(2);
                cell0.setCellType(CellType.STRING);
                cellsaldo = formatter.formatCellValue(cell0).replace(" ", "");

                if (cellcuenta.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(0);
                    log1[2] = "El Campo Cuenta debe estar informado";
                    lista.add(log1);
                }

                if (celldivisa.trim().length() != 2) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(1);
                    log1[2] = "El Campo Moneda debe estar informado con 2 caracteres";
                    lista.add(log1);
                }
                if (cellsaldo.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(2);
                    log1[2] = "El Campo Cuenta debe estar informado";
                    lista.add(log1);
                } else {

                   try {
                       Double.parseDouble(cellsaldo);
                   } catch (Exception e){
                        String[] log1 = new String[3];
                        log1[0] = String.valueOf(row.getRowNum() + 1);
                        log1[1] = CellReference.convertNumToColString(2);
                        log1[2] = "El Campo saldo debe ser Númerico";
                        lista.add(log1);
                    }

                }
                PlantillaBancoModel valoreseeffFiliales = new PlantillaBancoModel();
                valoreseeffFiliales.setCuenta(cellcuenta);
                valoreseeffFiliales.setDivisa(celldivisa);
                valoreseeffFiliales.setPeriodo(periodo);

                try {
                    valoreseeffFiliales.setSaldo(Double.parseDouble(cellsaldo));

                } catch (Exception e) {
                    e.printStackTrace();
                }
                toInsert.add(valoreseeffFiliales);
            }
        }
        if (lista.size() != 0) {
            stateFinal = "FAILED";
            String[] log2 = new String[3];
            log2[0] = String.valueOf((toInsert.size() * 3) - lista.size());
            log2[1] = String.valueOf(lista.size());
            log2[2] = stateFinal;
            lista.add(log2);
        }
        else {
            queryBancoAjusteRepository.deleteByPeriodo(periodo);
            queryBancoAjusteRepository.saveAll(toInsert);


            String[] log3 = new String[3];
            log3[0] = String.valueOf((toInsert.size() * 3) - lista.size());
            log3[1] = String.valueOf(lista.size());
            log3[2] = stateFinal;
            lista.add(log3);
        }
        toInsert.clear();
        return lista;
    }

    public ArrayList<String[]> saveFileBDBanco(InputStream file, String periodo) throws IOException, InvalidFormatException {
        ArrayList<String[]> list = new ArrayList<String[]>();
        if (file != null) {
            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            list = validarPlantillaBanco(rows, periodo);
        }
        return list;
    }

    public void loadAudit(User user, String mensaje) {
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("EEFF Consolidado");
        insert.setFecha(today);
        insert.setInput("Filiales");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public void procesoAjuste(String period){

        Query getData = entityManager.createNativeQuery("delete from nexco_query_banco_def where periodo = ? ;\n" +
                "\n" +
                "insert into nexco_query_banco_def (coddiv,codicons,codigest,fechproce,fecont,nucta,salmed,salmedd,salmes,salmesd,empresa,moneda,nombre_cuenta,periodo,indic,naturaleza)\n" +
                "select coddiv,codicons,codigest,fechproce,fecont,nucta,salmed,salmedd,salmes,salmesd,empresa,moneda,nombre_cuenta,periodo,indic,naturaleza from nexco_query_banco where periodo = ? and empresa = '0013';\n" +
                "\n" +
                "insert into nexco_query_banco_def (nucta,salmes,empresa,moneda,periodo)\n" +
                "select cuenta,(saldo*-1) as saldo,'0013',divisa,periodo from nexco_query_banco_ajuste where periodo = ? ;");
                //"update a set a.salmes = a.salmes + b.saldo from (select * from nexco_query_banco_def where periodo = ? ) as a, (select * from nexco_query_banco_ajuste where periodo = ?) as b where a.nucta = b.cuenta and a.coddiv = b.divisa;");
        getData.setParameter(1,period);
        getData.setParameter(2,period);
        getData.setParameter(3,period);
        //getData.setParameter(4,period);
        getData.executeUpdate();
    }
}


