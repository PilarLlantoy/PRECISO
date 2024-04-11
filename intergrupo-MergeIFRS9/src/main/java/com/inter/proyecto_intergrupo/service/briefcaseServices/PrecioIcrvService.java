package com.inter.proyecto_intergrupo.service.briefcaseServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.briefcase.BaseIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.PlantillaPrecioIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.PrecioIcrv;
import com.inter.proyecto_intergrupo.model.ifrs9.Anexo;
import com.inter.proyecto_intergrupo.model.parametric.StatusInfo;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.admin.ControlPanelJobsRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.PlantillaPrecioIcrvRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.PrecioIcrvRepository;
import com.inter.proyecto_intergrupo.service.adminServices.ControlPanelJobsService;
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
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
@Transactional
public class PrecioIcrvService {

    @Autowired
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private PlantillaPrecioIcrvRepository plantillaPrecioIcrvRepository;

    @Autowired
    private PrecioIcrvRepository precioIcrvRepository;

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Portafolio");
        insert.setFecha(today);
        insert.setInput("Precio ICRV");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public PrecioIcrv findByIdPrecio(Long id){
        return precioIcrvRepository.findByIdPrecio(id);
    }

    public PrecioIcrv modifyPrecio(PrecioIcrv toModify, User user)
    {
        if(toModify.getPrecioValoracion()==null)
            toModify.setPrecioValoracion(0.0);
        if(toModify.getPatrimonio()==null)
            toModify.setPatrimonio(0.0);
        if(toModify.getAcciones()==null) {
            toModify.setAcciones(0.0);
            toModify.setVrIntrinseco(0.0);
        }
        else {
            toModify.setVrIntrinseco(toModify.getPatrimonio() / toModify.getAcciones());
        }
        toModify.setFechaActualizacion(new Date());
        List<BaseIcrv> list = completeIsin(toModify.getEmpresa());
        if(!list.isEmpty())
            toModify.setIsin(list.get(0).getNoAsignado());
        loadAudit(user,"Modificación Exitosa Registro ("+toModify.getIdPrecio()+") Precio ICRV");
        return precioIcrvRepository.save(toModify);
    }

    public List<PlantillaPrecioIcrv> findAllPlantilla()
    {
        return plantillaPrecioIcrvRepository.findAll();
    }

    public List<PrecioIcrv> findAllPrecio(String periodo)
    {
        return precioIcrvRepository.findByPeriodo(periodo);
    }

    public void completeTable(String periodo) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate firstDayOfMonth = LocalDate.parse(periodo + "-01", formatter);
        LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());
        String ultimoDiaDelMes = lastDayOfMonth.format(formatter);

        Query limpieza = entityManager.createNativeQuery("delete from nexco_precio_icrv where periodo = :periodo ;\n" +
                "insert into nexco_precio_icrv (metodo, fecha_contable, empresa, periodo) \n" +
                "select metodo, '"+ultimoDiaDelMes+"', empresa, :periodo from nexco_plantilla_precio_icrv;");
        limpieza.setParameter("periodo",periodo);
        limpieza.executeUpdate();
    }

    public List<BaseIcrv> completeIsin(String empresa){
        Query consulta = entityManager.createNativeQuery("select * from nexco_base_icrv where empresa = ? ;", BaseIcrv.class);
        consulta.setParameter(1,empresa);
        return consulta.getResultList();
    }

    public void clearPrecio(User user, String periodo){
        completeTable(periodo);
        loadAudit(user,"Generación Exitosa de Precios para "+periodo);
    }

    @Scheduled(cron = "0 0 2 1 * ?")
    public void jobPrecios(){
        validatePrecios();
    }
    @Scheduled(cron = "0 0 7 1 * ?")
    public void jobPrecios2(){
        validatePrecios();
    }
    public void validatePrecios(){
        Date today = new Date();
        String todayString="";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        if(calendar.get(Calendar.MONTH)==0)
        {
            calendar.add(Calendar.YEAR,-1);
            todayString = calendar.get(Calendar.YEAR) + "-12";
        }
        else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
            todayString = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH);
        } else {
            todayString = calendar.get(Calendar.YEAR) + "-0" + calendar.get(Calendar.MONTH);
        }
        User user = new User();
        user.setCentro("");
        user.setNombre("SYSTEM JOB");
        user.setUsuario("");

        if(findAllPrecio(todayString).size()==0)
        {
            completeTable(todayString);
            loadAudit(user,"Ejecución Exitosa JOB Precios ICRV");
        }
        else{
            loadAudit(user,"Ejecución Anulada JOB Precios ICRV");
        }
    }

    public ArrayList<String[]> saveFileBDPlantilla(InputStream file, User user,String periodo) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<>();
        if (file!=null)
        {
            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            list=validarPlantilla(rows,periodo);
            if(list.get(0)[2].equals("SUCCESS"))
                loadAudit(user,"Cargue Exitoso Plantilla Precio ICRV");
            else
                loadAudit(user,"Cargue Fallido Plantilla Precio ICRV");
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows,String periodo) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<PlantillaPrecioIcrv> toInsert = new ArrayList<>();
        ArrayList<PrecioIcrv> toInsertFinal = new ArrayList<>();
        String stateFinal = "SUCCESS";
        XSSFRow row;

        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate firstDayOfMonth = LocalDate.parse(periodo + "-01", formatter1);
        LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());
        String ultimoDiaDelMes = lastDayOfMonth.format(formatter1);

        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    DataFormatter formatter = new DataFormatter();
                    int consecutive = 0;
                    String cellMetodo = formatter.formatCellValue(row.getCell(consecutive++)).trim().toUpperCase();
                    String cellFechaContable = formatter.formatCellValue(row.getCell(consecutive++)).trim();
                    String cellEmpresa = formatter.formatCellValue(row.getCell(consecutive++)).trim().toUpperCase();

                    XSSFCell cell0= row.getCell(consecutive++);
                    if(cell0!=null)
                        cell0.setCellType(CellType.STRING);
                    String cellPrecioValoracion = formatter.formatCellValue(cell0).replace(" ", "").replace(",","");

                    XSSFCell cell1= row.getCell(consecutive++);
                    if(cell1!=null)
                        cell1.setCellType(CellType.STRING);
                    String cellPatrimonio = formatter.formatCellValue(cell1).replace(" ", "").replace(",","");

                    XSSFCell cell2= row.getCell(consecutive++);
                    if(cell2!=null)
                        cell2.setCellType(CellType.STRING);
                    String cellAccionesCirculacion = formatter.formatCellValue(cell2).replace(" ", "").replace(",","");

                    XSSFCell cell3= row.getCell(consecutive++);
                    if(cell3!=null)
                        cell3.setCellType(CellType.STRING);
                    String cellVRIntrinseco = formatter.formatCellValue(cell3).replace(" ", "").replace(",","");

                    XSSFCell cell4= row.getCell(consecutive++);
                    if(cell4!=null)
                        cell4.setCellType(CellType.STRING);
                    String cellORI = formatter.formatCellValue(cell4).replace(" ", "").replace(",","");

                    String cellFechaRecibo = formatter.formatCellValue(row.getCell(consecutive++)).trim();
                    String cellFechaActualizacion = formatter.formatCellValue(row.getCell(consecutive++)).trim();
                    String cellResultado = formatter.formatCellValue(row.getCell(consecutive++)).trim();

                    if (cellMetodo.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "El campo Método no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellEmpresa.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "El campo Empresa no puede estar vacio.";
                        lista.add(log);
                    }
                    if(cellPrecioValoracion.length()!=0)
                    {
                        try{
                            Double.parseDouble(cellPrecioValoracion);
                        }
                        catch (Exception e)
                        {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(3);
                            log[2] = "El campo Precio Valoración no puede estar vacio.";
                            lista.add(log);
                        }
                    }
                    else {
                        cellPrecioValoracion="0";
                    }
                    if(cellPatrimonio.length()!=0)
                    {
                        try{
                            Double.parseDouble(cellPatrimonio);
                        }
                        catch (Exception e)
                        {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(4);
                            log[2] = "El campo Patrimonio no puede estar vacio.";
                            lista.add(log);
                        }
                    }
                    else {
                        cellPatrimonio="0";
                    }
                    if(cellAccionesCirculacion.length()!=0)
                    {
                        try{
                            Double.parseDouble(cellAccionesCirculacion);
                        }
                        catch (Exception e)
                        {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(5);
                            log[2] = "El campo Acciones en Circulación no puede estar vacio.";
                            lista.add(log);
                        }
                    }
                    else {
                        cellAccionesCirculacion="0";
                    }
                    if(cellORI.length()!=0)
                    {
                        try{
                            Double.parseDouble(cellORI);
                        }
                        catch (Exception e)
                        {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(7);
                            log[2] = "El campo ORI no puede estar vacio.";
                            lista.add(log);
                        }
                    }
                    else {
                        cellORI="0";
                    }

                    if(lista.size() == 0) {
                        PlantillaPrecioIcrv data = new PlantillaPrecioIcrv();
                        data.setMetodo(cellMetodo);
                        data.setEmpresa(cellEmpresa);
                        toInsert.add(data);



                        PrecioIcrv data1 = new PrecioIcrv();
                        data1.setMetodo(cellMetodo);
                        data1.setEmpresa(cellEmpresa);
                        data1.setPrecioValoracion(Double.parseDouble(cellPrecioValoracion));
                        data1.setPatrimonio(Double.parseDouble(cellPatrimonio));
                        data1.setAcciones(Double.parseDouble(cellAccionesCirculacion));
                        data1.setOri(Double.parseDouble(cellORI));
                        data1.setFechaRecibo(cellFechaRecibo);
                        data1.setFechaActualizacion(new Date());
                        data1.setResultado(cellResultado);
                        data1.setPeriodo(periodo);
                        data1.setFechaContable(ultimoDiaDelMes);

                        if(cellAccionesCirculacion.length()!=0){
                            data1.setVrIntrinseco(Double.parseDouble(cellPatrimonio) / Double.parseDouble(cellAccionesCirculacion));
                        }
                        List<BaseIcrv> list = completeIsin(cellEmpresa);
                        if(!list.isEmpty())
                            data1.setIsin(list.get(0).getNoAsignado());

                        toInsertFinal.add(data1);
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
            plantillaPrecioIcrvRepository.deleteAll();
            plantillaPrecioIcrvRepository.saveAll(toInsert);
            precioIcrvRepository.deleteByPeriodo(periodo);
            precioIcrvRepository.saveAll(toInsertFinal);
        }
        toInsert.clear();
        toInsertFinal.clear();
        return lista;
    }

    public List<PrecioIcrv> findByFilter(String value, String filter, String period) {
        List<PrecioIcrv> list = new ArrayList<>();
        try {
            switch (filter) {
                case "Método":
                    list = precioIcrvRepository.findByMetodoAndPeriodo(value, period);
                    break;
                case "Fecha Contable":
                    list = precioIcrvRepository.findByFechaContableAndPeriodo(value, period);
                    break;
                case "Empresa":
                    list = precioIcrvRepository.findByEmpresaAndPeriodo(value, period);
                    break;
                case "Precio Valoración":
                    list = precioIcrvRepository.findByPrecioValoracionAndPeriodo(Double.parseDouble(value), period);
                    break;
                case "Patrimonio":
                    list = precioIcrvRepository.findByPatrimonioAndPeriodo(Double.parseDouble(value), period);
                    break;
                case "Acciones Circulación":
                    list = precioIcrvRepository.findByAccionesAndPeriodo(Double.parseDouble(value), period);
                    break;
                case "VR Intrinseco":
                    list = precioIcrvRepository.findByVrIntrinsecoAndPeriodo(Double.parseDouble(value), period);
                    break;
                case "ORI":
                    list = precioIcrvRepository.findByOriAndPeriodo(Double.parseDouble(value), period);
                    break;
                case "Fecha Recibo":
                    list = precioIcrvRepository.findByFechaReciboAndPeriodo(value, period);
                    break;
                case "Fecha Actualización":
                    list = precioIcrvRepository.findByFechaActualizacionAndPeriodo(value, period);
                    break;
                case "Resultado":
                    list = precioIcrvRepository.findByResultadoAndPeriodo(value, period);
                    break;
                case "ISIN":
                    list = precioIcrvRepository.findByIsinAndPeriodo(value, period);
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


