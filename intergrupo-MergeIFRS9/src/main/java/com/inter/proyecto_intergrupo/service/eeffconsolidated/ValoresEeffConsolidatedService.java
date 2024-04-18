package com.inter.proyecto_intergrupo.service.eeffconsolidated;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;

import com.inter.proyecto_intergrupo.model.eeffConsolidated.FiduciariaeeffFiliales;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.ValoreseeffFiliales;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.ValoresEeffConsolidatedRepository;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

@Service
@Transactional
public class ValoresEeffConsolidatedService {

    @Autowired
    private ValoresEeffConsolidatedRepository valoresEeffConsolidatedRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    public ValoresEeffConsolidatedService(ValoresEeffConsolidatedRepository valoresEeffConsolidatedRepository) {
        this.valoresEeffConsolidatedRepository = valoresEeffConsolidatedRepository;

    }
    public List<ValoreseeffFiliales> getEeffValoresConsolidatedDataByPeriod(String periodo) {
        return valoresEeffConsolidatedRepository.findByPeriodo(periodo);
    }

    public Page getAll(Pageable pageable, String periodo) {

        List<ValoreseeffFiliales> list = getEeffValoresConsolidatedDataByPeriod(periodo);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<ValoreseeffFiliales> pageAval = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return pageAval;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows, String periodo) {
        ArrayList<String[]> lista = new ArrayList();
        XSSFRow row;
        String stateFinal = "SUCCESS";
        ArrayList<ValoreseeffFiliales> toInsert = new ArrayList<>();
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                DataFormatter formatter = new DataFormatter();
                String cellcompania = formatter.formatCellValue(row.getCell(0));
                String cellnombreCompania = formatter.formatCellValue(row.getCell(1));
                String cellcuenta = formatter.formatCellValue(row.getCell(2));
                String cellnombreCuenta = formatter.formatCellValue(row.getCell(3));
                String cellSaldoAnterior = formatter.formatCellValue(row.getCell(4));
                String celldebitos = formatter.formatCellValue(row.getCell(5));
                String cellcreditos = formatter.formatCellValue(row.getCell(6));
                String cellsaldoFinal = formatter.formatCellValue(row.getCell(7));
                String cellcodigoCategoria = formatter.formatCellValue(row.getCell(8));
                String cellnombreCategoria = formatter.formatCellValue(row.getCell(9));
                String cellcodidoOyD = formatter.formatCellValue(row.getCell(10));
                String cellperiodo = formatter.formatCellValue(row.getCell(11));
                String cellnormaContable = formatter.formatCellValue(row.getCell(12));


                if (cellcompania.trim().length() != 1) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(0);
                    log1[2] = "El campo Compañia debe estar reportado por un caracer";
                    lista.add(log1);
                }

                if (cellnombreCompania.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(1);
                    log1[2] = "El nombre compañia no puede estar vacio";
                    lista.add(log1);
                }
                if (cellcuenta.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(2);
                    log1[2] = "El nombre compañia no puede estar vacio";
                    lista.add(log1);
                }

                if (cellnombreCuenta.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(3);
                    log1[2] = "El nombre compañia no puede estar vacio";
                    lista.add(log1);
                }

                try {
                    XSSFCell cell1 = row.getCell(4);
                    cell1.setCellType(CellType.STRING);
                    cellSaldoAnterior = formatter.formatCellValue(cell1).replace(" ", "");
                    Double.parseDouble(cellSaldoAnterior);
                } catch (Exception e) {
                    String[] log = new String[3];
                    log[0] = String.valueOf(row.getRowNum() + 1);
                    log[1] = CellReference.convertNumToColString(4);
                    log[2] = "Tipo dato incorrecto, debe ser un númerico decimal";
                    lista.add(log);
                }

                try {
                    XSSFCell cell1 = row.getCell(5);
                    cell1.setCellType(CellType.STRING);
                    celldebitos = formatter.formatCellValue(cell1).replace(" ", "");
                    Double.parseDouble(celldebitos);
                } catch (Exception e) {
                    String[] log = new String[3];
                    log[0] = String.valueOf(row.getRowNum() + 1);
                    log[1] = CellReference.convertNumToColString(5);
                    log[2] = "Tipo dato incorrecto, debe ser un númerico decimal";
                    lista.add(log);
                }

                try {
                    XSSFCell cell1 = row.getCell(6);
                    cell1.setCellType(CellType.STRING);
                    cellcreditos = formatter.formatCellValue(cell1).replace(" ", "");
                    Double.parseDouble(cellcreditos);
                } catch (Exception e) {
                    String[] log = new String[3];
                    log[0] = String.valueOf(row.getRowNum() + 1);
                    log[1] = CellReference.convertNumToColString(6);
                    log[2] = "Tipo dato incorrecto, debe ser un númerico decimal";
                    lista.add(log);
                }

                try {
                    XSSFCell cell1 = row.getCell(7);
                    cell1.setCellType(CellType.STRING);
                    cellsaldoFinal = formatter.formatCellValue(cell1).replace(" ", "");
                    Double.parseDouble(cellsaldoFinal);
                } catch (Exception e) {
                    String[] log = new String[3];
                    log[0] = String.valueOf(row.getRowNum() + 1);
                    log[1] = CellReference.convertNumToColString(7);
                    log[2] = "Tipo dato incorrecto, debe ser un númerico decimal";
                    lista.add(log);
                }

                if (cellcodigoCategoria.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(8);
                    log1[2] = "El campo Categoria de puede estar vacio ";
                    lista.add(log1);
                }

                if (cellnombreCategoria.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(9);
                    log1[2] = "El campo Nombre Categoria no puede estar vacio";
                    lista.add(log1);
                }

                if (cellcodidoOyD == null || cellcodidoOyD.trim().isEmpty() || cellcodidoOyD.matches("\\d*")) {
                    // No hacer nada en este caso, ya que cumple los criterios permitidos
                } else {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(10);
                    log1[2] = "El campo Codigo OyD no debe contener caracteres no numéricos";
                    lista.add(log1);
                }



                if (cellperiodo.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(11);
                    log1[2] = "El campo Periodo no debe estar vacio";
                    lista.add(log1);
                }

                if (cellnormaContable.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(12);
                    log1[2] = "El campo norma contable no debe estar vacio";
                    lista.add(log1);
                }

                ValoreseeffFiliales valoreseeffFiliales = new ValoreseeffFiliales();
                valoreseeffFiliales.setCompania(cellcompania);
                valoreseeffFiliales.setNombreCompania(cellnombreCompania);
                valoreseeffFiliales.setCuenta(cellcuenta);
                valoreseeffFiliales.setNombreCuenta(cellnombreCuenta);
                valoreseeffFiliales.setCodigoCategoria(cellcodigoCategoria);
                valoreseeffFiliales.setNombreCategoria(cellnombreCategoria);
                valoreseeffFiliales.setCodidoOyD(cellcodidoOyD);
                valoreseeffFiliales.setEmpresa("00560");
                valoreseeffFiliales.setPeriodo(periodo);
                valoreseeffFiliales.setNormaContable(cellnormaContable);
                try {
                    valoreseeffFiliales.setSaldoAnterior(Double.parseDouble(cellSaldoAnterior));
                    valoreseeffFiliales.setDebitos(Double.parseDouble(celldebitos));
                    valoreseeffFiliales.setCreditos(Double.parseDouble(cellcreditos));
                    valoreseeffFiliales.setSaldoFinal(Double.parseDouble(cellsaldoFinal));
                    valoreseeffFiliales.setMoneda("ML");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                toInsert.add(valoreseeffFiliales);
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
            valoresEeffConsolidatedRepository.deleteByPeriodo(periodo);
            valoresEeffConsolidatedRepository.saveAll(toInsert);
            Query validated = entityManager.createNativeQuery("SELECT EEFF.CUENTA AS CuentasCruzadas1,\n" +
                    "PUC.ID_CUENTA AS CuentaPuc From nexco_eeff_valores_filiales EEFF\n" +
                    "LEFT JOIN (Select * from nexco_puc_valores_filiales) PUC\n" +
                    "ON EEFF.cuenta = PUC.id_cuenta WHERE PUC.id_cuenta is null;");

            List<Object[]> listaInconsistencias1 = validated.getResultList();

            Query update = entityManager.createNativeQuery("update b set b.cod_cons = a.cod_cons from (select * from nexco_puc_valores_filiales) as a, (select * from nexco_eeff_valores_filiales where periodo = ?) as b where a.id_cuenta = b.cuenta");
            update.setParameter(1, toInsert.get(0).getPeriodo());
            update.executeUpdate();


            for (Object[] inconsistencia : listaInconsistencias1) {
                String[] log3 = new String[3];
                log3[0] = "Validacion";
                log3[1] = CellReference.convertNumToColString(2);
                log3[2] = "La Cuenta " + inconsistencia[0].toString() + " no se encuentra dentro del PUC." ;
                lista.add(log3);
            }
            if (lista.size() != 0) {
                valoresEeffConsolidatedRepository.deleteByPeriodo(periodo);
                stateFinal = "FAILED";
            } else {
                Query Naturaleza = entityManager.createNativeQuery("update b set b.naturaleza = a.naturaleza from nexco_puc_valores_filiales as a, (select * from nexco_eeff_valores_filiales where periodo = ?) as b where a.id_cuenta = b.cuenta\n");
                Naturaleza.setParameter(1,periodo);
                Naturaleza.executeUpdate();
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
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public void descargarEeffValores(HttpServletResponse response, String periodo) throws IOException {

        List<ValoreseeffFiliales> eeffDataListValores = getEeffValoresConsolidatedDataByPeriod(periodo);

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=CopiaEeffValores_" + periodo + ".xls");

        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("EEFFValores");

        Row headerRow = sheet.createRow(0);
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        headerRow.createCell(0).setCellValue("Empresa");
        headerRow.createCell(1).setCellValue("Cuenta");
        headerRow.createCell(2).setCellValue("Nombre Cuenta");
        headerRow.createCell(3).setCellValue("Naturaleza");
        headerRow.createCell(4).setCellValue("Saldo Anterior");
        headerRow.createCell(5).setCellValue("Debitos");
        headerRow.createCell(6).setCellValue("Creditos");
        headerRow.createCell(7).setCellValue("Saldo Final");
        headerRow.createCell(8).setCellValue("Moneda");
        headerRow.createCell(9).setCellValue("Cod Cons");
        headerRow.createCell(10).setCellValue("Periodo");


        int rowNum = 1;

        for (ValoreseeffFiliales eeffValores : eeffDataListValores) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(eeffValores.getEmpresa());
            row.createCell(1).setCellValue(eeffValores.getCuenta());
            row.createCell(2).setCellValue(eeffValores.getNombreCuenta());
            row.createCell(3).setCellValue(eeffValores.getNaturaleza());

            row.createCell(4).setCellValue(eeffValores.getSaldoAnterior());
            row.getCell(4).setCellStyle(style);

            row.createCell(5).setCellValue(eeffValores.getDebitos());
            row.getCell(5).setCellStyle(style);

            row.createCell(6).setCellValue(eeffValores.getCreditos());
            row.getCell(6).setCellStyle(style);

            row.createCell(7).setCellValue(eeffValores.getSaldoFinal());
            row.getCell(7).setCellStyle(style);

            row.createCell(8).setCellValue(eeffValores.getMoneda());
            row.createCell(9).setCellValue(eeffValores.getCodCons());
            row.createCell(10).setCellValue(eeffValores.getPeriodo());


        }
        workbook.write(response.getOutputStream());
        response.flushBuffer();
    }

}

