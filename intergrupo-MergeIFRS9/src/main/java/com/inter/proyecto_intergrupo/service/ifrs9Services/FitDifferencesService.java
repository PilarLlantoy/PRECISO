package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.ControlPanel;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.FitDifferences;
import com.inter.proyecto_intergrupo.model.temporal.ContingentTemporal;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.FitDifferencesRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.Part;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
@Transactional
public class FitDifferencesService {


    @Autowired
    private final FitDifferencesRepository fitDifferencesRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public FitDifferencesService(FitDifferencesRepository fitDifferencesRepository) {
        this.fitDifferencesRepository = fitDifferencesRepository;
    }

    public void clearAjuste(String periodo) {
        Query firstQuery = entityManager.createNativeQuery("DELETE FROM nexco_cuadre_motor_diferencias where periodo = ?");
        firstQuery.setParameter(1,periodo);
        firstQuery.executeUpdate();
    }

    public List<FitDifferences> getFitDifferences(String periodo){
        Query data = entityManager.createNativeQuery("select * from nexco_cuadre_motor_diferencias where periodo = ?", FitDifferences.class);
        data.setParameter(1,periodo);
        return data.getResultList();
    }

    public List<Object[]> getDB140(String periodo){
        Query data = entityManager.createNativeQuery("select b.CODICONS46,a.cuenta,b.DERECTA,a.divisa, sum(a.saldo_aplicativo) SA,sum(a.saldo_contable) SC,sum(a.saldo_aplicativo) - sum(a.saldo_contable) SD, a.fecha FROM nexco_h140_completa a\n" +
                "INNER JOIN (SELECT NUCTA,CODICONS46, DERECTA FROM CUENTAS_PUC WHERE EMPRESA = '0013' and CODICONS46 IN (select neocon from nexco_cuadre_motor_diferencias where periodo = ? group by neocon) GROUP BY NUCTA, CODICONS46, DERECTA) b ON trim(a.cuenta) = trim(b.NUCTA)\n" +
                "where fecha like ? group by b.CODICONS46,a.cuenta,b.DERECTA,a.divisa,a.fecha\n" +
                "order by CODICONS46,cuenta");
        data.setParameter(1,periodo);
        data.setParameter(2,periodo+"%");
        return data.getResultList();
    }

    public List<Object[]> getMatchFit(String periodo){
        Query data = entityManager.createNativeQuery("select z.CODICONS46A,y.descripcion ,z.diferencia_eeff_conciliacionA,sum(z.diferenciaA), ISNULL(z.diferencia_eeff_conciliacionA,0)+ISNULL(sum(z.diferenciaA),0) total\n" +
                "FROM\n" +
                "(select b.CODICONS46 CODICONS46A, a.diferencia_eeff_conciliacion diferencia_eeff_conciliacionA,c.saldo_aplicativo-c.saldo_contable diferenciaA, ISNULL(a.diferencia_eeff_conciliacion,0)+ISNULL(c.saldo_aplicativo-c.saldo_contable,0) totalA\n" +
                "FROM nexco_cuadre_motor_diferencias a\n" +
                "INNER JOIN (SELECT NUCTA,CODICONS46 FROM CUENTAS_PUC WHERE EMPRESA = '0013' GROUP BY NUCTA, CODICONS46) b ON trim(a.neocon) = trim(b.CODICONS46)\n" +
                "LEFT JOIN (SELECT cuenta,divisa,saldo_aplicativo,saldo_contable FROM nexco_h140_completa WHERE fecha like ? ) c ON trim(b.NUCTA) = trim(c.cuenta)\n" +
                "where periodo = ? group by b.CODICONS46, a.diferencia_eeff_conciliacion,saldo_aplicativo,saldo_contable, ISNULL(a.diferencia_eeff_conciliacion,0)-ISNULL(c.saldo_aplicativo-c.saldo_contable,0)) z \n" +
                "LEFT JOIN (select cuenta,descripcion from nexco_cuentas_neocon) y ON z.CODICONS46A = y.cuenta \n" +
                "group by z.CODICONS46A,y.descripcion ,z.diferencia_eeff_conciliacionA");
        data.setParameter(1,periodo+"%");
        data.setParameter(2,periodo);
        return data.getResultList();
    }

    public List<String[]> saveFilePlantilla(Collection<Part>  parts, String period, User user) throws IOException, InvalidFormatException {

        List<String[]> finalList = new ArrayList<String[]>();
        Iterator<Row> rows = null;

        for (Part part : parts) {
            InputStream file = part.getInputStream();
            if (part != null && file != null && part.getSubmittedFileName() != null) {
                XSSFWorkbook wb = new XSSFWorkbook(file);
                XSSFSheet sheetTipoAval = wb.getSheetAt(0);
                rows = sheetTipoAval.iterator();
                finalList = validarPlantilla(rows, period, user);
            }
        }

        return finalList;
    }

    public List<String[]> validarPlantilla(Iterator<Row> rows, String mes,User user) {
        ArrayList<String[]> lista= new ArrayList<String[]>();
        ArrayList<FitDifferences> listTemp= new ArrayList<FitDifferences>();
        XSSFRow row;
        int fail=0;
        int success =0;
        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(row.getRowNum()>0)
            {
                String[] log1=new String[4];
                log1[2]="true";
                DataFormatter formatter = new DataFormatter();
                String cellNeocon = formatter.formatCellValue(row.getCell(0)).replace(" ", "");
                String cellFicheroSaldosIfrs9 = formatter.formatCellValue(row.getCell(1)).replace(" ", "");
                String cellFichaSaldosInicial = formatter.formatCellValue(row.getCell(2)).replace(" ", "");
                String cellDiferenciasIFRS9PI = formatter.formatCellValue(row.getCell(3)).replace(" ", "");
                String cellSaldoEEFF = formatter.formatCellValue(row.getCell(4)).replace(" ", "");
                String cellDiferenciaIFRS9EEFF = formatter.formatCellValue(row.getCell(5)).replace(" ", "");
                String cellPorcentajeEEFF= formatter.formatCellValue(row.getCell(6)).replace(" ", "");
                String cellSaldoConciliacion = formatter.formatCellValue(row.getCell(7)).replace(" ", "");
                String cellDiferenciaIFRS9Conciliacion = formatter.formatCellValue(row.getCell(8)).replace(" ", "");
                String cellPorcentajeConciliacion = formatter.formatCellValue(row.getCell(9)).replace(" ", "");
                String cellDiferenciaEEFFConciliacion = formatter.formatCellValue(row.getCell(10)).replace(" ", "");
                log1[0]=String.valueOf(row.getRowNum()+1);

                try {
                    log1[1] = CellReference.convertNumToColString(1) + " - (2)";
                    XSSFCell cell9= row.getCell(1);
                    cell9.setCellType(CellType.STRING);
                    cellFicheroSaldosIfrs9 = formatter.formatCellValue(cell9).replace(" ", "");
                    Double.parseDouble(cellFicheroSaldosIfrs9);

                    log1[1] = CellReference.convertNumToColString(2) + " - (3)";
                    XSSFCell cell8= row.getCell(2);
                    cell8.setCellType(CellType.STRING);
                    cellFichaSaldosInicial = formatter.formatCellValue(cell8).replace(" ", "");
                    Double.parseDouble(cellFichaSaldosInicial);

                    log1[1] = CellReference.convertNumToColString(3) + " - (4)";
                    XSSFCell cell7= row.getCell(3);
                    cell7.setCellType(CellType.STRING);
                    cellDiferenciasIFRS9PI = formatter.formatCellValue(cell7).replace(" ", "");
                    Double.parseDouble(cellDiferenciasIFRS9PI);

                    log1[1] = CellReference.convertNumToColString(4) + " - (5)";
                    XSSFCell cell6= row.getCell(4);
                    cell6.setCellType(CellType.STRING);
                    cellSaldoEEFF = formatter.formatCellValue(cell6).replace(" ", "");
                    Double.parseDouble(cellSaldoEEFF);

                    log1[1] = CellReference.convertNumToColString(5) + " - (6)";
                    XSSFCell cell5= row.getCell(5);
                    cell5.setCellType(CellType.STRING);
                    cellDiferenciaIFRS9EEFF = formatter.formatCellValue(cell5).replace(" ", "");
                    Double.parseDouble(cellDiferenciaIFRS9EEFF);

                    log1[1] = CellReference.convertNumToColString(6) + " - (7)";
                    XSSFCell cell4= row.getCell(6);
                    cell4.setCellType(CellType.STRING);
                    cellPorcentajeEEFF = formatter.formatCellValue(cell4).replace(" ", "");
                    Double.parseDouble(cellPorcentajeEEFF);

                    log1[1] = CellReference.convertNumToColString(7) + " - (8)";
                    XSSFCell cell3= row.getCell(7);
                    cell3.setCellType(CellType.STRING);
                    cellSaldoConciliacion = formatter.formatCellValue(cell3).replace(" ", "");
                    Double.parseDouble(cellSaldoConciliacion);

                    log1[1] = CellReference.convertNumToColString(8) + " - (9)";
                    XSSFCell cell2= row.getCell(8);
                    cell2.setCellType(CellType.STRING);
                    cellDiferenciaIFRS9Conciliacion = formatter.formatCellValue(cell2).replace(" ", "");
                    Double.parseDouble(cellDiferenciaIFRS9Conciliacion);

                    log1[1] = CellReference.convertNumToColString(9) + " - (10)";
                    XSSFCell cell1= row.getCell(9);
                    cell1.setCellType(CellType.STRING);
                    cellPorcentajeConciliacion = formatter.formatCellValue(cell1).replace(" ", "");
                    Double.parseDouble(cellPorcentajeConciliacion);

                    log1[1] = CellReference.convertNumToColString(10) + " - (11)";
                    XSSFCell cell0= row.getCell(10);
                    cell0.setCellType(CellType.STRING);
                    cellDiferenciaEEFFConciliacion = formatter.formatCellValue(cell0).replace(" ", "");
                    Double.parseDouble(cellDiferenciaEEFFConciliacion);
                }
                catch(Exception e)
                {
                    fail++;
                    log1[2] = "false";
                    log1[3] = "Tipo Dato de Celda Inválido";
                    lista.add(log1);
                }

                String[] log=new String[4];
                log[0]=String.valueOf(row.getRowNum()+1);
                log[2]="true";
                if (cellNeocon.isEmpty() || cellNeocon.isBlank()){
                    log[1] = CellReference.convertNumToColString(0) + " - (1)";
                    log[2] = "false";
                    log[3] = "Código Neocon no puede venir vacío";
                    fail++;
                    lista.add(log);
                } else if (cellDiferenciaEEFFConciliacion.isEmpty() || cellDiferenciaEEFFConciliacion.isBlank()) {
                    log[1] = CellReference.convertNumToColString(1) + " - (2)";
                    log[2] = "false";
                    log[3] = "Saldo de diferencia no puede venir vacío";
                    fail++;
                    lista.add(log);
                }
                else {
                    success++;
                    FitDifferences fitDifferencesInsert = new FitDifferences();
                    fitDifferencesInsert.setNeocon(cellNeocon);
                    fitDifferencesInsert.setFicheroSaldosIFRS9(Double.parseDouble(cellFicheroSaldosIfrs9));
                    fitDifferencesInsert.setFichaSaldosInicial(Double.parseDouble(cellFichaSaldosInicial));
                    fitDifferencesInsert.setDiferenciasIFRS9PI(Double.parseDouble(cellDiferenciasIFRS9PI));
                    fitDifferencesInsert.setSaldoEEFF(Double.parseDouble(cellSaldoEEFF));
                    fitDifferencesInsert.setDiferenciaIFRS9EEFF(Double.parseDouble(cellDiferenciaIFRS9EEFF));
                    fitDifferencesInsert.setPorcentajeEEFF(Double.parseDouble(cellPorcentajeEEFF));
                    fitDifferencesInsert.setSaldoConciliacion(Double.parseDouble(cellSaldoConciliacion));
                    fitDifferencesInsert.setDiferenciaIFRS9Conciliacion(Double.parseDouble(cellDiferenciaIFRS9Conciliacion));
                    fitDifferencesInsert.setPorcentajeConciliacion(Double.parseDouble(cellPorcentajeConciliacion));
                    fitDifferencesInsert.setDiferenciaEEFFConciliacion(Double.parseDouble(cellDiferenciaEEFFConciliacion));
                    fitDifferencesInsert.setPeriodo(mes);
                    listTemp.add(fitDifferencesInsert);
                    log[2] = "true";
                }
            }
        }
        String[] logFinal=new String[4];
        logFinal[0]="Plantilla";
        logFinal[1]=String.valueOf(success);
        logFinal[2]=String.valueOf(fail);
        logFinal[3]="PLANTILLA";// Lo cambie de true a PLANTILLA
        lista.add(logFinal);

        if(fail>0)
        {
            Date today=new Date();
            Audit insert = new Audit();
            insert.setAccion("Fallo Inserción Documento Cuadre Motor Diferencias");
            insert.setCentro(user.getCentro());
            insert.setComponente("IFRS9");
            insert.setFecha(today);
            insert.setInput("Motor Cuadre Diferencias");
            insert.setNombre(user.getNombre());
            insert.setUsuario(user.getUsuario());
            auditRepository.save(insert);
        }
        else
        {
            Date today=new Date();
            Audit insert = new Audit();
            insert.setAccion("Inserción Documento Cuadre Motor Diferencias");
            insert.setCentro(user.getCentro());
            insert.setComponente("IFRS9");
            insert.setFecha(today);
            insert.setInput("Motor Cuadre Diferencias");
            insert.setNombre(user.getNombre());
            insert.setUsuario(user.getUsuario());
            auditRepository.save(insert);

            clearAjuste(mes);
            fitDifferencesRepository.saveAll(listTemp);
        }

        return lista;
    }
}


