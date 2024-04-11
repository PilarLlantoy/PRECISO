package com.inter.proyecto_intergrupo.service.accountsReceivable;

import com.inter.proyecto_intergrupo.model.accountsReceivable.InvoicesCc;
import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.dataquality.PointRulesDQ;
import com.inter.proyecto_intergrupo.model.dataquality.RulesDQ;
import com.inter.proyecto_intergrupo.model.parametric.Signature;
import com.inter.proyecto_intergrupo.model.parametric.ThirdsCc;
import com.inter.proyecto_intergrupo.repository.accountsReceivable.InvoicesCcRepository;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.dataquality.PointRulesDQRepository;
import com.inter.proyecto_intergrupo.repository.dataquality.RulesDQRepository;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import com.inter.proyecto_intergrupo.service.parametricServices.SignatureService;
import com.inter.proyecto_intergrupo.service.parametricServices.ThirdsCcService;
import com.inter.proyecto_intergrupo.service.resourcesServices.SendEmailService;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import org.docx4j.Docx4J;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.misc.Signal;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class InvoicesCcService {

    @Autowired
    private InvoicesCcRepository invoicesCcRepository;

    @Autowired
    private ThirdsCcService thirdsCcService;

    @Autowired
    private SignatureService signatureService;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private SendEmailService sendEmailService;

    public InvoicesCcService(InvoicesCcRepository invoicesCcRepository) {
        this.invoicesCcRepository = invoicesCcRepository;
    }

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Cuentas Por Cobrar");
        insert.setFecha(today);
        insert.setInput("Informe");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public ArrayList<String[]> saveFileBD(InputStream file, String periodo) throws IOException, InvalidFormatException {
        ArrayList<String[]> list = new ArrayList<String[]>();
        if (file != null) {
            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            list = validarPlantilla(rows,periodo);
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows, String periodo) {
        ArrayList<String[]> lista = new ArrayList();
        XSSFRow row;
        String stateFinal = "SUCCESS";
        ArrayList<InvoicesCc> toInsert = new ArrayList<>();
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                DataFormatter formatter = new DataFormatter();
                String cellTercero = formatter.formatCellValue(row.getCell(0)).trim();
                String cellFecha = formatter.formatCellValue(row.getCell(1)).trim();
                String cellConcepto = formatter.formatCellValue(row.getCell(2)).trim();
                String cellPersona= formatter.formatCellValue(row.getCell(3)).trim();
                String cellValor = formatter.formatCellValue(row.getCell(4)).trim();
                //Date fechaDate= new Date();

                if (cellTercero.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(0);
                    log1[2] = "El campo Tercero no puede estar vacío";
                    lista.add(log1);
                }
                else
                {
                    if (validateThird(cellTercero)) {
                        String[] log1 = new String[3];
                        log1[0] = String.valueOf(row.getRowNum() + 1);
                        log1[1] = CellReference.convertNumToColString(0);
                        log1[2] = "El Tercero con Nit: "+ cellTercero +" no se encuentra parametrizado";
                        lista.add(log1);
                    }
                    else
                    {
                        if (cellConcepto.trim().length() == 0) {
                            String[] log1 = new String[3];
                            log1[0] = String.valueOf(row.getRowNum() + 1);
                            log1[1] = CellReference.convertNumToColString(2);
                            log1[2] = "El campo Concepto no puede estar vacío";
                            lista.add(log1);
                        }
                        else
                        {
                            if (validateConcept(cellConcepto,"CAUSACIÓN")) {
                                String[] log1 = new String[3];
                                log1[0] = String.valueOf(row.getRowNum() + 1);
                                log1[1] = CellReference.convertNumToColString(2);
                                log1[2] = "La Concepto "+ cellConcepto +" no tiene las 4 combianaciones ('SIN IMPUESTO','IVA','RETEFUENTE','CUENTA POR COBRAR') de Impuesto en la  parametrica de cuentas para el evento Cuasación";
                                lista.add(log1);
                            }
                            if (validateConcept(cellConcepto,"PAGO")) {
                                String[] log1 = new String[3];
                                log1[0] = String.valueOf(row.getRowNum() + 1);
                                log1[1] = CellReference.convertNumToColString(2);
                                log1[2] = "La Concepto "+ cellConcepto +" no tiene las 2 combianaciones ('PAGO','CUENTA POR COBRAR') de Impuesto en la  parametrica de cuentas para el evento Pago";
                                lista.add(log1);
                            }
                        }
                    }
                }
                if (cellFecha.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(1);
                    log1[2] = "El campo Fecha no puede estar vacío";
                    lista.add(log1);
                }
                /*else
                {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        fechaDate= sdf.parse(cellFecha);
                    } catch (Exception e) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(1);
                        log[2] = "Tipo dato incorrecto, debe ser un fecha en formato (YYYY-MM-DD)";
                        lista.add(log);
                    }
                }*/
                if (cellPersona.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(3);
                    log1[2] = "El campo Persona no puede estar vacío";
                    lista.add(log1);
                }
                try {
                    XSSFCell cell1 = row.getCell(4);
                    cell1.setCellType(CellType.STRING);
                    cellValor = formatter.formatCellValue(cell1).replace(" ", "");
                    Double.parseDouble(cellValor);
                } catch (Exception e) {
                    String[] log = new String[3];
                    log[0] = String.valueOf(row.getRowNum() + 1);
                    log[1] = CellReference.convertNumToColString(4);
                    log[2] = "Tipo dato incorrecto, debe ser un númerico decimal";
                    lista.add(log);
                }

                InvoicesCc invoicesCc = new InvoicesCc();
                invoicesCc.setTercero(cellTercero);
                invoicesCc.setConcepto(cellConcepto);
                invoicesCc.setPersona(cellPersona);
                invoicesCc.setEstado("Pendiente");
                invoicesCc.setPeriodo(periodo);
                try
                {
                    invoicesCc.setValor(Double.parseDouble(cellValor));
                    invoicesCc.setFecha(cellFecha);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                toInsert.add(invoicesCc);
            }
        }
        if(lista.size()!=0)
            stateFinal = "FAILED";
        String[] log2 = new String[3];
        log2[0] = String.valueOf((toInsert.size()*11)-lista.size());
        log2[1] = String.valueOf(lista.size());
        log2[2] = stateFinal;
        lista.add(log2);
        String[] temp = lista.get(0);
        if (temp[2].equals("SUCCESS"))
        {
            invoicesCcRepository.saveAll(toInsert);
            Query consulta = entityManager.createNativeQuery("select tercero from nexco_facturas_cc where periodo = ? group by tercero");
            consulta.setParameter(1,periodo);
            List<String> listTemp = consulta.getResultList();
            for (int i = 0; i < listTemp.size();i++)
            {
                int conse=i+1;
                Query update = entityManager.createNativeQuery("update nexco_facturas_cc set lote = ? where periodo = ? and tercero = ? and estado = 'Pendiente'");
                update.setParameter(1,"P"+conse);
                update.setParameter(2,periodo);
                update.setParameter(3,listTemp.get(i));
                update.executeUpdate();
            }
        }
        toInsert.clear();
        return lista;
    }

    public List<String[]> getAllData1(String periodo){
        Query consulta = entityManager.createNativeQuery("select estado,count(estado) from nexco_facturas_cc group by estado");
        return consulta.getResultList();
    }
    public List<String[]> getAllData3(String periodo){
        Query consulta = entityManager.createNativeQuery("select case when pago = 1 then 'Pagado' when pago = 0 then 'Pendiente' end as estado,count(pago) as conteo from nexco_facturas_cc where pago is not null group by pago");
        return consulta.getResultList();
    }

    public List<InvoicesCc> getInvoicePending(){
        Query consulta = entityManager.createNativeQuery("select * from nexco_facturas_cc ",InvoicesCc.class);
        return consulta.getResultList();
    }

    public boolean validateThird(String nit){
        Query consulta = entityManager.createNativeQuery("select * from nexco_terceros_cc where nit = ?");
        consulta.setParameter(1,nit);
        if(consulta.getResultList().isEmpty())
            return true;
        else
            return false;
    }

    public boolean validateConcept(String concepto, String evento){
        if(evento.equals("CAUSACIÓN")) {
            Query consulta = entityManager.createNativeQuery("select d.impuesto from nexco_cuentas_cc d where d.concepto = ? and d.evento = ? \n" +
                    "and d.impuesto IN ('SIN IMPUESTO','IVA','RETEFUENTE','CUENTA POR COBRAR')");
            consulta.setParameter(1, concepto);
            consulta.setParameter(2, evento);

            if (consulta.getResultList().size() < 2)
                return true;
            else
                return false;
        }
        else
        {
            Query consulta = entityManager.createNativeQuery("select d.impuesto from nexco_cuentas_cc d where d.concepto = ? and d.evento = ? \n" +
                    "and d.impuesto IN ('PAGO','CUENTA POR COBRAR')");
            consulta.setParameter(1, concepto);
            consulta.setParameter(2, evento);

            if (consulta.getResultList().size() != 2)
                return true;
            else
                return false;
        }
    }

    public String sequenceMax(){
        Query consulta = entityManager.createNativeQuery("select top 1 lote from nexco_facturas_cc where estado = 'Completado' order by lote desc");
        List<String> list = consulta.getResultList();
        if(list.isEmpty())
            return "0";
        else
            return list.get(0).split("-")[0];
    }

    public boolean procesarData(String periodo, int consecutivo, String firma){

        Query consulta = entityManager.createNativeQuery("select lote from nexco_facturas_cc where estado = 'Pendiente' and periodo = ? group by lote");
        consulta.setParameter(1,periodo);
        List<String> listFinal=consulta.getResultList();
        if(!listFinal.isEmpty())
        {
            String[] fechaPartida= periodo.split("-");
            Query cFirma = entityManager.createNativeQuery("select * from nexco_firmas where id_firma = ?", Signature.class);
            cFirma.setParameter(1,Long.parseLong(firma));
            Signature tempFirma = (Signature) cFirma.getResultList().get(0);

            Query update1 = entityManager.createNativeQuery("update nexco_facturas_cc set firma = ? where estado = 'Pendiente' and periodo = ?");
            update1.setParameter(1,Long.parseLong(firma));
            update1.setParameter(2,periodo);
            update1.executeUpdate();

            for (String parte: listFinal)
            {
                while(validateLote(consecutivo+"-"+fechaPartida[0]))
                {
                    consecutivo++;
                }
                String nuevoLote =consecutivo+"-"+fechaPartida[0];
                Query update2 = entityManager.createNativeQuery("update nexco_facturas_cc set lote = ? where estado = 'Pendiente' and periodo = ? and lote = ?");
                update2.setParameter(1,nuevoLote);
                update2.setParameter(2,periodo);
                update2.setParameter(3,parte);
                update2.executeUpdate();
                XWPFDocument data = generateFactura(nuevoLote,periodo);
                String htmlFinal = convertVarbinaryToHTML(data);

                Query cTercero = entityManager.createNativeQuery("select concat(correo,concat(';',concat(correo_alterno,concat(';',correo_alterno2)))) as correos, concat(correo_copia1,concat(';',correo_copia2)) as copias from (select * from nexco_facturas_cc where lote=?) a\n" +
                        "left join nexco_terceros_cc b on a.tercero=b.nit");
                cTercero.setParameter(1,nuevoLote);
                List<Object[]> tempThird = cTercero.getResultList();
                sendEmailInvoice(tempThird.get(0)[0].toString(),tempFirma.getCorreo()+";"+tempThird.get(0)[1].toString(),nuevoLote,htmlFinal);
                consecutivo++;

            }

            Query update = entityManager.createNativeQuery("update nexco_facturas_cc set estado = 'Completado', pago = ? where estado = 'Pendiente' and periodo = ?");
            update.setParameter(1,false);
            update.setParameter(2,periodo);
            update.executeUpdate();

            return true;
        }
        else
        {
            return false;
        }
    }

    public String convertVarbinaryToHTML(XWPFDocument document){
        try {

            String respuesta="";
            boolean process=true;
            boolean processImage=true;

            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                respuesta+="<br/>"+text;
                if(respuesta.contains("IVA.")&& process)
                {
                    respuesta+="<br/>";
                    for (XWPFTable table : document.getTables()) {
                        // Comenzar la tabla HTML
                        respuesta+="<table style=\"border-collapse: collapse;\">";

                        // Iterar a través de las filas de la tabla
                        for (XWPFTableRow row : table.getRows()) {
                            respuesta+="<tr>";

                            // Iterar a través de las celdas de la fila
                            for (XWPFTableCell cell : row.getTableCells()) {
                                // Obtener el contenido de la celda
                                String cellText = cell.getText();

                                // Crear una celda HTML y agregar el contenido
                                respuesta+="<td style=\"border: 1px solid black; padding:8px;\">" + cellText + "</td>";
                            }

                            // Cerrar la fila HTML
                            respuesta+="</tr>";
                        }

                        // Cerrar la tabla HTML
                        respuesta+="</table>";
                    }
                    process=false;
                }
                if(respuesta.contains("Cordialmente,")&& processImage)
                {
                    respuesta+="<br/>";
                    for (XWPFPictureData picture : document.getAllPictures()) {
                        String contentType = picture.getPackagePart().getContentType();
                        String imageFormat = getImageFormatFromContentType(contentType);

                        // Generar una etiqueta HTML <img> para cada imagen
                        String imageHtml = "<img src=\"data:" + contentType + ";base64," + picture.getData() + "\" alt=\"Image\" />";

                        // Escribir la etiqueta HTML de la imagen en el archivo HTML
                        respuesta+=imageHtml;
                    }
                    processImage=false;
                }
            }

            return respuesta;

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getImageFormatFromContentType(String contentType) {
        // Determinar el formato de imagen a partir del tipo de contenido
        if (contentType.contains("jpeg")) {
            return "jpeg";
        } else if (contentType.contains("png")) {
            return "png";
        } else if (contentType.contains("gif")) {
            return "gif";
        } else {
            return "jpg"; // Valor predeterminado para otros formatos
        }
    }


    public void anularFactura(Long id){
        Query consulta = entityManager.createNativeQuery("update nexco_facturas_cc set estado = 'Anulado', lote = 'XX' where id_factura = ?");
        consulta.setParameter(1,id);
        consulta.executeUpdate();
    }

    public List<InvoicesCc> getLote(String lote,String periodo){
        Query consulta = entityManager.createNativeQuery("select * from nexco_facturas_cc where lote = ? and periodo = ?",InvoicesCc.class);
        consulta.setParameter(1,lote);
        consulta.setParameter(2,periodo);
        return consulta.getResultList();
    }

    public boolean validateLote(String lote){
        Query consulta = entityManager.createNativeQuery("select * from nexco_facturas_cc where lote = ?");
        consulta.setParameter(1,lote);
        if(!consulta.getResultList().isEmpty()){
            return true;
        }
        else
        {
            return false;
        }
    }

    public ThirdsCc getImpuesto(Long id){
        Query consulta = entityManager.createNativeQuery("select distinct b.* from nexco_facturas_cc a, nexco_terceros_cc b where a.tercero = b.nit AND a.id_factura = ? ",ThirdsCc.class);
        consulta.setParameter(1,id);
        return (ThirdsCc) consulta.getResultList().get(0);
    }

    public List<InvoicesCc> getAllData2(String periodo){
        Query consulta = entityManager.createNativeQuery("select * from nexco_facturas_cc where periodo = ? order by tercero,lote,estado",InvoicesCc.class);
        consulta.setParameter(1,periodo);
        return consulta.getResultList();
    }

    public void downloadFactura(Long id, HttpServletResponse response)
    {
        try {
            InvoicesCc invoicesCc = invoicesCcRepository.findByIdFactura(id);
            ThirdsCc thirdsCC = thirdsCcService.findByNit(invoicesCc.getTercero());
            List<InvoicesCc> listLotes = getLote(invoicesCc.getLote(), invoicesCc.getPeriodo());
            String plantillaPath = "C:/Users/CE66916/Documents/BBVA Intergrupo/Plantillas/FORMATO_CUENTA_DE_COBRO.docx";
            Double valorTotalBase =0.0;
            //Double valorRetefuente =0.0;
            Double valorIva =0.0;
            String txtConcept="";
            String txtImpuestos = "Sin IVA";

            FileInputStream plantillaInputStream = new FileInputStream(new File(plantillaPath));
            XWPFDocument doc = new XWPFDocument(plantillaInputStream);
            plantillaInputStream.close();

            XWPFTable table = doc.getTables().get(0);
            CTTblPr tblPr = table.getCTTbl().getTblPr();
            CTTblBorders bordes=tblPr.addNewTblBorders();
            bordes.addNewInsideH().setVal(STBorder.SINGLE);
            bordes.addNewInsideV().setVal(STBorder.SINGLE);
            bordes.addNewRight().setVal(STBorder.SINGLE);
            bordes.addNewLeft().setVal(STBorder.SINGLE);
            bordes.addNewTop().setVal(STBorder.SINGLE);
            bordes.addNewBottom().setVal(STBorder.SINGLE);

            for(int i =0; i<listLotes.size();i++)
            {
                ThirdsCc impuesto = getImpuesto(listLotes.get(i).getIdFactura());
                XWPFTableRow newRow = table.createRow();
                XWPFTableCell cell1 = newRow.getCell(0);
                cell1.setText(listLotes.get(i).getPersona());
                XWPFTableCell cell2 = newRow.getCell(1);
                cell2.setText(listLotes.get(i).getConcepto()+" - "+listLotes.get(i).getFecha());
                XWPFTableCell cell3 = newRow.getCell(2);

                if(impuesto.getImpuesto().equals("IVA"))
                {
                    valorIva += (listLotes.get(i).getValor()*19)/100;
                    valorTotalBase += (listLotes.get(i).getValor());
                    cell3.setText("$"+formatearValor(listLotes.get(i).getValor()));
                }
                else {
                    valorTotalBase += listLotes.get(i).getValor();
                    cell3.setText("$"+formatearValor(listLotes.get(i).getValor()));
                }

                if(!txtConcept.contains(listLotes.get(i).getConcepto()))
                {
                    if(txtConcept.length()==0) {
                        txtConcept = listLotes.get(i).getConcepto();
                    }
                    else {
                        txtConcept = txtConcept.replace("| ",", ");
                        txtConcept = txtConcept + "| " + listLotes.get(i).getConcepto();
                    }
                }

                if(impuesto.getImpuesto().equals("IVA"))
                {
                    txtImpuestos = "Con IVA";
                }
                if(i==listLotes.size()-1) {
                    txtConcept = txtConcept.replace("| "," y ");
                }
            }

            if(txtImpuestos.equals("Con IVA"))
            {
                XWPFTableRow newRow3 = table.createRow();
                XWPFTableCell cell10 = newRow3.getCell(0);
                cell10.setText("");
                XWPFTableCell cell12 = newRow3.getCell(1);
                XWPFRun xwp5 = cell12.addParagraph().createRun();
                xwp5.setBold(true);
                xwp5.setText("Sub Total");
                XWPFTableCell cell11 = newRow3.getCell(2);
                XWPFRun xwp4 = cell11.addParagraph().createRun();
                xwp4.setBold(true);
                xwp4.setText("$"+formatearValor(valorTotalBase));

                XWPFTableRow newRow2 = table.createRow();
                XWPFTableCell cell7 = newRow2.getCell(0);
                cell7.setText("");
                XWPFTableCell cell8 = newRow2.getCell(1);
                XWPFRun xwp2 = cell8.addParagraph().createRun();
                xwp2.setBold(false);
                xwp2.setText("IVA (19%)");
                XWPFTableCell cell9 = newRow2.getCell(2);
                XWPFRun xwp3 = cell9.addParagraph().createRun();
                xwp3.setBold(false);
                xwp3.setText("$" + formatearValor(valorIva));
            }

            XWPFTableRow newRow1 = table.createRow();
            XWPFTableCell cell4 = newRow1.getCell(0);
            cell4.setText("");
            XWPFTableCell cell5 = newRow1.getCell(1);
            XWPFRun xwp = cell5.addParagraph().createRun();
            xwp.setBold(true);
            xwp.setText("Total");
            XWPFTableCell cell6 = newRow1.getCell(2);
            XWPFRun xwp1 = cell6.addParagraph().createRun();
            xwp1.setBold(true);
            xwp1.setText("$"+formatearValor(valorTotalBase+valorIva));

            for (XWPFParagraph paragraph : doc.getParagraphs()) {
                for (XWPFRun run : paragraph.getRuns()) {
                    String texto = run.getText(0);
                    if (texto != null && texto.contains("PR1")) {
                        texto = texto.replace("PR1", invoicesCc.getLote().replace("P","0"));
                        run.setText(texto, 0);
                    }
                    if (texto != null && texto.contains("PR2")) {
                        texto = texto.replace("PR2", thirdsCC.getNombre());
                        run.setText(texto, 0);
                    }
                    if (texto != null && texto.contains("PR3")) {
                        texto = texto.replace("PR3", thirdsCC.getNit());
                        run.setText(texto, 0);
                    }
                    if (texto != null && texto.contains("PR4")) {
                        texto = texto.replace("PR4", txtConcept);
                        run.setText(texto, 0);
                    }
                    if (texto != null && texto.contains("PR5")) {
                        Date fechaActual = new Date();

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(fechaActual);

                        String[] nombresMeses = {
                                "enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto",
                                "setiembre", "octubre", "noviembre", "diciembre"
                        };

                        int dia = calendar.get(Calendar.DAY_OF_MONTH);
                        int mes = calendar.get(Calendar.MONTH);
                        int anio = calendar.get(Calendar.YEAR);

                        String fechaFormateada = dia + " de " + nombresMeses[mes] + " de " + anio;

                        texto = texto.replace("PR5", fechaFormateada);
                        run.setText(texto, 0);
                    }
                    if (texto != null && texto.contains("PR6")) {
                        texto = texto.replace("PR6", "$"+formatearValor(valorTotalBase+valorIva));
                        run.setText(texto, 0);
                    }
                    if (texto != null && texto.contains("PR7")) {
                        texto = texto.replace("PR7", txtImpuestos);
                        run.setText(texto, 0);
                    }
                    if(invoicesCc.getFirma() != null)
                    {
                        Signature signature = signatureService.findByIdFirma(listLotes.get(0).getFirma());
                        if (texto != null && texto.contains("PR8")) {
                            texto = texto.replace("PR8", signature.getNombre());
                            run.setText(texto, 0);
                        }
                        if (texto != null && texto.contains("PR9")) {
                            texto = texto.replace("PR9", signature.getCargo());
                            run.setText(texto, 0);
                        }
                        if (texto != null && texto.contains("P1") && signature.getFirma() != null) {
                            run.setText("", 0);
                            run.addPicture(new ByteArrayInputStream(signature.getFirma()),XWPFDocument.PICTURE_TYPE_JPEG,"image.jpg", Units.toEMU(200),Units.toEMU(100));
                        }
                    }
                    else
                    {
                        if (texto != null && texto.contains("PR8")) {
                            texto = texto.replace("PR8", "Nombre Persona Firma");
                            run.setText(texto, 0);
                        }
                        if (texto != null && texto.contains("PR9")) {
                            texto = texto.replace("PR9", "Cargo Persona Firma");
                            run.setText(texto, 0);
                        }
                        if (texto != null && texto.contains("P1")) {
                            run.setText("", 0);
                        }
                    }
                }
            }

            ServletOutputStream outputStream = response.getOutputStream();
            doc.write(outputStream);
            outputStream.close();

        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
        }
    }

    public XWPFDocument generateFactura(String lote,String periodo)
    {
        try {
            List<InvoicesCc> listLotes = getLote(lote, periodo);
            ThirdsCc thirdsCC = thirdsCcService.findByNit(listLotes.get(0).getTercero());
            Signature signature = signatureService.findByIdFirma(listLotes.get(0).getFirma());
            String plantillaPath = "C:/Users/CE66916/Documents/BBVA Intergrupo/Plantillas/FORMATO_CUENTA_DE_COBRO_FIRMA.docx";
            Double valorTotalBase =0.0;
            Double valorIva =0.0;
            String txtConcept="";
            String txtImpuestos = "Sin IVA";

            FileInputStream plantillaInputStream = new FileInputStream(new File(plantillaPath));
            XWPFDocument doc = new XWPFDocument(plantillaInputStream);
            plantillaInputStream.close();

            XWPFTable table = doc.getTables().get(0);
            CTTblPr tblPr = table.getCTTbl().getTblPr();
            CTTblBorders bordes=tblPr.addNewTblBorders();
            bordes.addNewInsideH().setVal(STBorder.SINGLE);
            bordes.addNewInsideV().setVal(STBorder.SINGLE);
            bordes.addNewRight().setVal(STBorder.SINGLE);
            bordes.addNewLeft().setVal(STBorder.SINGLE);
            bordes.addNewTop().setVal(STBorder.SINGLE);
            bordes.addNewBottom().setVal(STBorder.SINGLE);

            for(int i =0; i<listLotes.size();i++)
            {
                ThirdsCc impuesto = getImpuesto(listLotes.get(i).getIdFactura());
                XWPFTableRow newRow = table.createRow();
                XWPFTableCell cell1 = newRow.getCell(0);
                cell1.setText(listLotes.get(i).getPersona());
                XWPFTableCell cell2 = newRow.getCell(1);
                cell2.setText(listLotes.get(i).getConcepto()+" - "+listLotes.get(i).getFecha());
                XWPFTableCell cell3 = newRow.getCell(2);

                if(impuesto.getImpuesto().equals("IVA"))
                {
                    valorIva += (listLotes.get(i).getValor()*19)/100;
                    valorTotalBase += (listLotes.get(i).getValor());
                    cell3.setText("$"+formatearValor(listLotes.get(i).getValor()));
                }
                else {
                    valorTotalBase += listLotes.get(i).getValor();
                    cell3.setText("$"+formatearValor(listLotes.get(i).getValor()));
                }

                if(!txtConcept.contains(listLotes.get(i).getConcepto()))
                {
                    if(txtConcept.length()==0) {
                        txtConcept = listLotes.get(i).getConcepto();
                    }
                    else {
                        txtConcept = txtConcept.replace("| ",", ");
                        txtConcept = txtConcept + "| " + listLotes.get(i).getConcepto();
                    }
                }

                if(impuesto.getImpuesto().equals("IVA"))
                {
                    txtImpuestos = "Con IVA";
                }
                if(i==listLotes.size()-1) {
                    txtConcept = txtConcept.replace("| "," y ");
                }
            }

            if(txtImpuestos.equals("Con IVA"))
            {
                XWPFTableRow newRow3 = table.createRow();
                XWPFTableCell cell10 = newRow3.getCell(0);
                cell10.setText("");
                XWPFTableCell cell12 = newRow3.getCell(1);
                XWPFRun xwp5 = cell12.addParagraph().createRun();
                xwp5.setBold(true);
                xwp5.setText("Sub Total");
                XWPFTableCell cell11 = newRow3.getCell(2);
                XWPFRun xwp4 = cell11.addParagraph().createRun();
                xwp4.setBold(true);
                xwp4.setText("$"+formatearValor(valorTotalBase));

                XWPFTableRow newRow2 = table.createRow();
                XWPFTableCell cell7 = newRow2.getCell(0);
                cell7.setText("");
                XWPFTableCell cell8 = newRow2.getCell(1);
                XWPFRun xwp2 = cell8.addParagraph().createRun();
                xwp2.setBold(false);
                xwp2.setText("IVA (19%)");
                XWPFTableCell cell9 = newRow2.getCell(2);
                XWPFRun xwp3 = cell9.addParagraph().createRun();
                xwp3.setBold(false);
                xwp3.setText("$" + formatearValor(valorIva));
            }

            XWPFTableRow newRow1 = table.createRow();
            XWPFTableCell cell4 = newRow1.getCell(0);
            cell4.setText("");
            XWPFTableCell cell5 = newRow1.getCell(1);
            XWPFRun xwp = cell5.addParagraph().createRun();
            xwp.setBold(true);
            xwp.setText("Total");
            XWPFTableCell cell6 = newRow1.getCell(2);
            XWPFRun xwp1 = cell6.addParagraph().createRun();
            xwp1.setBold(true);
            xwp1.setText("$"+formatearValor(valorTotalBase+valorIva));

            for (XWPFParagraph paragraph : doc.getParagraphs()) {
                for (XWPFRun run : paragraph.getRuns()) {
                    String texto = run.getText(0);
                    if (texto != null && texto.contains("PR1")) {
                        texto = texto.replace("PR1", lote);
                        run.setText(texto, 0);
                    }
                    if (texto != null && texto.contains("PR2")) {
                        texto = texto.replace("PR2", thirdsCC.getNombre());
                        run.setText(texto, 0);
                    }
                    if (texto != null && texto.contains("PR3")) {
                        texto = texto.replace("PR3", thirdsCC.getNit());
                        run.setText(texto, 0);
                    }
                    if (texto != null && texto.contains("PR4")) {
                        texto = texto.replace("PR4", txtConcept);
                        run.setText(texto, 0);
                    }
                    if (texto != null && texto.contains("PR5")) {
                        Date fechaActual = new Date();

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(fechaActual);

                        String[] nombresMeses = {
                                "enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto",
                                "setiembre", "octubre", "noviembre", "diciembre"
                        };

                        int dia = calendar.get(Calendar.DAY_OF_MONTH);
                        int mes = calendar.get(Calendar.MONTH);
                        int anio = calendar.get(Calendar.YEAR);

                        String fechaFormateada = dia + " de " + nombresMeses[mes] + " de " + anio;

                        texto = texto.replace("PR5", fechaFormateada);
                        run.setText(texto, 0);
                    }
                    if (texto != null && texto.contains("PR6")) {
                        texto = texto.replace("PR6", "$"+formatearValor(valorTotalBase+valorIva));
                        run.setText(texto, 0);
                    }
                    if (texto != null && texto.contains("PR7")) {
                        texto = texto.replace("PR7", txtImpuestos);
                        run.setText(texto, 0);
                    }
                    if (texto != null && texto.contains("PR8")) {
                        texto = texto.replace("PR8", signature.getNombre());
                        run.setText(texto, 0);
                    }
                    if (texto != null && texto.contains("PR9")) {
                        texto = texto.replace("PR9", signature.getCargo());
                        run.setText(texto, 0);
                    }
                    if (texto != null && texto.contains("P1")) {
                        run.setText("", 0);
                        run.addPicture(new ByteArrayInputStream(signature.getFirma()),XWPFDocument.PICTURE_TYPE_JPEG,"image.jpg", Units.toEMU(200),Units.toEMU(100));
                    }
                }
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            doc.write(bos);
            for(int i =0; i<listLotes.size();i++) {
                InvoicesCc invoicesCc = listLotes.get(i);
                invoicesCc.setDocx(bos.toByteArray());
                entityManager.persist(invoicesCc);
            }
            return doc;

        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String formatearFecha(Date fecha, String formato)
    {
        SimpleDateFormat simpleDateFormat= new SimpleDateFormat(formato);
        return simpleDateFormat.format(fecha);
    }

    public String formatearValor(Double valor)
    {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        numberFormat.setGroupingUsed(true);
        return numberFormat.format(valor);
    }

    public void fucionarCeldas(XWPFTableCell celda1, XWPFTableCell celda2)
    {
        celda1.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
        celda2.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
    }

    public boolean validarCargar(String periodo)
    {
        LocalDate fechaHoy = LocalDate.now();
        LocalDate fechaCadena = LocalDate.parse(periodo,DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        if(fechaCadena.equals(fechaHoy))
            return true;
        else
            return false;
    }

    public void sendEmailInvoice(String recipientEmail,String recipientCopyEmail,String lote,String data) {
        String subject = "Notificaciones Nexco Consolidación";

        String content = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title></title>\n" +
                "    <style>\n" +
                "        \n" +
                "        body {\n" +
                "            font-family: Arial, sans-serif;\n" +
                "            background-color: #f0f0f0;\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "\n" +
                "        \n" +
                "        header {\n" +
                "            background-color: #004481;\n" +
                "            color: white;\n" +
                "            padding: 20px;\n" +
                "        }\n" +
                "\n" +
                "        \n" +
                "        .content {\n" +
                "            background-color: white;\n" +
                "            border-radius: 10px;\n" +
                "            box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);\n" +
                "            margin: 20px;\n" +
                "            padding: 20px;\n" +
                "        }\n" +
                "\n" +
                "        \n" +
                "        .btn {\n" +
                "            display: inline-block;\n" +
                "            background-color: #004481;\n" +
                "            color: white;\n" +
                "            padding: 10px 20px;\n" +
                "            text-decoration: none;\n" +
                "            border-radius: 5px;\n" +
                "        }\n" +
                "\n" +
                "        footer {\n" +
                "            background-color: #004481;\n" +
                "            color: white;\n" +
                "            padding: 10px;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <header>\n" +
                "        <h1>¡Confirmación Envío Cuenta de Cobro!</h1>\n" +
                "    </header>\n" +
                "    <div class=\"content\">\n" +
                "        <h2>¡Se envía la Cuenta de Cobro "+lote+"!</h2>\n" +
                data+
                "    </div>\n" +
                "    <footer>\n" +
                "        Nexco Consolidación.\n" +
                "    </footer>\n" +
                "</body>\n" +
                "</html>";;

        sendEmailService.sendEmailCopAd(recipientEmail,recipientCopyEmail, subject, content);
    }

    public List<Object[]> generateMassiveCharge(String periodo) throws ParseException {

        Query temporal = entityManager.createNativeQuery("drop table nexco_carga_masiva_cc_temp;\n" +
                "\n" +
                "select z.* into nexco_carga_masiva_cc_temp from\n" +
                "(select c.centro,c.cuenta, 'COP' as divisa, '' as contr, a.fecha as ref, case when c.naturaleza ='H' then abs(a.valor)*-1 else abs(a.valor) end as valor,CONCAT(a.concepto,CONCAT(' ',a.periodo)) as descr, replace(a.periodo,'-','') as fechas,SUBSTRING(a.tercero,1,1) as td, SUBSTRING(a.tercero,2,len(a.tercero)-3) as numero ,SUBSTRING(a.tercero,len(a.tercero),len(a.tercero)) as dv,\n" +
                "'' as tipo_perdida,'' as clase_riesgo,'' as tipo_mov,'' as produc,'' as proces,'' as linea_ope, 0 as tbas, a.concepto,a.periodo,a.tercero,a.fecha\n" +
                "from (select e.* from nexco_facturas_cc e where e.periodo = :periodo and e.carga_masiva is null) as a\n" +
                "left join (select f.impuesto,f.nit from nexco_terceros_cc f) as b on a.tercero = b.nit\n" +
                "left join (select d.concepto,d.centro,d.cuenta,d.naturaleza from nexco_cuentas_cc d where d.evento = 'CAUSACIÓN' and d.impuesto='SIN IMPUESTO') as c on a.concepto = c.concepto \n" +
                "union all\n" +
                "select c.centro,c.cuenta, 'COP' as divisa, '' as contr, a.fecha as ref, case when c.naturaleza ='H' then abs(a.valor*0.11)*-1 else abs(a.valor*0.11) end as valor,CONCAT(a.concepto,CONCAT(' ',a.periodo)) as descr, replace(a.periodo,'-','') as fechas,SUBSTRING(a.tercero,1,1) as td, SUBSTRING(a.tercero,2,len(a.tercero)-3) as numero ,SUBSTRING(a.tercero,len(a.tercero),len(a.tercero)) as dv,\n" +
                "'' as tipo_perdida,'' as clase_riesgo,'' as tipo_mov,'' as produc,'' as proces,'' as linea_ope, a.valor as tbas, a.concepto,a.periodo,a.tercero,a.fecha\n" +
                "from (select e.* from nexco_facturas_cc e where e.periodo = :periodo and e.carga_masiva is null) as a\n" +
                "left join (select f.impuesto,f.nit from nexco_terceros_cc f) as b on a.tercero = b.nit\n" +
                "left join (select d.concepto,d.centro,d.cuenta,d.naturaleza from nexco_cuentas_cc d where d.evento = 'CAUSACIÓN' and d.impuesto='RETEFUENTE') as c on a.concepto = c.concepto\n" +
                "where b.impuesto = 'RETEFUENTE'\n" +
                "union all\n" +
                "select c.centro,c.cuenta, 'COP' as divisa, '' as contr, a.fecha as ref, case when c.naturaleza ='H' then abs(a.valor*0.19)*-1 else abs(a.valor*0.19) end as valor,CONCAT(a.concepto,CONCAT(' ',a.periodo)) as descr, replace(a.periodo,'-','') as fechas,SUBSTRING(a.tercero,1,1) as td, SUBSTRING(a.tercero,2,len(a.tercero)-3) as numero ,SUBSTRING(a.tercero,len(a.tercero),len(a.tercero)) as dv,\n" +
                "'' as tipo_perdida,'' as clase_riesgo,'' as tipo_mov,'' as produc,'' as proces,'' as linea_ope, a.valor as tbas, a.concepto,a.periodo,a.tercero,a.fecha\n" +
                "from (select e.* from nexco_facturas_cc e where e.periodo = :periodo and e.carga_masiva is null) as a\n" +
                "left join (select f.impuesto,f.nit from nexco_terceros_cc f) as b on a.tercero = b.nit\n" +
                "left join (select d.concepto,d.centro,d.cuenta,d.naturaleza from nexco_cuentas_cc d where d.evento = 'CAUSACIÓN' and d.impuesto='IVA') as c on a.concepto = c.concepto\n" +
                "where b.impuesto = 'IVA') z;" +
                "\n" +
                "update nexco_facturas_cc set carga_masiva = 'X'  where periodo = :periodo and carga_masiva is null;");
        temporal.setParameter("periodo", periodo);
        temporal.executeUpdate();

        Query consulta = entityManager.createNativeQuery("select z.centro,z.cuenta,z.divisa,z.contr,z.ref,sum(z.valor),z.descr,z.fechas,z.td,z.numero,z.dv,z.tipo_perdida,z.clase_riesgo,z.tipo_mov,z.produc,z.proces,z.linea_ope,z.tbas \n" +
                "from nexco_carga_masiva_cc_temp as z group by z.centro,z.cuenta,z.divisa,z.contr,z.ref,z.descr,z.fechas,z.td,z.numero,z.dv,z.tipo_perdida,z.clase_riesgo,z.tipo_mov,z.produc,z.proces,z.linea_ope,z.tbas " +
                "union all\n" +
                "select c.centro,c.cuenta, 'COP' as divisa, '' as contr, a.fecha as ref, a.valor*-1 as valor, CONCAT(a.concepto,CONCAT(' ',a.periodo)) as descr, replace(a.periodo,'-','') as fechas,SUBSTRING(a.tercero,1,1) as td, SUBSTRING(a.tercero,2,len(a.tercero)-3) as numero ,SUBSTRING(a.tercero,len(a.tercero),len(a.tercero)) as dv,\n" +
                "'' as tipo_perdida,'' as clase_riesgo,'' as tipo_mov,'' as produc,'' as proces,'' as linea_ope, 0 as tbas\n" +
                "from (select concepto,periodo,tercero,fecha,sum(valor) as valor from nexco_carga_masiva_cc_temp group by concepto,periodo,tercero,fecha) as a\n" +
                "left join (select d.concepto,d.centro,d.cuenta,d.naturaleza from nexco_cuentas_cc d where d.evento = 'CAUSACIÓN' and d.impuesto='CUENTA POR COBRAR') as c on a.concepto = c.concepto ");
        return consulta.getResultList();
    }

    public List<Object[]> generateMassiveCharge(String periodo,String[] opcionesLista) throws ParseException {

        List<String> useList = Arrays.asList(opcionesLista);
        Query consulta = entityManager.createNativeQuery("select z.centro,z.cuenta,z.divisa,z.contr,z.ref,sum(z.valor),z.descr,z.fechas,z.td,z.numero,z.dv,z.tipo_perdida,z.clase_riesgo,z.tipo_mov,z.produc,z.proces,z.linea_ope,z.tbas from " +
                "(select b.impuesto,c.centro,c.cuenta, 'COP' as divisa, '' as contr, a.fecha as ref, case when c.naturaleza ='H' then abs(case when b.impuesto='RETEFUENTE' then a.valor-(a.valor*0.11) when b.impuesto='IVA' then a.valor-(a.valor*0.19) else a.valor end)*-1 else abs(case when b.impuesto='RETEFUENTE' then a.valor-(a.valor*0.11) when b.impuesto='IVA' then a.valor-(a.valor*0.19) else a.valor end) end as valor,CONCAT(a.concepto,CONCAT(' ',a.periodo)) as descr, replace(a.periodo,'-','') as fechas,SUBSTRING(a.tercero,1,1) as td, SUBSTRING(a.tercero,2,len(a.tercero)-3) as numero ,SUBSTRING(a.tercero,len(a.tercero),len(a.tercero)) as dv,\n" +
                "'' as tipo_perdida,'' as clase_riesgo,'' as tipo_mov,'' as produc,'' as proces,'' as linea_ope, 0 as tbas\n" +
                "from (select e.* from nexco_facturas_cc e where e.periodo = :periodo and e.id_factura in (:valores) and estado = 'Completado' and pago = :pago) as a\n" +
                "left join (select f.impuesto,f.nit from nexco_terceros_cc f) as b on a.tercero = b.nit\n" +
                "left join (select d.impuesto,d.concepto,d.centro,d.cuenta,d.naturaleza from nexco_cuentas_cc d where d.evento = 'PAGO' and d.impuesto='CUENTA POR COBRAR') as c on a.concepto = c.concepto\n" +
                "union all\n" +
                "select b.impuesto,c.centro,c.cuenta, 'COP' as divisa, '' as contr, a.fecha as ref, case when c.naturaleza ='H' then abs(case when b.impuesto='RETEFUENTE' then a.valor-(a.valor*0.11) when b.impuesto='IVA' then a.valor-(a.valor*0.19) else a.valor end)*-1 else abs(case when b.impuesto='RETEFUENTE' then a.valor-(a.valor*0.11) when b.impuesto='IVA' then a.valor-(a.valor*0.19) else a.valor end) end as valor,CONCAT(a.concepto,CONCAT(' ',a.periodo)) as descr, replace(a.periodo,'-','') as fechas,SUBSTRING(a.tercero,1,1) as td, SUBSTRING(a.tercero,2,len(a.tercero)-3) as numero ,SUBSTRING(a.tercero,len(a.tercero),len(a.tercero)) as dv,\n" +
                "'' as tipo_perdida,'' as clase_riesgo,'' as tipo_mov,'' as produc,'' as proces,'' as linea_ope, 0 as tbas\n" +
                "from (select e.* from nexco_facturas_cc e where e.periodo = :periodo and e.id_factura in (:valores) and estado = 'Completado' and pago = :pago) as a\n" +
                "left join (select f.impuesto,f.nit from nexco_terceros_cc f) as b on a.tercero = b.nit\n" +
                "left join (select d.impuesto,d.concepto,d.centro,d.cuenta,d.naturaleza from nexco_cuentas_cc d where d.evento = 'PAGO' and d.impuesto='PAGO') as c on a.concepto = c.concepto) z " +
                "group by z.centro,z.cuenta,z.divisa,z.contr,z.ref,z.descr,z.fechas,z.td,z.numero,z.dv,z.tipo_perdida,z.clase_riesgo,z.tipo_mov,z.produc,z.proces,z.linea_ope,z.tbas ");
        consulta.setParameter("periodo",periodo);
        consulta.setParameter("valores",useList);
        consulta.setParameter("pago",false);
        List<Object[]>  result = consulta.getResultList();

        Query update = entityManager.createNativeQuery("update nexco_facturas_cc set pago = :pago where id_factura in (:valores) ");
        update.setParameter("valores",useList);
        update.setParameter("pago",true);
        update.executeUpdate();

        return result;
    }
}
