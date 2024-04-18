package com.inter.proyecto_intergrupo.service.eeffconsolidated;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.FiduciariaPucFiliales;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.FiduciariaPucTemporalFiliales;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.FiduciariaPucConsolidatedFiduciariaRepository;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.FiduciariaPucTemporalConsolidatedFiduciariaRepository;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional

public class FiduciariaPucConsolidatedService {
    @Autowired
    private FiduciariaPucConsolidatedFiduciariaRepository fiduciariaPucConsolidatedFiduciariaRepository;

    @Autowired
    private FiduciariaPucTemporalConsolidatedFiduciariaRepository fiduciariaPucTemporalConsolidatedFiduciariaRepository;
    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    public FiduciariaPucConsolidatedService(FiduciariaPucConsolidatedFiduciariaRepository fiduciariaPucConsolidatedFiduciariaRepository) {
        this.fiduciariaPucConsolidatedFiduciariaRepository = fiduciariaPucConsolidatedFiduciariaRepository;
    }

    public List<FiduciariaPucFiliales> getPucDataByPeriod(String periodo) {
        List<FiduciariaPucFiliales> pucData = fiduciariaPucConsolidatedFiduciariaRepository.findByPeriodo(periodo);
        return pucData;
    }

    public List<FiduciariaPucFiliales> getAllPucData() {

        Query load = entityManager.createNativeQuery("SELECT * FROM nexco_puc_fiduciaria_filiales order by cuenta",FiduciariaPucFiliales.class);
        return load.getResultList();
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows , String periodo) {
        ArrayList<String[]> lista = new ArrayList();
        List<String> listValidar = Arrays.asList("tipo_de_puc","clase_cuenta","cuenta","nombre_cuenta","nivel","imputable","naturaleza","estado_cuenta","descripcion_tipo_puc","codicons");
        Map<String,Integer> encabezados = new HashMap<>();
        XSSFRow row;
        String stateFinal = "SUCCESS";
        boolean fallo = false;
        ArrayList<FiduciariaPucTemporalFiliales> toInsert = new ArrayList<>();
        while (rows.hasNext()) {

          Query delete = entityManager.createNativeQuery("TRUNCATE TABLE nexco_puc_fiduciaria_filiales_temporal");
          delete.executeUpdate();

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
                String celltipodepuc = formatter.formatCellValue(row.getCell(encabezados.get("tipo_de_puc")));
                String cellclaseCuenta = formatter.formatCellValue(row.getCell(encabezados.get("clase_cuenta")));
                String cellcuenta = formatter.formatCellValue(row.getCell(encabezados.get("cuenta")));
                String cellnombreCuenta = formatter.formatCellValue(row.getCell(encabezados.get("nombre_cuenta")));
                String cellnivel = formatter.formatCellValue(row.getCell(encabezados.get("nivel")));
                String cellimputable = formatter.formatCellValue(row.getCell(encabezados.get("imputable")));
                String cellnaturaleza = formatter.formatCellValue(row.getCell(encabezados.get("naturaleza")));
                String cellestadocuenta = formatter.formatCellValue(row.getCell(encabezados.get("estado_cuenta")));
                String celldescripcionTipoPuc = formatter.formatCellValue(row.getCell(encabezados.get("descripcion_tipo_puc")));
                String cellcodCons = formatter.formatCellValue(row.getCell(encabezados.get("codicons")));

                if (celltipodepuc.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(encabezados.get("tipo_de_puc"));
                    log1[2] = "El campo Cuenta debe estar reportado por un caracer";
                    lista.add(log1);
                }

                if (cellclaseCuenta.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(encabezados.get("clase_cuenta"));
                    log1[2] = "El Clase Cuenta no puede estar vacio";
                    lista.add(log1);
                }
                if (cellcuenta.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(encabezados.get("cuenta"));
                    log1[2] = "El tipo de dato Cuenta no puede estar vacio";
                    lista.add(log1);
                }

                if (cellnombreCuenta.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(encabezados.get("nombre_cuenta"));
                    log1[2] = "El tipo de dato Nombre Cuenta s no puede estar vacio";
                    lista.add(log1);
                }

                if (cellnivel.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(encabezados.get("nivel"));
                    log1[2] = "El tipo de dato Nivel no puede estar vacio";
                    lista.add(log1);
                }


                if (cellimputable.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(encabezados.get("imputable"));
                    log1[2] = "El tipo de dato Imputable no puede estar vacio ";
                    lista.add(log1);
                }

                if (cellnaturaleza.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(encabezados.get("naturaleza"));
                    log1[2] = "El tipo de dato Naturaleza no puede estar vacio";
                    lista.add(log1);
                }
                if (cellestadocuenta.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(encabezados.get("estado_cuenta"));
                    log1[2] = "El tipo de dato Estado Cuenta no puede estar vacio";
                    lista.add(log1);
                }

                if (celldescripcionTipoPuc.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(encabezados.get("descripcion_tipo_puc"));
                    log1[2] = "El tipo de dato Tipo Puc no puede estar vacioo";
                    lista.add(log1);
                }

                FiduciariaPucTemporalFiliales fiduciariaPucFiliales = new FiduciariaPucTemporalFiliales();
                fiduciariaPucFiliales.setTipoDePuc(celltipodepuc);
                fiduciariaPucFiliales.setClaseCuenta(cellclaseCuenta);
                fiduciariaPucFiliales.setCuenta(cellcuenta);
                fiduciariaPucFiliales.setNombreCuenta(cellnombreCuenta);
                fiduciariaPucFiliales.setNivel(cellnivel);
                fiduciariaPucFiliales.setNaturaleza(cellnaturaleza);
                fiduciariaPucFiliales.setImputable(cellimputable);
                fiduciariaPucFiliales.setEstadoCuenta(cellestadocuenta);
                fiduciariaPucFiliales.setDescripcionTipoPuc(celldescripcionTipoPuc);

                if (cellcuenta.length()>=7) {
                    if (cellcuenta.charAt(6) == '1'){
                        fiduciariaPucFiliales.setMoneda("ML");
                    }else {
                        fiduciariaPucFiliales.setMoneda("ME");
                    }
                } else {
                    if (cellnombreCuenta.toUpperCase().contains("EXTRANJEERA")){
                        fiduciariaPucFiliales.setMoneda("ME");
                    }else {
                        fiduciariaPucFiliales.setMoneda("ML");
                    }
                }
                fiduciariaPucFiliales.setPeriodo(periodo);
                fiduciariaPucFiliales.setEmpresa("00561");
                fiduciariaPucFiliales.setCodCons(cellcodCons);
                toInsert.add(fiduciariaPucFiliales);
            }
        }
        if (lista.size() != 0)
            stateFinal = "FAILED";
        String[] log2 = new String[3];
        log2[0] = String.valueOf((toInsert.size() * 13) - lista.size());
        log2[1] = String.valueOf(lista.size());
        log2[2] = stateFinal;
        lista.add(log2);
        String[] temp = lista.get(0);
        if (temp[2].equals("SUCCESS")) {

            fiduciariaPucTemporalConsolidatedFiduciariaRepository.saveAll(toInsert);

            Query Update = entityManager.createNativeQuery("insert into nexco_puc_fiduciaria_filiales(clase_cuenta, cuenta , descripcion_tipo_puc, estado_cuenta, imputable, moneda, naturaleza, nivel, nombre_cuenta, periodo, tipo_de_puc, empresa, cod_cons)\n" +
                    "select a.clase_cuenta, a.cuenta , a.descripcion_tipo_puc, a.estado_cuenta, a.imputable, a.moneda, a.naturaleza, a.nivel, a.nombre_cuenta, a.periodo, a.tipo_de_puc, a.empresa, a.cod_cons from nexco_puc_fiduciaria_filiales_temporal as a\n" +
                    "LEFT JOIN nexco_puc_fiduciaria_filiales as b\n" +
                    "ON a.cuenta = b.cuenta where b.cuenta IS NULL ");
            Update.executeUpdate();

        }
        toInsert.clear();
        return lista;
    }

    public ArrayList<String[]> saveFileBD(InputStream file , String periodo) throws IOException, InvalidFormatException {
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

    public void downloadPuc(HttpServletResponse response, List<FiduciariaPucFiliales> pucData) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=PUC_Fiduciaria.xls");

        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("PUCFiduciaraia");

        Row headerRow = sheet.createRow(0);
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));


        headerRow.createCell(0).setCellValue("Empresa");
        headerRow.createCell(1).setCellValue("Cuenta");
        headerRow.createCell(2).setCellValue("Clase de Cuenta");
        headerRow.createCell(3).setCellValue("Nombre de Cuenta");
        headerRow.createCell(4).setCellValue("Naturaleza");
        headerRow.createCell(5).setCellValue("Indic");
        headerRow.createCell(6).setCellValue("Estado de Cuenta");
        headerRow.createCell(7).setCellValue("Moneda");
        headerRow.createCell(8).setCellValue("Cod Cons");
        headerRow.createCell(9).setCellValue("Periodo");


        int rowNum = 1;
        for (FiduciariaPucFiliales pucItem : pucData) {
            Row row = sheet.createRow(rowNum++);
            // Agregar los datos correspondientes a las celdas
            row.createCell(0).setCellValue(pucItem.getEmpresa());
            row.createCell(1).setCellValue(pucItem.getCuenta());
            row.createCell(2).setCellValue(pucItem.getClaseCuenta());
            row.createCell(3).setCellValue(pucItem.getNombreCuenta());
            row.createCell(4).setCellValue(pucItem.getNaturaleza());
            if (pucItem.getImputable().equals("N"))
                row.createCell(5).setCellValue("L");
            else
                row.createCell(5).setCellValue("I");
            row.createCell(6).setCellValue(pucItem.getEstadoCuenta());
            row.createCell(7).setCellValue(pucItem.getMoneda());
            row.createCell(8).setCellValue(pucItem.getCodCons());
            row.createCell(9).setCellValue(pucItem.getPeriodo());

        }

        workbook.write(response.getOutputStream());
        response.flushBuffer();
    }

    public void downloadAllPuc(HttpServletResponse response) throws IOException {

        List<FiduciariaPucFiliales> pucData = getAllPucData();

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=PUC_Completo.xlsx");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("PUCFiduciaria");


        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Empresa");
        headerRow.createCell(1).setCellValue("Cuenta");
        headerRow.createCell(2).setCellValue("Clase de Cuenta");
        headerRow.createCell(3).setCellValue("Nombre de Cuenta");
        headerRow.createCell(4).setCellValue("Naturaleza");
        headerRow.createCell(5).setCellValue("Indic");
        headerRow.createCell(6).setCellValue("Estado de Cuenta");
        headerRow.createCell(7).setCellValue("Moneda");
        headerRow.createCell(8).setCellValue("Cod Cons");
        headerRow.createCell(9).setCellValue("Periodo");

        int rowNum = 1;
        for (FiduciariaPucFiliales pucItem : pucData) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(pucItem.getEmpresa());
            row.createCell(1).setCellValue(pucItem.getCuenta());
            row.createCell(2).setCellValue(pucItem.getClaseCuenta());
            row.createCell(3).setCellValue(pucItem.getNombreCuenta());
            row.createCell(4).setCellValue(pucItem.getNaturaleza());
            if (pucItem.getImputable().equals("N"))
                row.createCell(5).setCellValue("L");
            else
                row.createCell(5).setCellValue("I");
            row.createCell(6).setCellValue(pucItem.getEstadoCuenta());
            row.createCell(7).setCellValue(pucItem.getMoneda());
            row.createCell(8).setCellValue(pucItem.getCodCons());
            row.createCell(9).setCellValue(pucItem.getPeriodo());

        }
        workbook.write(response.getOutputStream());
        response.flushBuffer();
    }
}

