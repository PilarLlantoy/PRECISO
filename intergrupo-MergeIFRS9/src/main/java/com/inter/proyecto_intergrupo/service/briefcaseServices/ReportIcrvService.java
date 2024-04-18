package com.inter.proyecto_intergrupo.service.briefcaseServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.briefcase.CalculoIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.PlantillaCalculoIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.PlantillaReportIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.ReportIcrv;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.CalculoIcrvRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.PlantillaCalculoIcrvRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.PlantillaReportIcrvRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.ReportIcrvRepository;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
@Transactional
public class ReportIcrvService {

    @Autowired
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private ReportIcrvRepository reportIcrvRepository;

    @Autowired
    private PlantillaReportIcrvRepository plantillaReportIcrvRepository;

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Portafolio");
        insert.setFecha(today);
        insert.setInput("Reporte ICRV");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public ReportIcrv findByIdReport(Long id){
        return reportIcrvRepository.findByIdReport(id);
    }

    public ReportIcrv modifyReport(ReportIcrv toModify, User user)
    {
        loadAudit(user,"Modificación Exitosa Registro ("+toModify.getIdReport()+") Reporte ICRV");
        return reportIcrvRepository.save(toModify);
    }
    public List<ReportIcrv> findAllReport(String periodo)
    {
        return reportIcrvRepository.findByPeriodo(periodo);
    }

    public void completeTable(String periodo) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate firstDayOfMonth = LocalDate.parse(periodo + "-01", formatter);
        LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());
        String ultimoDiaDelMes = lastDayOfMonth.format(formatter);

        Query query = entityManager.createNativeQuery("delete from nexco_report_icrv where periodo = :periodo ;\n" +
                "insert into nexco_report_icrv (entidad,cod_periodo,cod_sociinfo,xti_cartera,cod_socipart,signo_valor_contable,signo_microcobertura,periodo)\n" +
                "(select entidad,cod_periodo,cod_sociinfo,xti_cartera,cod_socipart,signo_valor_contable,signo_microcobertura, :periodo as periodo from nexco_plantilla_report_icrv);");
        query.setParameter("periodo",periodo);
        query.executeUpdate();

        updateTable(periodo);
    }

    public void updateTable(String periodo) {

        Query validate = entityManager.createNativeQuery("update a set a.cod_isin=b.isin,a.coste_valor=round(isnull(b.saldo_libros_valoracion,0)/1000,0) from (select * from nexco_report_icrv where periodo = :periodo ) a, (select * from nexco_calculo_icrv where periodo = :periodo ) b where a.entidad=b.empresa;\n" +
                "update a set a.ajuste_valor_razonable=round(isnull(b.saldo_libros_valoracion,0)/1000,0)-isnull(a.coste_valor,0) from (select * from nexco_report_icrv where periodo = :periodo ) a, (select * from nexco_calculo_icrv where periodo = :periodo ) b where a.entidad=b.empresa;\n" +
                "update a set a.desembolso_pdte=isnull(a.coste_valor,0)+isnull(a.ajuste_valor_razonable,0)-isnull(a.correcciones_por_deterioro,0) from (select * from nexco_report_icrv where periodo = :periodo ) a, (select * from nexco_calculo_icrv where periodo = :periodo ) b where a.entidad=b.empresa;" +
                "");
        validate.setParameter("periodo",periodo);
        validate.executeUpdate();
    }

    public void clearReport(User user, String periodo){
        completeTable(periodo);
        loadAudit(user,"Generación Exitosa de Reporte para "+periodo);
    }

    public ArrayList<String[]> saveFileBDPlantilla(InputStream file, User user, String periodo) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<>();
        if (file!=null)
        {
            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            list=validarPlantilla(rows, periodo);
            if(list.get(0)[2].equals("SUCCESS"))
                loadAudit(user,"Cargue Exitoso Plantilla Reporte ICRV");
            else
                loadAudit(user,"Cargue Fallido Plantilla Reporte ICRV");
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows, String periodo) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<ReportIcrv> toInsert = new ArrayList<>();
        ArrayList<PlantillaReportIcrv> toInsertPre = new ArrayList<>();
        String stateFinal = "SUCCESS";
        XSSFRow row;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    DataFormatter formatter = new DataFormatter();
                    int consecutive = 0;
                    String cellEntidad = formatter.formatCellValue(row.getCell(consecutive++)).trim();
                    String cellCodPeriodo = formatter.formatCellValue(row.getCell(consecutive++)).trim();
                    String cellCodSociinfo = formatter.formatCellValue(row.getCell(consecutive++)).trim();
                    String cellXtiCartera = formatter.formatCellValue(row.getCell(consecutive++)).trim();
                    String cellCodSocipart = formatter.formatCellValue(row.getCell(consecutive++)).trim();
                    String cellCodIsin = formatter.formatCellValue(row.getCell(consecutive++)).trim();

                    XSSFCell cell0= row.getCell(consecutive++);
                    if(cell0!=null)
                        cell0.setCellType(CellType.STRING);
                    String cellCosteValor = formatter.formatCellValue(cell0).replace(" ", "").replace(",","");

                    XSSFCell cell1= row.getCell(consecutive++);
                    if(cell1!=null)
                        cell1.setCellType(CellType.STRING);
                    String cellAjusteValorRazonable = formatter.formatCellValue(cell1).replace(" ", "").replace(",","");

                    XSSFCell cell2= row.getCell(consecutive++);
                    if(cell2!=null)
                        cell2.setCellType(CellType.STRING);
                    String cellMicrocoberturas = formatter.formatCellValue(cell2).replace(" ", "").replace(",","");

                    XSSFCell cell3= row.getCell(consecutive++);
                    if(cell3!=null)
                        cell3.setCellType(CellType.STRING);
                    String cellCorreccionesPorDeterioro = formatter.formatCellValue(cell3).replace(" ", "").replace(",","");

                    XSSFCell cell4= row.getCell(consecutive++);
                    if(cell4!=null)
                        cell4.setCellType(CellType.STRING);
                    String cellValorCotizado = formatter.formatCellValue(cell4).replace(" ", "").replace(",","");

                    XSSFCell cell5= row.getCell(consecutive++);
                    if(cell5!=null)
                        cell5.setCellType(CellType.STRING);
                    String cellDesembolsoPdte = formatter.formatCellValue(cell5).replace(" ", "").replace(",","");

                    XSSFCell cell6= row.getCell(consecutive++);
                    if(cell6!=null)
                        cell6.setCellType(CellType.STRING);
                    String cellNumTitulos = formatter.formatCellValue(cell6).replace(" ", "").replace(",","");

                    XSSFCell cell7= row.getCell(consecutive++);
                    if(cell7!=null)
                        cell7.setCellType(CellType.STRING);
                    String cellCapitalSocial = formatter.formatCellValue(cell7).replace(" ", "").replace(",","");

                    XSSFCell cell8= row.getCell(consecutive++);
                    if(cell8!=null)
                        cell8.setCellType(CellType.STRING);
                    String cellCosteAdquisicion = formatter.formatCellValue(cell8).replace(" ", "");

                    String cellSignoValorContable = formatter.formatCellValue(row.getCell(consecutive++)).trim();
                    String cellSignoMicrocobertura = formatter.formatCellValue(row.getCell(consecutive++)).trim();

                    if (cellEntidad.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "El campo Entidad no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellCodSociinfo.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "El campo CodSociinfo no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellXtiCartera.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(3);
                        log[2] = "El campo XtiCartera no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellCodSocipart.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(4);
                        log[2] = "El campo CodSocipart no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellMicrocoberturas.length()!=0) {
                        try{
                            Double.parseDouble(cellMicrocoberturas);
                        }
                        catch (Exception e)
                        {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(8);
                            log[2] = "El campo Microcoberturas debe ser numérico.";
                            lista.add(log);
                        }
                    }
                    else {
                        cellMicrocoberturas="0";
                    }
                    if (cellCorreccionesPorDeterioro.length()!=0) {
                        try{
                            Double.parseDouble(cellCorreccionesPorDeterioro);
                        }
                        catch (Exception e)
                        {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(9);
                            log[2] = "El campo Correcciones Por Deterioro debe ser numérico.";
                            lista.add(log);
                        }
                    }
                    else {
                        cellCorreccionesPorDeterioro="0";
                    }
                    if (cellValorCotizado.length()!=0) {
                        try{
                            Double.parseDouble(cellValorCotizado);
                        }
                        catch (Exception e)
                        {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(10);
                            log[2] = "El campo Valor Cotizado debe ser numérico.";
                            lista.add(log);
                        }
                    }
                    else {
                        cellValorCotizado="0";
                    }
                    if (cellCosteAdquisicion.length()!=0) {
                        try{
                            Double.parseDouble(cellCosteAdquisicion);
                        }
                        catch (Exception e)
                        {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(11);
                            log[2] = "El campo Coste Adquisicion debe ser numérico.";
                            lista.add(log);
                        }
                    }
                    else {
                        cellCosteAdquisicion="0";
                    }
                    if (!cellSignoValorContable.equals("+") && !cellSignoValorContable.equals("-")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(24);
                        log[2] = "El campo Signo Valor Contable debe ser + o -.";
                        lista.add(log);
                    }
                    if (!cellSignoMicrocobertura.equals("+") && !cellSignoMicrocobertura.equals("-")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(25);
                        log[2] = "El campo Signo Valor Contable debe ser + o -.";
                        lista.add(log);
                    }

                    if(lista.size() == 0) {
                        PlantillaReportIcrv data0 = new PlantillaReportIcrv();
                        data0.setEntidad(cellEntidad);
                        data0.setCodPeriodo(periodo.replace("-",""));
                        data0.setCodSociinfo(cellCodSociinfo);
                        data0.setXtiCartera(cellXtiCartera);
                        data0.setCodSocipart(cellCodSocipart);
                        data0.setSignoValorContable(cellSignoValorContable);
                        data0.setSignoMicrocobertura(cellSignoMicrocobertura);
                        data0.setPeriodo(periodo);
                        toInsertPre.add(data0);

                        ReportIcrv data = new ReportIcrv();
                        data.setEntidad(cellEntidad);
                        data.setCodPeriodo(periodo.replace("-",""));
                        data.setCodSociinfo(cellCodSociinfo);
                        data.setXtiCartera(cellXtiCartera);
                        data.setCodSocipart(cellCodSocipart);
                        data.setMicrocoberturas(Double.parseDouble(cellMicrocoberturas));
                        data.setCorreccionesPorDeterioro(Math.abs(Double.parseDouble(cellCorreccionesPorDeterioro)));
                        data.setValorCotizado(Double.parseDouble(cellValorCotizado));
                        data.setCosteAdquisicion(Double.parseDouble(cellCosteAdquisicion));
                        data.setSignoValorContable(cellSignoValorContable);
                        data.setSignoMicrocobertura(cellSignoMicrocobertura);
                        data.setPeriodo(periodo);
                        toInsert.add(data);
                    }
                }
            }
        }

        if (lista.size() != 0)
            stateFinal = "FAILED";
        String[] log2 = new String[3];
        log2[0] = String.valueOf((toInsert.size() * 5) - lista.size());
        log2[1] = String.valueOf(lista.size());
        log2[2] = stateFinal;
        lista.add(log2);
        String[] temp = lista.get(0);
        if (temp[2].equals("SUCCESS")) {
            plantillaReportIcrvRepository.deleteAll();
            plantillaReportIcrvRepository.saveAll(toInsertPre);
            reportIcrvRepository.deleteByPeriodo(periodo);
            reportIcrvRepository.saveAll(toInsert);
            updateTable(periodo);
        }
        toInsert.clear();
        return lista;
    }

    public List<ReportIcrv> findByFilter(String value, String filter, String period) {
        List<ReportIcrv> list = new ArrayList<>();
        try {
            switch (filter) {
                case "Entidad":
                    list = reportIcrvRepository.findByEntidadAndPeriodo(value, period);
                    break;
                case "Código Periodo":
                    list = reportIcrvRepository.findByCodPeriodoAndPeriodo(value, period);
                    break;
                case "CodSociinfo":
                    list = reportIcrvRepository.findByCodSociinfoAndPeriodo(value, period);
                    break;
                case "XtiCartera":
                    list = reportIcrvRepository.findByXtiCarteraAndPeriodo(value, period);
                    break;
                case "CodSocipart":
                    list = reportIcrvRepository.findByCodSocipartAndPeriodo(value, period);
                    break;
                case "CodISIN":
                    list = reportIcrvRepository.findByCodIsinAndPeriodo(value, period);
                    break;
                case "Signo Valor Contable":
                    list = reportIcrvRepository.findBySignoValorContableAndPeriodo(value, period);
                    break;
                case "Signo Microcobertura":
                    list = reportIcrvRepository.findBySignoMicrocoberturaAndPeriodo(value, period);
                    break;
                default:
                    break;
            }
        }
        catch (Exception e)
        {
            return list;
        }
        return list;
    }

}


