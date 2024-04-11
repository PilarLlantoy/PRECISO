package com.inter.proyecto_intergrupo.service.eeffconsolidated;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.FiduciariaeeffFiliales;
import com.inter.proyecto_intergrupo.model.parametric.AvalTypes;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.FiduciariaEeffConsolidatedRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import java.io.InputStream;


@Service
@Transactional
public class FiduciariaEeffConsolidatedService {

    @Autowired
    private FiduciariaEeffConsolidatedRepository fiduciariaEeffConsolidatedRepository;
    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    public FiduciariaEeffConsolidatedService(FiduciariaEeffConsolidatedRepository fiduciariaEeffConsolidatedRepository) {
        this.fiduciariaEeffConsolidatedRepository = fiduciariaEeffConsolidatedRepository;
    }

    public List<FiduciariaeeffFiliales> getEeffConsolidatedDataByPeriod(String periodo) {
        return fiduciariaEeffConsolidatedRepository.findByPeriodo(periodo);
    }

    public Page getAll(Pageable pageable, String periodo) {
        List<FiduciariaeeffFiliales> list = getEeffConsolidatedDataByPeriod(periodo);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<FiduciariaeeffFiliales> pageAval = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return pageAval;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows, String periodo) {
        ArrayList<String[]> lista = new ArrayList();
        Map<String,Integer> encabezados = new HashMap<>();
        List<String> listValidar = Arrays.asList("cuenta","nombre_cuenta","naturaleza","saldo_anterior","debitos","creditos","saldo_final_export","nivel");
        XSSFRow row;
        String stateFinal = "SUCCESS";
        ArrayList<FiduciariaeeffFiliales> toInsert = new ArrayList<>();
        boolean fallo = false;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if(row.getRowNum()==0)
            {
                DataFormatter formatter = new DataFormatter();
                for(int i = 0;row.getCell(i)!=null && !row.getCell(i).equals(""); i++)
                {
                    encabezados.put(formatter.formatCellValue(row.getCell(i)),i);
                }
                for (String entrada:listValidar) {
                    if(!encabezados.containsKey(entrada))
                    {
                        String[] log1 = new String[3];
                        log1[0] = "Error";
                        log1[1] = "Error";
                        log1[2] = "La Columna "+entrada+" no se encontro en el encabezado.";
                        lista.add(log1);
                        fallo=true;
                    }
                }
            }
            if (row.getRowNum() > 0 && fallo == false) {

                DataFormatter formatter = new DataFormatter();
                String cellcuenta = formatter.formatCellValue(row.getCell(encabezados.get("cuenta")));
                String cellnombreCuenta = formatter.formatCellValue(row.getCell(encabezados.get("nombre_cuenta")));
                String cellnaturaleza = formatter.formatCellValue(row.getCell(encabezados.get("naturaleza")));
                String cellSaldoAnterior = formatter.formatCellValue(row.getCell(encabezados.get("saldo_anterior")));
                String celldebitos = formatter.formatCellValue(row.getCell(encabezados.get("debitos")));
                String cellcreditos = formatter.formatCellValue(row.getCell(encabezados.get("creditos")));
                String cellsaldoFinalExport = formatter.formatCellValue(row.getCell(encabezados.get("saldo_final_export")));
                String cellnivel = formatter.formatCellValue(row.getCell(encabezados.get("nivel")));

                if (cellcuenta.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(encabezados.get("cuenta"));
                    log1[2] = "El Cuenta no puede estar vacio";
                    lista.add(log1);
                }

                if (cellnombreCuenta.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(encabezados.get("nombre_cuenta"));
                    log1[2] = "El nombre Cuenta no puede estar vacio";
                    lista.add(log1);
                }

                if (cellnaturaleza.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(encabezados.get("naturaleza"));
                    log1[2] = "El nombre Naturaleza no puede estar vacio";
                    lista.add(log1);
                }

                try {
                    XSSFCell cell1 = row.getCell(encabezados.get("saldo_anterior"));
                    cell1.setCellType(CellType.STRING);
                    cellSaldoAnterior = formatter.formatCellValue(cell1).replace(" ", "");
                    Double.parseDouble(cellSaldoAnterior);
                } catch (Exception e) {
                    String[] log = new String[3];
                    log[0] = String.valueOf(row.getRowNum() + 1);
                    log[1] = CellReference.convertNumToColString(encabezados.get("saldo_anterior"));
                    log[2] = "Tipo dato incorrecto, debe ser un númerico decimal";
                    lista.add(log);
                }

                try {
                    XSSFCell cell1 = row.getCell(encabezados.get("debitos"));
                    cell1.setCellType(CellType.STRING);
                    celldebitos = formatter.formatCellValue(cell1).replace(" ", "");
                    Double.parseDouble(celldebitos);
                } catch (Exception e) {
                    String[] log = new String[3];
                    log[0] = String.valueOf(row.getRowNum() + 1);
                    log[1] = CellReference.convertNumToColString(encabezados.get("debitos"));
                    log[2] = "Tipo dato incorrecto, debe ser un númerico decimal";
                    lista.add(log);
                }

                try {
                    XSSFCell cell1 = row.getCell(encabezados.get("creditos"));
                    cell1.setCellType(CellType.STRING);
                    cellcreditos = formatter.formatCellValue(cell1).replace(" ", "");
                    Double.parseDouble(cellcreditos);
                } catch (Exception e) {
                    String[] log = new String[3];
                    log[0] = String.valueOf(row.getRowNum() + 1);
                    log[1] = CellReference.convertNumToColString(encabezados.get("creditos"));
                    log[2] = "Tipo dato incorrecto, debe ser un númerico decimal";
                    lista.add(log);
                }

                try {
                    XSSFCell cell1 = row.getCell(encabezados.get("saldo_final_export"));
                    cell1.setCellType(CellType.STRING);
                    cellsaldoFinalExport = formatter.formatCellValue(cell1).replace(" ", "");
                    Double.parseDouble(cellsaldoFinalExport);
                } catch (Exception e) {
                    String[] log = new String[3];
                    log[0] = String.valueOf(row.getRowNum() + 1);
                    log[1] = CellReference.convertNumToColString(encabezados.get("saldo_final_export"));
                    log[2] = "Tipo dato incorrecto, debe ser un númerico decimal";
                    lista.add(log);
                }

                if (cellnivel.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(encabezados.get("nivel"));
                    log1[2] = "El campo Nivel no debe estar vacio";
                    lista.add(log1);
                }

                FiduciariaeeffFiliales FiduciariaeeffFiliales = new FiduciariaeeffFiliales();

                FiduciariaeeffFiliales.setCuenta(cellcuenta);
                FiduciariaeeffFiliales.setNombreCuenta(cellnombreCuenta);
                FiduciariaeeffFiliales.setNaturaleza(cellnaturaleza);
                FiduciariaeeffFiliales.setNivel(cellnivel);
                FiduciariaeeffFiliales.setEmpresa("00561");
                FiduciariaeeffFiliales.setPeriodo(periodo);

                if (cellcuenta.length()>=7) {
                    if (cellcuenta.charAt(6) == '1'){
                        FiduciariaeeffFiliales.setMoneda("ML");
                    }else {
                        FiduciariaeeffFiliales.setMoneda("ME");
                    }
                } else {
                    if (cellnombreCuenta.toUpperCase().contains("EXTRANJEERA")){
                        FiduciariaeeffFiliales.setMoneda("ME");
                    }else {
                        FiduciariaeeffFiliales.setMoneda("ML");
                    }
                }

                try {
                    FiduciariaeeffFiliales.setSaldoAnterior(Double.parseDouble(cellSaldoAnterior));
                    FiduciariaeeffFiliales.setDebitos(Double.parseDouble(celldebitos));
                    FiduciariaeeffFiliales.setCreditos(Double.parseDouble(cellcreditos));
                    FiduciariaeeffFiliales.setSaldoFinalExport(Double.parseDouble(cellsaldoFinalExport));

                } catch (Exception e) {
                    e.printStackTrace();
                }
                toInsert.add(FiduciariaeeffFiliales);
            }
        }

        if (lista.size() != 0) {
            stateFinal = "FAILED";
            String[] log2 = new String[3];
            log2[0] = String.valueOf((toInsert.size() * 13) - lista.size());
            log2[1] = String.valueOf(lista.size());
            log2[2] = stateFinal;
            lista.add(log2);
        }
        else {
            fiduciariaEeffConsolidatedRepository.deleteByPeriodo(periodo);
            fiduciariaEeffConsolidatedRepository.saveAll(toInsert);

            Query validated = entityManager.createNativeQuery("SELECT EEFF.cuenta AS CuentasCruzadas,\n" +
                    "PUC.cuenta AS CuentaPuc From nexco_eeff_fiduciaria_filiales EEFF\n" +
                    "LEFT JOIN (Select * from nexco_puc_fiduciaria_filiales) PUC\n" +
                    "ON EEFF.cuenta = PUC.cuenta WHERE PUC.cuenta is null;");

            List<Object[]> listaInconsistencias = validated.getResultList();

            Query update = entityManager.createNativeQuery("update b set b.cod_cons = a.cod_cons from (select * from nexco_puc_fiduciaria_filiales) as a, (select * from nexco_eeff_fiduciaria_filiales where periodo = ?) as b where a.cuenta = b.cuenta");
            update.setParameter(1, toInsert.get(0).getPeriodo());
            update.executeUpdate();


            for (Object[] inconsistencia : listaInconsistencias) {
                String[] log3 = new String[3];
                log3[0] = "Validacion";
                log3[1] = CellReference.convertNumToColString(2);
                log3[2] = "La Cuenta " + inconsistencia[0].toString() + " no se encuentra dentro del PUC." ;
                lista.add(log3);
            }
            if (lista.size() != 0) {
                stateFinal = "FAILED";
                fiduciariaEeffConsolidatedRepository.deleteByPeriodo(periodo);
            }
            String[] log3 = new String[3];
            log3[0] = String.valueOf((toInsert.size() * 13) - lista.size());
            log3[1] = String.valueOf(lista.size());
            log3[2] = stateFinal;
            lista.add(log3);
        }

        toInsert.clear();
        return lista;
    }

    public ArrayList<String[]> saveFileBD(InputStream file, String periodo) throws IOException, InvalidFormatException {
        ArrayList<String[]> list = new ArrayList<String[]>();
        if (file != null) {
            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            list = validarPlantilla(rows, periodo);
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
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }
    public void descargarEeff(HttpServletResponse response, String periodo) throws IOException {

        List<FiduciariaeeffFiliales> eeffData = getEeffConsolidatedDataByPeriod(periodo);

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=CopiaEeffFiduciaria_" + periodo + ".xls");

        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("EEFFFiduciaria");

        Row headerRow = sheet.createRow(0);
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        headerRow.createCell(0).setCellValue("Empresa");
        headerRow.createCell(1).setCellValue("Cuenta");
        headerRow.createCell(2).setCellValue("Nombre Cuenta");
        headerRow.createCell(3).setCellValue("Naturaleza");
        headerRow.createCell(4).setCellValue("Saldo Anterior");
        headerRow.createCell(5).setCellValue("Débitos");
        headerRow.createCell(6).setCellValue("Créditos");
        headerRow.createCell(7).setCellValue("Saldo Final Export");
        headerRow.createCell(8).setCellValue("Moneda");
        headerRow.createCell(9).setCellValue("Cod Cons");
        headerRow.createCell(10).setCellValue("Periodo");


        int rowNum = 1;
        for (FiduciariaeeffFiliales eeff : eeffData) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(eeff.getEmpresa());
            row.createCell(1).setCellValue(eeff.getCuenta());
            row.createCell(2).setCellValue(eeff.getNombreCuenta());
            row.createCell(3).setCellValue(eeff.getNaturaleza());

            double saldoFinalExport = eeff.getSaldoFinalExport();
            if ("C".equals(eeff.getNaturaleza())) {
                saldoFinalExport = Math.abs(saldoFinalExport)*-1;
            }else {
                saldoFinalExport = Math.abs(saldoFinalExport);
            }

            row.createCell(4).setCellValue(eeff.getSaldoAnterior());
            row.getCell(4).setCellStyle(style);

            row.createCell(5).setCellValue(eeff.getDebitos());
            row.getCell(5).setCellStyle(style);


            row.createCell(6).setCellValue(eeff.getCreditos());
            row.getCell(6).setCellStyle(style);

            Cell saldoFinalExportCell = row.createCell(7);
            saldoFinalExportCell.setCellValue(saldoFinalExport);
            saldoFinalExportCell.setCellStyle(style);

            row.createCell(8).setCellValue(eeff.getMoneda());
            row.createCell(9).setCellValue(eeff.getCodCons());
            row.createCell(10).setCellValue(eeff.getPeriodo());

        }
        workbook.write(response.getOutputStream());
        response.flushBuffer();
    }
}
