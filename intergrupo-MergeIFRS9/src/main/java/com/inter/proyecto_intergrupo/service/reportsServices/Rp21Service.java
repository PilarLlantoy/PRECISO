package com.inter.proyecto_intergrupo.service.reportsServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.ControlPanel;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.reports.Rp21;
import com.inter.proyecto_intergrupo.model.reports.Rp21_Extend;
import com.inter.proyecto_intergrupo.model.temporal.Rp21Temporal;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CurrencyRepository;
import com.inter.proyecto_intergrupo.repository.parametric.OperationAccountRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ResponsibleAccountRepository;
import com.inter.proyecto_intergrupo.repository.reports.Rp21Repository;
import com.inter.proyecto_intergrupo.repository.reports.Rp21TemporalRepository;
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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.Part;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class Rp21Service {

    @Autowired
    private Rp21Repository rp21Repository;

    @Autowired
    private Rp21TemporalRepository rp21TemporalRepository;

    @Autowired
    private ResponsibleAccountRepository responsibleAccountRepository;

    @Autowired
    private OperationAccountRepository operationAccountRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private AuditRepository auditRepository;

    @PersistenceContext
    EntityManager entityManager;

    private List<Rp21Temporal> rp21TemporalList = new ArrayList<>();
    private Boolean failForward = false;
    private Boolean failSwap = false;
    private Boolean failRyS = false;
    private Boolean failOpciones = false;

    public Rp21Service(CurrencyRepository currencyRepository, Rp21Repository rp21Repository,ResponsibleAccountRepository responsibleAccountRepository,AuditRepository auditRepository) {
        this.responsibleAccountRepository=responsibleAccountRepository;
        this.rp21Repository = rp21Repository;
        this.currencyRepository=currencyRepository;
        this.auditRepository = auditRepository;
    }

    public List<String[]> saveFileBD(Collection<Part>  parts, User user, String mes) throws IOException, InvalidFormatException, ParseException {
        rp21TemporalList.clear();
        List<String[]> listDolares=new ArrayList<String[]>();
        List<String[]> listDivisas=new ArrayList<String[]>();
        List<String[]> listTitulos=new ArrayList<String[]>();
        List<String[]> listSwap=new ArrayList<String[]>();
        List<String[]> listRYS=new ArrayList<String[]>();
        List<String[]> listOpciones=new ArrayList<String[]>();
        List<String[]> listFuturos=new ArrayList<String[]>();
        List<String[]> finalList= new ArrayList<String[]>();
        List<String> names=new ArrayList<String>();
        String[] listReportNames=new String[4];

        boolean duplicated=false;
        boolean error=false;
        boolean pass=true;

        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando as em " +
                "WHERE em.input = ? AND em.fecha_reporte = ? AND em.responsable = ? " +
                "AND em.componente = ?", ControlPanel.class);
        query.setParameter(1, "FW" );
        query.setParameter(2, mes);
        query.setParameter(3, user.getCentro());
        query.setParameter(4, "DERIVADOS");
        List<ControlPanel> forwardList= query.getResultList();

        Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando as em " +
                "WHERE em.input = ? AND em.fecha_reporte = ? AND em.responsable = ? " +
                "AND em.componente = ?", ControlPanel.class);
        query1.setParameter(1, "SWAP" );
        query1.setParameter(2, mes);
        query1.setParameter(3, user.getCentro());
        query1.setParameter(4, "DERIVADOS");
        List<ControlPanel> swapList= query1.getResultList();

        Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando as em " +
                "WHERE em.input = ? AND em.fecha_reporte = ? AND em.responsable = ? " +
                "AND em.componente = ?", ControlPanel.class);
        query2.setParameter(1, "OPCIONES" );
        query2.setParameter(2, mes);
        query2.setParameter(3, user.getCentro());
        query2.setParameter(4, "DERIVADOS");
        List<ControlPanel> opcionesList= query2.getResultList();

        Query query3 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando as em " +
                "WHERE em.input = ? AND em.fecha_reporte = ? AND em.responsable = ? AND em.componente = ?", ControlPanel.class);
        query3.setParameter(1, "RYS" );
        query3.setParameter(2, mes);
        query3.setParameter(3, user.getCentro());
        query3.setParameter(4, "DERIVADOS");
        List<ControlPanel> rysList= query3.getResultList();

        Query query4 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando as em " +
                "WHERE em.input = ? AND em.fecha_reporte = ? AND em.responsable = ? " +
                "AND em.componente = ?", ControlPanel.class);
        query4.setParameter(1, "FUTUROS" );
        query4.setParameter(2, mes);
        query4.setParameter(3, user.getCentro());
        query4.setParameter(4, "DERIVADOS");
        List<ControlPanel> futurosList= query4.getResultList();

        String listNames="";
        for(Part partName : parts) {
            if(names.contains(partName.getSubmittedFileName()))
            {
                duplicated=true;
            }
            else{
                names.add(partName.getSubmittedFileName());
                listNames = listNames+partName.getSubmittedFileName();
            }
        }
        listReportNames[0]=listNames;

        for(int j=0;j<names.size();j++) {
            if(names.get(j)!=null){
                if (!names.get(j).equals("FW_CONCILIACION.xlsx")) {
                    if (!names.get(j).equals("SWAP_CONCILIACION.xlsx")) {
                        if (!names.get(j).equals("RYS_CONCILIACION.xlsx")) {
                            if (!names.get(j).equals("OPCIONES_CONCILIACION.xlsx")) {
                                if (!names.get(j).equals("CRCC_CONCILIACION.xlsx")) {
                                    error = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        if(forwardList.size()>0 && names.contains("FW_CONCILIACION.xlsx") &&(!forwardList.get(0).getResponsable().equals(user.getCentro())||forwardList.get(0).getEstado()==false))
        {
            pass=false;
        }
        if(swapList.size()>0 && names.contains("SWAP_CONCILIACION.xlsx") && (!swapList.get(0).getResponsable().equals(user.getCentro())||swapList.get(0).getEstado()==false))
        {
            pass=false;
        }
        if(rysList.size()>0 && names.contains("RYS_CONCILIACION.xlsx") && (!rysList.get(0).getResponsable().equals(user.getCentro())||rysList.get(0).getEstado()==false))
        {
            pass=false;
        }
        if(opcionesList.size()>0 && names.contains("OPCIONES_CONCILIACION.xlsx") && (!opcionesList.get(0).getResponsable().equals(user.getCentro())||opcionesList.get(0).getEstado()==false))
        {
            pass=false;
        }
        if(futurosList.size()>0 && names.contains("CRCC_CONCILIACION.xlsx") && (!futurosList.get(0).getResponsable().equals(user.getCentro())||futurosList.get(0).getEstado()==false))
        {
            pass=false;
        }
        if(forwardList.size()==0 && names.contains("FW_CONCILIACION.xlsx"))
        {
            pass=false;
        }
        if(swapList.size()==0 && names.contains("SWAP_CONCILIACION.xlsx"))
        {
            pass=false;
        }
        if(rysList.size()==0 && names.contains("RYS_CONCILIACION.xlsx"))
        {
            pass=false;
        }
        if(opcionesList.size()==0 && names.contains("OPCIONES_CONCILIACION.xlsx"))
        {
            pass=false;
        }
        if(futurosList.size()==0 && names.contains("CRCC_CONCILIACION.xlsx"))
        {
            pass=false;
        }

        if(duplicated==true){
            String[] logFinal= new String[4];
            logFinal[0]="DUPLICADO";
            finalList.add(logFinal);
        }
        else if(error==true)
        {
            String[] logFinal= new String[4];
            logFinal[0]="ERROR";
            finalList.add(logFinal);
        }
        else if(pass==false)
        {
            String[] logFinal= new String[4];
            logFinal[0]="PERMISO";
            finalList.add(logFinal);
        }
        else
        {
            Iterator<Row> rowsDolares = null;
            Iterator<Row> rows1Dolares = null;

            Iterator<Row> rowsDivisas = null;
            Iterator<Row> rows1Divisas = null;

            Iterator<Row> rowsTitulos = null;
            Iterator<Row> rows1Titulos = null;

            Iterator<Row> rowsConciliacion = null;
            Iterator<Row> rows1Conciliacion = null;

            Iterator<Row> rowsSwap = null;
            Iterator<Row> rows1Swap = null;

            Iterator<Row> rowsOpciones = null;
            Iterator<Row> rows1Opciones= null;

            Iterator<Row> rowsFuturos = null;
            Iterator<Row> rows1Futuros= null;

            for(Part part : parts) {
                InputStream file = part.getInputStream();

                if (part!=null && file != null && part.getSubmittedFileName()!=null && part.getSubmittedFileName().equals("FW_CONCILIACION.xlsx"))
                {
                    XSSFWorkbook wb = new XSSFWorkbook(file);

                    XSSFSheet sheetDolares = wb.getSheet("FW DOLARES");
                    rowsDolares = sheetDolares.iterator();
                    rows1Dolares = sheetDolares.iterator();
                    listDolares = validarPlantillaFwConciliacionDolares(rowsDolares,mes,user);

                    XSSFSheet sheetDivisas = wb.getSheet("FW DIVISAS");
                    rowsDivisas = sheetDivisas.iterator();
                    rows1Divisas = sheetDivisas.iterator();
                    listDivisas = validarPlantillaFwConciliacionDivisas(rowsDivisas,mes,user);

                    XSSFSheet sheetTitulos = wb.getSheet("FW TITULOS");
                    rowsTitulos = sheetTitulos.iterator();
                    rows1Titulos = sheetTitulos.iterator();
                    listTitulos = validarPlantillaFwConciliacionTitulos(rowsTitulos,mes,user);
                }
                else if (part!=null &&file != null && part.getSubmittedFileName()!=null && part.getSubmittedFileName().equals("SWAP_CONCILIACION.xlsx"))
                {
                    XSSFWorkbook wb = new XSSFWorkbook(file);

                    XSSFSheet sheetSwap = wb.getSheet("SWAP");
                    rowsSwap = sheetSwap.iterator();
                    rows1Swap = sheetSwap.iterator();
                    listSwap = validarPlantillaFwConciliacionSwap(rowsSwap,mes,user);
                }
                else if(part!=null &&file != null && part.getSubmittedFileName()!=null && part.getSubmittedFileName().equals("RYS_CONCILIACION.xlsx")){

                    XSSFWorkbook wb = new XSSFWorkbook(file);

                    XSSFSheet sheetRepos = wb.getSheet("REPOS & SIMULTANEAS");
                    rowsConciliacion = sheetRepos.iterator();
                    rows1Conciliacion = sheetRepos.iterator();
                    listRYS = validarPlantillaRySConciliacion(rowsConciliacion,mes,user);

                }
                else if(part!=null &&file != null && part.getSubmittedFileName()!=null && part.getSubmittedFileName().equals("OPCIONES_CONCILIACION.xlsx")){

                    XSSFWorkbook wb = new XSSFWorkbook(file);

                    XSSFSheet sheetRepos = wb.getSheet("OPCIONES");
                    rowsOpciones = sheetRepos.iterator();
                    rows1Opciones = sheetRepos.iterator();
                    listOpciones = validarPlantillaOpciones(rowsOpciones,mes,user);
                }
                else if(part!=null &&file != null && part.getSubmittedFileName()!=null && part.getSubmittedFileName().equals("CRCC_CONCILIACION.xlsx")){

                    XSSFWorkbook wb = new XSSFWorkbook(file);

                    XSSFSheet sheetRepos = wb.getSheet("FUTUROS CRCC");
                    rowsFuturos = sheetRepos.iterator();
                    rows1Futuros= sheetRepos.iterator();
                    listFuturos = validarPlantillaFuturos(rowsFuturos,mes,user);
                }
            }
            String[] loadDate=mes.split("-");

            Query truncateTable = entityManager.createNativeQuery("TRUNCATE TABLE nexco_reporte_rp21_temporal");
            truncateTable.executeUpdate();

            if(rp21TemporalList.size() > 0){
                saveRp21Temporal(rp21TemporalList);
            }

            if(!listDolares.isEmpty() && !listDivisas.isEmpty() && !listTitulos.isEmpty()) {

                finalList = Stream.concat(finalList.stream(), listDolares.stream()).collect(Collectors.toList());
                finalList = Stream.concat(finalList.stream(), listDivisas.stream()).collect(Collectors.toList());
                finalList = Stream.concat(finalList.stream(), listTitulos.stream()).collect(Collectors.toList());

                if (listDolares.get(listDolares.size() - 1)[2].equals("0") && listDivisas.get(listDivisas.size() - 1)[2].equals("0") && listTitulos.get(listTitulos.size() - 1)[2].equals("0"))
                {
                    Query queryFor = entityManager.createNativeQuery("DELETE from nexco_reporte_rp21 " +
                            "WHERE origen = ? AND MONTH(fecont) = ? AND YEAR(fecont) = ?", Rp21.class);
                    queryFor.setParameter(3, loadDate[0]);
                    queryFor.setParameter(2, loadDate[1]);
                    queryFor.setParameter(1, "FORWARD");
                    queryFor.executeUpdate();

                    Query queryFor1 = entityManager.createNativeQuery("DELETE from nexco_reporte_rp21_extend " +
                            "WHERE origen = ? and MONTH(fecont) = ? AND YEAR(fecont) = ?", Rp21_Extend.class);
                    queryFor1.setParameter(3, loadDate[0]);
                    queryFor1.setParameter(2, loadDate[1]);
                    queryFor1.setParameter(1, "FORWARD");
                    queryFor1.executeUpdate();

                    ControlPanel temporal2 = forwardList.get(0);
                    temporal2.setSemaforoInput("PENDING");
                    temporal2.setUsuarioCarga(user.getPrimerNombre());

                    Query query7 = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando " +
                            "SET semaforo_input = ?,fecha_carga = ? " +
                            "WHERE responsable = ? AND input = ? AND componente = ? " +
                            "AND fecha_reporte = ?", ControlPanel.class);
                    query7.setParameter(6, mes);
                    query7.setParameter(1, temporal2.getSemaforoInput());
                    query7.setParameter(2, new Date());
                    query7.setParameter(3, temporal2.getResponsable());
                    query7.setParameter(4, temporal2.getInput());
                    query7.setParameter(5, temporal2.getComponente());
                    query7.executeUpdate();

                    Date today=new Date();
                    Audit insert = new Audit();
                    insert.setAccion("Inserción Documento Derivados FORWARD");
                    insert.setCentro(user.getCentro());
                    insert.setComponente("DERIVADOS");
                    insert.setFecha(today);
                    insert.setInput("FORWARD");
                    insert.setNombre(user.getPrimerNombre());
                    insert.setUsuario(user.getUsuario());
                    auditRepository.save(insert);
                }
                else
                {
                    deleteByOrigen("FORWARD");
                }
            }
            if(!listSwap.isEmpty()) {
                finalList = Stream.concat(finalList.stream(), listSwap.stream()).collect(Collectors.toList());
                if (listSwap.get(listSwap.size() - 1)[2].equals("0")) {
                    Query queryFor = entityManager.createNativeQuery("DELETE from nexco_reporte_rp21 " +
                            "WHERE origen = ? AND MONTH(fecont) = ? AND YEAR(fecont) = ?", Rp21.class);
                    queryFor.setParameter(3, loadDate[0]);
                    queryFor.setParameter(2, loadDate[1]);
                    queryFor.setParameter(1, "SWAP");
                    queryFor.executeUpdate();

                    Query queryFor1 = entityManager.createNativeQuery("DELETE from nexco_reporte_rp21_extend " +
                            "WHERE origen = ? and MONTH(fecont) = ? AND YEAR(fecont) = ?", Rp21_Extend.class);
                    queryFor1.setParameter(3, loadDate[0]);
                    queryFor1.setParameter(2, loadDate[1]);
                    queryFor1.setParameter(1, "SWAP");
                    queryFor1.executeUpdate();

                    //getRowsSwap(rows1Swap, user);
                    ControlPanel temporal = swapList.get(0);
                    temporal.setSemaforoInput("PENDING");
                    temporal.setUsuarioCarga(user.getPrimerNombre());
                    Query query5 = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando SET semaforo_input = ?,fecha_carga = ? " +
                            "WHERE responsable = ? AND input = ? AND componente = ? AND fecha_reporte = ?", ControlPanel.class);
                    query5.setParameter(6, mes);
                    query5.setParameter(1, temporal.getSemaforoInput());
                    query5.setParameter(2, new Date());
                    query5.setParameter(3, temporal.getResponsable());
                    query5.setParameter(4, temporal.getInput());
                    query5.setParameter(5, temporal.getComponente());
                    query5.executeUpdate();

                    Date today=new Date();
                    Audit insert = new Audit();
                    insert.setAccion("Inserción Documento Derivados SWAP");
                    insert.setCentro(user.getCentro());
                    insert.setComponente("DERIVADOS");
                    insert.setFecha(today);
                    insert.setInput("SWAP");
                    insert.setNombre(user.getPrimerNombre());
                    insert.setUsuario(user.getUsuario());
                    auditRepository.save(insert);
                }
                else
                {
                    deleteByOrigen("SWAP");
                }
            }
            if(!listRYS.isEmpty()) {
                finalList = Stream.concat(finalList.stream(), listRYS.stream()).collect(Collectors.toList());
                if (listRYS.get(listRYS.size() - 1)[2].equals("0")) {

                    Query queryFor = entityManager.createNativeQuery("DELETE from nexco_reporte_rp21 " +
                            "WHERE origen = ? AND MONTH(fecont) = ? AND YEAR(fecont) = ?", Rp21.class);
                    queryFor.setParameter(3, loadDate[0]);
                    queryFor.setParameter(2, loadDate[1]);
                    queryFor.setParameter(1, "RYS");
                    queryFor.executeUpdate();

                    Query queryFor1 = entityManager.createNativeQuery("DELETE from nexco_reporte_rp21_extend " +
                            "WHERE origen = ? and MONTH(fecont) = ? AND YEAR(fecont) = ?", Rp21_Extend.class);
                    queryFor1.setParameter(3, loadDate[0]);
                    queryFor1.setParameter(2, loadDate[1]);
                    queryFor1.setParameter(1, "RYS");
                    queryFor1.executeUpdate();

                    //getRowsRYS(rows1Conciliacion, user);
                    ControlPanel temporal = rysList.get(0);
                    temporal.setSemaforoInput("PENDING");
                    temporal.setUsuarioCarga(user.getPrimerNombre());
                    Query query5 = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando SET semaforo_input = ?,fecha_carga = ? " +
                            "WHERE responsable = ? AND input = ? AND componente = ? AND fecha_reporte = ?", ControlPanel.class);
                    query5.setParameter(6, mes);
                    query5.setParameter(1, temporal.getSemaforoInput());
                    query5.setParameter(2, new Date());
                    query5.setParameter(3, temporal.getResponsable());
                    query5.setParameter(4, temporal.getInput());
                    query5.setParameter(5, temporal.getComponente());
                    query5.executeUpdate();

                    Date today=new Date();
                    Audit insert = new Audit();
                    insert.setAccion("Inserción Documento Derivados RYS");
                    insert.setCentro(user.getCentro());
                    insert.setComponente("DERIVADOS");
                    insert.setFecha(today);
                    insert.setInput("RYS");
                    insert.setNombre(user.getPrimerNombre());
                    insert.setUsuario(user.getUsuario());
                    auditRepository.save(insert);
                }
                else
                {
                    deleteByOrigen("RYS");
                }
            }
            if(!listOpciones.isEmpty()) {
                finalList = Stream.concat(finalList.stream(), listOpciones.stream()).collect(Collectors.toList());
                if (listOpciones.get(listOpciones.size() - 1)[2].equals("0")) {

                    Query queryFor = entityManager.createNativeQuery("DELETE from nexco_reporte_rp21 " +
                            "WHERE origen = ? AND MONTH(fecont) = ? AND YEAR(fecont) = ?", Rp21.class);
                    queryFor.setParameter(3, loadDate[0]);
                    queryFor.setParameter(2, loadDate[1]);
                    queryFor.setParameter(1, "OPCIONES");
                    queryFor.executeUpdate();

                    Query queryFor1 = entityManager.createNativeQuery("DELETE from nexco_reporte_rp21_extend " +
                            "WHERE origen = ? and MONTH(fecont) = ? AND YEAR(fecont) = ?", Rp21_Extend.class);
                    queryFor1.setParameter(3, loadDate[0]);
                    queryFor1.setParameter(2, loadDate[1]);
                    queryFor1.setParameter(1, "OPCIONES");
                    queryFor1.executeUpdate();

                    //getRowsOpciones(rows1Opciones, user);
                    ControlPanel temporal = opcionesList.get(0);
                    temporal.setSemaforoInput("PENDING");
                    temporal.setUsuarioCarga(user.getPrimerNombre());
                    Query query5 = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando SET semaforo_input = ?,fecha_carga = ? " +
                            "WHERE responsable = ? AND input = ? AND componente = ? AND fecha_reporte = ?", ControlPanel.class);
                    query5.setParameter(6, mes);
                    query5.setParameter(1, temporal.getSemaforoInput());
                    query5.setParameter(2, new Date());
                    query5.setParameter(3, temporal.getResponsable());
                    query5.setParameter(4, temporal.getInput());
                    query5.setParameter(5, temporal.getComponente());
                    query5.executeUpdate();

                    Date today=new Date();
                    Audit insert = new Audit();
                    insert.setAccion("Inserción Documento Derivados OPCIONES");
                    insert.setCentro(user.getCentro());
                    insert.setComponente("DERIVADOS");
                    insert.setFecha(today);
                    insert.setInput("OPCIONES");
                    insert.setNombre(user.getPrimerNombre());
                    insert.setUsuario(user.getUsuario());
                    auditRepository.save(insert);
                }
                else
                {
                    deleteByOrigen("OPCIONES");
                }
            }
            if(!listFuturos.isEmpty()) {
                finalList = Stream.concat(finalList.stream(), listFuturos.stream()).collect(Collectors.toList());
                if (listFuturos.get(listFuturos.size() - 1)[2].equals("0")) {

                    Query queryFor = entityManager.createNativeQuery("DELETE from nexco_reporte_rp21 " +
                            "WHERE origen = ? AND MONTH(fecont) = ? AND YEAR(fecont) = ?", Rp21.class);
                    queryFor.setParameter(3, loadDate[0]);
                    queryFor.setParameter(2, loadDate[1]);
                    queryFor.setParameter(1, "FUTUROS");
                    queryFor.executeUpdate();

                    Query queryFor1 = entityManager.createNativeQuery("DELETE from nexco_reporte_rp21_extend " +
                            "WHERE origen = ? and MONTH(fecont) = ? AND YEAR(fecont) = ?", Rp21_Extend.class);
                    queryFor1.setParameter(3, loadDate[0]);
                    queryFor1.setParameter(2, loadDate[1]);
                    queryFor1.setParameter(1, "FUTUROS");
                    queryFor1.executeUpdate();

                    //getRowsOpciones(rows1Opciones, user);
                    ControlPanel temporal = futurosList.get(0);
                    temporal.setSemaforoInput("PENDING");
                    temporal.setUsuarioCarga(user.getPrimerNombre());
                    Query query5 = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando SET semaforo_input = ?,fecha_carga = ? " +
                            "WHERE responsable = ? AND input = ? AND componente = ? AND fecha_reporte = ?", ControlPanel.class);
                    query5.setParameter(6, mes);
                    query5.setParameter(1, temporal.getSemaforoInput());
                    query5.setParameter(2, new Date());
                    query5.setParameter(3, temporal.getResponsable());
                    query5.setParameter(4, temporal.getInput());
                    query5.setParameter(5, temporal.getComponente());
                    query5.executeUpdate();

                    Date today=new Date();
                    Audit insert = new Audit();
                    insert.setAccion("Inserción Documento Derivados FUTUROS");
                    insert.setCentro(user.getCentro());
                    insert.setComponente("DERIVADOS");
                    insert.setFecha(today);
                    insert.setInput("FUTUROS");
                    insert.setNombre(user.getPrimerNombre());
                    insert.setUsuario(user.getUsuario());
                    auditRepository.save(insert);
                }
                else
                {
                    deleteByOrigen("FUTUROS");
                }
            }

            String monthPeriod = mes.split("-")[1];
            String yearPeriod = mes.split("-")[0];

            Query insertData = entityManager.createNativeQuery("INSERT INTO nexco_reporte_rp21 (contraparte, divisa, div_rp21, dvsaconciliacion1, dvsaconciliacion2, f_alta, \n" +
                    "f_vencimiento,fecont, intergrupo , local_derec, local_obligacion, local_rp21, mtm_cop, neocon ,neocon_, \n" +
                    "nit, operacion, origen, pais, pyg, tipo_contraparte, tipo_operacion, tipo_riesgo, vr_nominal_cop, vr_nominal_divisa, vr_nominal_mtm, yintp,clase_futuros,centro_cuenta)\n" +
                    "select distinct a.contraparte, a.divisa, a.div_rp21, a.dvsaconciliacion1, a.dvsaconciliacion2, a.f_alta, \n" +
                    "a.f_vencimiento, a.fecont, case when d.nit_contraparte is null then 'No' else 'Si'end intergrupo, \n" +
                    "a.local_derec, a.local_obligacion, a.local_rp21, a.mtm_cop, g.CODICONS46 neocon, h.CODICONS46 neocon_, a.nit_original, \n" +
                    "a.operacion, a.origen, isnull(f.nombre_pais, isnull(l.nombre_pais, 'COLOMBIA')) pais, a.pyg, a.tipo_contraparte, i.tipo_operacion,\n" +
                    "i.tipo_riesgo, CASE WHEN a.origen = 'RYS' THEN isnull(a.vr_nominal_cop, 0)*isnull(j.valor, 1) ELSE a.vr_nominal_cop END, \n" +
                    "a.vr_nominal_divisa,  \n" +
                    "CASE WHEN a.origen = 'RYS' THEN  \n" +
                    "CASE WHEN a.vr_nominal_mtm < 0 THEN isnull(a.vr_nominal_cop, 0)*isnull(j.valor, 1) ELSE (isnull(a.vr_nominal_cop, 0)*isnull(j.valor, 1))+a.vr_nominal_mtm END \n" +
                    "ELSE a.vr_nominal_mtm END, isnull(convert(varchar, e.yntp), 'No') yntp,clase_futuros, a.centro_cuenta \n" +
                    "from nexco_reporte_rp21_temporal a \n" +
                    "left join nexco_cuentas_responsables b on a.local_rp21 = b.cuenta_local \n" +
                    "left join nexco_operacion_riesgo c on a.local_rp21 = c.cuenta_local \n" +
                    "left join nexco_terceros d on a.nit = d.nit_contraparte \n" +
                    "left join nexco_sociedades_yntp e on d.yntp = e.yntp \n" +
                    "left join nexco_paises f on e.id_pais = f.id_pais \n" +
                    "left join (select CODICONS46, NUCTA from cuentas_puc where EMPRESA = '0060') g on a.local_rp21 = g.NUCTA \n" +
                    "left join (select CODICONS46, NUCTA from cuentas_puc where EMPRESA = '0060') h on a.local_derec = h.NUCTA \n" +
                    "left join nexco_operacion_riesgo i on a.local_rp21 = i.cuenta_local \n" +
                    "left join (select * from nexco_divisas_valor where MONTH(fecha) = ? and YEAR(fecha) = ?) j on isnull(a.dvsaconciliacion1, 'COP') = j.divisa \n" +
                    "left join nexco_banco_garante k on a.nit = k.nit \n" +
                    "left join nexco_paises l on k.pais = l.id_pais");

            insertData.setParameter(1, monthPeriod);
            insertData.setParameter(2, yearPeriod);
            insertData.executeUpdate();

            Query insertData1 = entityManager.createNativeQuery("INSERT INTO nexco_reporte_rp21_extend (contraparte, divisa, div_rp21, dvsaconciliacion1, dvsaconciliacion2, f_alta, \n" +
                    "f_vencimiento,fecont, intergrupo , local_derec, local_obligacion, local_rp21, mtm_cop, neocon ,neocon_, \n" +
                    "nit, operacion, origen, pais, pyg, tipo_contraparte, tipo_operacion, tipo_riesgo, vr_nominal_cop, vr_nominal_divisa, vr_nominal_mtm, yintp,clase_futuros,centro_cuenta,negocio)\n" +
                    "select distinct a.contraparte, a.divisa, a.div_rp21, a.dvsaconciliacion1, a.dvsaconciliacion2, a.f_alta, \n" +
                    "a.f_vencimiento, a.fecont, case when d.nit_contraparte is null then 'No' else 'Si'end intergrupo, \n" +
                    "a.local_derec, a.local_obligacion, a.local_rp21, a.mtm_cop, g.CODICONS46 neocon, h.CODICONS46 neocon_, a.nit_original, \n" +
                    "a.operacion, a.origen, isnull(f.nombre_pais, isnull(l.nombre_pais, 'COLOMBIA')) pais, a.pyg, a.tipo_contraparte, i.tipo_operacion,\n" +
                    "i.tipo_riesgo, CASE WHEN a.origen = 'RYS' THEN isnull(a.vr_nominal_cop, 0)*isnull(j.valor, 1) ELSE a.vr_nominal_cop END, \n" +
                    "a.vr_nominal_divisa,  \n" +
                    "CASE WHEN a.origen = 'RYS' THEN  \n" +
                    "CASE WHEN a.vr_nominal_mtm < 0 THEN isnull(a.vr_nominal_cop, 0)*isnull(j.valor, 1) ELSE (isnull(a.vr_nominal_cop, 0)*isnull(j.valor, 1))+a.vr_nominal_mtm END \n" +
                    "ELSE a.vr_nominal_mtm END, isnull(convert(varchar, e.yntp), 'No') yntp,clase_futuros, a.centro_cuenta,a.negocio \n" +
                    "from nexco_reporte_rp21_temporal a \n" +
                    "left join nexco_cuentas_responsables b on a.local_rp21 = b.cuenta_local \n" +
                    "left join nexco_operacion_riesgo c on a.local_rp21 = c.cuenta_local \n" +
                    "left join nexco_terceros d on a.nit = d.nit_contraparte \n" +
                    "left join nexco_sociedades_yntp e on d.yntp = e.yntp \n" +
                    "left join nexco_paises f on e.id_pais = f.id_pais \n" +
                    "left join (select CODICONS46, NUCTA from cuentas_puc where EMPRESA = '0060') g on a.local_rp21 = g.NUCTA \n" +
                    "left join (select CODICONS46, NUCTA from cuentas_puc where EMPRESA = '0060') h on a.local_derec = h.NUCTA \n" +
                    "left join nexco_operacion_riesgo i on a.local_rp21 = i.cuenta_local \n" +
                    "left join (select * from nexco_divisas_valor where MONTH(fecha) = ? and YEAR(fecha) = ?) j on isnull(a.dvsaconciliacion1, 'COP') = j.divisa \n" +
                    "left join nexco_banco_garante k on a.nit = k.nit \n" +
                    "left join nexco_paises l on k.pais = l.id_pais");

            insertData1.setParameter(1, monthPeriod);
            insertData1.setParameter(2, yearPeriod);
            insertData1.executeUpdate();

            validateComponent("DERIVADOS",mes);
            finalList.add(listReportNames);

            if(!listFuturos.isEmpty()) {
                if (listFuturos.get(listFuturos.size() - 1)[2].equals("0")) {
                    Query queryUpdate4 = entityManager.createNativeQuery("update nexco_reporte_rp21_extend set vr_nominal_divisa = vr_nominal_divisa * 50000  where month(fecont)=? and year(fecont)=? and operacion like 'TRM%' and divisa ='USD' and origen='FUTUROS'");
                    queryUpdate4.setParameter(2, loadDate[0]);
                    queryUpdate4.setParameter(1, loadDate[1]);
                    queryUpdate4.executeUpdate();

                    Query queryUpdate3 = entityManager.createNativeQuery("update nexco_reporte_rp21_extend set vr_nominal_divisa = vr_nominal_divisa * 250000000  where month(fecont)=? and year(fecont)=? and operacion like 'T%' and operacion not like 'TRM%' and divisa ='COP' and origen='FUTUROS'");
                    queryUpdate3.setParameter(2, loadDate[0]);
                    queryUpdate3.setParameter(1, loadDate[1]);
                    queryUpdate3.executeUpdate();
                }
            }

            rp21TemporalList.clear();

            Query queryUpdate1 = entityManager.createNativeQuery("update nexco_reporte_rp21_extend set negocio = 'Compra',id_negocio=1 where (negocio IN ('Cpra','Compra') or tipo_operacion IN ('FuturosCompraT.Interes','FuturosCompraDivisas')) and MONTH(fecont) = ? AND YEAR(fecont) = ? ");
            queryUpdate1.setParameter(2, loadDate[0]);
            queryUpdate1.setParameter(1, loadDate[1]);
            queryUpdate1.executeUpdate();

            Query queryUpdate2 = entityManager.createNativeQuery("update nexco_reporte_rp21_extend set negocio = 'Venta',id_negocio=2 where (negocio IN ('Vta','Venta') or tipo_operacion IN ('FuturosVentaT.Interes','FuturosVentaDivisas')) and MONTH(fecont) = ? AND YEAR(fecont) = ? ");
            queryUpdate2.setParameter(2, loadDate[0]);
            queryUpdate2.setParameter(1, loadDate[1]);
            queryUpdate2.executeUpdate();

        }

        return finalList;
    }

    public void validateComponent(String component, String mes){

        Query queryFinal = entityManager.createNativeQuery("SELECT * FROM nexco_cuadro_mando " +
                "WHERE componente = ? AND semaforo_input = 'PENDING' AND fecha_reporte = ?"
                , ControlPanel.class);
        queryFinal.setParameter(1, component);
        queryFinal.setParameter(2, mes);

        Query query5 = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando " +
                "SET semaforo_componente = ? " +
                "WHERE componente = ? AND fecha_reporte = ?", ControlPanel.class);
        query5.setParameter(2, component);
        query5.setParameter(3, mes);

        if(queryFinal.getResultList().size()>0)
        {
            query5.setParameter(1, "PENDING");
            query5.executeUpdate();
        }
    }

    public List<String[]> validarPlantillaFwConciliacionDolares(Iterator<Row> rows, String mes, User user) throws ParseException {
        ArrayList<String[]> lista= new ArrayList<String[]>();
        XSSFRow row;
        int firstRow=1;
        int fail=0;
        int success =0;
        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(firstRow!=1 && row.getRowNum()>0)
            {
                String[] log=new String[4];
                log[2]="true";
                DataFormatter formatter = new DataFormatter();
                String cellNitContraparte = formatter.formatCellValue(row.getCell(23));
                String cellNitContraparteOriginal = formatter.formatCellValue(row.getCell(23));
                String cellContraparte = formatter.formatCellValue(row.getCell(1));
                String cellFAlta = formatter.formatCellValue(row.getCell(5));
                String cellFuturos = formatter.formatCellValue(row.getCell(0));
                String cellFVencimiento = formatter.formatCellValue(row.getCell(6));
                String cellDivisa = formatter.formatCellValue(row.getCell(29));

                String cellVrNominalDivisa = "";
                String cellMtmCop = "";
                String cellVrNominalCop = "";

                String cellLocalRp21 = formatter.formatCellValue(row.getCell(62));
                String cellDivRp21 = formatter.formatCellValue(row.getCell(63));
                String cellLocalDerec = formatter.formatCellValue(row.getCell(38));
                String cellLocalObligacion = formatter.formatCellValue(row.getCell(42));
                String cellPyG = formatter.formatCellValue(row.getCell(54));
                String cellTipoContraparte = formatter.formatCellValue(row.getCell(28)).trim();
                String cellOperacion = formatter.formatCellValue(row.getCell(3));
                String cellDvsaConciliacion1 = formatter.formatCellValue(row.getCell(39));
                String cellDvsaConciliacion2 = formatter.formatCellValue(row.getCell(43));
                String cellFecont = formatter.formatCellValue(row.getCell(36));
                String cellNegocio = formatter.formatCellValue(row.getCell(2));

                log[0]=String.valueOf(row.getRowNum()+1);
                Date fechaDateC;
                Calendar calendar = Calendar.getInstance();
                String[] loadDate;
                try
                {
                    log[1]=CellReference.convertNumToColString(8)+" - (9)";
                    XSSFCell cell0= row.getCell(8);
                    cell0.setCellType(CellType.STRING);
                    cellVrNominalDivisa = formatter.formatCellValue(cell0).replace(" ", "");

                    log[1]=CellReference.convertNumToColString(56)+" - (57)";
                    XSSFCell cell1= row.getCell(56);
                    cell1.setCellType(CellType.STRING);
                    cellMtmCop = formatter.formatCellValue(cell1).replace(" ", "");

                    log[1]=CellReference.convertNumToColString(64)+" - (65)";
                    XSSFCell cell2= row.getCell(64);
                    cell2.setCellType(CellType.STRING);
                    cellVrNominalCop = formatter.formatCellValue(cell2).replace(" ", "");

                    log[1]=CellReference.convertNumToColString(62)+" - (63)"; Long.parseLong(cellLocalRp21);
                    log[1]=CellReference.convertNumToColString(23)+" - (24)"; cellNitContraparte = formatter.formatCellValue(row.getCell(23)).split("/")[1];
                    cellNitContraparte = cellNitContraparte.substring(0,cellNitContraparte.length()-1);
                    log[1]=CellReference.convertNumToColString(56)+" - (57)"; Double.parseDouble(cellMtmCop);
                    log[1]=CellReference.convertNumToColString(8)+" - (9)"; Double.parseDouble(cellVrNominalDivisa);
                    log[1]=CellReference.convertNumToColString(64)+" - (65)"; Double.parseDouble(cellVrNominalCop);
                    log[1]=CellReference.convertNumToColString(38)+" - (39)"; Long.parseLong(cellLocalDerec);
                    log[1]=CellReference.convertNumToColString(42)+" - (43)"; Long.parseLong(cellLocalObligacion);
                    log[1]=CellReference.convertNumToColString(54)+" - (55)"; Long.parseLong(cellPyG);
                    SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
                    formato.applyPattern("yyyy-MM-dd");
                    log[1]=CellReference.convertNumToColString(5)+" - (6)"; Date fechaDate = formato.parse(cellFAlta);
                    log[1]=CellReference.convertNumToColString(6)+" - (7)"; Date fechaDateV = formato.parse(cellFVencimiento);
                    log[1]=CellReference.convertNumToColString(36)+" - (37)"; fechaDateC = formato.parse(cellFecont);

                    calendar.setTime(fechaDateC);
                    loadDate=mes.split("-");
                }
                catch(Exception e){
                    fail++;
                    log[2]="false";
                    log[3]="Tipo Dato de Celda Inválido";
                    lista.add(log);
                    continue;

                }
                if(cellNitContraparte.isEmpty() || cellNitContraparte.isBlank() ||cellNitContraparte.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(23)+" - (24)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellFuturos.isEmpty() || cellFuturos.isBlank())
                {
                    log[1]=CellReference.convertNumToColString(0)+" - (1)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellContraparte.isEmpty() || cellContraparte.isBlank() ||cellContraparte.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(1)+" - (2)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellFAlta.isEmpty() || cellFAlta.isBlank() )
                {
                    log[1]=CellReference.convertNumToColString(5)+" - (6)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellFVencimiento.isEmpty() || cellFVencimiento.isBlank() )
                {
                    log[1]=CellReference.convertNumToColString(6)+" - (7)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellDivisa.isEmpty() || cellDivisa.isBlank() ||cellDivisa.length()!=3)
                {
                    log[1]=CellReference.convertNumToColString(29)+" - (30)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellVrNominalDivisa.isEmpty() || cellVrNominalDivisa.isBlank() ||cellVrNominalDivisa.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(8)+" - (9)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellMtmCop.isEmpty() || cellMtmCop.isBlank())
                {
                    log[1]=CellReference.convertNumToColString(56)+" - (57)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellVrNominalCop.isEmpty() || cellVrNominalCop.isBlank() ||cellVrNominalCop.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(64)+" - (65)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    fail++;
                    lista.add(log);
                }
                else if(cellLocalRp21.isEmpty() || cellLocalRp21.isBlank() || cellLocalRp21.length()>18|| cellLocalRp21.length()<4)
                {
                    log[1]=CellReference.convertNumToColString(6)+" - (7)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellDivRp21.isEmpty() || cellDivRp21.isBlank() ||cellDivRp21.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(63)+" - (64)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellLocalDerec.isEmpty() || cellLocalDerec.isBlank() ||cellLocalDerec.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(38)+" - (39)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellLocalObligacion.isEmpty() || cellLocalObligacion.isBlank() ||cellLocalObligacion.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(42)+" - (43)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellPyG.isEmpty() || cellPyG.isBlank() ||cellPyG.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(54)+" - (55)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellTipoContraparte.isEmpty() || cellTipoContraparte.isBlank() ||cellTipoContraparte.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(28)+" - (29)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellOperacion.isEmpty() || cellOperacion.isBlank() ||cellOperacion.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(3)+" - (4)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellDvsaConciliacion1.isEmpty() || cellDvsaConciliacion1.isBlank() ||cellDvsaConciliacion1.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(39)+" - (40)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellDvsaConciliacion2.isEmpty() || cellDvsaConciliacion2.isBlank() ||cellDvsaConciliacion2.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(43)+" - (44)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellNegocio.isEmpty() || cellNegocio.isBlank() ||cellNegocio.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(2)+" - (3)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellFecont.isEmpty() || cellFecont.isBlank() ||  Integer.parseInt(loadDate[0])!=(calendar.get(Calendar.YEAR)) ||  Integer.parseInt(loadDate[1])!=(calendar.get(Calendar.MONTH) + 1))
                {
                    log[1]=CellReference.convertNumToColString(36)+" - (37)";
                    log[2]="false";
                    log[3]="Fecha de Cargue documento no corresponde al periodo";
                    fail++;
                    lista.add(log);
                }
                else if(fail == 0)
                {
                    SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
                    formato.applyPattern("yyyy-MM-dd");

                    Rp21Temporal dolares = new Rp21Temporal();
                    dolares.setNit(cellNitContraparte);
                    dolares.setClaseFuturos(cellFuturos);
                    dolares.setNitOriginal(cellNitContraparteOriginal);
                    dolares.setContraparte(cellContraparte);
                    dolares.setFAlta(formato.parse(cellFAlta));
                    dolares.setFVenciemiento(formato.parse(cellFVencimiento));
                    dolares.setDivisa(cellDivisa);
                    dolares.setVrNominalDivisa(Double.parseDouble(cellVrNominalDivisa));
                    dolares.setMtmCOP(Double.parseDouble(cellMtmCop)*-1);
                    dolares.setVrNominalCOP(Double.parseDouble(cellVrNominalCop));
                    dolares.setLocalRp21(cellLocalRp21);
                    dolares.setDivrp21(cellDivRp21);
                    dolares.setLocalDerec(cellLocalDerec);
                    dolares.setLocalObligacion(cellLocalObligacion);
                    dolares.setPyg(cellPyG);
                    dolares.setTipoContraparte(cellTipoContraparte);
                    dolares.setOperacion(cellOperacion);
                    dolares.setDvsaconciliacion1(cellDvsaConciliacion1);
                    dolares.setDvsaconciliacion2(cellDvsaConciliacion2);
                    dolares.setFecont(formato.parse(cellFecont));
                    dolares.setNegocio(cellNegocio);

                    if((Double.parseDouble(cellMtmCop)*-1)<0)
                    {
                        dolares.setVrNominalMtm(Double.parseDouble(cellVrNominalCop));
                    }
                    else
                    {
                        dolares.setVrNominalMtm(Double.parseDouble(cellVrNominalCop)+(Double.parseDouble(cellMtmCop)*-1));
                    }

                    dolares.setOrigen("FORWARD");

                    rp21TemporalList.add(dolares);

                    success++;
                }
            }
            else
            {
                firstRow=0;
            }
        }
        String[] logFinal=new String[4];
        logFinal[0]="FW DOLARES";
        logFinal[1]=String.valueOf(success);
        logFinal[2]=String.valueOf(fail);
        logFinal[3]="true";
        lista.add(logFinal);

        if(fail>0)
        {
            failForward = true;
            Date today=new Date();
            Audit insert = new Audit();
            insert.setAccion("Fallo Inserción Documento Derivados FORWARD (Dolares)");
            insert.setCentro(user.getCentro());
            insert.setComponente("DERIVADOS");
            insert.setFecha(today);
            insert.setInput("FORWARD");
            insert.setNombre(user.getPrimerNombre());
            insert.setUsuario(user.getUsuario());
            auditRepository.save(insert);
        }
        return lista;
    }



    public List<String[]> validarPlantillaFwConciliacionDivisas(Iterator<Row> rows, String mes, User user) throws ParseException {
        ArrayList<String[]> lista= new ArrayList<String[]>();
        XSSFRow row;
        int firstRow=1;
        int fail=0;
        int success =0;
        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(firstRow!=1 && row.getRowNum()>0)
            {
                String[] log=new String[4];
                log[2]="true";
                DataFormatter formatter = new DataFormatter();
                String cellNitContraparte = formatter.formatCellValue(row.getCell(29));
                String cellNitContraparteOriginal = formatter.formatCellValue(row.getCell(29));
                String cellContraparte = formatter.formatCellValue(row.getCell(1));
                String cellFAlta = formatter.formatCellValue(row.getCell(5));
                String cellFVencimiento = formatter.formatCellValue(row.getCell(6));
                String cellDivisa = formatter.formatCellValue(row.getCell(8));

                String cellVrNominalDivisa = "";
                String cellMtmCop = "";
                String cellVrNominalCop = "";

                String cellLocalRp21 = formatter.formatCellValue(row.getCell(67));
                String cellDivRp21 = formatter.formatCellValue(row.getCell(68));
                String cellLocalDerec = formatter.formatCellValue(row.getCell(43));
                String cellLocalObligacion = formatter.formatCellValue(row.getCell(47));
                String cellPyG = formatter.formatCellValue(row.getCell(59));
                String cellTipoContraparte = formatter.formatCellValue(row.getCell(36)).trim();
                String cellOperacion = formatter.formatCellValue(row.getCell(3));
                String cellDvsaConciliacion1 = formatter.formatCellValue(row.getCell(44));
                String cellDvsaConciliacion2 = formatter.formatCellValue(row.getCell(48));
                String cellFecont = formatter.formatCellValue(row.getCell(41));
                String cellNegocio = formatter.formatCellValue(row.getCell(2));

                log[0]=String.valueOf(row.getRowNum()+1);
                Date fechaDateC;
                Calendar calendar = Calendar.getInstance();
                String[] loadDate;

                try
                {
                    log[1]=CellReference.convertNumToColString(9)+" - (10)";
                    XSSFCell cell0= row.getCell(9);
                    cell0.setCellType(CellType.STRING);
                    cellVrNominalDivisa = formatter.formatCellValue(cell0).replace(" ", "");

                    log[1]=CellReference.convertNumToColString(61)+" - (62)";
                    XSSFCell cell1= row.getCell(61);
                    cell1.setCellType(CellType.STRING);
                    cellMtmCop = formatter.formatCellValue(cell1).replace(" ", "");

                    log[1]=CellReference.convertNumToColString(69)+" - (70)";
                    XSSFCell cell2= row.getCell(69);
                    cell2.setCellType(CellType.STRING);
                    cellVrNominalCop = formatter.formatCellValue(cell2).replace(" ", "");

                    log[1]=CellReference.convertNumToColString(67)+" - (68)"; Long.parseLong(cellLocalRp21);
                    log[1]=CellReference.convertNumToColString(29)+" - (30)"; cellNitContraparte = formatter.formatCellValue(row.getCell(29)).split("/")[1];
                    cellNitContraparte = cellNitContraparte.substring(0,cellNitContraparte.length()-1);
                    log[1]=CellReference.convertNumToColString(61)+" - (62)"; Double.parseDouble(cellMtmCop);
                    log[1]=CellReference.convertNumToColString(9)+" - (10)"; Double.parseDouble(cellVrNominalDivisa);
                    log[1]=CellReference.convertNumToColString(69)+" - (70)"; Double.parseDouble(cellVrNominalCop);
                    log[1]=CellReference.convertNumToColString(43)+" - (44)"; Long.parseLong(cellLocalDerec);
                    log[1]=CellReference.convertNumToColString(47)+" - (48)"; Long.parseLong(cellLocalObligacion);
                    log[1]=CellReference.convertNumToColString(59)+" - (60)"; Long.parseLong(cellPyG);
                    SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
                    formato.applyPattern("yyyy-MM-dd");
                    log[1]=CellReference.convertNumToColString(5)+" - (6)"; Date fechaDate = formato.parse(cellFAlta);
                    log[1]=CellReference.convertNumToColString(6)+" - (7)"; Date fechaDateV = formato.parse(cellFVencimiento);
                    log[1]=CellReference.convertNumToColString(43)+" - (42)"; fechaDateC = formato.parse(cellFecont);

                    calendar.setTime(fechaDateC);
                    loadDate=mes.split("-");
                }
                catch(Exception e){
                    fail++;
                    log[2]="false";
                    log[3]="Tipo Dato de Celda Inválido";
                    lista.add(log);
                    continue;

                }

                if(cellNitContraparte.isEmpty() || cellNitContraparte.isBlank() ||cellNitContraparte.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(29)+" - (30)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellContraparte.isEmpty() || cellContraparte.isBlank() ||cellContraparte.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(1)+" - (2)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellFAlta.isEmpty() || cellFAlta.isBlank() )
                {
                    log[1]=CellReference.convertNumToColString(5)+" - (6)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellFVencimiento.isEmpty() || cellFVencimiento.isBlank() )
                {
                    log[1]=CellReference.convertNumToColString(6)+" - (7)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellDivisa.isEmpty() || cellDivisa.isBlank() ||cellDivisa.length()!=3)
                {
                    log[1]=CellReference.convertNumToColString(8)+" - (9)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellVrNominalDivisa.isEmpty() || cellVrNominalDivisa.isBlank() ||cellVrNominalDivisa.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(9)+" - (10)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellMtmCop.isEmpty() || cellMtmCop.isBlank())
                {
                    log[1]=CellReference.convertNumToColString(61)+" - (62)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellVrNominalCop.isEmpty() || cellVrNominalCop.isBlank() ||cellVrNominalCop.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(69)+" - (70)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellNegocio.isEmpty() || cellNegocio.isBlank() ||cellNegocio.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(2)+" - (3)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellLocalRp21.isEmpty() || cellLocalRp21.isBlank() || cellLocalRp21.length()>18|| cellLocalRp21.length()<4)
                {
                    log[1]=CellReference.convertNumToColString(67)+" - (68)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellDivRp21.isEmpty() || cellDivRp21.isBlank() ||cellDivRp21.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(68)+" - (69)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellLocalDerec.isEmpty() || cellLocalDerec.isBlank() ||cellLocalDerec.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(43)+" - (44)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellLocalObligacion.isEmpty() || cellLocalObligacion.isBlank() ||cellLocalObligacion.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(47)+" - (48)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellPyG.isEmpty() || cellPyG.isBlank() ||cellPyG.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(59)+" - (60)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellTipoContraparte.isEmpty() || cellTipoContraparte.isBlank() ||cellTipoContraparte.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(36)+" - (37)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellOperacion.isEmpty() || cellOperacion.isBlank() ||cellOperacion.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(3)+" - (4)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellDvsaConciliacion1.isEmpty() || cellDvsaConciliacion1.isBlank() ||cellDvsaConciliacion1.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(44)+" - (45)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellDvsaConciliacion2.isEmpty() || cellDvsaConciliacion2.isBlank() ||cellDvsaConciliacion2.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(48)+" - (49)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellFecont.isEmpty() || cellFecont.isBlank() ||  Integer.parseInt(loadDate[0])!=(calendar.get(Calendar.YEAR)) ||  Integer.parseInt(loadDate[1])!=(calendar.get(Calendar.MONTH) + 1))
                {
                    log[1]=CellReference.convertNumToColString(41)+" - (42)";
                    log[2]="false";
                    log[3]="Fecha de Cargue documento no corresponde al periodo";
                    fail++;
                    lista.add(log);
                }
                else if(fail == 0)
                {
                    SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
                    formato.applyPattern("yyyy-MM-dd");

                    Rp21Temporal divisas = new Rp21Temporal();
                    divisas.setNit(cellNitContraparte);
                    divisas.setNitOriginal(cellNitContraparteOriginal);
                    divisas.setContraparte(cellContraparte);
                    divisas.setFAlta(formato.parse(cellFAlta));
                    divisas.setFVenciemiento(formato.parse(cellFVencimiento));
                    divisas.setDivisa(cellDivisa);
                    divisas.setVrNominalDivisa(Double.parseDouble(cellVrNominalDivisa));
                    divisas.setMtmCOP(Double.parseDouble(cellMtmCop)*-1);
                    divisas.setVrNominalCOP(Double.parseDouble(cellVrNominalCop));
                    divisas.setLocalRp21(cellLocalRp21);
                    divisas.setDivrp21(cellDivRp21);
                    divisas.setLocalDerec(cellLocalDerec);
                    divisas.setLocalObligacion(cellLocalObligacion);
                    divisas.setPyg(cellPyG);
                    divisas.setTipoContraparte(cellTipoContraparte);
                    divisas.setOperacion(cellOperacion);
                    divisas.setDvsaconciliacion1(cellDvsaConciliacion1);
                    divisas.setDvsaconciliacion2(cellDvsaConciliacion2);
                    divisas.setFecont(formato.parse(cellFecont));
                    divisas.setNegocio(cellNegocio);

                    if((Double.parseDouble(cellMtmCop)*-1)<0)
                    {
                        divisas.setVrNominalMtm(Double.parseDouble(cellVrNominalCop));
                    }
                    else
                    {
                        divisas.setVrNominalMtm(Double.parseDouble(cellVrNominalCop)+(Double.parseDouble(cellMtmCop)*-1));
                    }

                    divisas.setOrigen("FORWARD");

                    rp21TemporalList.add(divisas);

                    success++;
                    log[2]="true";
                }
            }
            else
            {
                firstRow=0;
            }
        }
        String[] logFinal=new String[4];
        logFinal[0]="FW DIVISAS";
        logFinal[1]=String.valueOf(success);
        logFinal[2]=String.valueOf(fail);
        logFinal[3]="true";
        lista.add(logFinal);

        if(fail>0)
        {
            failForward = true;
            Date today=new Date();
            Audit insert = new Audit();
            insert.setAccion("Fallo Inserción Documento Derivados FORWARD (Divisas)");
            insert.setCentro(user.getCentro());
            insert.setComponente("DERIVADOS");
            insert.setFecha(today);
            insert.setInput("FORWARD");
            insert.setNombre(user.getPrimerNombre());
            insert.setUsuario(user.getUsuario());
            auditRepository.save(insert);
        }

        return lista;
    }

    public List<String[]> validarPlantillaFwConciliacionTitulos(Iterator<Row> rows, String mes,User user) throws ParseException {
        ArrayList<String[]> lista= new ArrayList<String[]>();
        XSSFRow row;
        int firstRow=1;
        int fail=0;
        int success =0;
        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(firstRow!=1 && row.getRowNum()>0)
            {
                String[] log=new String[4];
                log[2]="true";
                DataFormatter formatter = new DataFormatter();
                String cellNitContraparte = formatter.formatCellValue(row.getCell(30));
                String cellNitContraparteOriginal = formatter.formatCellValue(row.getCell(30));
                String cellContraparte = formatter.formatCellValue(row.getCell(7));
                String cellFAlta = formatter.formatCellValue(row.getCell(2));
                String cellFVencimiento = formatter.formatCellValue(row.getCell(1));
                String cellDivisa = formatter.formatCellValue(row.getCell(20));

                String cellVrNominalDivisa = "";
                String cellMtmCop = "";
                String cellVrNominalCop = "";

                String cellLocalRp21 = formatter.formatCellValue(row.getCell(70));
                String cellDivRp21 = formatter.formatCellValue(row.getCell(71));
                String cellLocalDerec = formatter.formatCellValue(row.getCell(46));
                String cellLocalObligacion = formatter.formatCellValue(row.getCell(50));
                String cellPyG = formatter.formatCellValue(row.getCell(62));
                String cellTipoContraparte = formatter.formatCellValue(row.getCell(34)).trim();
                String cellOperacion = formatter.formatCellValue(row.getCell(4));
                String cellDvsaConciliacion1 = formatter.formatCellValue(row.getCell(47));
                String cellDvsaConciliacion2 = formatter.formatCellValue(row.getCell(51)).trim();
                String cellFecont = formatter.formatCellValue(row.getCell(44));
                String cellNegocio = formatter.formatCellValue(row.getCell(5));
                log[0]=String.valueOf(row.getRowNum()+1);
                Date fechaDateC;
                Calendar calendar = Calendar.getInstance();
                String[] loadDate;

                try
                {
                    log[1]=CellReference.convertNumToColString(21)+" - (22)";
                    XSSFCell cell0= row.getCell(21);
                    cell0.setCellType(CellType.STRING);
                    cellVrNominalDivisa = formatter.formatCellValue(cell0).replace(" ", "");

                    log[1]=CellReference.convertNumToColString(64)+" - (65)";
                    XSSFCell cell1= row.getCell(64);
                    cell1.setCellType(CellType.STRING);
                    cellMtmCop = formatter.formatCellValue(cell1).replace(" ", "");

                    log[1]=CellReference.convertNumToColString(72)+" - (73)";
                    XSSFCell cell2= row.getCell(72);
                    cell2.setCellType(CellType.STRING);
                    cellVrNominalCop = formatter.formatCellValue(cell2).replace(" ", "");

                    log[1]=CellReference.convertNumToColString(70)+" - (71)"; Long.parseLong(cellLocalRp21);
                    log[1]=CellReference.convertNumToColString(64)+" - (65)"; Double.parseDouble(cellMtmCop);
                    log[1]=CellReference.convertNumToColString(21)+" - (22)"; Double.parseDouble(cellVrNominalDivisa);
                    log[1]=CellReference.convertNumToColString(30)+" - (31)"; cellNitContraparte = formatter.formatCellValue(row.getCell(30)).split("/")[1];
                    log[1]=CellReference.convertNumToColString(72)+" - (73)"; Double.parseDouble(cellVrNominalCop);
                    log[1]=CellReference.convertNumToColString(46)+" - (47)"; Long.parseLong(cellLocalDerec);
                    if(cellLocalObligacion.length()>0)
                    {log[1]=CellReference.convertNumToColString(50)+" - (51)"; Long.parseLong(cellLocalObligacion);}
                    log[1]=CellReference.convertNumToColString(62)+" - (63)"; Long.parseLong(cellPyG);
                    SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
                    formato.applyPattern("yyyy-MM-dd");
                    log[1]=CellReference.convertNumToColString(2)+" - (3)"; Date fechaDate = formato.parse(cellFAlta);
                    log[1]=CellReference.convertNumToColString(1)+" - (2)"; Date fechaDateV = formato.parse(cellFVencimiento);
                    log[1]=CellReference.convertNumToColString(44)+" - (45)"; fechaDateC = formato.parse(cellFecont);

                    calendar.setTime(fechaDateC);
                    loadDate=mes.split("-");
                }
                catch(Exception e){
                    fail++;
                    log[2]="false";
                    log[3]="Tipo Dato de Celda Inválido";
                    lista.add(log);
                    continue;
                }

                if(cellNitContraparte.isEmpty() || cellNitContraparte.isBlank() ||cellNitContraparte.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(30)+" - (31)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellContraparte.isEmpty() || cellContraparte.isBlank() ||cellContraparte.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(7)+" - (8)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellFAlta.isEmpty() || cellFAlta.isBlank() )
                {
                    log[1]=CellReference.convertNumToColString(2)+" - (3)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellFVencimiento.isEmpty() || cellFVencimiento.isBlank() )
                {
                    log[1]=CellReference.convertNumToColString(1)+" - (2)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellDivisa.isEmpty() || cellDivisa.isBlank() ||cellDivisa.length()!=3)
                {
                    log[1]=CellReference.convertNumToColString(20)+" - (21)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellVrNominalDivisa.isEmpty() || cellVrNominalDivisa.isBlank() ||cellVrNominalDivisa.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(21)+" - (22)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellMtmCop.isEmpty() || cellMtmCop.isBlank())
                {
                    log[1]=CellReference.convertNumToColString(64)+" - (65)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellVrNominalCop.isEmpty() || cellVrNominalCop.isBlank() ||cellVrNominalCop.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(72)+" - (73)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellLocalRp21.isEmpty() || cellLocalRp21.isBlank() || cellLocalRp21.length()>18|| cellLocalRp21.length()<4)
                {
                    log[1]=CellReference.convertNumToColString(70)+" - (71)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellDivRp21.isEmpty() || cellDivRp21.isBlank() ||cellDivRp21.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(71)+" - (72)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellLocalDerec.isEmpty() || cellLocalDerec.isBlank() ||cellLocalDerec.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(46)+" - (47)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellLocalObligacion.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(50)+" - (51)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellPyG.isEmpty() || cellPyG.isBlank() ||cellPyG.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(62)+" - (63)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellTipoContraparte.isEmpty() || cellTipoContraparte.isBlank() ||cellTipoContraparte.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(34)+" - (35)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellNegocio.isEmpty() || cellNegocio.isBlank() ||cellNegocio.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(5)+" - (6)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellOperacion.isEmpty() || cellOperacion.isBlank() ||cellOperacion.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(4)+" - (5)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellDvsaConciliacion1.isEmpty() || cellDvsaConciliacion1.isBlank() ||cellDvsaConciliacion1.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(47)+" - (48)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellDvsaConciliacion2.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(51)+" - (52)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellFecont.isEmpty() || cellFecont.isBlank() ||  Integer.parseInt(loadDate[0])!=(calendar.get(Calendar.YEAR)) ||  Integer.parseInt(loadDate[1])!=(calendar.get(Calendar.MONTH) + 1))
                {
                    log[1]=CellReference.convertNumToColString(44)+" - (45)";
                    log[2]="false";
                    log[3]="Fecha de Cargue documento no corresponde al periodo";
                    fail++;
                    lista.add(log);
                }
                else if(fail == 0)
                {
                    SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
                    formato.applyPattern("yyyy-MM-dd");

                    Rp21Temporal titulos = new Rp21Temporal();
                    titulos.setNit(cellNitContraparte);
                    titulos.setNitOriginal(cellNitContraparteOriginal);
                    titulos.setContraparte(cellContraparte);
                    titulos.setFAlta(formato.parse(cellFAlta));
                    titulos.setFVenciemiento(formato.parse(cellFVencimiento));
                    titulos.setDivisa(cellDivisa);
                    titulos.setVrNominalDivisa(Double.parseDouble(cellVrNominalDivisa));
                    titulos.setMtmCOP(Double.parseDouble(cellMtmCop)*-1);
                    titulos.setVrNominalCOP(Double.parseDouble(cellVrNominalCop));
                    titulos.setLocalRp21(cellLocalRp21);
                    titulos.setDivrp21(cellDivRp21);
                    titulos.setLocalDerec(cellLocalDerec);
                    titulos.setLocalObligacion(cellLocalObligacion);
                    titulos.setPyg(cellPyG);
                    titulos.setTipoContraparte(cellTipoContraparte);
                    titulos.setOperacion(cellOperacion);
                    titulos.setDvsaconciliacion1(cellDvsaConciliacion1);
                    titulos.setDvsaconciliacion2(cellDvsaConciliacion2);
                    titulos.setFecont(formato.parse(cellFecont));
                    titulos.setNegocio(cellNegocio);

                    if((Double.parseDouble(cellMtmCop)*-1)<0)
                    {
                        titulos.setVrNominalMtm(Double.parseDouble(cellVrNominalCop));
                    }
                    else
                    {
                        titulos.setVrNominalMtm(Double.parseDouble(cellVrNominalCop)+(Double.parseDouble(cellMtmCop)*-1));
                    }

                    titulos.setOrigen("FORWARD");

                    rp21TemporalList.add(titulos);

                    success++;
                    log[2]="true";
                }
            }
            else
            {
                firstRow=0;
            }
        }
        String[] logFinal=new String[4];
        logFinal[0]="FW TITULOS";
        logFinal[1]=String.valueOf(success);
        logFinal[2]=String.valueOf(fail);
        logFinal[3]="true";
        lista.add(logFinal);

        if(fail>0)
        {
            failForward = true;
            Date today=new Date();
            Audit insert = new Audit();
            insert.setAccion("Fallo Inserción Documento Derivados FORWARD (Titulos)");
            insert.setCentro(user.getCentro());
            insert.setComponente("DERIVADOS");
            insert.setFecha(today);
            insert.setInput("FORWARD");
            insert.setNombre(user.getPrimerNombre());
            insert.setUsuario(user.getUsuario());
            auditRepository.save(insert);
        }

        return lista;
    }

    public List<String[]> validarPlantillaFwConciliacionSwap(Iterator<Row> rows, String mes,User user) throws ParseException {
        ArrayList<String[]> lista= new ArrayList<String[]>();
        XSSFRow row;
        int firstRow=1;
        int fail=0;
        int success =0;
        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(firstRow!=1 && row.getRowNum()>0)
            {
                String[] log=new String[4];
                log[2]="true";
                DataFormatter formatter = new DataFormatter();
                String cellNitContraparte =null;

                String cellNitContraparteOriginal = formatter.formatCellValue(row.getCell(2));
                String cellContraparte = formatter.formatCellValue(row.getCell(3));
                String cellFAlta = formatter.formatCellValue(row.getCell(15));
                String cellFVencimiento = formatter.formatCellValue(row.getCell(16));
                String cellDivisaDrMoneda = formatter.formatCellValue(row.getCell(4));
                String cellDivisaOblMoneda = formatter.formatCellValue(row.getCell(6));

                String cellVrNominalDivisa = "";
                String cellMtmCop = "";
                String cellVrNominalCop = "";

                String cellLocalRp21 = formatter.formatCellValue(row.getCell(136));
                String cellDivRp21 = formatter.formatCellValue(row.getCell(137));
                String cellLocalDerec = formatter.formatCellValue(row.getCell(112));
                String cellLocalObligacion = formatter.formatCellValue(row.getCell(116));
                String cellPyG = formatter.formatCellValue(row.getCell(128));
                String cellTipoContraparte = formatter.formatCellValue(row.getCell(98)).trim();
                String cellOperacion = formatter.formatCellValue(row.getCell(1));
                String cellDvsaConciliacion1 = formatter.formatCellValue(row.getCell(113));
                String cellDvsaConciliacion2 = formatter.formatCellValue(row.getCell(117));
                String cellFecont = formatter.formatCellValue(row.getCell(110));

                log[0]=String.valueOf(row.getRowNum()+1);
                Date fechaDateC;
                Calendar calendar = Calendar.getInstance();
                String[] loadDate;

                try
                {
                    log[1]=CellReference.convertNumToColString(5)+" - (6)";
                    XSSFCell cell0= row.getCell(5);
                    cell0.setCellType(CellType.STRING);
                    cellVrNominalDivisa = formatter.formatCellValue(cell0).replace(" ", "");

                    log[1]=CellReference.convertNumToColString(130)+" - (131)";
                    XSSFCell cell1= row.getCell(130);
                    cell1.setCellType(CellType.STRING);
                    cellMtmCop = formatter.formatCellValue(cell1).replace(" ", "");

                    log[1]=CellReference.convertNumToColString(138)+" - (139)";
                    XSSFCell cell2= row.getCell(138);
                    cell2.setCellType(CellType.STRING);
                    cellVrNominalCop = formatter.formatCellValue(cell2).replace(" ", "");

                    log[1]=CellReference.convertNumToColString(136)+" - (137)"; Long.parseLong(cellLocalRp21);
                    log[1]=CellReference.convertNumToColString(2)+" - (3)"; cellNitContraparte = formatter.formatCellValue(row.getCell(2)).split("/")[1];
                    cellNitContraparte = cellNitContraparte.substring(0,cellNitContraparte.length()-1);
                    log[1]=CellReference.convertNumToColString(130)+" - (131)"; Double.parseDouble(cellMtmCop);
                    log[1]=CellReference.convertNumToColString(5)+" - (6)"; Double.parseDouble(cellVrNominalDivisa);
                    log[1]=CellReference.convertNumToColString(138)+" - (139)"; Double.parseDouble(cellVrNominalCop);
                    log[1]=CellReference.convertNumToColString(112)+" - (113)"; Long.parseLong(cellLocalDerec);
                    log[1]=CellReference.convertNumToColString(116)+" - (117)"; Long.parseLong(cellLocalObligacion);
                    log[1]=CellReference.convertNumToColString(128)+" - (129)"; Long.parseLong(cellPyG);
                    SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
                    formato.applyPattern("yyyy-MM-dd");
                    if(cellFAlta.length()!=0 && !cellFAlta.equals("--")) {
                        log[1] = CellReference.convertNumToColString(15) + " - (16)";
                        Date fechaDate = formato.parse(cellFAlta);
                    }
                    log[1]=CellReference.convertNumToColString(16)+" - (17)"; Date fechaDateV = formato.parse(cellFVencimiento);
                    //log[1]=CellReference.convertNumToColString(110)+" - (111)"; fechaDateC = formato.parse(cellFecont);

                    //calendar.setTime(fechaDateC);
                    loadDate=mes.split("-");
                }
                catch(Exception e){
                    fail++;
                    log[2]="false";
                    log[3]="Tipo Dato de Celda Inválido";
                    lista.add(log);
                    continue;
                }

                if(cellNitContraparte.isEmpty() || cellNitContraparte.isBlank() ||cellNitContraparte.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(2)+" - (3)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellContraparte.isEmpty() || cellContraparte.isBlank() ||cellContraparte.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(3)+" - (4)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                /*else if(cellFAlta.isEmpty() || cellFAlta.isBlank() )
                {
                    log[1]=CellReference.convertNumToColString(46)+" - (47)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }*/
                else if(cellFVencimiento.isEmpty() || cellFVencimiento.isBlank() )
                {
                    log[1]=CellReference.convertNumToColString(16)+" - (17)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellDivisaDrMoneda.isEmpty() || cellDivisaDrMoneda.isBlank() ||cellDivisaDrMoneda.length()!=3)
                {
                    log[1]=CellReference.convertNumToColString(20)+" - (21)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellVrNominalDivisa.isEmpty() || cellVrNominalDivisa.isBlank() ||cellVrNominalDivisa.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(4)+" - (5)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellDivisaOblMoneda.isEmpty() || cellDivisaOblMoneda.isBlank() ||cellDivisaOblMoneda.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(6)+" - (7)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellMtmCop.isEmpty() || cellMtmCop.isBlank())
                {
                    log[1]=CellReference.convertNumToColString(130)+" - (131)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellVrNominalCop.isEmpty() || cellVrNominalCop.isBlank() ||cellVrNominalCop.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(138)+" - (138)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellLocalRp21.isEmpty() || cellLocalRp21.isBlank() || cellLocalRp21.length()>18|| cellLocalRp21.length()<4)
                {
                    log[1]=CellReference.convertNumToColString(136)+" - (137)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellDivRp21.isEmpty() || cellDivRp21.isBlank() ||cellDivRp21.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(137)+" - (138)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellLocalDerec.isEmpty() || cellLocalDerec.isBlank() ||cellLocalDerec.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(112)+" - (113)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellLocalObligacion.isEmpty() || cellLocalObligacion.isBlank() ||cellLocalObligacion.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(116)+" - (117)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellPyG.isEmpty() || cellPyG.isBlank() ||cellPyG.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(128)+" - (129)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellTipoContraparte.isEmpty() || cellTipoContraparte.isBlank() ||cellTipoContraparte.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(98)+" - (99)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellOperacion.isEmpty() || cellOperacion.isBlank() ||cellOperacion.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(1)+" - (2)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellDvsaConciliacion1.isEmpty() || cellDvsaConciliacion1.isBlank() ||cellDvsaConciliacion1.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(113)+" - (114)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellDvsaConciliacion2.isEmpty() || cellDvsaConciliacion2.isBlank() ||cellDvsaConciliacion2.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(117)+" - (118)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                /*else if(cellFecont.length()!=0 &&  Integer.parseInt(loadDate[0])!=(calendar.get(Calendar.YEAR)) ||  Integer.parseInt(loadDate[1])!=(calendar.get(Calendar.MONTH) + 1))
                {
                    log[1]=CellReference.convertNumToColString(110)+" - (111)";
                    log[2]="false";
                    log[3]="Fecha de Cargue documento no corresponde al periodo";
                    fail++;
                    lista.add(log);
                }*/
                else if(fail == 0)
                {
                    SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
                    formato.applyPattern("yyyy-MM-dd");

                    Date fechaDateA = new Date();
                    if(cellFAlta.length()!=0 && !cellFAlta.equals("--"))
                        fechaDateA = formato.parse(cellFAlta);
                    Date fechaDateV = formato.parse(cellFVencimiento);

                    Rp21Temporal swap = new Rp21Temporal();
                    swap.setNit(cellNitContraparte);
                    swap.setNitOriginal(cellNitContraparteOriginal);
                    swap.setContraparte(cellContraparte);
                    swap.setFVenciemiento(formato.parse(cellFVencimiento));
                    swap.setVrNominalDivisa(Double.parseDouble(cellVrNominalDivisa));
                    swap.setMtmCOP(Double.parseDouble(cellMtmCop)*-1);
                    swap.setVrNominalCOP(Double.parseDouble(cellVrNominalCop));
                    swap.setLocalRp21(cellLocalRp21);
                    swap.setDivrp21(cellDivRp21);
                    swap.setLocalDerec(cellLocalDerec);
                    swap.setLocalObligacion(cellLocalObligacion);
                    swap.setPyg(cellPyG);
                    swap.setTipoContraparte(cellTipoContraparte);
                    swap.setOperacion(cellOperacion);
                    swap.setDvsaconciliacion1(cellDvsaConciliacion1);
                    swap.setDvsaconciliacion2(cellDvsaConciliacion2);
                    swap.setFecont(formato.parse(cellFecont));
                    swap.setNegocio("Compra");

                    if((Double.parseDouble(cellMtmCop)*-1)<0)
                    {
                        swap.setVrNominalMtm(Double.parseDouble(cellVrNominalCop));
                    }
                    else
                    {
                        swap.setVrNominalMtm(Double.parseDouble(cellVrNominalCop)+(Double.parseDouble(cellMtmCop)*-1));
                    }

                    if(cellFAlta.length()!=0 && !cellFAlta.equals("--"))
                        swap.setFAlta(fechaDateA);
                        swap.setFVenciemiento(fechaDateV);

                    if(cellDivisaDrMoneda.equals(cellDivisaOblMoneda))
                    {
                        swap.setDivisa(cellDivisaDrMoneda);
                    }
                    else if(!cellDivisaDrMoneda.equals("COP"))
                    {
                        swap.setDivisa(cellDivisaDrMoneda);
                    }else{
                        swap.setDivisa(cellDivisaOblMoneda);
                    }

                    swap.setOrigen("SWAP");

                    rp21TemporalList.add(swap);

                    success++;
                    log[2]="true";
                }
            }
            else
            {
                firstRow=0;
            }
        }
        String[] logFinal=new String[4];
        logFinal[0]="SWAP";
        logFinal[1]=String.valueOf(success);
        logFinal[2]=String.valueOf(fail);
        logFinal[3]="true";
        lista.add(logFinal);

        if(fail>0)
        {
            failSwap = true;
            Date today=new Date();
            Audit insert = new Audit();
            insert.setAccion("Fallo Inserción Documento Derivados SWAP");
            insert.setCentro(user.getCentro());
            insert.setComponente("DERIVADOS");
            insert.setFecha(today);
            insert.setInput("SWAP");
            insert.setNombre(user.getPrimerNombre());
            insert.setUsuario(user.getUsuario());
            auditRepository.save(insert);
        }

        return lista;
    }

    public List<String[]> validarPlantillaRySConciliacion(Iterator<Row> rows, String mes,User user) throws ParseException {
        ArrayList<String[]> lista= new ArrayList<String[]>();
        XSSFRow row;
        int firstRow=1;
        int fail=0;
        int success =0;
        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(firstRow!=1 && row.getRowNum()>0)
            {
                String[] log=new String[4];
                log[2]="true";
                DataFormatter formatter = new DataFormatter();
                String cellNitContraparte ="";
                String cellNitContraparteOriginal ="";
                try {
                    cellNitContraparteOriginal = formatter.formatCellValue(row.getCell(21));
                    cellNitContraparte = formatter.formatCellValue(row.getCell(21)).split("/")[1];
                    cellNitContraparte = cellNitContraparte.substring(0,cellNitContraparte.length()-1);
                }
                catch(Exception e)
                {
                    continue;
                }
                String cellContraparte = formatter.formatCellValue(row.getCell(20));
                String cellFAlta = formatter.formatCellValue(row.getCell(4));
                String cellFVencimiento = formatter.formatCellValue(row.getCell(5));
                String cellDivisa = formatter.formatCellValue(row.getCell(23));

                XSSFCell cell0= row.getCell(56);
                cell0.setCellType(CellType.STRING);
                String cellVrNominalDivisa = formatter.formatCellValue(cell0).replace(" ", "");


                //String cellMtmCop = formatter.formatCellValue(row.getCell(64)).replace(" ", "");
                String cellMtmCop = "0";
                /*if(cellMtmCop.length()!=0) {
                    XSSFCell cell1 = row.getCell(64);
                    cell1.setCellType(CellType.STRING);
                    cellMtmCop = formatter.formatCellValue(cell1).replace(" ", "");
                }
                if(cellMtmCop.trim().length()==1)
                {
                    cellMtmCop.replace("-","0");
                }*/
                XSSFCell cell2= row.getCell(56); // Se meodifico de 37 a 56 por hallazgo
                cell2.setCellType(CellType.STRING);
                String cellVrNominalCop = formatter.formatCellValue(cell2).replace(" ", "");

                String cellLocalDerec = formatter.formatCellValue(row.getCell(54));
                String cellCentroContable = formatter.formatCellValue(row.getCell(53));
                String cellLocalObligacion = formatter.formatCellValue(row.getCell(58));
                String cellPyG = formatter.formatCellValue(row.getCell(62)).trim();
                String cellTipoContraparte = formatter.formatCellValue(row.getCell(25)).trim();
                String cellOperacion = formatter.formatCellValue(row.getCell(12));
                String cellDvsaConciliacion1 = formatter.formatCellValue(row.getCell(55));
                String cellDvsaConciliacion2 = formatter.formatCellValue(row.getCell(59));
                String cellFecont = formatter.formatCellValue(row.getCell(52));
                log[0]=String.valueOf(row.getRowNum()+1);
                Date fechaDateC;
                Calendar calendar = Calendar.getInstance();
                String[] loadDate;

                try
                {
                    if(cellMtmCop.trim().length()!=0) {
                        log[1] = CellReference.convertNumToColString(64) + " - (65)";
                        //Double.parseDouble(cellMtmCop);
                    }
                    log[1]=CellReference.convertNumToColString(56)+" - (57)";
                    log[1]=CellReference.convertNumToColString(37)+" - (38)"; Double.parseDouble(cellVrNominalCop);
                    log[1]=CellReference.convertNumToColString(54)+" - (55)"; Long.parseLong(cellLocalDerec);
                    if(cellLocalObligacion.length()!=0) {
                        log[1] = CellReference.convertNumToColString(58) + " - (59)";
                        Long.parseLong(cellLocalObligacion);
                    }
                    if(cellPyG.length()!=0) {
                        log[1] = CellReference.convertNumToColString(62) + " - (63)";
                        Long.parseLong(cellPyG);
                    }
                    SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
                    formato.applyPattern("yyyy-MM-dd");
                    log[1]=CellReference.convertNumToColString(4)+" - (5)"; Date fechaDate = formato.parse(cellFAlta);
                    log[1]=CellReference.convertNumToColString(5)+" - (6)"; Date fechaDateV = formato.parse(cellFVencimiento);
                    log[1]=CellReference.convertNumToColString(52)+" - (53)"; fechaDateC = formato.parse(cellFecont);

                    calendar.setTime(fechaDateC);
                    loadDate=mes.split("-");

                }
                catch(Exception e){
                    fail++;
                    log[2]="false";
                    log[3]="Tipo Dato de Celda Inválido";
                    lista.add(log);
                    continue;
                }

                if(cellNitContraparte.isEmpty() || cellNitContraparte.isBlank() ||cellNitContraparte.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(21)+" - (22)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellContraparte.isEmpty() || cellContraparte.isBlank() ||cellContraparte.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(20)+" - (21)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellFAlta.isEmpty() || cellFAlta.isBlank() )
                {
                    log[1]=CellReference.convertNumToColString(4)+" - (5)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellFVencimiento.isEmpty() || cellFVencimiento.isBlank() )
                {
                    log[1]=CellReference.convertNumToColString(5)+" - (6)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellDivisa.isEmpty() || cellDivisa.isBlank() ||cellDivisa.length()!=3)
                {
                    log[1]=CellReference.convertNumToColString(23)+" - (24)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellVrNominalDivisa.isEmpty() || cellVrNominalDivisa.isBlank() ||cellVrNominalDivisa.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(56)+" - (57)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellVrNominalCop.isEmpty() || cellVrNominalCop.isBlank() ||cellVrNominalCop.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(37)+" - (38)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellLocalDerec.isEmpty() || cellLocalDerec.isBlank() ||cellLocalDerec.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(54)+" - (55)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellLocalObligacion.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(58)+" - (59)";
                    log[2]="false";
                    log[3]="Falló en validación de campo.";
                    fail++;
                    lista.add(log);
                }
                else if(cellPyG.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(62)+" - (63)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellTipoContraparte.isEmpty() || cellTipoContraparte.isBlank() ||cellTipoContraparte.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(25)+" - (26)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellOperacion.isEmpty() || cellOperacion.isBlank() ||cellOperacion.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(12)+" - (13)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellDvsaConciliacion1.isEmpty() || cellDvsaConciliacion1.isBlank() ||cellDvsaConciliacion1.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(55)+" - (56)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if((cellLocalObligacion.length()==0) && (cellDvsaConciliacion2.length()!=0 || cellDvsaConciliacion2.length()>254))
                {
                    log[1]=CellReference.convertNumToColString(59)+" - (60)";
                    log[2]="false";
                    log[3]="Falló en validación. No se permite divisa con registro anterior vacío";
                    fail++;
                    lista.add(log);
                }
                else if((cellLocalObligacion.length()!=0) && (cellDvsaConciliacion2.length()==0 ||cellDvsaConciliacion2.length()>254))
                {
                    log[1]=CellReference.convertNumToColString(59)+" - (60)";
                    log[2]="false";
                    log[3]="Falló en validación. No se permite divisa vacía.";
                    fail++;
                    lista.add(log);
                }
                else if(cellFecont.length()!=0 &&  Integer.parseInt(loadDate[0])!=(calendar.get(Calendar.YEAR)) ||  Integer.parseInt(loadDate[1])!=(calendar.get(Calendar.MONTH) + 1))
                {
                    log[1]=CellReference.convertNumToColString(52)+" - (53)";
                    log[2]="false";
                    log[3]="Fecha de Cargue documento no corresponde al periodo";
                    fail++;
                    lista.add(log);
                }
                else if(fail == 0)
                {
                    SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
                    formato.applyPattern("yyyy-MM-dd");

                    Rp21Temporal rys = new Rp21Temporal();
                    rys.setNit(cellNitContraparte);
                    rys.setNitOriginal(cellNitContraparteOriginal);
                    rys.setContraparte(cellContraparte);
                    rys.setFAlta(formato.parse(cellFAlta));
                    rys.setFVenciemiento(formato.parse(cellFVencimiento));
                    rys.setVrNominalDivisa(Double.parseDouble(cellVrNominalDivisa));
                    rys.setVrNominalCOP(Double.parseDouble(cellVrNominalCop));
                    rys.setLocalRp21("-");
                    rys.setDivrp21("");
                    rys.setLocalDerec(cellLocalDerec);
                    rys.setLocalObligacion(cellLocalObligacion);
                    rys.setPyg(cellPyG);
                    rys.setTipoContraparte(cellTipoContraparte);
                    rys.setOperacion(cellOperacion);
                    rys.setDvsaconciliacion1(cellDvsaConciliacion1);
                    rys.setDvsaconciliacion2(cellDvsaConciliacion2);
                    rys.setFecont(formato.parse(cellFecont));
                    rys.setDivisa(cellDivisa);
                    rys.setCentroCuenta(cellCentroContable);

                    if(cellMtmCop.trim().length()>0) {
                        rys.setMtmCOP(Double.parseDouble(cellMtmCop)*-1);
                    }

                    if(cellMtmCop.isEmpty())
                    {
                        rys.setVrNominalMtm(0);
                    }else{
                        rys.setVrNominalMtm(Double.parseDouble(cellMtmCop)*-1);
                    }
                    rys.setOrigen("RYS");

                    rp21TemporalList.add(rys);

                    success++;
                    log[2]="true";
                }
            }
            else
            {
                firstRow=0;
            }
        }

        boolean divisas = validateDivisas(mes);

        if(!divisas){
            String[] log=new String[4];
            log[0]="General";
            log[1]="General";
            log[2]="false";
            log[3]="No se encuentran cargadas las divisas para la fecha";
            fail++;
            lista.add(log);
        }

        String[] logFinal=new String[4];
        logFinal[0]="RYS";
        logFinal[1]=String.valueOf(success);
        logFinal[2]=String.valueOf(fail);
        logFinal[3]="true";
        lista.add(logFinal);

        if(fail>0)
        {
            failRyS = true;
            Date today=new Date();
            Audit insert = new Audit();
            insert.setAccion("Fallo Inserción Documento Derivados RYS");
            insert.setCentro(user.getCentro());
            insert.setComponente("DERIVADOS");
            insert.setFecha(today);
            insert.setInput("RYS");
            insert.setNombre(user.getPrimerNombre());
            insert.setUsuario(user.getUsuario());
            auditRepository.save(insert);
        }

        return lista;
    }

    public List<String[]> validarPlantillaOpciones(Iterator<Row> rows, String mes,User user) throws ParseException {
        ArrayList<String[]> lista= new ArrayList<String[]>();
        //List<String> listColumns=List.of();
        XSSFRow row;
        int fail=0;
        int success =0;
        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(row.getRowNum()>0)
            {
                String[] log=new String[4];
                log[2]="true";
                DataFormatter formatter = new DataFormatter();
                String cellNitContraparte = formatter.formatCellValue(row.getCell(23));
                String cellNitContraparteOriginal = formatter.formatCellValue(row.getCell(23));
                String cellContraparte = formatter.formatCellValue(row.getCell(0));
                String cellFAlta = formatter.formatCellValue(row.getCell(2));
                String cellFVencimiento = formatter.formatCellValue(row.getCell(3));
                String cellDivisa = formatter.formatCellValue(row.getCell(25));
                String cellNegocio = formatter.formatCellValue(row.getCell(1));

                XSSFCell cell99= row.getCell(6);
                XSSFCell cell0= row.getCell(7);
                XSSFCell cell98= row.getCell(8);
                cell0.setCellType(CellType.STRING);
                cell99.setCellType(CellType.STRING);
                cell98.setCellType(CellType.STRING);
                String cellVrNominalDivisa = formatter.formatCellValue(cell0).replace(" ", "");
                String cellVrNominalDivisa2 = formatter.formatCellValue(cell99).replace(" ", "");
                String cellTasa = formatter.formatCellValue(cell98).replace(" ", "");

                XSSFCell cell1= row.getCell(69);
                cell1.setCellType(CellType.STRING);
                String cellMtmCop = formatter.formatCellValue(cell1).replace(" ", "");

                XSSFCell cell2= row.getCell(61);
                cell2.setCellType(CellType.STRING);
                String cellVrNominalCop = formatter.formatCellValue(cell2).replace(" ", "");

                String cellLocalRp21 = formatter.formatCellValue(row.getCell(59));
                String cellDivRp21 = formatter.formatCellValue(row.getCell(60));
                String cellLocalDerec = formatter.formatCellValue(row.getCell(43));
                String cellLocalObligacion = formatter.formatCellValue(row.getCell(47));
                String cellPyG = formatter.formatCellValue(row.getCell(67));
                String cellTipoContraparte = formatter.formatCellValue(row.getCell(27)).trim();
                String cellOperacion = formatter.formatCellValue(row.getCell(5));
                String cellDvsaConciliacion1 = formatter.formatCellValue(row.getCell(44));
                String cellDvsaConciliacion2 = formatter.formatCellValue(row.getCell(48));
                String cellFecont = formatter.formatCellValue(row.getCell(41));
                log[0]=String.valueOf(row.getRowNum()+1);
                Date fechaDateC;
                Calendar calendar = Calendar.getInstance();
                String[] loadDate;

                try
                {
                    log[1]=CellReference.convertNumToColString(59)+" - (60)"; Long.parseLong(cellLocalRp21);
                    log[1]=CellReference.convertNumToColString(23)+" - (24)"; cellNitContraparte = formatter.formatCellValue(row.getCell(23)).split("/")[1];
                    cellNitContraparte = cellNitContraparte.substring(0,cellNitContraparte.length()-1);
                    log[1]=CellReference.convertNumToColString(69)+" - (70)"; Double.parseDouble(cellMtmCop);
                    log[1]=CellReference.convertNumToColString(6)+" - (7)"; Double.parseDouble(cellVrNominalDivisa2);
                    log[1]=CellReference.convertNumToColString(7)+" - (8)"; Double.parseDouble(cellVrNominalDivisa);
                    if(cellLocalObligacion.length()>0){
                    log[1]=CellReference.convertNumToColString(8)+" - (9)"; Double.parseDouble(cellTasa);}
                    else{
                        //cellTasa="0";
                    }
                    log[1]=CellReference.convertNumToColString(61)+" - (62)"; Double.parseDouble(cellVrNominalCop);
                    log[1]=CellReference.convertNumToColString(43)+" - (44)"; Long.parseLong(cellLocalDerec);
                    if(cellLocalObligacion.length()>0)
                    {log[1]=CellReference.convertNumToColString(47)+" - (48)"; Long.parseLong(cellLocalObligacion);}
                    log[1]=CellReference.convertNumToColString(67)+" - (68)"; Long.parseLong(cellPyG);
                    SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
                    formato.applyPattern("yyyy-MM-dd");
                    log[1]=CellReference.convertNumToColString(2)+" - (3)"; Date fechaDate = formato.parse(cellFAlta);
                    log[1]=CellReference.convertNumToColString(3)+" - (4)"; Date fechaDateV = formato.parse(cellFVencimiento);
                    log[1]=CellReference.convertNumToColString(41)+" - (42)"; fechaDateC = formato.parse(cellFecont);
                    cellMtmCop = String.valueOf(Double.parseDouble(cellMtmCop) *-1);

                    calendar.setTime(fechaDateC);
                    loadDate=mes.split("-");
                }
                catch(Exception e){
                    fail++;
                    log[2]="false";
                    log[3]="Tipo Dato de Celda Inválido";
                    lista.add(log);
                    continue;
                }

                if(cellNitContraparte.isEmpty() || cellNitContraparte.isBlank() ||cellNitContraparte.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(23)+" - (24)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellContraparte.isEmpty() || cellContraparte.isBlank() ||cellContraparte.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(0)+" - (1)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellFAlta.isEmpty() || cellFAlta.isBlank() )
                {
                    log[1]=CellReference.convertNumToColString(2)+" - (3)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellFVencimiento.isEmpty() || cellFVencimiento.isBlank() )
                {
                    log[1]=CellReference.convertNumToColString(3)+" - (4)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellDivisa.isEmpty() || cellDivisa.isBlank() ||cellDivisa.length()!=3)
                {
                    log[1]=CellReference.convertNumToColString(25)+" - (26)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellVrNominalDivisa.isEmpty() || cellVrNominalDivisa.isBlank() ||cellVrNominalDivisa.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(7)+" - (8)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellNegocio.isEmpty() || cellNegocio.isBlank() ||cellNegocio.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(1)+" - (2)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellVrNominalDivisa2.isEmpty() || cellVrNominalDivisa2.isBlank() ||cellVrNominalDivisa2.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(6)+" - (8)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellMtmCop.isEmpty() || cellMtmCop.isBlank())
                {
                    log[1]=CellReference.convertNumToColString(69)+" - (70)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellVrNominalCop.isEmpty() || cellVrNominalCop.isBlank() ||cellVrNominalCop.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(61)+" - (62)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellLocalRp21.isEmpty() || cellLocalRp21.isBlank() || cellLocalRp21.length()>18|| cellLocalRp21.length()<4)
                {
                    log[1]=CellReference.convertNumToColString(59)+" - (60)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellDivRp21.isEmpty() || cellDivRp21.isBlank() ||cellDivRp21.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(60)+" - (61)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellLocalDerec.isEmpty() || cellLocalDerec.isBlank() ||cellLocalDerec.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(43)+" - (44)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellLocalObligacion.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(47)+" - (48)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellPyG.isEmpty() || cellPyG.isBlank() ||cellPyG.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(67)+" - (68)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellTipoContraparte.isEmpty() || cellTipoContraparte.isBlank() ||cellTipoContraparte.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(27)+" - (28)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellOperacion.isEmpty() || cellOperacion.isBlank() ||cellOperacion.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(5)+" - (6)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellDvsaConciliacion1.isEmpty() || cellDvsaConciliacion1.isBlank() ||cellDvsaConciliacion1.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(44)+" - (45)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellDvsaConciliacion2.length()>254)
                {
                    log[1]=CellReference.convertNumToColString(48)+" - (49)";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }
                else if(cellFecont.isEmpty() || cellFecont.isBlank() ||  Integer.parseInt(loadDate[0])!=(calendar.get(Calendar.YEAR)) ||  Integer.parseInt(loadDate[1])!=(calendar.get(Calendar.MONTH) + 1))
                {
                    log[1]=CellReference.convertNumToColString(41)+" - (42)";
                    log[2]="false";
                    log[3]="Fecha de Cargue documento no corresponde al periodo";
                    fail++;
                    lista.add(log);
                }
                else if(fail == 0)
                {
                    SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
                    formato.applyPattern("yyyy-MM-dd");

                    Rp21Temporal opc = new Rp21Temporal();
                    opc.setNit(cellNitContraparte);
                    opc.setNitOriginal(cellNitContraparteOriginal);
                    opc.setContraparte(cellContraparte);
                    opc.setFAlta(formato.parse(cellFAlta));
                    opc.setFVenciemiento(formato.parse(cellFVencimiento));
                    opc.setVrNominalDivisa(Double.parseDouble(cellVrNominalDivisa)+Double.parseDouble(cellVrNominalDivisa2));
                    opc.setMtmCOP(Double.parseDouble(cellMtmCop));
                    if(row.getRowNum()==716 || row.getRowNum()==717 ||row.getRowNum()==718)
                        opc.setVrNominalCOP((Double.parseDouble(cellVrNominalDivisa)+Double.parseDouble(cellVrNominalDivisa2))*Double.parseDouble(cellTasa)); //Cambio a multiplicaciòn por tasa
                    opc.setVrNominalCOP((Double.parseDouble(cellVrNominalDivisa)+Double.parseDouble(cellVrNominalDivisa2))*Double.parseDouble(cellTasa)); //Cambio a multiplicaciòn por tasa
                    opc.setLocalRp21(cellLocalRp21);
                    opc.setDivrp21(cellDivRp21);
                    opc.setLocalDerec(cellLocalDerec);
                    opc.setLocalObligacion(cellLocalObligacion);
                    opc.setPyg(cellPyG);
                    opc.setTipoContraparte(cellTipoContraparte);
                    opc.setOperacion(cellOperacion);
                    opc.setDvsaconciliacion1(cellDvsaConciliacion1);
                    opc.setDvsaconciliacion2(cellDvsaConciliacion2);
                    opc.setFecont(formato.parse(cellFecont));
                    opc.setDivisa(cellDivisa);
                    opc.setNegocio(cellNegocio);

                    if((Double.parseDouble(cellMtmCop))<0)
                    {
                        opc.setVrNominalMtm(opc.getVrNominalCOP());
                    }
                    else
                    {
                        opc.setVrNominalMtm(opc.getVrNominalCOP()+(Double.parseDouble(cellMtmCop)));
                    }

                    opc.setOrigen("OPCIONES");

                    rp21TemporalList.add(opc);

                    success++;
                    log[2]="true";
                }
            }
        }
        String[] logFinal=new String[4];
        logFinal[0]="OPCIONES";
        logFinal[1]=String.valueOf(success);
        logFinal[2]=String.valueOf(fail);
        logFinal[3]="true";
        lista.add(logFinal);

        if(fail>0)
        {
            failOpciones = true;
            Date today=new Date();
            Audit insert = new Audit();
            insert.setAccion("Fallo Inserción Documento Derivados OPCIONES");
            insert.setCentro(user.getCentro());
            insert.setComponente("DERIVADOS");
            insert.setFecha(today);
            insert.setInput("OPCIONES");
            insert.setNombre(user.getPrimerNombre());
            insert.setUsuario(user.getUsuario());
            auditRepository.save(insert);
        }

        return lista;
    }

    public List<String[]> validarPlantillaFuturos(Iterator<Row> rows, String mes,User user) throws ParseException {
        ArrayList<String[]> lista= new ArrayList<String[]>();
        HashMap <String, Object> map = new HashMap <String, Object> ();
        List<String> listColumns=List.of("nit_cliente","nombre_cliente","FCHA_CONCILIACION","moneda","vlr_volumen","vlr_variation","VLOR_CNTBLE_1_RP21","CNTA_CNTBLE_1_RP21","DVSA_CNTBLE_1_RP21","CNTA_CNTBLE_1_CONCILIACION","CNTA_CNTBLE_2_CONCILIACION","tipo_entidad","contrato","DVSA_CNTBLE_1_CONCILIACION","DVSA_CNTBLE_2_CONCILIACION");
        XSSFRow row;
        String columna = "";
        boolean validado= true;
        int fail=0;
        int success =0;
        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(row.getRowNum()==0)
            {
                DataFormatter formatter = new DataFormatter();
                for (int i =0;i<row.getPhysicalNumberOfCells();i++)
                {
                    map.put(formatter.formatCellValue(row.getCell(i)), i);
                }
                for (String value: listColumns)
                {
                    if(!map.containsKey(value))
                    {
                        validado = false;
                        columna=value;
                        break;
                    }
                }

            }
            else if(row.getRowNum()>0 && validado == true)
            {
                String[] log=new String[4];
                log[2]="true";
                DataFormatter formatter = new DataFormatter();
                String cellMiembro = formatter.formatCellValue(row.getCell(Integer.parseInt(map.get("miembro").toString())));
                String cellTitular = formatter.formatCellValue(row.getCell(Integer.parseInt(map.get("titular").toString())));
                if(cellMiembro.trim().equals("M380") && cellTitular.trim().equals("P01")) {
                    String cellNitContraparte = formatter.formatCellValue(row.getCell(Integer.parseInt(map.get("nit_cliente").toString())));
                    String cellNitContraparteOriginal = formatter.formatCellValue(row.getCell(Integer.parseInt(map.get("nit_cliente").toString())));
                    String cellContraparte = formatter.formatCellValue(row.getCell(Integer.parseInt(map.get("nombre_cliente").toString())));
                    String cellFAlta = formatter.formatCellValue(row.getCell(Integer.parseInt(map.get("FCHA_CONCILIACION").toString())));
                    String cellFVencimiento = formatter.formatCellValue(row.getCell(Integer.parseInt(map.get("FCHA_CONCILIACION").toString())));
                    String cellDivisa = formatter.formatCellValue(row.getCell(Integer.parseInt(map.get("moneda").toString())));

                    XSSFCell cell99 = row.getCell(Integer.parseInt(map.get("vlr_volumen").toString()));
                    XSSFCell cell0 = row.getCell(Integer.parseInt(map.get("vlr_volumen").toString()));
                    cell0.setCellType(CellType.STRING);
                    cell99.setCellType(CellType.STRING);
                    String cellVrNominalDivisa = formatter.formatCellValue(cell0).replace(" ", "");
                    String cellVrNominalDivisa2 = formatter.formatCellValue(cell99).replace(" ", "");

                    XSSFCell cell1 = row.getCell(Integer.parseInt(map.get("VLOR_CNTBLE_1_COMPENSACION").toString()));
                    try {
                        cell1.setCellType(CellType.STRING);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    String cellMtmCop = formatter.formatCellValue(cell1).replace(" ", "");

                    XSSFCell cell2 = row.getCell(Integer.parseInt(map.get("VLOR_CNTBLE_1_RP21").toString()));
                    cell2.setCellType(CellType.STRING);
                    String cellVrNominalCop = formatter.formatCellValue(cell2).replace(" ", "");

                    String cellLocalRp21 = formatter.formatCellValue(row.getCell(Integer.parseInt(map.get("CNTA_CNTBLE_1_RP21").toString())));
                    String cellDivRp21 = formatter.formatCellValue(row.getCell(Integer.parseInt(map.get("DVSA_CNTBLE_1_RP21").toString())));
                    String cellLocalDerec = formatter.formatCellValue(row.getCell(Integer.parseInt(map.get("CNTA_CNTBLE_1_CONCILIACION").toString())));
                    String cellLocalObligacion = formatter.formatCellValue(row.getCell(Integer.parseInt(map.get("CNTA_CNTBLE_2_CONCILIACION").toString())));
                    String cellPyG = "";
                    String cellTipoContraparte = formatter.formatCellValue(row.getCell(Integer.parseInt(map.get("tipo_entidad").toString()))).trim();
                    String cellOperacion = formatter.formatCellValue(row.getCell(Integer.parseInt(map.get("contrato").toString())));
                    String cellDvsaConciliacion1 = formatter.formatCellValue(row.getCell(Integer.parseInt(map.get("DVSA_CNTBLE_1_CONCILIACION").toString())));
                    String cellDvsaConciliacion2 = formatter.formatCellValue(row.getCell(Integer.parseInt(map.get("DVSA_CNTBLE_2_CONCILIACION").toString())));
                    String cellFecont = formatter.formatCellValue(row.getCell(Integer.parseInt(map.get("FCHA_CONCILIACION").toString())));
                    log[0] = String.valueOf(row.getRowNum() + 1);
                    Date fechaDateC;
                    Calendar calendar = Calendar.getInstance();
                    String[] loadDate;

                    try {
                        log[1] = CellReference.convertNumToColString(Integer.parseInt(map.get("nit_cliente").toString())) + " - (" + map.get("nit_cliente").toString() + ")";
                        cellNitContraparte = formatter.formatCellValue(row.getCell(Integer.parseInt(map.get("nit_cliente").toString()))).split("/")[1];
                        cellNitContraparte = cellNitContraparte.substring(0, cellNitContraparte.length() - 1);
                        log[1] = CellReference.convertNumToColString(Integer.parseInt(map.get("vlr_volumen").toString())) + " - (" + map.get("vlr_volumen").toString() + ")";
                        Double.parseDouble(cellVrNominalDivisa2);
                        log[1] = CellReference.convertNumToColString(Integer.parseInt(map.get("vlr_volumen").toString())) + " - (" + map.get("vlr_volumen").toString() + ")";
                        Double.parseDouble(cellVrNominalDivisa);
                        log[1] = CellReference.convertNumToColString(Integer.parseInt(map.get("VLOR_CNTBLE_1_RP21").toString())) + " - (" + map.get("VLOR_CNTBLE_1_RP21").toString() + ")";
                        Double.parseDouble(cellVrNominalCop);
                        log[1] = CellReference.convertNumToColString(Integer.parseInt(map.get("CNTA_CNTBLE_1_CONCILIACION").toString())) + " - (" + map.get("CNTA_CNTBLE_1_CONCILIACION").toString() + ")";
                        Long.parseLong(cellLocalDerec);
                        if (cellLocalRp21.trim().length() > 0) {
                            log[1] = CellReference.convertNumToColString(Integer.parseInt(map.get("CNTA_CNTBLE_1_RP21").toString())) + " - (" + map.get("CNTA_CNTBLE_1_RP21").toString() + ")";
                            Long.parseLong(cellLocalRp21);
                        }
                        if (cellLocalObligacion.trim().length() > 0) {
                            log[1] = CellReference.convertNumToColString(Integer.parseInt(map.get("CNTA_CNTBLE_2_CONCILIACION").toString())) + " - (" + map.get("CNTA_CNTBLE_2_CONCILIACION").toString() + ")";
                            Long.parseLong(cellLocalObligacion);
                        }
                        SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
                        formato.applyPattern("yyyy-MM-dd");
                        log[1] = CellReference.convertNumToColString(Integer.parseInt(map.get("FCHA_CONCILIACION").toString())) + " - (" + map.get("FCHA_CONCILIACION").toString() + ")";
                        Date fechaDate = formato.parse(cellFAlta);
                        log[1] = CellReference.convertNumToColString(Integer.parseInt(map.get("FCHA_CONCILIACION").toString())) + " - (" + map.get("FCHA_CONCILIACION").toString() + ")";
                        Date fechaDateV = formato.parse(cellFVencimiento);
                        log[1] = CellReference.convertNumToColString(Integer.parseInt(map.get("FCHA_CONCILIACION").toString())) + " - (" + map.get("FCHA_CONCILIACION").toString() + ")";
                        fechaDateC = formato.parse(cellFecont);
                        log[1] = CellReference.convertNumToColString(Integer.parseInt(map.get("VLOR_CNTBLE_1_COMPENSACION").toString())) + " - (" + map.get("VLOR_CNTBLE_1_COMPENSACION").toString() + ")";
                        cellMtmCop = String.valueOf(Double.parseDouble(cellMtmCop));

                        calendar.setTime(fechaDateC);
                        loadDate = mes.split("-");
                    } catch (Exception e) {
                        fail++;
                        log[2] = "false";
                        log[3] = "Tipo Dato de Celda Inválido";
                        lista.add(log);
                        continue;
                    }

                    if (cellNitContraparte.isEmpty() || cellNitContraparte.isBlank() || cellNitContraparte.length() > 254) {
                        log[1] = CellReference.convertNumToColString(Integer.parseInt(map.get("nit_cliente").toString())) + " - (" + map.get("nit_cliente").toString() + ")";
                        log[2] = "false";
                        log[3] = "Falló en validación de campo";
                        fail++;
                        lista.add(log);
                    } else if (cellContraparte.isEmpty() || cellContraparte.isBlank() || cellContraparte.length() > 254) {
                        log[1] = CellReference.convertNumToColString(Integer.parseInt(map.get("nombre_cliente").toString())) + " - (" + map.get("nombre_cliente").toString() + ")";
                        log[2] = "false";
                        log[3] = "Falló en validación de campo";
                        fail++;
                        lista.add(log);
                    } else if (cellFAlta.isEmpty() || cellFAlta.isBlank()) {
                        log[1] = CellReference.convertNumToColString(Integer.parseInt(map.get("FCHA_CONCILIACION").toString())) + " - (" + map.get("FCHA_CONCILIACION").toString() + ")";
                        log[2] = "false";
                        log[3] = "Falló en validación de campo";
                        fail++;
                        lista.add(log);
                    } else if (cellFVencimiento.isEmpty() || cellFVencimiento.isBlank()) {
                        log[1] = CellReference.convertNumToColString(Integer.parseInt(map.get("FCHA_CONCILIACION").toString())) + " - (" + map.get("FCHA_CONCILIACION").toString() + ")";
                        log[2] = "false";
                        log[3] = "Falló en validación de campo";
                        fail++;
                        lista.add(log);
                    } else if (cellDivisa.isEmpty() || cellDivisa.isBlank() || cellDivisa.length() != 3) {
                        log[1] = CellReference.convertNumToColString(Integer.parseInt(map.get("moneda").toString())) + " - (" + map.get("moneda").toString() + ")";
                        log[2] = "false";
                        log[3] = "Falló en validación de campo";
                        fail++;
                        lista.add(log);
                    } else if (cellVrNominalDivisa.isEmpty() || cellVrNominalDivisa.isBlank() || cellVrNominalDivisa.length() > 254) {
                        log[1] = CellReference.convertNumToColString(Integer.parseInt(map.get("vlr_volumen").toString())) + " - (" + map.get("vlr_volumen").toString() + ")";
                        log[2] = "false";
                        log[3] = "Falló en validación de campo";
                        fail++;
                        lista.add(log);
                    } else if (cellVrNominalDivisa2.isEmpty() || cellVrNominalDivisa2.isBlank() || cellVrNominalDivisa2.length() > 254) {
                        log[1] = CellReference.convertNumToColString(Integer.parseInt(map.get("vlr_volumen").toString())) + " - (" + map.get("vlr_volumen").toString() + ")";
                        log[2] = "false";
                        log[3] = "Falló en validación de campo";
                        fail++;
                        lista.add(log);
                    }else if (cellVrNominalCop.isEmpty() || cellVrNominalCop.isBlank() || cellVrNominalCop.length() > 254) {
                        log[1] = CellReference.convertNumToColString(Integer.parseInt(map.get("vlr").toString())) + " - (" + map.get("vlr").toString() + ")";
                        log[2] = "false";
                        log[3] = "Falló en validación de campo";
                        fail++;
                        lista.add(log);
                    }
                /*else if((cellLocalRp21.length()>18|| cellLocalRp21.length()<4) && cellLocalRp21.length()>0)
                {
                    log[1]=CellReference.convertNumToColString(Integer.parseInt(map.get("CNTA_CNTBLE_1_RP21").toString()))+" - ("+map.get("CNTA_CNTBLE_1_RP21").toString()+")";
                    log[2]="false";
                    log[3]="Falló en validación de campo";
                    fail++;
                    lista.add(log);
                }*/
                    else if (cellDivRp21.length() > 254) {
                        log[1] = CellReference.convertNumToColString(Integer.parseInt(map.get("DVSA_CNTBLE_1_RP21").toString())) + " - (" + map.get("DVSA_CNTBLE_1_RP21").toString() + ")";
                        log[2] = "false";
                        log[3] = "Falló en validación de campo";
                        fail++;
                        lista.add(log);
                    } else if (cellLocalDerec.isEmpty() || cellLocalDerec.isBlank() || cellLocalDerec.length() > 254) {
                        log[1] = CellReference.convertNumToColString(Integer.parseInt(map.get("CNTA_CNTBLE_1_CONCILIACION").toString())) + " - (" + map.get("CNTA_CNTBLE_1_CONCILIACION").toString() + ")";
                        log[2] = "false";
                        log[3] = "Falló en validación de campo";
                        fail++;
                        lista.add(log);
                    } else if (cellLocalObligacion.length() > 254) {
                        log[1] = CellReference.convertNumToColString(Integer.parseInt(map.get("CNTA_CNTBLE_2_CONCILIACION").toString())) + " - (" + map.get("CNTA_CNTBLE_2_CONCILIACION").toString() + ")";
                        log[2] = "false";
                        log[3] = "Falló en validación de campo";
                        fail++;
                        lista.add(log);
                    } else if (cellTipoContraparte.isEmpty() || cellTipoContraparte.isBlank() || cellTipoContraparte.length() > 254) {
                        log[1] = CellReference.convertNumToColString(Integer.parseInt(map.get("tipo_entidad").toString())) + " - (" + map.get("tipo_entidad").toString() + ")";
                        log[2] = "false";
                        log[3] = "Falló en validación de campo";
                        fail++;
                        lista.add(log);
                    } else if (cellOperacion.isEmpty() || cellOperacion.isBlank() || cellOperacion.length() > 254) {
                        log[1] = CellReference.convertNumToColString(Integer.parseInt(map.get("contrato").toString())) + " - (" + map.get("contrato").toString() + ")";
                        log[2] = "false";
                        log[3] = "Falló en validación de campo";
                        fail++;
                        lista.add(log);
                    } else if (cellMtmCop.isEmpty() || cellMtmCop.isBlank()) {
                        log[1] = CellReference.convertNumToColString(Integer.parseInt(map.get("VLOR_CNTBLE_1_COMPENSACION").toString())) + " - (" + map.get("VLOR_CNTBLE_1_COMPENSACION").toString() + ")";
                        log[2] = "false";
                        log[3] = "Falló en validación de campo";
                        fail++;
                        lista.add(log);
                    } else if (cellDvsaConciliacion1.isEmpty() || cellDvsaConciliacion1.isBlank() || cellDvsaConciliacion1.length() > 254) {
                        log[1] = CellReference.convertNumToColString(Integer.parseInt(map.get("DVSA_CNTBLE_1_CONCILIACION").toString())) + " - (" + map.get("DVSA_CNTBLE_1_CONCILIACION").toString() + ")";
                        log[2] = "false";
                        log[3] = "Falló en validación de campo";
                        fail++;
                        lista.add(log);
                    } else if (cellDvsaConciliacion2.length() > 254) {
                        log[1] = CellReference.convertNumToColString(Integer.parseInt(map.get("DVSA_CNTBLE_2_CONCILIACION").toString())) + " - (" + map.get("DVSA_CNTBLE_2_CONCILIACION").toString() + ")";
                        log[2] = "false";
                        log[3] = "Falló en validación de campo";
                        fail++;
                        lista.add(log);
                    } else if (cellFecont.isEmpty() || cellFecont.isBlank() || Integer.parseInt(loadDate[0]) != (calendar.get(Calendar.YEAR)) || Integer.parseInt(loadDate[1]) != (calendar.get(Calendar.MONTH) + 1)) {
                        log[1] = CellReference.convertNumToColString(Integer.parseInt(map.get("FCHA_CONCILIACION").toString())) + " - (" + map.get("FCHA_CONCILIACION").toString() + ")";
                        log[2] = "false";
                        log[3] = "Fecha de Cargue documento no corresponde al periodo";
                        fail++;
                        lista.add(log);
                    } else if (fail == 0) {
                        SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
                        formato.applyPattern("yyyy-MM-dd");

                        Rp21Temporal opc = new Rp21Temporal();
                        opc.setNit(cellNitContraparte);
                        opc.setNitOriginal(cellNitContraparteOriginal);
                        opc.setContraparte(cellContraparte);
                        opc.setFAlta(formato.parse(cellFAlta));
                        opc.setFVenciemiento(formato.parse(cellFVencimiento));
                        opc.setVrNominalDivisa(Double.parseDouble(cellVrNominalDivisa));
                        opc.setMtmCOP(Double.parseDouble(cellMtmCop));
                        opc.setVrNominalCOP(Double.parseDouble(cellVrNominalCop));
                        opc.setLocalRp21(cellLocalRp21);
                        opc.setDivrp21(cellDivRp21);
                        opc.setLocalDerec(cellLocalDerec);
                        opc.setLocalObligacion(cellLocalObligacion);
                        opc.setPyg(cellPyG);
                        opc.setTipoContraparte(cellTipoContraparte);
                        opc.setOperacion(cellOperacion);
                        opc.setDvsaconciliacion1(cellDvsaConciliacion1);
                        opc.setDvsaconciliacion2(cellDvsaConciliacion2);
                        opc.setFecont(formato.parse(cellFecont));
                        opc.setDivisa(cellDivisa);

                        if ((Double.parseDouble(cellMtmCop)) < 0) {
                            opc.setVrNominalMtm(opc.getVrNominalCOP());
                        } else {
                            opc.setVrNominalMtm(Double.parseDouble(cellVrNominalCop) + (Double.parseDouble(cellMtmCop)));
                        }

                        opc.setOrigen("FUTUROS");

                        rp21TemporalList.add(opc);

                        success++;
                        log[2] = "true";
                    }
                }
            }
            else
            {
                String[] log=new String[4];
                log[0]="";
                log[1]="";
                log[2]="false";
                log[3]="Falló de cargue estructura. No se encuentra la columna "+ columna;
                fail++;
                lista.add(log);
                break;
            }
        }
        String[] logFinal=new String[4];
        logFinal[0]="FUTUROS";
        logFinal[1]=String.valueOf(success);
        logFinal[2]=String.valueOf(fail);
        logFinal[3]="true";
        lista.add(logFinal);

        if(fail>0)
        {
            failOpciones = true;
            Date today=new Date();
            Audit insert = new Audit();
            insert.setAccion("Fallo Inserción Documento Derivados FUTUROS");
            insert.setCentro(user.getCentro());
            insert.setComponente("DERIVADOS");
            insert.setFecha(today);
            insert.setInput("FUTUROS");
            insert.setNombre(user.getPrimerNombre());
            insert.setUsuario(user.getUsuario());
            auditRepository.save(insert);
        }

        return lista;
    }

    public List<Rp21> findAll(String periodo){
        Query query = entityManager.createNativeQuery("SELECT *" +
                " FROM nexco_reporte_rp21 as em WHERE MONTH(em.fecont) = ? AND YEAR(em.fecont) = ?", Rp21.class);
        try{
            String[] month = periodo.split("-");
            query.setParameter(1,month[1]);
            query.setParameter(2,month[0]);
        }
        catch (Exception e){
            query.setParameter(1,"0");
            query.setParameter(2,"0");
        }
        return query.getResultList();
    }

    public List<Rp21_Extend> findAllExclude(String periodo,Object fw,Object swap,Object rys,Object opc, String futuros,Object fu){

        String parametros="";
        if(fw!=null)
        {
            parametros=parametros+" OR ORIGEN ='FORWARD'";
        }
        if(swap!=null)
        {
            parametros=parametros+" OR ORIGEN ='SWAP'";
        }
        if(rys!=null)
        {
            parametros=parametros+" OR ORIGEN ='RYS'";
        }
        if(opc!=null)
        {
            parametros=parametros+" OR ORIGEN ='OPCIONES'";
        }
        if(fu!=null)
        {
            parametros=parametros+" OR ORIGEN ='FUTUROS'";
        }
        if(parametros.length()>0)
        {
            parametros=" AND ("+parametros.substring(3,parametros.length())+")";
        }

        if(futuros.equals("true")){

            Query query = entityManager.createNativeQuery("SELECT *" +
                    " FROM nexco_reporte_rp21_extend as em WHERE isnull(em.clase_futuros, '') <> 'CCRC NDF' AND MONTH(em.fecont) = ? AND YEAR(em.fecont) = ?"+parametros+" ORDER BY id_reporte", Rp21_Extend.class);
            try{
                String[] month = periodo.split("-");
                query.setParameter(1,month[1]);
                query.setParameter(2,month[0]);
            }
            catch (Exception e){
                query.setParameter(1,"0");
                query.setParameter(2,"0");
            }

            return query.getResultList();

        }else{

            Query query = entityManager.createNativeQuery("SELECT *" +
                    " FROM nexco_reporte_rp21_extend as em WHERE MONTH(em.fecont) = ? AND YEAR(em.fecont) = ?"+parametros+" ORDER BY id_reporte", Rp21_Extend.class);
            try{
                String[] month = periodo.split("-");
                query.setParameter(1,month[1]);
                query.setParameter(2,month[0]);
            }
            catch (Exception e){
                query.setParameter(1,"0");
                query.setParameter(2,"0");
            }

            return query.getResultList();
        }

    }

    public List<Rp21> findAllExcludeOriginal(String periodo,Object fw,Object swap,Object rys,Object opc, String futuros,Object fu){

        String parametros="";
        if(fw!=null)
        {
            parametros=parametros+" OR ORIGEN ='FORWARD'";
        }
        if(swap!=null)
        {
            parametros=parametros+" OR ORIGEN ='SWAP'";
        }
        if(rys!=null)
        {
            parametros=parametros+" OR ORIGEN ='RYS'";
        }
        if(opc!=null)
        {
            parametros=parametros+" OR ORIGEN ='OPCIONES'";
        }
        if(fu!=null)
        {
            parametros=parametros+" OR ORIGEN ='FUTUROS'";
        }
        if(parametros.length()>0)
        {
            parametros=" AND ("+parametros.substring(3,parametros.length())+")";
        }

        if(futuros.equals("true")){

            Query query = entityManager.createNativeQuery("SELECT *" +
                    " FROM nexco_reporte_rp21 as em WHERE isnull(em.clase_futuros, '') <> 'CCRC NDF' AND MONTH(em.fecont) = ? AND YEAR(em.fecont) = ?"+parametros+" ORDER BY id_reporte", Rp21.class);
            try{
                String[] month = periodo.split("-");
                query.setParameter(1,month[1]);
                query.setParameter(2,month[0]);
            }
            catch (Exception e){
                query.setParameter(1,"0");
                query.setParameter(2,"0");
            }

            return query.getResultList();

        }else{

            Query query = entityManager.createNativeQuery("SELECT *" +
                    " FROM nexco_reporte_rp21 as em WHERE MONTH(em.fecont) = ? AND YEAR(em.fecont) = ?"+parametros+" ORDER BY id_reporte", Rp21.class);
            try{
                String[] month = periodo.split("-");
                query.setParameter(1,month[1]);
                query.setParameter(2,month[0]);
            }
            catch (Exception e){
                query.setParameter(1,"0");
                query.setParameter(2,"0");
            }

            return query.getResultList();
        }

    }

    public List<Object[]> findAllExtra(String periodo,Object fw,Object swap,Object rys,Object opc, String futuros,Object fu) {
        String parametros = "";
        if (fw != null) {
            parametros = parametros + " OR ORIGEN ='FORWARD'";
        }
        if (swap != null) {
            parametros = parametros + " OR ORIGEN ='SWAP'";
        }
        if (rys != null) {
            parametros = parametros + " OR ORIGEN ='RYS'";
        }
        if (opc != null) {
            parametros = parametros + " OR ORIGEN ='OPCIONES'";
        }
        if (fu != null) {
            parametros = parametros + " OR ORIGEN ='FUTUROS'";
        }
        if (parametros.length() > 0) {
            parametros = " AND (" + parametros.substring(3, parametros.length()) + ")";
        }

        if (futuros.equals("true")) {

            Query query = entityManager.createNativeQuery("SELECT CAST(DATEDIFF (DAY, f_alta,f_vencimiento) AS FLOAT)/30 dias,\n" +
                    "CAST(DATEDIFF (DAY, fecont,f_vencimiento) AS FLOAT)/30 dias1, \n" +
                    "CASE \n" +
                    "WHEN CAST(DATEDIFF (DAY, f_alta,f_vencimiento)AS FLOAT)/30 <= 12.99 THEN '1 Año' \n" +
                    "WHEN CAST(DATEDIFF (DAY, f_alta,f_vencimiento)AS FLOAT)/30 <= 60 THEN '5 Años'\n" +
                    "WHEN CAST(DATEDIFF (DAY, f_alta,f_vencimiento)AS FLOAT)/30 > 60 THEN 'Mas de 5 Años'\n" +
                    "END Vto_Real,\n" +
                    "CASE \n" +
                    "WHEN CAST(DATEDIFF (DAY, fecont,f_vencimiento)AS FLOAT)/30 <= 12.99 THEN '1 Año' \n" +
                    "WHEN CAST(DATEDIFF (DAY, fecont,f_vencimiento)AS FLOAT)/30 <= 60 THEN '5 Años'\n" +
                    "WHEN CAST(DATEDIFF (DAY, fecont,f_vencimiento)AS FLOAT)/30 > 60 THEN 'Mas de 5 Años'\n" +
                    "END Vto_Residual,\n" +
                    "DATEDIFF (DAY, fecont,f_vencimiento) DIAS\n" +
                    "FROM nexco_reporte_rp21 as em \n" +
                    "WHERE isnull(em.clase_futuros, '') <> 'CCRC NDF' \n" +
                    "AND  MONTH(em.fecont) = ? AND YEAR(em.fecont) = ?" + parametros + " ORDER BY id_reporte");
            try {
                String[] month = periodo.split("-");
                query.setParameter(1, month[1]);
                query.setParameter(2, month[0]);
            } catch (Exception e) {
                query.setParameter(1, "0");
                query.setParameter(2, "0");
            }

            return query.getResultList();

        } else {

            Query query = entityManager.createNativeQuery("SELECT CAST(DATEDIFF (DAY, f_alta,f_vencimiento) AS FLOAT)/30 dias,\n" +
                    "CAST(DATEDIFF (DAY, fecont,f_vencimiento) AS FLOAT)/30 dias1, \n" +
                    "CASE \n" +
                    "WHEN CAST(DATEDIFF (DAY, f_alta,f_vencimiento)AS FLOAT)/30 <= 12.99 THEN '1 Año' \n" +
                    "WHEN CAST(DATEDIFF (DAY, f_alta,f_vencimiento)AS FLOAT)/30 <= 60 THEN '5 Años'\n" +
                    "WHEN CAST(DATEDIFF (DAY, f_alta,f_vencimiento)AS FLOAT)/30 > 60 THEN 'Mas de 5 Años'\n" +
                    "END Vto_Real,\n" +
                    "CASE \n" +
                    "WHEN CAST(DATEDIFF (DAY, fecont,f_vencimiento)AS FLOAT)/30 <= 12.99 THEN '1 Año' \n" +
                    "WHEN CAST(DATEDIFF (DAY, fecont,f_vencimiento)AS FLOAT)/30 <= 60 THEN '5 Años'\n" +
                    "WHEN CAST(DATEDIFF (DAY, fecont,f_vencimiento)AS FLOAT)/30 > 60 THEN 'Mas de 5 Años'\n" +
                    "END Vto_Residual,\n" +
                    "DATEDIFF (DAY, fecont,f_vencimiento) DIAS\n" +
                    "FROM nexco_reporte_rp21 as em \n" +
                    "WHERE MONTH(em.fecont) = ? AND YEAR(em.fecont) = ?" + parametros + " ORDER BY id_reporte");
            try {
                String[] month = periodo.split("-");
                query.setParameter(1, month[1]);
                query.setParameter(2, month[0]);
            } catch (Exception e) {
                query.setParameter(1, "0");
                query.setParameter(2, "0");
            }

            return query.getResultList();
        }
    }

    public List<Object[]> findAllLiquidez(String periodo,Object fw,Object swap,Object rys,Object opc, String futuros,Object fu) {
        String parametros = "";
        if (fw != null) {
            parametros = parametros + " OR ORIGEN ='FORWARD'";
        }
        if (swap != null) {
            parametros = parametros + " OR ORIGEN ='SWAP'";
        }
        if (rys != null) {
            parametros = parametros + " OR ORIGEN ='RYS'";
        }
        if (opc != null) {
            parametros = parametros + " OR ORIGEN ='OPCIONES'";
        }
        if (fu != null) {
            parametros = parametros + " OR ORIGEN ='FUTUROS'";
        }
        if (parametros.length() > 0) {
            parametros = " AND (" + parametros.substring(3, parametros.length()) + ")";
        }

        if (futuros.equals("true")) {

            Query query = entityManager.createNativeQuery("SELECT em.tipo_operacion,\n" +
                    "em.tipo_riesgo,\n" +
                    "em.nit,\n" +
                    "em.contraparte,\n" +
                    "em.f_alta,\n" +
                    "em.f_vencimiento,\n" +
                    "em.divisa,\n" +
                    "em.vr_nominal_divisa,\n" +
                    "em.mtm_cop,\n" +
                    "em.vr_nominal_cop,\n" +
                    "em.vr_nominal_mtm,\n" +
                    "em.intergrupo,\n" +
                    "em.pais,\n" +
                    "em.neocon,\n" +
                    "em.local_rp21,\n" +
                    "em.div_rp21,\n" +
                    "CAST(DATEDIFF (DAY, f_alta,f_vencimiento) AS FLOAT)/30 dias,\n" +
                    "CAST(DATEDIFF (DAY, fecont,f_vencimiento) AS FLOAT)/30 dias1, \n" +
                    "CASE \n" +
                    "WHEN CAST(DATEDIFF (DAY, f_alta,f_vencimiento)AS FLOAT)/30 <= 12.99 THEN '1 Año' \n" +
                    "WHEN CAST(DATEDIFF (DAY, f_alta,f_vencimiento)AS FLOAT)/30 <= 60 THEN '5 Años'\n" +
                    "WHEN CAST(DATEDIFF (DAY, f_alta,f_vencimiento)AS FLOAT)/30 > 60 THEN 'Mas de 5 Años'\n" +
                    "END Vto_Real,\n" +
                    "CASE \n" +
                    "WHEN CAST(DATEDIFF (DAY, fecont,f_vencimiento)AS FLOAT)/30 <= 12.99 THEN '1 Año' \n" +
                    "WHEN CAST(DATEDIFF (DAY, fecont,f_vencimiento)AS FLOAT)/30 <= 60 THEN '5 Años'\n" +
                    "WHEN CAST(DATEDIFF (DAY, fecont,f_vencimiento)AS FLOAT)/30 > 60 THEN 'Mas de 5 Años'\n" +
                    "END Vto_Residual,\n" +
                    "em.neocon_,\n" +
                    "em.local_derec,\n" +
                    "em.local_obligacion,\n" +
                    "em.pyg,\n" +
                    "em.tipo_contraparte,\n" +
                    "em.yintp,\n" +
                    "DATEDIFF (DAY, fecont,f_vencimiento) DIAS,\n" +
                    "em.operacion,em.dvsaconciliacion1,\n" +
                    "em.dvsaconciliacion2,\n" +
                    "em.fecont,\n" +
                    "nte.tipo_entidad NA,\n" +
                    "ntes.tipo_entidad NA1,\n" +
                    "ntes1.tipo_entidad NA2,\n" +
                    "nv.eliminacion \n" +
                    "FROM nexco_reporte_rp21 as em\n" +
                    //"LEFT JOIN (SELECT tipo_contraparte,eliminacion,tipo_entidad,intergrupo FROM nexco_tipo_entidad) nv ON em.tipo_contraparte = nv.tipo_contraparte \n" +
                    "LEFT JOIN (SELECT tipo_contraparte,eliminacion,tipo_entidad,intergrupo FROM nexco_tipo_entidad) nv ON em.tipo_contraparte = nv.tipo_contraparte \n" +
                    "LEFT JOIN (SELECT tipo_contraparte,eliminacion,tipo_entidad,intergrupo FROM nexco_tipo_entidad WHERE (intergrupo IS NULL) AND (nit IS NULL OR nit = '')) nte ON em.tipo_contraparte = nte.tipo_contraparte\n" +
                    "LEFT JOIN (SELECT tipo_contraparte,eliminacion,tipo_entidad,intergrupo FROM nexco_tipo_entidad WHERE intergrupo IS NOT NULL AND (nit IS NULL OR nit='')) ntes ON em.tipo_contraparte = ntes.tipo_contraparte AND CASE em.intergrupo WHEN 'No' THEN 0 ELSE 1 END = ntes.intergrupo\n" +
                    "LEFT JOIN (SELECT tipo_contraparte,eliminacion,tipo_entidad,nit,intergrupo FROM nexco_tipo_entidad WHERE intergrupo IS NOT NULL AND nit IS NOT NULL AND nit !='') ntes1 ON em.tipo_contraparte = ntes1.tipo_contraparte AND em.nit = ntes1.nit AND CASE em.intergrupo WHEN 'No' THEN 0 ELSE 1 END = ntes1.intergrupo\n" +
                    "WHERE isnull(em.clase_futuros, '') <> 'CCRC NDF' \n" +
                    "AND  MONTH(em.fecont) = ? AND YEAR(em.fecont) = ? AND (((nv.eliminacion = '0' OR nv.eliminacion IS NULL) AND (nte.eliminacion = '0' OR nte.eliminacion IS NULL) AND (ntes.eliminacion = '0' OR ntes.eliminacion IS NULL) AND (ntes1.eliminacion = '0' OR ntes1.eliminacion IS NULL)) OR (nte.tipo_entidad IS NULL AND ntes.tipo_entidad IS NULL AND ntes1.tipo_entidad IS NULL)) " + parametros + " \n" +
                    "GROUP BY em.tipo_operacion,\n" +
                    "em.tipo_riesgo,\n" +
                    "em.nit,\n" +
                    "em.contraparte,\n" +
                    "em.f_alta,\n" +
                    "em.f_vencimiento,\n" +
                    "em.divisa,\n" +
                    "em.vr_nominal_divisa,\n" +
                    "em.mtm_cop,\n" +
                    "em.vr_nominal_cop,\n" +
                    "em.vr_nominal_mtm,\n" +
                    "em.intergrupo,\n" +
                    "em.pais,\n" +
                    "em.neocon,\n" +
                    "em.local_rp21,\n" +
                    "em.div_rp21,\n" +
                    "CAST(DATEDIFF (DAY, f_alta,f_vencimiento) AS FLOAT)/30 ,\n" +
                    "CAST(DATEDIFF (DAY, fecont,f_vencimiento) AS FLOAT)/30 , \n" +
                    "CASE \n" +
                    "WHEN CAST(DATEDIFF (DAY, f_alta,f_vencimiento)AS FLOAT)/30 <= 12.99 THEN '1 Año' \n" +
                    "WHEN CAST(DATEDIFF (DAY, f_alta,f_vencimiento)AS FLOAT)/30 <= 60 THEN '5 Años'\n" +
                    "WHEN CAST(DATEDIFF (DAY, f_alta,f_vencimiento)AS FLOAT)/30 > 60 THEN 'Mas de 5 Años'\n" +
                    "END ,\n" +
                    "CASE \n" +
                    "WHEN CAST(DATEDIFF (DAY, fecont,f_vencimiento)AS FLOAT)/30 <= 12.99 THEN '1 Año' \n" +
                    "WHEN CAST(DATEDIFF (DAY, fecont,f_vencimiento)AS FLOAT)/30 <= 60 THEN '5 Años'\n" +
                    "WHEN CAST(DATEDIFF (DAY, fecont,f_vencimiento)AS FLOAT)/30 > 60 THEN 'Mas de 5 Años'\n" +
                    "END ,\n" +
                    "em.neocon_,\n" +
                    "em.local_derec,\n" +
                    "em.local_obligacion,\n" +
                    "em.pyg,\n" +
                    "em.tipo_contraparte,\n" +
                    "em.yintp,\n" +
                    "DATEDIFF (DAY, fecont,f_vencimiento) ,\n" +
                    "em.operacion,em.dvsaconciliacion1,\n" +
                    "em.dvsaconciliacion2,\n" +
                    "em.fecont,\n" +
                    "nte.tipo_entidad,\n" +
                    "ntes.tipo_entidad,\n" +
                    "ntes1.tipo_entidad,\n"+
                    "nv.eliminacion");
            try {
                String[] month = periodo.split("-");
                query.setParameter(1, month[1]);
                query.setParameter(2, month[0]);
            } catch (Exception e) {
                query.setParameter(1, "0");
                query.setParameter(2, "0");
            }

            return query.getResultList();

        } else {

            Query query = entityManager.createNativeQuery("SELECT em.tipo_operacion,\n" +
                    "em.tipo_riesgo,\n" +
                    "em.nit,\n" +
                    "em.contraparte,\n" +
                    "em.f_alta,\n" +
                    "em.f_vencimiento,\n" +
                    "em.divisa,\n" +
                    "em.vr_nominal_divisa,\n" +
                    "em.mtm_cop,\n" +
                    "em.vr_nominal_cop,\n" +
                    "em.vr_nominal_mtm,\n" +
                    "em.intergrupo,\n" +
                    "em.pais,\n" +
                    "em.neocon,\n" +
                    "em.local_rp21,\n" +
                    "em.div_rp21,\n" +
                    "CAST(DATEDIFF (DAY, f_alta,f_vencimiento) AS FLOAT)/30 dias,\n" +
                    "CAST(DATEDIFF (DAY, fecont,f_vencimiento) AS FLOAT)/30 dias1, \n" +
                    "CASE \n" +
                    "WHEN CAST(DATEDIFF (DAY, f_alta,f_vencimiento)AS FLOAT)/30 <= 12.99 THEN '1 Año' \n" +
                    "WHEN CAST(DATEDIFF (DAY, f_alta,f_vencimiento)AS FLOAT)/30 <= 60 THEN '5 Años'\n" +
                    "WHEN CAST(DATEDIFF (DAY, f_alta,f_vencimiento)AS FLOAT)/30 > 60 THEN 'Mas de 5 Años'\n" +
                    "END Vto_Real,\n" +
                    "CASE \n" +
                    "WHEN CAST(DATEDIFF (DAY, fecont,f_vencimiento)AS FLOAT)/30 <= 12.99 THEN '1 Año' \n" +
                    "WHEN CAST(DATEDIFF (DAY, fecont,f_vencimiento)AS FLOAT)/30 <= 60 THEN '5 Años'\n" +
                    "WHEN CAST(DATEDIFF (DAY, fecont,f_vencimiento)AS FLOAT)/30 > 60 THEN 'Mas de 5 Años'\n" +
                    "END Vto_Residual,\n" +
                    "em.neocon_,\n" +
                    "em.local_derec,\n" +
                    "em.local_obligacion,\n" +
                    "em.pyg,\n" +
                    "em.tipo_contraparte,\n" +
                    "em.yintp,\n" +
                    "DATEDIFF (DAY, fecont,f_vencimiento) DIAS,\n" +
                    "em.operacion,em.dvsaconciliacion1,\n" +
                    "em.dvsaconciliacion2,\n" +
                    "em.fecont,\n" +
                    "nte.tipo_entidad NA,\n" +
                    "ntes.tipo_entidad NA1,\n" +
                    "ntes1.tipo_entidad NA2,\n" +
                    "nv.eliminacion \n" +
                    "FROM nexco_reporte_rp21 as em\n" +
                    //"LEFT JOIN (SELECT tipo_contraparte,eliminacion,tipo_entidad,intergrupo FROM nexco_tipo_entidad) nv ON em.tipo_contraparte = nv.tipo_contraparte \n" +
                    "LEFT JOIN (SELECT tipo_contraparte,eliminacion,tipo_entidad,intergrupo FROM nexco_tipo_entidad) nv ON em.tipo_contraparte = nv.tipo_contraparte \n" +
                    "LEFT JOIN (SELECT tipo_contraparte,eliminacion,tipo_entidad,intergrupo FROM nexco_tipo_entidad WHERE (intergrupo IS NULL) AND (nit IS NULL OR nit = '')) nte ON em.tipo_contraparte = nte.tipo_contraparte\n" +
                    "LEFT JOIN (SELECT tipo_contraparte,eliminacion,tipo_entidad,intergrupo FROM nexco_tipo_entidad WHERE intergrupo IS NOT NULL AND (nit IS NULL OR nit='')) ntes ON em.tipo_contraparte = ntes.tipo_contraparte AND CASE em.intergrupo WHEN 'No' THEN 0 ELSE 1 END = ntes.intergrupo\n" +
                    "LEFT JOIN (SELECT tipo_contraparte,eliminacion,tipo_entidad,nit,intergrupo FROM nexco_tipo_entidad WHERE intergrupo IS NOT NULL AND nit IS NOT NULL AND nit !='') ntes1 ON em.tipo_contraparte = ntes1.tipo_contraparte AND em.nit = ntes1.nit AND CASE em.intergrupo WHEN 'No' THEN 0 ELSE 1 END = ntes1.intergrupo\n" +
                    "WHERE  MONTH(em.fecont) = ? AND YEAR(em.fecont) = ? AND (((nv.eliminacion = '0' OR nv.eliminacion IS NULL) AND (nte.eliminacion = '0' OR nte.eliminacion IS NULL) AND (ntes.eliminacion = '0' OR ntes.eliminacion IS NULL) AND (ntes1.eliminacion = '0' OR ntes1.eliminacion IS NULL)) OR (nte.tipo_entidad IS NULL AND ntes.tipo_entidad IS NULL AND ntes1.tipo_entidad IS NULL)) " + parametros + " \n" +
                    "GROUP BY em.tipo_operacion,\n" +
                    "em.tipo_riesgo,\n" +
                    "em.nit,\n" +
                    "em.contraparte,\n" +
                    "em.f_alta,\n" +
                    "em.f_vencimiento,\n" +
                    "em.divisa,\n" +
                    "em.vr_nominal_divisa,\n" +
                    "em.mtm_cop,\n" +
                    "em.vr_nominal_cop,\n" +
                    "em.vr_nominal_mtm,\n" +
                    "em.intergrupo,\n" +
                    "em.pais,\n" +
                    "em.neocon,\n" +
                    "em.local_rp21,\n" +
                    "em.div_rp21,\n" +
                    "CAST(DATEDIFF (DAY, f_alta,f_vencimiento) AS FLOAT)/30 ,\n" +
                    "CAST(DATEDIFF (DAY, fecont,f_vencimiento) AS FLOAT)/30 , \n" +
                    "CASE \n" +
                    "WHEN CAST(DATEDIFF (DAY, f_alta,f_vencimiento)AS FLOAT)/30 <= 12.99 THEN '1 Año' \n" +
                    "WHEN CAST(DATEDIFF (DAY, f_alta,f_vencimiento)AS FLOAT)/30 <= 60 THEN '5 Años'\n" +
                    "WHEN CAST(DATEDIFF (DAY, f_alta,f_vencimiento)AS FLOAT)/30 > 60 THEN 'Mas de 5 Años'\n" +
                    "END ,\n" +
                    "CASE \n" +
                    "WHEN CAST(DATEDIFF (DAY, fecont,f_vencimiento)AS FLOAT)/30 <= 12.99 THEN '1 Año' \n" +
                    "WHEN CAST(DATEDIFF (DAY, fecont,f_vencimiento)AS FLOAT)/30 <= 60 THEN '5 Años'\n" +
                    "WHEN CAST(DATEDIFF (DAY, fecont,f_vencimiento)AS FLOAT)/30 > 60 THEN 'Mas de 5 Años'\n" +
                    "END ,\n" +
                    "em.neocon_,\n" +
                    "em.local_derec,\n" +
                    "em.local_obligacion,\n" +
                    "em.pyg,\n" +
                    "em.tipo_contraparte,\n" +
                    "em.yintp,\n" +
                    "DATEDIFF (DAY, fecont,f_vencimiento) ,\n" +
                    "em.operacion,em.dvsaconciliacion1,\n" +
                    "em.dvsaconciliacion2,\n" +
                    "em.fecont,\n" +
                    "nte.tipo_entidad,\n" +
                    "ntes.tipo_entidad,\n" +
                    "ntes1.tipo_entidad,\n"+
                    "nv.eliminacion");
            try {
                String[] month = periodo.split("-");
                query.setParameter(1, month[1]);
                query.setParameter(2, month[0]);
            } catch (Exception e) {
                query.setParameter(1, "0");
                query.setParameter(2, "0");
            }

            return query.getResultList();
        }
    }

    public List<Object[]> insertAllLiquidez(String periodo,Object fw,Object swap,Object rys,Object opc, String futuros,Object fu) {
        String parametros = "";
        if (fw != null) {
            parametros = parametros + " OR ORIGEN ='FORWARD'";
        }
        if (swap != null) {
            parametros = parametros + " OR ORIGEN ='SWAP'";
        }
        if (rys != null) {
            parametros = parametros + " OR ORIGEN ='RYS'";
        }
        if (opc != null) {
            parametros = parametros + " OR ORIGEN ='OPCIONES'";
        }
        if (fu != null) {
            parametros = parametros + " OR ORIGEN ='FUTUROS'";
        }
        if (parametros.length() > 0) {
            parametros = " AND (" + parametros.substring(3, parametros.length()) + ")";
        }

        Query query1 = entityManager.createNativeQuery("INSERT INTO nexco_tipo_entidad (tipo_contraparte,tipo_entidad,eliminacion)\n" +
                "SELECT \n" +
                "em.tipo_contraparte,\n" +
                "'SIN PARAMETRIZAR',0\n" +
                "FROM nexco_reporte_rp21 as em\n" +
                "LEFT JOIN (SELECT tipo_contraparte,eliminacion,tipo_entidad,intergrupo FROM nexco_tipo_entidad) nv ON em.tipo_contraparte = nv.tipo_contraparte \n" +
                "LEFT JOIN (SELECT tipo_contraparte,eliminacion,tipo_entidad,intergrupo FROM nexco_tipo_entidad WHERE  (intergrupo IS NULL) AND (nit IS NULL OR nit = '')) nte ON em.tipo_contraparte = nte.tipo_contraparte\n" +
                "LEFT JOIN (SELECT tipo_contraparte,eliminacion,tipo_entidad,intergrupo FROM nexco_tipo_entidad WHERE intergrupo IS NOT NULL AND (nit IS NULL OR nit='')) ntes ON em.tipo_contraparte = ntes.tipo_contraparte AND CASE em.intergrupo WHEN 'No' THEN 0 ELSE 1 END = ntes.intergrupo\n" +
                "LEFT JOIN (SELECT tipo_contraparte,eliminacion,tipo_entidad,nit,intergrupo FROM nexco_tipo_entidad WHERE intergrupo IS NOT NULL AND nit IS NOT NULL AND nit !='') ntes1 ON em.tipo_contraparte = ntes1.tipo_contraparte AND em.nit = ntes1.nit AND CASE em.intergrupo WHEN 'No' THEN 0 ELSE 1 END = ntes1.intergrupo\n" +
                "WHERE  MONTH(em.fecont) = ? AND YEAR(em.fecont) = ? " + parametros + " AND ntes1.tipo_entidad IS NULL AND ntes.tipo_entidad IS NULL AND nte.tipo_entidad IS NULL \n" +
                "GROUP BY em.tipo_contraparte");
        try {
            String[] month = periodo.split("-");
            query1.setParameter(1, month[1]);
            query1.setParameter(2, month[0]);
        } catch (Exception e) {
            query1.setParameter(1, "0");
            query1.setParameter(2, "0");
        }

        query1.executeUpdate();

        Query query2 = entityManager.createNativeQuery("SELECT em.tipo_contraparte,\n" +
                "em.nit,\n" +
                "em.contraparte,\n" +
                "em.intergrupo,\n" +
                "'Revisar, residentes exterior',\n" +
                "nv.eliminacion \n" +
                "FROM nexco_reporte_rp21 as em\n" +
                "INNER JOIN (SELECT tipo_contraparte,eliminacion,tipo_entidad,intergrupo FROM nexco_tipo_entidad) nv ON em.tipo_contraparte = nv.tipo_contraparte \n" +
                "LEFT JOIN (SELECT tipo_contraparte,eliminacion,tipo_entidad,intergrupo FROM nexco_tipo_entidad WHERE (intergrupo IS NULL) AND (nit IS NULL OR nit = '')) nte ON em.tipo_contraparte = nte.tipo_contraparte\n" +
                "LEFT JOIN (SELECT tipo_contraparte,eliminacion,tipo_entidad,intergrupo FROM nexco_tipo_entidad WHERE intergrupo IS NOT NULL AND (nit IS NULL OR nit='')) ntes ON em.tipo_contraparte = ntes.tipo_contraparte AND CASE em.intergrupo WHEN 'No' THEN 0 ELSE 1 END = ntes.intergrupo\n" +
                "LEFT JOIN (SELECT tipo_contraparte,eliminacion,tipo_entidad,nit,intergrupo FROM nexco_tipo_entidad WHERE intergrupo IS NOT NULL AND nit IS NOT NULL AND nit !='') ntes1 ON em.tipo_contraparte = ntes1.tipo_contraparte AND em.nit = ntes1.nit AND CASE em.intergrupo WHEN 'No' THEN 0 ELSE 1 END = ntes1.intergrupo\n" +
                "WHERE  MONTH(em.fecont) = '10' AND YEAR(em.fecont) = '2021' AND nv.eliminacion = '0' " + parametros + " AND \n" +
                "((nte.tipo_entidad ='Revisar, residentes exterior' AND ntes.tipo_entidad IS NULL AND ntes1.tipo_entidad IS NULL) OR \n" +
                "(nte.tipo_entidad IS NULL AND ntes.tipo_entidad = 'Revisar, residentes exterior' AND ntes1.tipo_entidad IS NULL) OR \n" +
                "(nte.tipo_entidad IS NULL AND ntes.tipo_entidad IS NULL AND ntes1.tipo_entidad = 'Revisar, residentes exterior'))\n" +
                "GROUP BY\n" +
                "em.tipo_contraparte,\n" +
                "em.nit,\n" +
                "em.contraparte,\n" +
                "em.intergrupo,\n" +
                "nv.eliminacion\n");

        List<Object[]> list= new ArrayList<>();
        list=query2.getResultList();
        return list;


        /*

        Query query = entityManager.createNativeQuery("INSERT INTO nexco_tipo_entidad (tipo_contraparte,nit,contraparte,intergrupo,tipo_entidad,eliminacion)\n" +
                "SELECT em.tipo_contraparte,\n" +
                "em.nit,\n" +
                "em.contraparte,\n" +
                "CASE em.intergrupo WHEN 'No'THEN 0 ELSE 1 END,\n" +
                "'Revisar, residente exterior .',\n" +
                "0 \n" +
                "FROM nexco_reporte_rp21 as em\n" +
                "INNER JOIN (SELECT tipo_contraparte,eliminacion,tipo_entidad,intergrupo FROM nexco_tipo_entidad WHERE eliminacion = 0) nv ON em.tipo_contraparte = nv.tipo_contraparte \n" +
                "LEFT JOIN (SELECT tipo_contraparte,eliminacion,tipo_entidad,intergrupo FROM nexco_tipo_entidad WHERE eliminacion = 0 AND (intergrupo IS NULL) AND (nit IS NULL OR nit = '')) nte ON em.tipo_contraparte = nte.tipo_contraparte\n" +
                "LEFT JOIN (SELECT tipo_contraparte,eliminacion,tipo_entidad,intergrupo FROM nexco_tipo_entidad WHERE eliminacion = 0 AND intergrupo IS NOT NULL AND (nit IS NULL OR nit='')) ntes ON em.tipo_contraparte = ntes.tipo_contraparte AND CASE em.intergrupo WHEN 'No' THEN 0 ELSE 1 END = ntes.intergrupo\n" +
                "LEFT JOIN (SELECT tipo_contraparte,eliminacion,tipo_entidad,nit,intergrupo FROM nexco_tipo_entidad WHERE eliminacion = 0 AND intergrupo IS NOT NULL AND nit IS NOT NULL AND nit !='') ntes1 ON em.tipo_contraparte = ntes1.tipo_contraparte AND em.nit = ntes1.nit AND CASE em.intergrupo WHEN 'No' THEN 0 ELSE 1 END = ntes1.intergrupo\n" +
                "WHERE  MONTH(em.fecont) = ? AND YEAR(em.fecont) = ? AND (ntes.tipo_entidad = 'Revisar, residente exterior' OR ntes1.tipo_entidad = 'Revisar, residente exterior' OR nte.tipo_entidad = 'Revisar, residente exterior')\n" +
                "GROUP BY em.tipo_contraparte,\n" +
                "em.nit,\n" +
                "em.contraparte,\n" +
                "em.intergrupo\n");
        try {
            String[] month = periodo.split("-");
            query.setParameter(1, month[1]);
            query.setParameter(2, month[0]);
        } catch (Exception e) {
            query.setParameter(1, "0");
            query.setParameter(2, "0");
        }

        query.executeUpdate();*/

    }

    public List<Object[]> getAllReport (String todayString, int vista)
    {
        if(vista==1)
        {
            /*Query query = entityManager.createNativeQuery("SELECT em.local_rp21, em.yintp, em.divisa,CAST(SUM(vr_nominal_divisa) AS bigint) AS nomd,CAST(SUM(mtm_cop) AS bigint) AS mtm,CAST(SUM(vr_nominal_cop) AS bigint) AS cop," +
                    "CAST(SUM(vr_nominal_mtm) AS bigint) AS nmtm FROM nexco_reporte_rp21 as em WHERE MONTH(em.fecont) = ? AND YEAR(em.fecont) = ? GROUP BY em.local_rp21,em.yintp,em.divisa");*/

            Query query = entityManager.createNativeQuery("SELECT em.local_rp21, em.yintp, em.div_rp21 ,CAST(SUM(vr_nominal_divisa) AS NUMERIC) AS nomd,  CAST(SUM(mtm_cop) AS NUMERIC) AS mtm,  CAST(SUM(vr_nominal_cop) AS NUMERIC) AS cop," +
                    "  CAST(SUM(vr_nominal_mtm) AS NUMERIC) AS nmtm FROM nexco_reporte_rp21 as em WHERE MONTH(em.fecont) = ? AND YEAR(em.fecont) = ? GROUP BY em.local_rp21,em.yintp, em.div_rp21");
            try{
                String[] month = todayString.split("-");
                query.setParameter(1,month[1]);
                query.setParameter(2,month[0]);
            }
            catch (Exception e){
                query.setParameter(1,"0");
                query.setParameter(2,"0");
            }
            return query.getResultList();
        }
        else
        {
            Query query = entityManager.createNativeQuery("SELECT em.local_rp21, em.yintp, em.divisa,SUM(vr_nominal_divisa) AS nomd, SUM(mtm_cop)  AS mtm, SUM(vr_nominal_cop) AS cop," +
                    "SUM(vr_nominal_mtm)  AS nmtm FROM nexco_reporte_rp21 as em WHERE MONTH(em.fecont) = ? AND YEAR(em.fecont) = ? GROUP BY em.local_rp21,em.yintp,em.divisa");
            try{
                String[] month = todayString.split("-");
                query.setParameter(1,month[1]);
                query.setParameter(2,month[0]);
            }
            catch (Exception e){
                query.setParameter(1,"0");
                query.setParameter(2,"0");
            }
            return query.getResultList();
        }
    }

    public Rp21 findRp21ByIdReporte(Long id){
        return rp21Repository.findByIdReporte(id);
    }

    public Rp21 saveRp21(Rp21 rp21){
        return rp21Repository.save(rp21);
    }

    public void removeRp21(Long id){
        rp21Repository.deleteById(id);
    }

    public Page<Rp21> getAll(Pageable pageable){
        return rp21Repository.findAll(pageable);
    }

    public List<ControlPanel> validateQueryAndVertical(String periodo, String responsable){
        Query query = entityManager.createNativeQuery("SELECT * FROM nexco_cuadro_mando WHERE fecha_reporte = ? AND responsable = ? AND componente = ?;", ControlPanel.class);
        query.setParameter(1,periodo);
        query.setParameter(2,responsable);
        query.setParameter(3,"DERIVADOS");
        List<ControlPanel> listControlPanel = query.getResultList();
        return listControlPanel;
    }

/*    public List<Object[]> getListRp21(String periodo){
        Query query = entityManager.createNativeQuery("SELECT local_rp21, div_rp21, CAST(SUM(vr_nominal_cop) AS NUMERIC) AS suma " +
                "FROM nexco_reporte_rp21 WHERE CONVERT(varchar(7),fecont,120) = ? GROUP BY local_rp21, div_rp21;");
        query.setParameter(1,periodo);
        List<Object[]> listRp21 = query.getResultList();
        return listRp21;
    }*/

    public List<Object[]> getListQuery(String periodo, String empresa){
        Query query = entityManager.createNativeQuery("SELECT nucta,coddiv, CAST(SUM(salmes) AS NUMERIC)  AS suma FROM nexco_query WHERE fecont = ? AND empresa = ? GROUP BY empresa,nucta,coddiv;");
        query.setParameter(1,periodo);
        query.setParameter(2,empresa);
        List<Object[]> listQuery = query.getResultList();
        return listQuery;
    }

    public List<Object[]> validateRp21AndQuery(String periodo, String responsable, String empresa){

        String monthPeriodo = periodo.split("-")[1];
        String yearPeriodo = periodo.split("-")[0];

        String nameTable ="";
        String tempValue ="";
        Query verify = entityManager.createNativeQuery("SELECT TOP 1 * FROM nexco_query_marcados WHERE origen = 'LOCAL' AND fecont LIKE ? ");
        verify.setParameter(1,periodo+"%");

        if(verify.getResultList().isEmpty())
        {
            nameTable ="nexco_query";
            tempValue ="";
        }
        else
        {
            nameTable ="nexco_query_marcados";
            tempValue ="origen = 'LOCAL' AND";
        }

        Query result = entityManager.createNativeQuery("SELECT a.cuenta_local, ISNULL(b.divisa, C.coddiv) divisa, CAST(ISNULL(b.saldo_rp, 0) AS FLOAT) saldo_rp,\n" +
                "abs(CAST(ISNULL(c.saldo_query, 0) AS FLOAT)) saldo_query, \n" +
                "CAST(ISNULL(b.saldo_rp, 0)-abs(isnull(c.saldo_query, 0)) AS FLOAT) diferencia,c.fechproce \n" +
                "FROM nexco_operacion_riesgo a\n" +
                "LEFT JOIN (SELECT fecont, local_rp21 cuenta_rp21, div_rp21 divisa, sum(vr_nominal_cop) saldo_rp\n" +
                "FROM nexco_reporte_rp21 \n" +
                "where local_rp21 != ''\n" +
                "AND MONTH(fecont) = ? AND YEAR(fecont) = ?\n" +
                "group by fecont, local_rp21, div_rp21) b\n" +
                "on a.cuenta_local = b.cuenta_rp21\n" +
                "LEFT JOIN (SELECT fecont,fechproce, nucta, coddiv, sum(saldoquery) saldo_query\n" +
                "FROM "+nameTable+" \n" +
                "where "+tempValue+" \n" +
                "substring(fecont, 1, 7) = ? and empresa = ?\n" +
                "group by fecont,fechproce, nucta, coddiv) c\n" +
                "on a.cuenta_local = c.nucta\n" +
                "where CAST(ISNULL(b.saldo_rp, 0)-isnull(c.saldo_query, 0) AS FLOAT) NOT BETWEEN 0 and 1000\n" +
                "ORDER BY 2 asc\n" +
                ";");

        result.setParameter(1,monthPeriodo);
        result.setParameter(2,yearPeriodo);
        result.setParameter(3,periodo);
        result.setParameter(4,empresa);

        return result.getResultList();

    }

    public List<Object[]> validateRp21AndQueryRes(String periodo, String responsable, String empresa){

        String monthPeriodo = periodo.split("-")[1];
        String yearPeriodo = periodo.split("-")[0];

        String nameTable ="";
        String tempValue ="";
        Query verify = entityManager.createNativeQuery("SELECT TOP 1 * FROM nexco_query_marcados WHERE origen = 'LOCAL' AND fecont LIKE ? ");
        verify.setParameter(1,periodo+"%");

        if(verify.getResultList().isEmpty())
        {
            nameTable ="nexco_query";
            tempValue ="";
        }
        else
        {
            nameTable ="nexco_query_marcados";
            tempValue ="origen = 'LOCAL' AND";
        }

        Query result = entityManager.createNativeQuery("SELECT '"+periodo+"' fecont, a.cuenta_local, ISNULL(b.divisa, C.coddiv) divisa, a.tipo_operacion, CAST(ISNULL(b.saldo_rp, 0) AS FLOAT) saldo_rp,\n" +
                "abs(CAST(ISNULL(c.saldo_query, 0) AS FLOAT)) saldo_query, \n" +
                "CAST(ISNULL(b.saldo_rp, 0)-abs(isnull(c.saldo_query, 0)) AS FLOAT) diferencia, c.fechproce  \n" +
                "FROM nexco_operacion_riesgo a\n" +
                "LEFT JOIN (SELECT fecont, local_rp21 cuenta_rp21, div_rp21 divisa, sum(vr_nominal_cop) saldo_rp\n" +
                "FROM nexco_reporte_rp21 \n" +
                "where local_rp21 != ''\n" +
                "AND MONTH(fecont) = ? AND YEAR(fecont) = ?\n" +
                "group by fecont, local_rp21, div_rp21) b\n" +
                "on a.cuenta_local = b.cuenta_rp21\n" +
                "LEFT JOIN (SELECT fecont, fechproce,nucta, coddiv, sum(saldoquery) saldo_query\n" +
                "FROM "+nameTable+" \n" +
                "where "+tempValue+" \n" +
                "substring(fecont, 1, 7) = ? and empresa = ?\n" +
                "group by fecont, fechproce, nucta, coddiv) c\n" +
                "on a.cuenta_local = c.nucta\n" +
                "ORDER BY 2 asc\n" +
                ";");

        result.setParameter(1,monthPeriodo);
        result.setParameter(2,yearPeriodo);
        result.setParameter(3,periodo);
        result.setParameter(4,empresa);

        return result.getResultList();

    }

    public List<Object[]> getCompany(String periodo){

        String nameTable ="";
        Query verify = entityManager.createNativeQuery("SELECT TOP 1 * FROM nexco_query_marcados WHERE origen = 'LOCAL' AND fecont LIKE ? ");
        verify.setParameter(1,periodo+"%");

        if(verify.getResultList().isEmpty())
        {
            nameTable ="nexco_query";
        }
        else
        {
            nameTable ="nexco_query_marcados";
        }
        Query companies = entityManager.createNativeQuery(
                "select distinct empresa from "+nameTable+" where substring(fecont, 1, 7) = ?");

        companies.setParameter(1,periodo);

        return companies.getResultList();


    }

    public void updateState(List<ControlPanel> controlPanelList, String state){
        for (ControlPanel panel: controlPanelList) {
            Query query = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando SET semaforo_input = ? , semaforo_componente = ?" +
                    " WHERE responsable = ? AND input = ? AND componente = ? AND fecha_reporte = ?", ControlPanel.class);
            query.setParameter(1, state);
            query.setParameter(2, state);
            query.setParameter(3, panel.getResponsable());
            query.setParameter(4, panel.getInput());
            query.setParameter(5, panel.getComponente());
            query.setParameter(6, panel.getFechaReporte());
            query.executeUpdate();
        }
    }

    public void saveRp21Temporal(List<Rp21Temporal> rp21){
        rp21TemporalRepository.saveAll(rp21);
    }

    public void deleteByOrigen(String origen) {
        Query deleteAll = entityManager.createNativeQuery("DELETE FROM nexco_reporte_rp21_temporal where origen = ?");
        deleteAll.setParameter(1,origen);
        deleteAll.executeUpdate();
    }

    public boolean insertIntoIntergroup(String periodo, String futuros) {

        String monthPeriodo = periodo.split("-")[1];
        String yearPeriodo = periodo.split("-")[0];

        try{

            Query deleteAll = entityManager.createNativeQuery("DELETE FROM nexco_intergrupo_v1 where periodo = ? and fuente = ?");
            deleteAll.setParameter(1,periodo);
            deleteAll.setParameter(2,"RP21");
            deleteAll.executeUpdate();

            if(futuros.equals("true")){

                Query insertAll = entityManager.createNativeQuery("insert into nexco_intergrupo_v1 (yntp_empresa_reportante, yntp, periodo, nit, divisa, cuenta_local, contrato, cod_neocon, cod_pais, pais, sociedad_yntp, valor, fuente)\n" +
                        "select '00548', a.yintp, '"+periodo+"', substring(a.nit, 4, 9), a.div_rp21, a.local_rp21, a.operacion, a.neocon, b.id_pais, a.pais, a.contraparte, a.vr_nominal_cop, ? \n" +
                        "from nexco_reporte_rp21 a \n" +
                        "left join nexco_paises b on a.pais = b.nombre_pais \n" +
                        "where MONTH(a.fecont) = ? AND YEAR(a.fecont) = ? \n" +
                        "AND a.intergrupo = 'Si' \n" +
                        "AND isnull(a.clase_futuros, '') <> 'CCRC NDF' \n" +
                        "AND a.local_rp21 <> '-'");
                insertAll.setParameter(1,"RP21");
                insertAll.setParameter(2,monthPeriodo);
                insertAll.setParameter(3,yearPeriodo);
                insertAll.executeUpdate();

                Query insertAll2 = entityManager.createNativeQuery("insert into nexco_intergrupo_v1 (yntp_empresa_reportante, yntp, periodo, nit, divisa, cuenta_local, contrato, cod_neocon, cod_pais, pais, sociedad_yntp, valor, fuente)\n" +
                        "select '00548', a.yintp, '"+periodo+"', substring(a.nit, 4, 9), case when a.div_rp21 = '' then a.dvsaconciliacion1 else a.div_rp21 end, a.local_derec, a.operacion, a.neocon_, b.id_pais, a.pais, a.contraparte, case when a.origen = 'RYS' THEN a.vr_nominal_mtm else a.mtm_cop end, ? \n" +
                        "from nexco_reporte_rp21 a \n" +
                        "left join nexco_paises b on a.pais = b.nombre_pais \n" +
                        "where MONTH(a.fecont) = ? AND YEAR(a.fecont) = ? \n" +
                        "AND a.intergrupo = 'Si' \n" +
                        "AND isnull(a.clase_futuros, '') <> 'CCRC NDF'");
                insertAll2.setParameter(1,"RP21");
                insertAll2.setParameter(2,monthPeriodo);
                insertAll2.setParameter(3,yearPeriodo);
                insertAll2.executeUpdate();

            }else{

                Query insertAll = entityManager.createNativeQuery("insert into nexco_intergrupo_v1 (yntp_empresa_reportante, yntp, periodo, nit, divisa, cuenta_local, contrato, cod_neocon, cod_pais, pais, sociedad_yntp, valor, fuente)\n" +
                        "select '00548', a.yintp, '"+periodo+"', substring(a.nit, 4, 9), a.div_rp21, a.local_rp21, a.operacion, a.neocon, b.id_pais, a.pais, a.contraparte, a.vr_nominal_cop, ? \n" +
                        "from nexco_reporte_rp21 a \n" +
                        "left join nexco_paises b on a.pais = b.nombre_pais \n" +
                        "where MONTH(a.fecont) = ? AND YEAR(a.fecont) = ? \n" +
                        "AND a.intergrupo = 'Si' \n" +
                        "AND a.local_rp21 <> '-'");
                insertAll.setParameter(1,"RP21");
                insertAll.setParameter(2,monthPeriodo);
                insertAll.setParameter(3,yearPeriodo);
                insertAll.executeUpdate();

                Query insertAll2 = entityManager.createNativeQuery("insert into nexco_intergrupo_v1 (yntp_empresa_reportante, yntp, periodo, nit, divisa, cuenta_local, contrato, cod_neocon, cod_pais, pais, sociedad_yntp, valor, fuente)\n" +
                        "select '00548', a.yintp, '"+periodo+"', substring(a.nit, 4, 9), case when a.div_rp21 = '' then a.dvsaconciliacion1 else a.div_rp21 end, a.local_derec, a.operacion, a.neocon_, b.id_pais, a.pais, a.contraparte, case when a.origen = 'RYS' THEN a.vr_nominal_mtm else a.mtm_cop end, ? \n" +
                        "from nexco_reporte_rp21 a \n" +
                        "left join nexco_paises b on a.pais = b.nombre_pais \n" +
                        "where MONTH(a.fecont) = ? AND YEAR(a.fecont) = ? \n" +
                        "AND a.intergrupo = 'Si'");
                insertAll2.setParameter(1,"RP21");
                insertAll2.setParameter(2,monthPeriodo);
                insertAll2.setParameter(3,yearPeriodo);
                insertAll2.executeUpdate();

            }

            Query updateAll = entityManager.createNativeQuery("update nexco_cuadro_mando \n" +
                    "set semaforo_componente = 'FULL', estado = 0\n" +
                    "where componente = 'DERIVADOS' \n" +
                    "and fecha_reporte = ?");
            updateAll.setParameter(1,periodo);
            updateAll.executeUpdate();

            Query updateAll2 = entityManager.createNativeQuery("update nexco_cuadro_mando \n" +
                    "SET semaforo_input = 'FULL', estado = 0,fecha_carga = ? \n" +
                    "WHERE componente = 'DERIVADOS' \n" +
                    "AND fecha_reporte = ? AND semaforo_componente = 'FULL' AND semaforo_input != 'EMPTY' ");
            updateAll2.setParameter(1,new Date());
            updateAll2.setParameter(2,periodo);
            updateAll2.executeUpdate();

            Query updateAll3 = entityManager.createNativeQuery("update a set componente = 'DERIVADOS', input=b.origen \n" +
                    "from (select * from nexco_intergrupo_v1 where periodo=?) a, (select operacion,origen from nexco_reporte_rp21 where year(fecont)=? and month(fecont)=?) b \n" +
                    "where a.contrato=b.operacion and a.componente is null and a.fuente='RP21'");
            updateAll3.setParameter(1,periodo);
            updateAll3.setParameter(2,yearPeriodo);
            updateAll3.setParameter(3,monthPeriodo);
            updateAll3.executeUpdate();

            Query updateAll4 = entityManager.createNativeQuery("update nexco_intergrupo_v1 set input='FW'\n" +
                    "where input='FORWARD'");
            updateAll4.executeUpdate();
        }
        catch (Exception e){
            return false;
        }

        return true;
    }

    public boolean deleteInputRP21(String periodo, String input) {

        String monthPeriodo = periodo.split("-")[1];
        String yearPeriodo = periodo.split("-")[0];
        String inputTable = "";
        String inputTableC = "";

        if(input.equals("fw")){
            inputTable = "FORWARD";
            inputTableC = "FW";
        }else if(input.equals("op")){
            inputTable = "OPCIONES";
            inputTableC = "OPCIONES";
        }else if(input.equals("rs")){
            inputTable = "RYS";
            inputTableC = "RYS";
        }else if(input.equals("sw")){
            inputTable = "SWAP";
            inputTableC = "SWAP";
        }else if(input.equals("fu")){
            inputTable = "FUTUROS";
            inputTableC = "FUTUROS";
        }

        try{

            Query deleteAll = entityManager.createNativeQuery("DELETE FROM nexco_reporte_rp21 " +
                    "where origen = ? \n" +
                    "and MONTH(fecont) = ? AND YEAR(fecont) = ?");
            deleteAll.setParameter(1,inputTable);
            deleteAll.setParameter(2,monthPeriodo);
            deleteAll.setParameter(3,yearPeriodo);

            deleteAll.executeUpdate();

            Query updateAll2 = entityManager.createNativeQuery("update nexco_cuadro_mando \n" +
                    "set semaforo_input = 'EMPTY' \n" +
                    "where input = ? \n" +
                    "and fecha_reporte = ?");
            updateAll2.setParameter(1,inputTableC);
            updateAll2.setParameter(2,periodo);
            updateAll2.executeUpdate();
        }
        catch (Exception e){
            return false;
        }

        return true;
    }

    public boolean validateDivisas(String period){

        String monthPeriod = period.split("-")[1];
        String yearPeriod = period.split("-")[0];

        Query query = entityManager.createNativeQuery("select * from nexco_divisas_valor where month(fecha) = ? and year(fecha) = ?");
        query.setParameter(1,monthPeriod);
        query.setParameter(2,yearPeriod);
        List<Object[]> divisas = query.getResultList();

        if(divisas.size() == 0){
            return false;
        }else{
            return true;
        }

    }
}
