package com.inter.proyecto_intergrupo.service.eeffconsolidated;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;

import com.inter.proyecto_intergrupo.model.eeffConsolidated.FiduciariaPucFiliales;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.ValoresPucFiliales;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.ValoresPucTemporalFiliales;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.ValoresPucConsolidatedRepository;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.ValoresPucTemporalConsolidatedRepository;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
@Transactional

public class ValoresPucConsolidatedService {
    @Autowired
    private ValoresPucConsolidatedRepository valoresPucConsolidatedRepository;

    @Autowired
    private ValoresPucTemporalConsolidatedRepository valoresPucTemporalConsolidatedRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    public ValoresPucConsolidatedService(ValoresPucConsolidatedRepository valoresPucConsolidatedRepository) {
        this.valoresPucConsolidatedRepository = valoresPucConsolidatedRepository;
    }

    public List<ValoresPucFiliales> getPucDataByPeriod(String periodo) {
        List<ValoresPucFiliales> pucData = valoresPucConsolidatedRepository.findByPeriodo(periodo);
        return pucData;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows, String periodo) {
        ArrayList<String[]> lista = new ArrayList();
        XSSFRow row;
        String stateFinal = "SUCCESS";
        ArrayList<ValoresPucTemporalFiliales> toInsert = new ArrayList<>();
        while (rows.hasNext()) {

            Query deleteValores = entityManager.createNativeQuery("TRUNCATE TABLE nexco_puc_valores_filiales_temporal");
            deleteValores.executeUpdate();

            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                DataFormatter formatter = new DataFormatter();
                String cellIdCuenta = formatter.formatCellValue(row.getCell(0));
                String cellnombreCuenta = formatter.formatCellValue(row.getCell(1));
                String celltipoCuenta = formatter.formatCellValue(row.getCell(2));
                String cellmanejaCostos = formatter.formatCellValue(row.getCell(3));
                String cellmanejaCierre = formatter.formatCellValue(row.getCell(4));
                String cellmanejaMovimientos = formatter.formatCellValue(row.getCell(5));
                String cellmanejaMoneda = formatter.formatCellValue(row.getCell(6));
                String cellmanejaAjustes = formatter.formatCellValue(row.getCell(7));
                String cellpresupuesto = formatter.formatCellValue(row.getCell(8));
                String cellporcentajeImpuesto = formatter.formatCellValue(row.getCell(9));
                String cellctaPuc = formatter.formatCellValue(row.getCell(10));
                String cellflujoEfecivo = formatter.formatCellValue(row.getCell(11));
                String cellcodigoFlujoEfectivo = formatter.formatCellValue(row.getCell(12));
                String cellnaturaleza = formatter.formatCellValue(row.getCell(13));
                String cellcuentaOrdenSuperValores = formatter.formatCellValue(row.getCell(14));
                String cellcuentaOrdenDiferenciaEnCambio = formatter.formatCellValue(row.getCell(15));
                String cellmanejaSegmento = formatter.formatCellValue(row.getCell(16));
                String cellcodigoNormaContable = formatter.formatCellValue(row.getCell(17));
                String cellKardex = formatter.formatCellValue(row.getCell(18));
                String cellmoneda = formatter.formatCellValue(row.getCell(19));
                String cellusuarioActualizacion = formatter.formatCellValue(row.getCell(20));
                String cellfechaActualizacion = formatter.formatCellValue(row.getCell(21));
                String cellusuarioCreacion = formatter.formatCellValue(row.getCell(22));
                String cellfechaCreacion = formatter.formatCellValue(row.getCell(23));
                String cellcodCons = formatter.formatCellValue(row.getCell(24));


                if (cellIdCuenta.trim().length() == 255) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(0);
                    log1[2] = "El campo Cuenta debe estar reportado por un caracer";
                    lista.add(log1);
                }

                if (cellnombreCuenta.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(1);
                    log1[2] = "El nombre Cuenta no puede estar vacio";
                    lista.add(log1);
                }
                if (celltipoCuenta.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(2);
                    log1[2] = "El Tipo Cuenta no puede estar vacio";
                    lista.add(log1);
                }

                if (cellmanejaCostos.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(3);
                    log1[2] = "El tipo de dato maneja costos no puede estar vacio";
                    lista.add(log1);
                }

                if (cellmanejaCierre.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(4);
                    log1[2] = "El tipo de dato Maneja Cierre no puede estar vacio";
                    lista.add(log1);
                }


                if (cellmanejaMovimientos.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(5);
                    log1[2] = "El tipo de dato Maneja Movimientos no puede estar vacio ";
                    lista.add(log1);
                }

                if (cellmanejaMoneda.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(6);
                    log1[2] = "El tipo de dato Maneja Moneda no puede estar vacio";
                    lista.add(log1);
                }


                if (cellmanejaAjustes.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(7);
                    log1[2] = "El tipo de dato Maneja Ajustes no puede estar vacio";
                    lista.add(log1);
                }

                if (cellpresupuesto.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(8);
                    log1[2] = "El tipo de dato Maneja Presupuesto no puede estar vacioo";
                    lista.add(log1);
                }

                if (cellporcentajeImpuesto.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(9);
                    log1[2] = "El tipo de dato Porcentaje Impuesto no puede estar vacio";
                    lista.add(log1);
                }

                if (cellctaPuc == null || cellctaPuc.trim().isEmpty() || cellctaPuc.matches("\\d*")) {
                    // No hacer nada en este caso, ya que cumple los criterios permitidos
                } else {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(10);
                    log1[2] = "El campo Cta Puc esta mal diligenciado";
                    lista.add(log1);
                }


                if (cellflujoEfecivo.trim().length() == 255) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(11);
                    log1[2] = "El tipo de dato Flujo Efectivo no puede estar vacio";
                    lista.add(log1);
                }

                if (cellcodigoFlujoEfectivo == null || cellcodigoFlujoEfectivo.trim().isEmpty() || cellcodigoFlujoEfectivo.matches("\\d*")) {
                    // No hacer nada en este caso, ya que cumple los criterios permitidos
                } else {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(12);
                    log1[2] = "El campo Cta Puc esta mal diligenciado";
                    lista.add(log1);
                }

                if (cellnaturaleza.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(13);
                    log1[2] = "El tipo de dato Naturaleza no puede estar vacio";
                    lista.add(log1);
                }

                if (cellcuentaOrdenSuperValores == null || cellcuentaOrdenSuperValores.trim().isEmpty() || cellcuentaOrdenSuperValores.matches("\\d*")) {
                    // No hacer nada en este caso, ya que cumple los criterios permitidos
                } else {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(14);
                    log1[2] = "El campo Cta Puc esta mal diligenciado";
                    lista.add(log1);
                }

                if (cellcuentaOrdenDiferenciaEnCambio == null || cellcuentaOrdenDiferenciaEnCambio.trim().isEmpty() || cellcuentaOrdenDiferenciaEnCambio.matches("\\d*")) {
                    // No hacer nada en este caso, ya que cumple los criterios permitidos
                } else {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(14);
                    log1[2] = "El campo Cta Puc esta mal diligenciado";
                    lista.add(log1);
                }


                if (cellmanejaSegmento.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(16);
                    log1[2] = "El tipo de dato Maneja Segmento no puede estar vacio";
                    lista.add(log1);
                }
                if (cellcodigoNormaContable.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(17);
                    log1[2] = "El tipo de dato Codigo Norma Contable no puede estar vacio";
                    lista.add(log1);
                }
                if (cellKardex.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(18);
                    log1[2] = "El tipo de dato Maneja Kardex no puede estar vacio";
                    lista.add(log1);
                }

                if (cellmoneda == null || cellmoneda.trim().isEmpty()) {
                    // El campo está vacío, no se agrega mensaje de error
                } else if (!cellmoneda.matches("[a-zA-Z]+")) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(19);
                    log1[2] = "El tipo de dato Usuario Actualizacion solo puede contener letras o estar vacío";
                    lista.add(log1);
                }

                if (cellusuarioActualizacion.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(20);
                    log1[2] = "El tipo de dato Usuario Actualizacion no puede estar vacio";
                    lista.add(log1);
                }

                if (cellfechaActualizacion.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(21);
                    log1[2] = "El tipo de dato Fecha Actualizacion no puede estar vacio";
                    lista.add(log1);
                }
                if (cellusuarioCreacion.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(22);
                    log1[2] = "El tipo de dato Usuario Creacion no puede estar vacio";
                    lista.add(log1);
                }
                if (cellfechaCreacion.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(23);
                    log1[2] = "El tipo de dato Fecha Creacion no puede estar vacio";
                    lista.add(log1);
                }


                ValoresPucTemporalFiliales valoresPucFiliales = new ValoresPucTemporalFiliales();
                valoresPucFiliales.setPeriodo(periodo);
                valoresPucFiliales.setIdcuenta(cellIdCuenta);
                valoresPucFiliales.setNombreCuenta(cellnombreCuenta);
                valoresPucFiliales.setTipoCuenta(celltipoCuenta);
                valoresPucFiliales.setManejaCostos(cellmanejaCostos);
                valoresPucFiliales.setManejaCierre(cellmanejaCierre);
                valoresPucFiliales.setManejaMovimientos(cellmanejaMovimientos);
                valoresPucFiliales.setManejaMoneda(cellmanejaMoneda);
                valoresPucFiliales.setManejaAjustes(cellmanejaAjustes);
                valoresPucFiliales.setPresupuesto(cellpresupuesto);
                valoresPucFiliales.setPorcentajeImpuesto(cellporcentajeImpuesto);
                valoresPucFiliales.setCtaPuc(cellctaPuc);
                valoresPucFiliales.setFlujoEfecivo(cellflujoEfecivo);
                valoresPucFiliales.setCodigoFlujoEfectivo(cellcodigoFlujoEfectivo);
                if (cellnaturaleza.equals("CR"))
                    valoresPucFiliales.setNaturaleza("C");
                else
                    valoresPucFiliales.setNaturaleza("D");
                valoresPucFiliales.setCuentaOrdenSuperValores(cellcuentaOrdenSuperValores);
                valoresPucFiliales.setCuentaOrdenDiferenciaEnCambio(cellcuentaOrdenDiferenciaEnCambio);
                valoresPucFiliales.setManejaSegmento(cellmanejaSegmento);
                valoresPucFiliales.setCodigoNormaContable(cellcodigoNormaContable);
                valoresPucFiliales.setManejaKardex(cellKardex);
                valoresPucFiliales.setCodCons(cellcodCons);
                valoresPucFiliales.setEmpresa("00560");
                valoresPucFiliales.setUsuarioActualizacion(cellusuarioActualizacion);
                valoresPucFiliales.setUsuarioCreacion(cellusuarioCreacion);
                valoresPucFiliales.setMoneda("ML");
                try {

                    String formatPattern = "dd/MM/yyyy";
                    SimpleDateFormat dateFormat = new SimpleDateFormat(formatPattern);


                    Date fechaActualizacionCasteada = dateFormat.parse(cellfechaActualizacion);
                    valoresPucFiliales.setFechaActualizacion(fechaActualizacionCasteada);


                    Date fechaCreacion = dateFormat.parse(cellfechaCreacion);
                    valoresPucFiliales.setFechaCreacion(fechaCreacion);


                } catch (Exception e) {
                    e.printStackTrace();
                }
                toInsert.add(valoresPucFiliales);
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
            valoresPucTemporalConsolidatedRepository.saveAll(toInsert);

            Query Update = entityManager.createNativeQuery("insert into nexco_puc_valores_filiales(id_cuenta , codigo_flujo_efectivo, codigo_norma_contable, cta_puc, cuenta_orden_diferencia_en_cambio, cuenta_orden_super_valores, fecha_actualizacion, fecha_creacion, flujo_efectivo, maneja_ajustes, maneja_cierre, maneja_costos, maneja_kardex, maneja_moneda, maneja_movimientos, maneja_segmento, moneda, naturaleza, nombre_cuenta, periodo, porcentaje_impuesto, presupuesto, tipo_cuenta, usuario_actualizacion, usuario_creacion, empresa, cod_cons)\n" +
                    "select a.id_cuenta , a.codigo_flujo_efectivo, a.codigo_norma_contable, a.cta_puc, a.cuenta_orden_diferencia_en_cambio, a.cuenta_orden_super_valores, a.fecha_actualizacion, a.fecha_creacion, a.flujo_efectivo, a.maneja_ajustes, a.maneja_cierre, a.maneja_costos, a.maneja_kardex, a.maneja_moneda, a.maneja_movimientos, a.maneja_segmento, a.moneda, a.naturaleza, a.nombre_cuenta, a.periodo, a.porcentaje_impuesto, a.presupuesto, a.tipo_cuenta, a.usuario_actualizacion, a.usuario_creacion, a.empresa, a.cod_cons from nexco_puc_valores_filiales_temporal as a\n" +
                    "LEFT JOIN nexco_puc_valores_filiales as b\n" +
                    "ON a.id_cuenta = b.id_cuenta where b.id_cuenta IS NULL");
            Update.executeUpdate();

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

    public void downloadPucValores(HttpServletResponse response, List<ValoresPucFiliales> pucData) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=PUC_Valores.xls");

        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("PUCValores");

        Row headerRow = sheet.createRow(0);
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));


        headerRow.createCell(0).setCellValue("Empresa");
        headerRow.createCell(1).setCellValue("Cuenta");
        headerRow.createCell(2).setCellValue("Clase Cuenta");
        headerRow.createCell(3).setCellValue("Nombre Cuenta");
        headerRow.createCell(4).setCellValue("Naturaleza");
        headerRow.createCell(5).setCellValue("Indic");
        headerRow.createCell(6).setCellValue("Estado de Cuenta");
        headerRow.createCell(7).setCellValue("Moneda");
        headerRow.createCell(8).setCellValue("Cod Cons");
        headerRow.createCell(9).setCellValue("Periodo");


        int rowNum = 1;
        for (ValoresPucFiliales pucItem : pucData) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(pucItem.getEmpresa());
            row.createCell(1).setCellValue(pucItem.getIdcuenta());
            row.createCell(2).setCellValue(pucItem.getIdcuenta().substring(0, 1));
            row.createCell(3).setCellValue(pucItem.getNombreCuenta());
            row.createCell(4).setCellValue(pucItem.getNaturaleza());
            if (pucItem.getManejaMovimientos().equals("0"))
                row.createCell(5).setCellValue("L");
            else
                row.createCell(5).setCellValue("I");
            row.createCell(6).setCellValue("A");
            row.createCell(7).setCellValue(pucItem.getMoneda());
            row.createCell(8).setCellValue(pucItem.getCodCons());
            row.createCell(9).setCellValue(pucItem.getPeriodo());

        }

        workbook.write(response.getOutputStream());
        response.flushBuffer();
    }

    public void downloadAllPuc(HttpServletResponse response) throws IOException {
        List<ValoresPucFiliales> pucData = getAllPucData();

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=PUC_Completo.xlsx");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("PUCValores");


        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Empresa");
        headerRow.createCell(1).setCellValue("Cuenta");
        headerRow.createCell(2).setCellValue("Clase Cuenta");
        headerRow.createCell(3).setCellValue("Nombre Cuenta");
        headerRow.createCell(4).setCellValue("Naturaleza");
        headerRow.createCell(5).setCellValue("Indic");
        headerRow.createCell(6).setCellValue("Estado de Cuenta");
        headerRow.createCell(7).setCellValue("Moneda");
        headerRow.createCell(8).setCellValue("Cod Cons");
        headerRow.createCell(9).setCellValue("Periodo");


        int rowNum = 1;
        for (ValoresPucFiliales pucItem : pucData) {
            Row row = sheet.createRow(rowNum++);
            // Agregar los datos correspondientes a las celdas
            row.createCell(0).setCellValue(pucItem.getEmpresa());
            row.createCell(1).setCellValue(pucItem.getIdcuenta());
            row.createCell(2).setCellValue(pucItem.getIdcuenta().substring(0, 1));
            row.createCell(3).setCellValue(pucItem.getNombreCuenta());
            row.createCell(4).setCellValue(pucItem.getNaturaleza());
            if (pucItem.getManejaMovimientos().equals("0"))
                row.createCell(5).setCellValue("L");
            else
                row.createCell(5).setCellValue("I");
            row.createCell(6).setCellValue("A");
            row.createCell(7).setCellValue(pucItem.getMoneda());
            row.createCell(8).setCellValue(pucItem.getCodCons());
            row.createCell(9).setCellValue(pucItem.getPeriodo());


        }
        workbook.write(response.getOutputStream());
        response.flushBuffer();
    }

    public List<ValoresPucFiliales> getAllPucData() {
        return valoresPucConsolidatedRepository.findAll();
    }

}

