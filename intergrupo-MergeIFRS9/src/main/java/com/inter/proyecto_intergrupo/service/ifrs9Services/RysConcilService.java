package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.ControlPanel;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.RiskRys;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.model.reports.ContingentIntergroup;
import com.inter.proyecto_intergrupo.model.ifrs9.RysConcil;
import com.inter.proyecto_intergrupo.model.reports.ReclassificationContingent;
import com.inter.proyecto_intergrupo.model.reports.ReclassificationContingentCom;
import com.inter.proyecto_intergrupo.model.temporal.ContingentTemporal;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.RiskRysRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.RysConcilRepository;
import com.inter.proyecto_intergrupo.repository.parametric.*;
import com.inter.proyecto_intergrupo.repository.ifrs9.RysConcilRepository;
import com.inter.proyecto_intergrupo.repository.reports.ReclassificationContingentRepository;
import com.inter.proyecto_intergrupo.repository.temporal.ContingentTemporalRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.Part;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class RysConcilService {

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private RysConcilRepository rysConcilRepository;

    @Autowired
    private RiskRysRepository riskRysRepository;

    @PersistenceContext
    EntityManager entityManager;

    public List<String[]> saveFileBD(Collection<Part>  parts, User user, String periodo) throws IOException, InvalidFormatException
    {
        List<String[]> listRisk=new ArrayList<String[]>();

        Iterator<Row> rowsRisk = null;

        for(Part part : parts) {
            InputStream file = part.getInputStream();
            if (part.getSubmittedFileName()!=null && file != null)
            {
                XSSFWorkbook wb = new XSSFWorkbook(file);

                XSSFSheet sheetTipoAval = wb.getSheetAt(0);
                rowsRisk = sheetTipoAval.iterator();
                listRisk = validarPlantilla(rowsRisk,user, periodo);
            }
        }

        return listRisk;
    }

    public List<String[]> validarPlantilla(Iterator<Row> rows, User user, String periodo) {
        ArrayList<String[]> lista= new ArrayList<String[]>();
        ArrayList<RiskRys> listaAdd= new ArrayList<RiskRys>();
        XSSFRow row;
        int fail=0;
        int failCount=0;
        int success =0;
        while (rows.hasNext())
        {
            failCount=lista.size();
            row = (XSSFRow) rows.next();
            if(row.getRowNum()>3)
            {
                String[] log1=new String[4];
                log1[2]="true";

                DataFormatter formatter = new DataFormatter();
                String cellNumeroPapeleta = formatter.formatCellValue(row.getCell(0)).trim();
                String cellCodNombre = formatter.formatCellValue(row.getCell(1)).trim();
                String cellCodPuc = formatter.formatCellValue(row.getCell(2)).trim();
                String cellFecha = formatter.formatCellValue(row.getCell(3)).trim();
                String cellFechaFinal = formatter.formatCellValue(row.getCell(4)).trim();
                String cellValorTotal = formatter.formatCellValue(row.getCell(5)).trim();
                String cellIntereses = formatter.formatCellValue(row.getCell(6)).trim();
                String cellCausacionHoy = formatter.formatCellValue(row.getCell(7)).trim();
                String cellMoneda = formatter.formatCellValue(row.getCell(8)).trim();
                String cellFechaCorte = formatter.formatCellValue(row.getCell(9)).trim();
                String cellExposicion = formatter.formatCellValue(row.getCell(10)).trim();
                String cellDiasAlVto = formatter.formatCellValue(row.getCell(11)).trim();
                String cellTasaDescuento = formatter.formatCellValue(row.getCell(12)).trim();
                String cellFd = formatter.formatCellValue(row.getCell(13)).trim();
                String cellValorPresente = formatter.formatCellValue(row.getCell(14)).trim();
                String cellDiferencia= formatter.formatCellValue(row.getCell(15)).trim();

                Date fechaDate = new Date();
                Date fechaDateF = new Date();
                Date fechaDateC = new Date();
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
                formato.applyPattern("dd/MM/yyyy");
                String[] loadDate = new String[2];
                try
                {
                    log1[1]=CellReference.convertNumToColString(5)+" - (6)";
                    XSSFCell cell0= row.getCell(5);
                    cell0.setCellType(CellType.STRING);
                    cellValorTotal = formatter.formatCellValue(cell0).replace(" ", "");
                    Double.parseDouble(cellValorTotal);

                    log1[1]=CellReference.convertNumToColString(6)+" - (7)";
                    XSSFCell cell1= row.getCell(6);
                    cell1.setCellType(CellType.STRING);
                    cellIntereses = formatter.formatCellValue(cell1).replace(" ", "");
                    Double.parseDouble(cellIntereses);

                    log1[1]=CellReference.convertNumToColString(7)+" - (8)";
                    XSSFCell cell2= row.getCell(7);
                    cell2.setCellType(CellType.STRING);
                    cellCausacionHoy = formatter.formatCellValue(cell2).replace(" ", "");
                    Double.parseDouble(cellCausacionHoy);

                    log1[1]=CellReference.convertNumToColString(12)+" - (13)";
                    XSSFCell cell3= row.getCell(12);
                    cell3.setCellType(CellType.STRING);
                    cellTasaDescuento = formatter.formatCellValue(cell3).replace(" ", "");
                    Double.parseDouble(cellTasaDescuento);

                    log1[1]=CellReference.convertNumToColString(14)+" - (15)";
                    XSSFCell cell4= row.getCell(14);
                    cell4.setCellType(CellType.STRING);
                    cellValorPresente = formatter.formatCellValue(cell4).replace(" ", "");
                    Double.parseDouble(cellValorPresente);

                    log1[1]=CellReference.convertNumToColString(15)+" - (16)";
                    XSSFCell cell5= row.getCell(15);
                    cell5.setCellType(CellType.STRING);
                    cellDiferencia = formatter.formatCellValue(cell5).replace(" ", "");
                    Double.parseDouble(cellDiferencia);

                    log1[1]=CellReference.convertNumToColString(3)+" - (4)"; fechaDate = formato.parse(cellFecha);
                    log1[1]=CellReference.convertNumToColString(4)+" - (5)"; fechaDateF = formato.parse(cellFechaFinal);
                    log1[1]=CellReference.convertNumToColString(9)+" - (10)"; fechaDateC = formato.parse(cellFechaCorte);

                    calendar.setTime(fechaDate);
                    calendar.setTime(fechaDateF);
                    calendar.setTime(fechaDateC);
                    loadDate=periodo.split("-");
                }
                catch(Exception e){
                    fail++;
                    log1[0]=String.valueOf(row.getRowNum()+1);
                    log1[2]="false";
                    log1[3]="Valide el tipo de dato, es inválido";
                    lista.add(log1);
                }
                if(cellNumeroPapeleta.isEmpty() || cellNumeroPapeleta.isBlank())
                {
                    String[] log=new String[4];
                    log[0]=String.valueOf(row.getRowNum()+1);
                    log[1]=CellReference.convertNumToColString(0)+" - (1)";
                    log[2]="false";
                    log[3]="Falló Número de Papeleta no puede ingresarse vacía";
                    fail++;
                    lista.add(log);
                }
                if(cellCodNombre.isEmpty() || cellCodNombre.isBlank())
                {
                    String[] log=new String[4];
                    log[0]=String.valueOf(row.getRowNum()+1);
                    log[1]=CellReference.convertNumToColString(1)+" - (2)";
                    log[2]="false";
                    log[3]="Falló Código Nombre no puede ingresarse vacío";
                    fail++;
                    lista.add(log);
                }
                if(cellCodPuc.isEmpty() || cellCodPuc.isBlank())
                {
                    String[] log=new String[4];
                    log[0]=String.valueOf(row.getRowNum()+1);
                    log[1]=CellReference.convertNumToColString(2)+" - (3)";
                    log[2]="false";
                    log[3]="Falló Código PUC no puede ingresarse vacío";
                    fail++;
                    lista.add(log);
                }
                if(cellFecha.isEmpty() || cellFecha.isBlank())
                {
                    String[] log=new String[4];
                    log[0]=String.valueOf(row.getRowNum()+1);
                    log[1]=CellReference.convertNumToColString(3)+" - (4)";
                    log[2]="false";
                    log[3]="Falló Fecha no puede ingresarse vacía";
                    fail++;
                    lista.add(log);
                }
                if(cellFechaFinal.isEmpty() || cellFechaFinal.isBlank())
                {
                    String[] log=new String[4];
                    log[0]=String.valueOf(row.getRowNum()+1);
                    log[1]=CellReference.convertNumToColString(4)+" - (5)";
                    log[2]="false";
                    log[3]="Falló Fecha Final no puede ingresarse vacía";
                    fail++;
                    lista.add(log);
                }
                if(cellValorTotal.isEmpty() || cellValorTotal.isBlank())
                {
                    String[] log=new String[4];
                    log[0]=String.valueOf(row.getRowNum()+1);
                    log[1]=CellReference.convertNumToColString(5)+" - (6)";
                    log[2]="false";
                    log[3]="Falló Valor Total no puede ingresarse vacío";
                    fail++;
                    lista.add(log);
                }
                if(cellIntereses.isEmpty() || cellIntereses.isBlank())
                {
                    String[] log=new String[4];
                    log[0]=String.valueOf(row.getRowNum()+1);
                    log[1]=CellReference.convertNumToColString(6)+" - (7)";
                    log[2]="false";
                    log[3]="Falló Intereses no puede ingresarse vacío";
                    fail++;
                    lista.add(log);
                }
                if(cellCausacionHoy.isEmpty() || cellCausacionHoy.isBlank())
                {
                    String[] log=new String[4];
                    log[0]=String.valueOf(row.getRowNum()+1);
                    log[1]=CellReference.convertNumToColString(7)+" - (8)";
                    log[2]="false";
                    log[3]="Falló Causación Hoy no puede ingresarse vacío";
                    fail++;
                    lista.add(log);
                }
                if(cellMoneda.isEmpty() || cellMoneda.isBlank())
                {
                    String[] log=new String[4];
                    log[0]=String.valueOf(row.getRowNum()+1);
                    log[1]=CellReference.convertNumToColString(8)+" - (9)";
                    log[2]="false";
                    log[3]="Falló Moneda Hoy no puede ingresarse vacío";
                    fail++;
                    lista.add(log);
                }
                if(cellFechaCorte.isEmpty() || cellFechaCorte.isBlank())
                {
                    String[] log=new String[4];
                    log[0]=String.valueOf(row.getRowNum()+1);
                    log[1]=CellReference.convertNumToColString(9)+" - (10)";
                    log[2]="false";
                    log[3]="Falló Fecha Corte no puede ingresarse vacío";
                    fail++;
                    lista.add(log);
                }
                if(cellExposicion.isEmpty() || cellExposicion.isBlank())
                {
                    String[] log=new String[4];
                    log[0]=String.valueOf(row.getRowNum()+1);
                    log[1]=CellReference.convertNumToColString(10)+" - (11)";
                    log[2]="false";
                    log[3]="Falló Exposición no puede ingresarse vacío";
                    fail++;
                    lista.add(log);
                }
                if(cellDiasAlVto.isEmpty() || cellDiasAlVto.isBlank())
                {
                    String[] log=new String[4];
                    log[0]=String.valueOf(row.getRowNum()+1);
                    log[1]=CellReference.convertNumToColString(11)+" - (12)";
                    log[2]="false";
                    log[3]="Falló Días al Vto no puede ingresarse vacío";
                    fail++;
                    lista.add(log);
                }
                if(cellTasaDescuento.isEmpty() || cellTasaDescuento.isBlank())
                {
                    String[] log=new String[4];
                    log[0]=String.valueOf(row.getRowNum()+1);
                    log[1]=CellReference.convertNumToColString(12)+" - (13)";
                    log[2]="false";
                    log[3]="Falló Tasa Descuento no puede ingresarse vacío";
                    fail++;
                    lista.add(log);
                }
                if(cellFd.isEmpty() || cellFd.isBlank())
                {
                    String[] log=new String[4];
                    log[0]=String.valueOf(row.getRowNum()+1);
                    log[1]=CellReference.convertNumToColString(13)+" - (14)";
                    log[2]="false";
                    log[3]="Falló FD no puede ingresarse vacío";
                    fail++;
                    lista.add(log);
                }
                if(cellValorPresente.isEmpty() || cellValorPresente.isBlank())
                {
                    String[] log=new String[4];
                    log[0]=String.valueOf(row.getRowNum()+1);
                    log[1]=CellReference.convertNumToColString(14)+" - (15)";
                    log[2]="false";
                    log[3]="Falló Valor Presente no puede ingresarse vacío";
                    fail++;
                    lista.add(log);
                }
                if(cellDiferencia.isEmpty() || cellDiferencia.isBlank())
                {
                    String[] log=new String[4];
                    log[0]=String.valueOf(row.getRowNum()+1);
                    log[1]=CellReference.convertNumToColString(15)+" - (16)";
                    log[2]="false";
                    log[3]="Falló Diferencia no puede ingresarse vacío";
                    fail++;
                    lista.add(log);
                }
                if(failCount == lista.size())
                {
                    RiskRys insert= new RiskRys();
                    insert.setNumeroPapeleta(cellNumeroPapeleta);
                    insert.setCodNombre(cellCodNombre);
                    insert.setCodPuc(cellCodPuc);
                    insert.setFecha(fechaDate);
                    insert.setFechaFinal(fechaDateF);
                    insert.setValorTotal(Double.parseDouble(cellValorTotal));
                    insert.setIntereses(Double.parseDouble(cellIntereses));
                    insert.setCausacionHoy(Double.parseDouble(cellCausacionHoy));
                    insert.setMoneda(cellMoneda);
                    insert.setFechaCorte(fechaDateC);
                    insert.setExposicion(cellExposicion);
                    insert.setDiasAlVto(cellDiasAlVto);
                    insert.setTasaDescuento(Double.parseDouble(cellTasaDescuento));
                    insert.setFd(cellFd);
                    insert.setValorPresente(Double.parseDouble(cellValorPresente));
                    insert.setDiferencia(Double.parseDouble(cellDiferencia));
                    insert.setPeriodo(periodo);
                    listaAdd.add(insert);
                    success++;
                }
            }
        }
        String[] logFinal=new String[4];
        logFinal[0]="RYS";
        logFinal[1]=String.valueOf(success);
        logFinal[2]=String.valueOf(fail);
        logFinal[3]="true";
        lista.add(logFinal);

        if(fail>0)
        {
            Date today=new Date();
            Audit insert = new Audit();
            insert.setAccion("Fallo Inserción Documento Riesgos mercados de RYS");
            insert.setCentro(user.getCentro());
            insert.setComponente("RYS");
            insert.setFecha(today);
            insert.setInput("RIESGOS MERCADOS");
            insert.setNombre(user.getPrimerNombre());
            insert.setUsuario(user.getUsuario());
            auditRepository.save(insert);
        }
        else
        {
            cleanRiskPeriod(periodo);
            riskRysRepository.saveAll(listaAdd);

            Date today=new Date();
            Audit insert = new Audit();
            insert.setAccion("Inserción Documento Riesgos mercados de RYS");
            insert.setCentro(user.getCentro());
            insert.setComponente("RYS");
            insert.setFecha(today);
            insert.setInput("RIESGOS MERCADOS");
            insert.setNombre(user.getPrimerNombre());
            insert.setUsuario(user.getUsuario());
            auditRepository.save(insert);
        }
        return lista;
    }

    public List<RysConcil> findAll(){
        return rysConcilRepository.findAll();
    }

    public List<RiskRys> findAllRiskRys(){
        return riskRysRepository.findAll();
    }

    public List<RiskRys> getAllRisk(String todayString){

        Query query = entityManager.createNativeQuery("SELECT * FROM nexco_riesgos_mercados WHERE periodo = ?", RiskRys.class);
        query.setParameter(1,todayString);
        return query.getResultList();
    }

    public void cleanRiskPeriod(String todayString){

        Query query = entityManager.createNativeQuery("DELETE FROM nexco_riesgos_mercados WHERE periodo = ?", RiskRys.class);
        query.setParameter(1,todayString);
        query.executeUpdate();
    }

    public List<RysConcil> getAllReport(String todayString){

        Query query = entityManager.createNativeQuery("SELECT * FROM nexco_rys_conciliacion WHERE periodo = ?", RysConcil.class);
        query.setParameter(1,todayString);
        return query.getResultList();
    }

    public List<Object[]> getPlano(String todayString,boolean ajuste, boolean reversion) throws ParseException
    {
        List<Object[]> resumeList = new ArrayList<>();
        if(reversion)
        {
            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM");
            Date fecha = formato.parse(todayString);
            Calendar c = Calendar.getInstance();
            c.setTime(fecha);
            c.add(Calendar.MONTH, 1);
            int partMonth = c.get(Calendar.MONTH)+1;
            String dateS = c.get(Calendar.YEAR)+"-"+String.valueOf(partMonth);
            if(partMonth<10)
                dateS = c.get(Calendar.YEAR)+"-0"+String.valueOf(partMonth);
            Query query = entityManager.createNativeQuery("SELECT ISNULL(A.cntro_cntble_1_conciliacion,'') CUENTAC, ISNULL(A.cnta_cntble_1_conciliacion,'') CRS, ISNULL(A.dvsa_cntble_1_conciliacion,'') DRS,A.numero_papeleta,ISNULL(A.cta_pyg,'') CPG,ISNULL(SUM(A.ajuste)*-1*-1,0) TOTAL,ISNULL((SELECT REPLACE(MIN(N.FechaHabil),'-','') FROM FECHAS_HABILES AS N WHERE N.FechaHabil LIKE ? AND N.DiaHabil <> 0),'') DIAS,ISNULL(SUBSTRING(A.nro_identificacion,1,2),'') TD,ISNULL(SUBSTRING(A.nro_identificacion,4,LEN(A.nro_identificacion)-4),'') ID, ISNULL(SUBSTRING(A.nro_identificacion,LEN(A.nro_identificacion),1),'') DV, 'REVER VALOR RAZONABLE REPOS' DES FROM nexco_rys_conciliacion A\n" +
                    "WHERE A.periodo = ? GROUP BY numero_papeleta,ISNULL(A.cntro_cntble_1_conciliacion,''),ISNULL(A.cnta_cntble_1_conciliacion,''),ISNULL(A.dvsa_cntble_1_conciliacion,''),ISNULL(A.cta_pyg,''),ISNULL(SUBSTRING(A.nro_identificacion,1,2),''),ISNULL(SUBSTRING(A.nro_identificacion,4,LEN(A.nro_identificacion)-4),''),ISNULL(SUBSTRING(A.nro_identificacion,LEN(A.nro_identificacion),1),'')");
            query.setParameter(1, dateS+ "%");
            query.setParameter(2, todayString);
            resumeList.addAll(query.getResultList());
        }
        if(ajuste)
        {
            Query query = entityManager.createNativeQuery("SELECT ISNULL(A.cntro_cntble_1_conciliacion,'') CUENTAC, ISNULL(A.cnta_cntble_1_conciliacion,'') CRS, ISNULL(A.dvsa_cntble_1_conciliacion,'') DRS,A.numero_papeleta,ISNULL(A.cta_pyg,'') CPG,ISNULL(SUM(A.ajuste*-1),0) TOTAL,ISNULL((SELECT REPLACE(MAX(N.FechaHabil),'-','') FROM FECHAS_HABILES AS N WHERE N.FechaHabil LIKE ? AND N.DiaHabil <> 0),'') DIAS,ISNULL(SUBSTRING(A.nro_identificacion,1,2),'') TD,ISNULL(SUBSTRING(A.nro_identificacion,4,LEN(A.nro_identificacion)-4),'') ID, ISNULL(SUBSTRING(A.nro_identificacion,LEN(A.nro_identificacion),1),'') DV,'AJUSTE VALOR RAZONABLE REPOS' DES FROM nexco_rys_conciliacion A\n" +
                    "WHERE A.periodo = ? GROUP BY numero_papeleta,ISNULL(A.cntro_cntble_1_conciliacion,''),ISNULL(A.cnta_cntble_1_conciliacion,''),ISNULL(A.dvsa_cntble_1_conciliacion,''),ISNULL(A.cta_pyg,''),ISNULL(SUBSTRING(A.nro_identificacion,1,2),''),ISNULL(SUBSTRING(A.nro_identificacion,4,LEN(A.nro_identificacion)-4),''),ISNULL(SUBSTRING(A.nro_identificacion,LEN(A.nro_identificacion),1),'')");
            query.setParameter(1, todayString + "%");
            query.setParameter(2, todayString);
            resumeList.addAll( query.getResultList());
        }
        return resumeList;
    }

    public List<Object[]> getAllDiferences(String todayString){

        Query query = entityManager.createNativeQuery("SELECT A.numero_papeleta,B.diferencia,A.ajuste_pyg,A.valida FROM nexco_rys_conciliacion A\n" +
                "LEFT JOIN nexco_riesgos_mercados B ON A.numero_papeleta = B.numero_papeleta\n" +
                "WHERE A.periodo = ? AND A.valida <> 0");
        query.setParameter(1,todayString);
        return query.getResultList();
    }

    public List<Object[]> getAllReportResumen(String todayString)
    {
        Query query = entityManager.createNativeQuery("SELECT numero_papeleta, nro_identificacion,tipo_entidad,cntro_cntble_1_conciliacion,cnta_cntble_1_conciliacion,dvsa_cntble_1_conciliacion,cnta_cntble_1_pyg, ISNULL(SUM(total_rys),0) AS SALDORYS FROM nexco_rys_conciliacion WHERE periodo = ? GROUP BY numero_papeleta, nro_identificacion,tipo_entidad,cntro_cntble_1_conciliacion,cnta_cntble_1_conciliacion,dvsa_cntble_1_conciliacion,cnta_cntble_1_pyg");
        query.setParameter(1,todayString);
        return query.getResultList();
    }

    public List<Object[]> getAllReportResumenHoja2(String todayString)
    {
        Query query = entityManager.createNativeQuery("SELECT Z.cta_neocon,Z.cnta_cntble_1_conciliacion, Z.cta_pyg,Z.cta_neocon_pyg,Z.dvsa_cntble_1_conciliacion,Z.tipo_entidad,\n" +
                "SUM(Z.ajuste) AJ,SUM(Z.ajuste_pyg) AJG\n" +
                "FROM nexco_rys_conciliacion Z WHERE Z.periodo = ? \n" +
                "GROUP BY Z.cta_neocon,Z.cnta_cntble_1_conciliacion, Z.cta_pyg,Z.cta_neocon_pyg,Z.dvsa_cntble_1_conciliacion,Z.tipo_entidad");
        query.setParameter(1,todayString);
        return query.getResultList();
    }
    public void generateCruce(String todayString)
    {
        Query query1 = entityManager.createNativeQuery("DELETE FROM nexco_rys_conciliacion WHERE periodo = ?");
        query1.setParameter(1,todayString);
        query1.executeUpdate();

        String[] partDate = todayString.split("-");
        Query query = entityManager.createNativeQuery("INSERT INTO nexco_rys_conciliacion(cnta_cntble_1_conciliacion,tipo_entidad,cntro_cntble_1_conciliacion,dvsa_cntble_1_conciliacion,numero_papeleta,nro_identificacion,cnta_cntble_1_pyg,total_rys,total_riesgos,ajuste,cta_pyg,ajuste_pyg,valida,cta_neocon,cta_neocon_pyg,periodo,diferencia)\n" +
                "SELECT B.local_derec, b.tipo_contraparte,B.centro_cuenta,B.dvsaconciliacion1,ISNULL(B.operacion,A.numero_papeleta),B.nit,B.pyg,B.vr_nominal_divisa,A.valor_presente,(A.valor_presente-B.vr_nominal_divisa), C.cuenta,(B.vr_nominal_divisa-A.valor_presente),((A.diferencia)-(A.valor_presente-B.vr_nominal_divisa)),D.CODICONS46,E.CODICONS46,?, A.diferencia\n" +
                "FROM nexco_riesgos_mercados A\n" +
                "LEFT JOIN (SELECT * FROM nexco_reporte_rp21 WHERE origen='RYS' AND MONTH(fecont) = ? AND YEAR(fecont)=?) B ON A.numero_papeleta = B.operacion\n" +
                "LEFT JOIN nexco_rys_parametrica C ON B.local_derec = C.codigo\n" +
                "LEFT JOIN (SELECT NUCTA, CODICONS46 FROM CUENTAS_PUC WHERE empresa ='0013') D ON B.local_derec = D.NUCTA\n" +
                "LEFT JOIN (SELECT NUCTA, CODICONS46 FROM CUENTAS_PUC WHERE empresa ='0013') E ON C.cuenta = E.NUCTA WHERE A.periodo = ? " +
                "group by B.local_derec, b.tipo_contraparte,B.centro_cuenta,B.dvsaconciliacion1,ISNULL(B.operacion,A.numero_papeleta),B.nit,B.pyg,B.vr_nominal_divisa,A.valor_presente,(A.valor_presente-B.vr_nominal_divisa), C.cuenta,(B.vr_nominal_divisa-A.valor_presente),((A.diferencia)-(A.valor_presente-B.vr_nominal_divisa)),D.CODICONS46,E.CODICONS46,A.diferencia");
        query.setParameter(1,todayString);
        query.setParameter(2,partDate[1]);
        query.setParameter(3,partDate[0]);
        query.setParameter(4,todayString);
        query.executeUpdate();
    }

    /*public List<String[]> saveFilePlantilla(Collection<Part>  parts, String period,User user) throws IOException, InvalidFormatException {

        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando as em " +
                "WHERE em.input = ? AND em.fecha_reporte = ? AND em.responsable = ? AND em.componente = ? AND em.estado = 1", ControlPanel.class);
        query.setParameter(1, "PLANTILLA CONTINGENTES - NEXCO" );
        query.setParameter(2, period);
        query.setParameter(3, user.getCentro());
        query.setParameter(4, "CONTINGENTES");
        List<ControlPanel> controlList= query.getResultList();

        List<String[]> finalList = new ArrayList<String[]>();

        if(controlList.size()>0)
        {
            Iterator<Row> rows = null;
            Iterator<Row> rows1 = null;

            for (Part part : parts) {
                InputStream file = part.getInputStream();
                if (part != null && file != null && part.getSubmittedFileName() != null) {
                    XSSFWorkbook wb = new XSSFWorkbook(file);
                    XSSFSheet sheetTipoAval = wb.getSheetAt(0);
                    rows = sheetTipoAval.iterator();
                    rows1 = sheetTipoAval.iterator();
                    finalList = validarPlantilla(rows, period, user);
                }
            }

            if (finalList.get(finalList.size() - 1)[2].equals("0")) {
                String success = finalList.get(finalList.size() - 1)[1];
                String fail = finalList.get(finalList.size() - 1)[2];

                finalList.clear();
                finalList = validateRysConcil(period, success, fail);
                if (finalList.get(finalList.size() - 1)[3].equals("PLANTILLA")) {

                    Query query7 = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando SET semaforo_input = ? , usuario_carga = ? " +
                            "WHERE responsable = ? AND input = ? AND componente = ? AND fecha_reporte = ?", ControlPanel.class);
                    query7.setParameter(1, "PENDING");
                    query7.setParameter(2, user.getPrimerNombre());
                    query7.setParameter(3, user.getCentro());
                    query7.setParameter(4, "PLANTILLA CONTINGENTES - NEXCO");
                    query7.setParameter(5, "CONTINGENTES");
                    query7.setParameter(6, period);
                    query7.executeUpdate();

                    String[] part = period.split("-");
                    insertRysConcil(part[1], part[0]);
                    Date today = new Date();
                    Audit insert = new Audit();
                    insert.setAccion("Inserción exitosa plantilla contingentes");
                    insert.setCentro(user.getCentro());
                    insert.setComponente("CONTINGENTES");
                    insert.setFecha(today);
                    insert.setInput("PLANTILLA CONTINGENTES - NEXCO");
                    insert.setNombre(user.getPrimerNombre());
                    insert.setUsuario(user.getUsuario());
                    auditRepository.save(insert);
                } else if (finalList.get(finalList.size() - 1)[3].equals("BANCO")) {

                    Query query7 = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando SET semaforo_input = ?, usuario_carga = ? " +
                            "WHERE responsable = ? AND input = ? AND componente = ? AND fecha_reporte = ?", ControlPanel.class);
                    query7.setParameter(1, "EMPTY");
                    query7.setParameter(2, user.getPrimerNombre());
                    query7.setParameter(3, user.getCentro());
                    query7.setParameter(4, "PLANTILLA CONTINGENTES - NEXCO");
                    query7.setParameter(5, "CONTINGENTES");
                    query7.setParameter(6, period);
                    query7.executeUpdate();

                    Date today = new Date();
                    Audit insert = new Audit();
                    insert.setAccion("Fallo inserción plantilla contingentes, falta parametrización bancos");
                    insert.setCentro(user.getCentro());
                    insert.setComponente("CONTINGENTES");
                    insert.setFecha(today);
                    insert.setInput("PLANTILLA CONTINGENTES - NEXCO");
                    insert.setNombre(user.getPrimerNombre());
                    insert.setUsuario(user.getUsuario());
                    auditRepository.save(insert);
                } else if (finalList.get(finalList.size() - 1)[3].equals("CONTRATO")) {

                    Query query7 = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando SET semaforo_input = ? , usuario_carga = ? " +
                            "WHERE responsable = ? AND input = ? AND componente = ? AND fecha_reporte = ?", ControlPanel.class);
                    query7.setParameter(1, "PENDING");
                    query7.setParameter(2, user.getPrimerNombre());
                    query7.setParameter(3, user.getCentro());
                    query7.setParameter(4, "PLANTILLA CONTINGENTES - NEXCO");
                    query7.setParameter(5, "CONTINGENTES");
                    query7.setParameter(6, period);
                    query7.executeUpdate();

                    String[] part = period.split("-");
                    insertRysConcil(part[1], part[0]);
                    Date today = new Date();
                    Audit insert = new Audit();
                    insert.setAccion("Inserción plantilla contingentes con parametrización contratos");
                    insert.setCentro(user.getCentro());
                    insert.setComponente("CONTINGENTES");
                    insert.setFecha(today);
                    insert.setInput("PLANTILLA CONTINGENTES - NEXCO");
                    insert.setNombre(user.getPrimerNombre());
                    insert.setUsuario(user.getUsuario());
                    auditRepository.save(insert);
                }
            }
        }
        else
        {
            String[] log1=new String[4];
            log1[3]="ERROR";
            finalList.add(log1);
        }
        validateContingentesComponent(user,period,"PLANTILLA CONTINGENTES - NEXCO");

        return finalList;
    }

    public void validateContingentesComponent(User user, String period, String input)
    {
        Query query8 = entityManager.createNativeQuery("SELECT * FROM nexco_cuadro_mando " +
                "WHERE responsable = ? AND input = ? AND componente = ? AND fecha_reporte = ? AND semaforo_input != ?", ControlPanel.class);
        query8.setParameter(1, user.getCentro());
        query8.setParameter(2, input);
        query8.setParameter(3, "CONTINGENTES");
        query8.setParameter(4, period);
        query8.setParameter(5, "FULL");

        if(query8.getResultList().size()>0) {
            Query query7 = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando SET semaforo_componente = ? " +
                    "WHERE responsable = ? AND componente = ? AND fecha_reporte = ?", ControlPanel.class);
            query7.setParameter(1, "EMPTY");
            query7.setParameter(2, user.getCentro());
            query7.setParameter(3, "CONTINGENTES");
            query7.setParameter(4, period);
            query7.executeUpdate();
        }
    }

    public List<String[]> validarPlantilla(Iterator<Row> rows, String mes,User user) {
        ArrayList<String[]> lista= new ArrayList<String[]>();
        ArrayList<ContingentTemporal> listTemp= new ArrayList<ContingentTemporal>();
        XSSFRow row;
        int fail=0;
        int success =0;
        boolean validation=false;
        if(validateTipoAvalExist("FINANCIERO")!=null)
            validation=true;
        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(row.getRowNum()>0)
            {
                String[] log1=new String[4];
                log1[2]="true";
                DataFormatter formatter = new DataFormatter();
                String cellFechaCierre = formatter.formatCellValue(row.getCell(0));
                String cellCuentaContable = formatter.formatCellValue(row.getCell(1));
                String cellDivisa = formatter.formatCellValue(row.getCell(2));
                String cellSaldoDivisa = formatter.formatCellValue(row.getCell(3)).replace(" ", "");
                String cellFechaAlta = formatter.formatCellValue(row.getCell(4));
                String cellFechaVencimiento = formatter.formatCellValue(row.getCell(5));
                String cellNit = formatter.formatCellValue(row.getCell(6)).trim();
                String cellNombreCliente = formatter.formatCellValue(row.getCell(7));
                String cellContrato = formatter.formatCellValue(row.getCell(8));
                String cellNombreBanco = formatter.formatCellValue(row.getCell(9));
                String cellPaisBanco = formatter.formatCellValue(row.getCell(10));
                String cellIntergrupo = formatter.formatCellValue(row.getCell(11));
                String cellTasa = "";
                String cellPrefijo = formatter.formatCellValue(row.getCell(13));
                String cellNumero = formatter.formatCellValue(row.getCell(14));
                String cellNitBanco = formatter.formatCellValue(row.getCell(15));
                log1[0]=String.valueOf(row.getRowNum()+1);
                Date fechaDateC = new Date();
                Date fechaDateV= new Date();
                Date fechaDateA= new Date();
                Calendar calendar = Calendar.getInstance();
                String[] loadDate = new String[2];
                try {
                    XSSFCell cell0= row.getCell(3);
                    cell0.setCellType(CellType.STRING);
                    cellSaldoDivisa = formatter.formatCellValue(cell0).replace(" ", "");
                    Double.parseDouble(cellSaldoDivisa);
                }
                catch(Exception e)
                {

                }
                if(cellSaldoDivisa.trim().length()!=0 && Double.parseDouble(cellSaldoDivisa)!=0)
                {
                    try
                    {
                        log1[1] = CellReference.convertNumToColString(3) + " - (4)";
                        XSSFCell cell0= row.getCell(3);
                        cell0.setCellType(CellType.STRING);
                        cellSaldoDivisa = formatter.formatCellValue(cell0).replace(" ", "");
                        Double.parseDouble(cellSaldoDivisa);

                        log1[1] = CellReference.convertNumToColString(12)+ " - (13)";
                        XSSFCell cell1= row.getCell(12);
                        cell1.setCellType(CellType.STRING);
                        cellTasa = formatter.formatCellValue(cell1).replace(" ", "");
                        Double.parseDouble(cellTasa);

                        log1[1] = CellReference.convertNumToColString(8) + " - (9)";
                        Long.parseLong(cellContrato);
                        SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
                        formato.applyPattern("yyyy-MM-dd");
                        log1[1] = CellReference.convertNumToColString(0) + " - (1)";
                        fechaDateC = formato.parse(cellFechaCierre);
                        log1[1] = CellReference.convertNumToColString(4) + " - (5)";
                        cellFechaAlta=cellFechaAlta.substring(0,4)+"-"+cellFechaAlta.substring(4,6)+"-"+cellFechaAlta.substring(6,8);
                        fechaDateA = formato.parse(cellFechaAlta);
                        log1[1] = CellReference.convertNumToColString(5) + " - (6)";
                        cellFechaVencimiento=cellFechaVencimiento.substring(0,4)+"-"+cellFechaVencimiento.substring(4,6)+"-"+cellFechaVencimiento.substring(6,8);
                        if(cellNit.length()>0)
                            cellNit = cellNit.replace(" ","");
                        if(cellNitBanco.length()>0)
                            cellNitBanco = cellNitBanco.replace(" ","");
                        fechaDateV = formato.parse(cellFechaVencimiento);
                        calendar.setTime(fechaDateC);
                        loadDate = mes.split("-");
                    }
                    catch (Exception e)
                    {
                        fail++;
                        log1[2] = "false";
                        log1[3] = "Tipo Dato de Celda Inválido";
                        lista.add(log1);
                    }
                    String[] log=new String[4];
                    log[0]=String.valueOf(row.getRowNum()+1);
                    log[2]="true";
                    if (cellFechaCierre.isEmpty() || cellFechaCierre.isBlank() || fechaDateC == null  ||  Integer.parseInt(loadDate[0])!=(calendar.get(Calendar.YEAR)) ||  Integer.parseInt(loadDate[1])!=(calendar.get(Calendar.MONTH) + 1)){
                        log[1] = CellReference.convertNumToColString(0) + " - (1)";
                        log[2] = "false";
                        log[3] = "Fecha Cierre no pertenece al periodo seleccionado";
                        fail++;
                        lista.add(log);
                    } else if (cellCuentaContable.isEmpty() || cellCuentaContable.isBlank() || cellCuentaContable.length() > 254) {
                        log[1] = CellReference.convertNumToColString(1) + " - (2)";
                        log[2] = "false";
                        log[3] = "La Cuenta Contable no puede estar vacía";
                        fail++;
                        lista.add(log);
                    } else if (cellDivisa.isEmpty() || cellDivisa.isBlank()) {
                        log[1] = CellReference.convertNumToColString(2) + " - (3)";
                        log[2] = "false";
                        log[3] = "La Divisa no puede estar vacía y debe cruzar con la parametría de Divisas";
                        fail++;
                        lista.add(log);
                    } else if (cellSaldoDivisa.isEmpty() || cellSaldoDivisa.isBlank()) {
                        log[1] = CellReference.convertNumToColString(3) + " - (4)";
                        log[2] = "false";
                        log[3] = "El campo no puede ir vacío";
                        fail++;
                        lista.add(log);
                    } else if (cellFechaAlta.isEmpty() || cellFechaAlta.isBlank() || fechaDateA == null || formatter.formatCellValue(row.getCell(4)).trim().length()!=8){
                        log[1] = CellReference.convertNumToColString(4) + " - (5)";
                        log[2] = "false";
                        log[3] = "La Fecha de Alta no puede estar vacío y debe ser formato fecha";
                        fail++;
                        lista.add(log);
                    } else if (cellFechaVencimiento.isEmpty() || cellFechaVencimiento.isBlank() || cellFechaVencimiento == null || formatter.formatCellValue(row.getCell(5)).trim().length()!=8) {
                        log[1] = CellReference.convertNumToColString(5) + " - (6)";
                        log[2] = "false";
                        log[3] = "La Fecha de Vencimiento no puede estar vacío y debe ser formato fecha";
                        fail++;
                        lista.add(log);
                    } else if (cellNit.isEmpty() || cellNit.isBlank()) {
                        log[1] = CellReference.convertNumToColString(6) + " - (7)";
                        log[2] = "false";
                        log[3] = "El NIT no puede estar vacío";
                        fail++;
                        lista.add(log);
                    } else if (cellNombreCliente.isEmpty() || cellNombreCliente.isBlank()) {
                        log[1] = CellReference.convertNumToColString(7) + " - (8)";
                        log[2] = "false";
                        log[3] = "El Nombre del Cliente no puede estar vacío";
                        fail++;
                        lista.add(log);
                    } else if (cellContrato.isEmpty() || cellContrato.isBlank()||cellContrato.length()!=18) {
                        log[1] = CellReference.convertNumToColString(8) + " - (9)";
                        log[2] = "false";
                        log[3] = "El Contrato debe tener 18 caracteres";
                        fail++;
                        lista.add(log);
                    } else if (cellIntergrupo.isEmpty() || cellIntergrupo.isBlank()) {
                        log[1] = CellReference.convertNumToColString(11) + " - (12)";
                        log[2] = "false";
                        log[3] = "El campo Intergrupo no puede estar vacío";
                        fail++;
                        lista.add(log);
                    } else if (cellTasa.isEmpty() || cellTasa.isBlank()) {
                        log[1] = CellReference.convertNumToColString(12) + " - (13)";
                        log[2] = "false";
                        log[3] = "La tasa no puede estar vacía";
                        fail++;
                        lista.add(log);
                    } else if (cellPrefijo.isEmpty() || cellPrefijo.isBlank()) {
                        log[1] = CellReference.convertNumToColString(13) + " - (14)";
                        log[2] = "false";
                        log[3] = "El campo Prefijo no puede estar vacío";
                        fail++;
                        lista.add(log);
                    } else if (cellNumero.isEmpty() || cellNumero.isBlank()) {
                        log[1] = CellReference.convertNumToColString(14) + " - (15)";
                        log[2] = "false";
                        log[3] = "El campo Número no puede estar vacío";
                        fail++;
                        lista.add(log);
                    }else if(validation ==false)
                    {
                        log[1] = CellReference.convertNumToColString(8) + " - (9)";
                        log[2] = "false";
                        log[3] = "No existe el Aval Financiero en paramétricas";
                        fail++;
                        lista.add(log);
                    }else if (cellNitBanco.isEmpty() || cellNitBanco.isBlank()) {
                        log[1] = CellReference.convertNumToColString(15) + " - (16)";
                        log[2] = "false";
                        log[3] = "El NIT Banco no puede estar vacío";
                        fail++;
                        lista.add(log);
                    }
                    else {
                        success++;
                        ContingentTemporal contingentTemplate = new ContingentTemporal();
                        contingentTemplate.setFechaCierre(fechaDateC);
                        contingentTemplate.setCuentaContable(cellCuentaContable);
                        contingentTemplate.setDivisa(cellDivisa);
                        //List<Object> tasaExtract=getSaldoTasa(period,contingentTemplate.getDivisa().getId());
                        contingentTemplate.setSaldoDivisa(Double.parseDouble(cellSaldoDivisa));
                        contingentTemplate.setFechaAlta(fechaDateA);
                        contingentTemplate.setFechaVenciemiento(fechaDateV);
                        contingentTemplate.setNitBanco(cellNitBanco);
                        if (cellNit.length() > 0)
                            contingentTemplate.setNit(cellNit);
                        contingentTemplate.setContrato(cellContrato);
                        contingentTemplate.setIntergrupo(cellIntergrupo);
                        contingentTemplate.setTasa(Double.parseDouble(cellTasa));
                        contingentTemplate.setNombreCliente(cellNombreCliente);
                        contingentTemplate.setPrefijo(cellPrefijo);
                        contingentTemplate.setNumero(cellNumero);
                        contingentTemplate.setPeriodo(mes);
                        contingentTemplate.setTipoMoneda("EXTRANJERA");
                        contingentTemplate.setOrigen("PLANTILLA");
                        contingentTemplate.setFila(String.valueOf(row.getRowNum()+1));
                        listTemp.add(contingentTemplate);
                        log[2] = "true";
                    }
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
            insert.setAccion("Fallo Inserción Documento Plantilla Contingentes");
            insert.setCentro(user.getCentro());
            insert.setComponente("DERIVADOS");
            insert.setFecha(today);
            insert.setInput("Contingentes");
            insert.setNombre(user.getPrimerNombre());
            insert.setUsuario(user.getUsuario());
            auditRepository.save(insert);
        }
        else{
            Query delete = entityManager.createNativeQuery("TRUNCATE TABLE nexco_contingentes_inv_temporal");
            delete.executeUpdate();
            contingentTemporalRepository.saveAll(listTemp);
        }

        return lista;
    }

    public void getRowsPlantilla(Iterator<Row> rows, User user,String period)
    {
        XSSFRow row;
        Date today=new Date();

        Query query2 = entityManager.createNativeQuery("DELETE FROM nexco_inventario_contingentes WHERE periodo = ? AND origen='PLANTILLA';");
        query2.setParameter(1, period);
        query2.executeUpdate();

        List<RysConcil> listaContingentes = new ArrayList<RysConcil>();
        List<Contract> listaContratos= new ArrayList<Contract>();
        List<GarantBank> listaGarantes= new ArrayList<GarantBank>();
        List<String[]> listaFinal= new ArrayList<String[]>();
        List<String[]> listaLogContrato= new ArrayList<String[]>();
        List<String[]> listaLogGarante= new ArrayList<String[]>();
        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(row.getRowNum()>0)
            {
                DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
                String cellFechaCierre = formatter.formatCellValue(row.getCell(0));
                String cellCuentaContable = formatter.formatCellValue(row.getCell(1));
                String cellDivisa = formatter.formatCellValue(row.getCell(2));

                XSSFCell cell0= row.getCell(3);
                cell0.setCellType(CellType.STRING);
                String cellSaldoDivisa = formatter.formatCellValue(cell0).replace(" ", "");

                String cellFechaAlta = formatter.formatCellValue(row.getCell(4));
                String cellFechaVencimiento = formatter.formatCellValue(row.getCell(5));
                String cellNit = formatter.formatCellValue(row.getCell(6));
                if(cellNit.length()>0)
                    cellNit = formatter.formatCellValue(row.getCell(6)).replace(" ","");
                String cellNombreCliente = formatter.formatCellValue(row.getCell(7));
                String cellContrato = formatter.formatCellValue(row.getCell(8));
                String cellNombreBanco = formatter.formatCellValue(row.getCell(9));
                String cellPaisBanco = formatter.formatCellValue(row.getCell(10));
                String cellIntergrupo = formatter.formatCellValue(row.getCell(11));

                XSSFCell cell1= row.getCell(12);
                cell1.setCellType(CellType.STRING);
                String cellTasa = formatter.formatCellValue(cell1).replace(" ", "");

                String cellPrefijo = formatter.formatCellValue(row.getCell(13));
                String cellNumero = formatter.formatCellValue(row.getCell(14));
                String cellNitBanco = formatter.formatCellValue(row.getCell(15));
                if(cellNitBanco.length()>0)
                    cellNitBanco =cellNitBanco.replace(" ","");

                //Calendar calendar = Calendar.getInstance();
                //String[] loadDate;

                if(cellSaldoDivisa.trim().length()!=0 && !cellSaldoDivisa.trim().equals("0")) {
                    try {
                        cellFechaAlta = cellFechaAlta.substring(0, 4) + "-" + cellFechaAlta.substring(4, 6) + "-" + cellFechaAlta.substring(6, 8);
                        cellFechaVencimiento = cellFechaVencimiento.substring(0, 4) + "-" + cellFechaVencimiento.substring(4, 6) + "-" + cellFechaVencimiento.substring(6, 8);
                        SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
                        formato.applyPattern("yyyy-MM-dd");

                        Date fechaDateA = formato.parse(cellFechaAlta);
                        Date fechaDateV = formato.parse(cellFechaVencimiento);
                        Date fechaDateC = formato.parse(cellFechaCierre);

                        RysConcil contingentTemplate = new RysConcil();
                        contingentTemplate.setFechaCierre(fechaDateC);
                        contingentTemplate.setCuentaContable(cellCuentaContable);
                        contingentTemplate.setDivisa(currencyRepository.findAllById(cellDivisa));
                        List<Object> tasaExtract=getSaldoTasa(period,contingentTemplate.getDivisa().getId());
                        contingentTemplate.setSaldoDivisa(Double.parseDouble(cellSaldoDivisa));
                        contingentTemplate.setFechaAlta(fechaDateA);
                        contingentTemplate.setFechaVenciemiento(fechaDateV);
                        contingentTemplate.setNitBanco(cellNitBanco);
                        if (cellNit.length() > 0)
                            contingentTemplate.setNit(cellNit);

                        List<Contract> contractNew = new ArrayList<>();
                        List<GarantBank> bankNew = new ArrayList<>();

                        try {
                            contractNew = findContract(cellContrato);
                            String temp = cellNitBanco.replace(" ","");
                            if(temp.length()>2) {
                                bankNew = garantBankRepository.findAllByNit(temp.substring(1, temp.length() - 1));
                            }
                            else{
                                bankNew = garantBankRepository.findAllByNit(temp);
                            }
                        } catch (Exception e) {

                        }
                        if (contractNew.size() == 0) {
                            Contract insert = new Contract();
                            insert.setContrato(cellContrato);
                            insert.setTipoAval(String.valueOf(validateTipoAvalExist("FINANCIERO").get(0)));
                            insert.setTipoAvalOrigen("FINANCIERO");
                            insert.setArchivoEntrada("EXTRANJERO");
                            if (bankNew.size() > 0) {
                                Country ic = countryRepository.findAllById(bankNew.get(0).getPais());
                                insert.setBanco(bankNew.get(0).getNit());
                                if (ic != null)
                                    insert.setPaisContrato(ic);
                            }
                            insert.setTipoProceso("CONTINGENTES");
                            listaContratos.add(insert);
                        } else {
                            //Contract insertContract = contractRepository.findAllByContrato(fmt.format("%18s",cellContrato).toString());
                            if (bankNew.size() > 0 && bankNew.get(0).getNit()!=null && bankNew.get(0).getPais()!=null) {
                                Country ic = countryRepository.findAllById(bankNew.get(0).getPais());
                                contractNew.get(0).setBanco(bankNew.get(0).getNit());
                                if (ic != null)
                                    contractNew.get(0).setPaisContrato(ic);
                            }
                            listaContratos.add(contractNew.get(0));
                        }
                        if (bankNew.size() > 0 && bankNew.get(0)!=null && bankNew.get(0).getNombreBancoReal()!=null && bankNew.get(0).getPais()!=null){
                            contingentTemplate.setNombreBanco(bankNew.get(0).getNombreBancoReal());
                            contingentTemplate.setPaisBanco(bankNew.get(0).getPais());
                        }
                        else{
                            String temp = cellNitBanco.replace(" ","");
                            GarantBank newGarant = new GarantBank();
                            if(temp.length()>2) {
                                newGarant.setNombreSimilar("Banco Nuevo NIT " + temp.substring(1, temp.length() - 1));
                                newGarant.setNit(temp.substring(1,temp.length()-1));
                            }
                            else{
                                newGarant.setNombreSimilar("Banco Nuevo NIT " + temp);
                                newGarant.setNit(temp);
                            }

                            listaGarantes.add(newGarant);
                            String[] log= new String[2];
                            log[0]=newGarant.getNit();
                            listaLogGarante.add(log);
                        }

                        contingentTemplate.setContrato(cellContrato);
                        contingentTemplate.setIntergrupo(cellIntergrupo);

                        if(tasaExtract.size()>0)
                        {
                            contingentTemplate.setSaldoPesos(Double.parseDouble(cellSaldoDivisa)*Double.parseDouble(tasaExtract.get(0).toString()));
                            contingentTemplate.setTasa(Double.parseDouble(tasaExtract.get(0).toString()));
                        }
                        else{
                            contingentTemplate.setSaldoPesos(Double.parseDouble(cellSaldoDivisa)*Double.parseDouble(cellTasa));
                            contingentTemplate.setTasa(Double.parseDouble(cellTasa));
                        }
                        contingentTemplate.setNombreCliente(cellNombreCliente);
                        contingentTemplate.setPrefijo(cellPrefijo);
                        contingentTemplate.setNumero(cellNumero);
                        contingentTemplate.setPeriodo(period);
                        contingentTemplate.setTipoMoneda("EXTRANJERA");
                        contingentTemplate.setOrigen("PLANTILLA");
                        listaContingentes.add(contingentTemplate);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        contractRepository.saveAll(listaContratos);
        garantBankRepository.saveAll(listaGarantes);
        contingentTemplateRepository.saveAll(listaContingentes);
        //updatePlantillaBase(period);
    }

    public List<Contract> findContract(String contract){

        Query result = entityManager.createNativeQuery("SELECT * FROM nexco_contratos where id_contrato = ?", Contract.class);
        result.setParameter(1, contract);

        return result.getResultList();
    }

    public List<String[]> validateRysConcil(String periodo,String success,String fail)
    {

        Query result = entityManager.createNativeQuery("SELECT nct.contrato AS CONTRA1,\n" +
                "nc.id_contrato AS CONTRA2,\n" +
                "nd.id_divisa AS DIV1,\n" +
                "nct.id_divisa AS DIV2,\n" +
                "CASE WHEN LEN(nct.nit_banco)>2 THEN SUBSTRING(nct.nit_banco,2,(LEN(nct.nit_banco)-2)) ELSE nct.nit_banco END AS NIT1,\n" +
                "CASE WHEN nc.banco IS NOT NULL THEN nbg2.nombre_banco_real ELSE nbg.nombre_banco_real END AS BANCO1,\n" +
                "CASE WHEN nc.banco IS NOT NULL THEN nbg2.pais ELSE nbg.pais END AS PAIS1,\n" +
                "nct.fila AS F1,\n" +
                "CASE WHEN nbg2.nit IS NOT NULL THEN nbg2.nombre_banco_real ELSE nbg.nombre_banco_real END AS BANCO2,\n" +
                "CASE WHEN nbg2.nit IS NOT NULL THEN nbg2.pais ELSE nbg.pais END AS PAIS2, \n" +
                "nc.banco \n" +
                "FROM nexco_contingentes_inv_temporal AS nct\n" +
                "LEFT JOIN (SELECT id_divisa FROM nexco_divisas) AS nd ON nct.id_divisa = nd.id_divisa\n" +
                "LEFT JOIN (SELECT * FROM nexco_divisas_valor WHERE MONTH(fecha) = ? AND YEAR(fecha) = ?) AS ndv ON nct.id_divisa = ndv.divisa\n" +
                "LEFT JOIN (SELECT * FROM nexco_banco_garante) AS nbg ON CASE WHEN LEN(nct.nit_banco)>2 THEN SUBSTRING(nct.nit_banco,2,(LEN(nct.nit_banco)-2)) ELSE nct.nit_banco END = nbg.nit\n" +
                "LEFT JOIN (SELECT id_contrato,banco FROM nexco_contratos) AS nc ON nct.contrato = nc.id_contrato\n" +
                "LEFT JOIN (SELECT * FROM nexco_banco_garante) AS nbg2 ON nc.banco = nbg2.nit \n" +
                "WHERE nc.id_contrato IS NULL OR \n" +
                "CASE WHEN nc.banco IS NOT NULL THEN nbg2.nombre_banco_real ELSE nbg.nombre_banco_real END IS NULL OR \n" +
                "CASE WHEN nc.banco IS NOT NULL THEN nbg2.pais ELSE nbg.pais END IS NULL OR \n" +
                "CASE WHEN nbg2.nit IS NOT NULL THEN nbg2.nombre_banco_real ELSE nbg.nombre_banco_real END IS NULL OR \n" +
                "CASE WHEN nbg2.nit IS NOT NULL THEN nbg2.pais ELSE nbg.pais END IS NULL OR\n" +
                "nc.banco IS NULL");

        String[] parts= periodo.split("-");
        result.setParameter(1, parts[1]);
        result.setParameter(2, parts[0]);

        List<Object[]> listQuery = result.getResultList();
        ArrayList<String[]> listLog = new ArrayList<String[]>();
        ArrayList<String[]> listLogBanco = new ArrayList<String[]>();
        ArrayList<Contract> listContratos= new ArrayList<Contract>();
        ArrayList<GarantBank> listBancos= new ArrayList<GarantBank>();
        ArrayList<String> listSave= new ArrayList<String>();
        ArrayList<String> listSaveC= new ArrayList<String>();

        String avalDefault =String.valueOf(validateTipoAvalExist("FINANCIERO").get(0));

        for (Object[] item:listQuery)
        {
            if(item[8]==null || item[9]==null)
            {
                GarantBank newGarant = new GarantBank();
                newGarant.setNombreSimilar("Pendiente Parametrizar " + item[4].toString());
                newGarant.setNit(item[4].toString());
                listBancos.add(newGarant);

                String[] log= new String[4];
                log[0]=newGarant.getNit();
                log[2]=newGarant.getNombreSimilar();
                if(!listSave.contains(newGarant.getNit())) {
                    listSave.add(newGarant.getNit());
                    listLogBanco.add(log);
                }
            }
            else if(item[1]==null && item[0]!=null && listLogBanco.size()==0)
            {
                Contract insert = new Contract();
                insert.setContrato(item[0].toString());
                insert.setTipoAval(avalDefault);
                insert.setTipoAvalOrigen("FINANCIERO");
                insert.setArchivoEntrada("EXTRANJERO");
                insert.setTipoProceso("CONTINGENTES");
                if (item[9]!=null && item[8]!=null && item[4]!=null)
                {
                    Country ic = new Country();
                    ic.setId(item[9].toString());
                    //ic.setNombre(item[8].toString());
                    insert.setBanco(item[4].toString());
                    if(ic != null)
                        insert.setPaisContrato(ic);
                }
                listContratos.add(insert);

                String[] log= new String[4];
                log[0]=insert.getContrato();
                log[2]=insert.getBanco();
                log[3]="true";
                if(!listSaveC.contains(insert.getContrato())) {
                    listSaveC.add(insert.getContrato());
                    listLog.add(log);
                }
            }
            else if(item[1]!=null && item[0]!=null && listBancos.size()==0 && (item[5]==null || item[6]==null || item[10]==null))
            {
                Contract insert = contractRepository.findByContrato(item[1].toString());
                if (item[9]!=null && item[8]!=null && item[4]!=null)
                {
                    Country ic = new Country();
                    ic.setId(item[9].toString());
                    insert.setBanco(item[4].toString());
                    if(ic != null)
                        insert.setPaisContrato(ic);
                }
                listContratos.add(insert);

                String[] log= new String[4];
                log[0]=insert.getContrato();
                log[2]=insert.getBanco();
                log[3]="true";
                if(!listSaveC.contains(insert.getContrato())) {
                    listSaveC.add(insert.getContrato());
                    listLog.add(log);
                }
            }
        }
        listLog = (ArrayList<String[]>) listLog.stream().distinct().collect(Collectors.toList());
        if(listBancos.size()>0)
        {
            garantBankRepository.saveAll(listBancos);

            String[] log= new String[4];
            log[0]="Plantilla";
            log[1]=success;
            log[2]=fail;
            log[3]="BANCO";
            listLogBanco.add(log);
            listLog=listLogBanco;
        }
        else if(listContratos.size()>0)
        {
            contractRepository.saveAll(listContratos);

            String[] log= new String[4];
            log[0]="Plantilla";
            log[1]=success;
            log[2]=fail;
            log[3]="CONTRATO";
            listLog.add(log);
        }
        else
        {
            String[] log= new String[4];
            log[0]="Plantilla";
            log[1]=success;
            log[2]=fail;
            log[3]="PLANTILLA";
            listLog.add(log);
        }

        return listLog;
    }


    public void insertRysConcil(String mes, String year){

        Query query2 = entityManager.createNativeQuery("DELETE FROM nexco_inventario_contingentes WHERE periodo = ? AND origen='PLANTILLA';");
        query2.setParameter(1, year+"-"+mes);
        query2.executeUpdate();

        Query result = entityManager.createNativeQuery("INSERT INTO nexco_inventario_contingentes (centro,contrato,cuenta_contable,id_divisa,fecha_alta,fecha_cierre,fecha_vencimiento,intergrupo,nit,nombre_cliente,\n" +
                "nit_banco,nombre_banco,numero,origen,pais_banco,periodo,prefijo,saldo_divisa,saldo_pesos,tasa,tipo_moneda) " +
                "SELECT nct.centro,\n" +
                "nc.id_contrato,\n" +
                "nct.cuenta_contable,\n" +
                "nd.id_divisa,\n" +
                "nct.fecha_alta,\n" +
                "nct.fecha_cierre,\n" +
                "nct.fecha_vencimiento,\n" +
                "nct.intergrupo,\n" +
                "nct.nit,\n" +
                "nct.nombre_cliente,\n" +
                "nct.nit_banco,\n" +
                "CASE WHEN nc.banco IS NOT NULL THEN nbg2.nombre_banco_real ELSE nbg.nombre_banco_real END,\n" +
                "nct.numero,\n" +
                "nct.origen,\n" +
                "CASE WHEN nc.banco IS NOT NULL THEN nbg2.pais ELSE nbg.pais END,\n" +
                "nct.periodo,\n" +
                "nct.prefijo,\n" +
                "nct.saldo_divisa,\n" +
                "CASE WHEN ndv.valor IS NOT NULL THEN nct.saldo_divisa*ndv.valor ELSE nct.saldo_divisa*nct.tasa END,\n" +
                "CASE WHEN ndv.valor IS NOT NULL THEN ndv.valor ELSE nct.tasa END,\n" +
                "nct.tipo_moneda\n" +
                "FROM nexco_contingentes_inv_temporal AS nct\n" +
                "LEFT JOIN (SELECT id_divisa FROM nexco_divisas) AS nd ON nct.id_divisa = nd.id_divisa\n" +
                "LEFT JOIN (SELECT * FROM nexco_divisas_valor WHERE MONTH(fecha) = ? AND YEAR(fecha) = ?) AS ndv ON nct.id_divisa = ndv.divisa\n" +
                "LEFT JOIN (SELECT * FROM nexco_banco_garante) AS nbg ON CASE WHEN LEN(nct.nit_banco)>3 THEN SUBSTRING(nct.nit_banco,2,(LEN(nct.nit_banco)-2)) ELSE nct.nit_banco END = nbg.nit\n" +
                "LEFT JOIN (SELECT id_contrato,banco FROM nexco_contratos) AS nc ON nct.contrato = nc.id_contrato\n" +
                "LEFT JOIN (SELECT * FROM nexco_banco_garante) AS nbg2 ON nc.banco = nbg2.nit");
        result.setParameter(1, mes);
        result.setParameter(2, year);
        result.executeUpdate();
    }

    public List<ReclassificationContingent> getReclasficacion(){

        return reclassificationContingentRepository.findAll();

    }

    public List<Object[]> getReclasficacionCSV(Object rec, Object rev, String period){

        String where = "";
        if (rec!=null && rev==null){
            where=where+" and tipo_info='REC'";
        }else if(rec==null && rev!=null){
            where=where+" and tipo_info='REV'";
        }

        Query cargaMasiva = entityManager.createNativeQuery("select 'CENTRO DE COSTO;CUENTA;DIVISA;CONTRATO;REFERENCIA DE CRUCE;IMPORTE; DESCRIPCION ;FECHA;TIPO DE DOCUMENTO;NUMERO DE DOCUMENTO;DIGITO DE VERIFICACION;TIPO DE PERDIDA;CLASE DE RIESGO;TIPO DE MOVIMIENTO;PRODUCTO;PROCESO;LINEA OPERATIVA' \n" +
                "union all\n" +
                "select centro_costos+';'+cuenta+';'+divisa+';'+contrato+';'+convert(varchar, fecha, 112)+';'\n" +
                "+\n" +
                "convert(varchar, FORMAT(convert(decimal(20,2), importe),'g', 'de-de'))+ \n" +
                //"';'+descripcion+';'+convert(varchar, fecha, 112)+';'+td+';'+numero_documento+';'+dv\n" +
                "';'+descripcion+';'+(SELECT REPLACE(MAX(FechaHabil),'-','') FROM FECHAS_HABILES WHERE FechaHabil LIKE ? AND DiaHabil <> 0)+';'+td+';'+numero_documento+';'+dv\n" +
                "+';'+''+';'+''+';'+''+';'+''+';'+''+';'+'' \n" +
                "from nexco_carga_masiva_contingentes \n" +
                "where periodo_origen = ? \n" +
                ""+where+";");
        cargaMasiva.setParameter(1, period+"%");
        cargaMasiva.setParameter(2, period);
        return cargaMasiva.getResultList();
    }

    public void getArmadoCargaMasiva(String month, String year){

        String period = year+"-"+month;

        List<Object> days = getHabilDay(period);

        String day1 = days.get(0).toString();
        String day2 = days.get(1).toString();


        Query deleteInfo1 = entityManager.createNativeQuery("DELETE FROM nexco_plano_reclasificacion where periodo = ?");
        deleteInfo1.setParameter(1,period);
        deleteInfo1.executeUpdate();

        Query deleteInfo2 = entityManager.createNativeQuery("DELETE FROM nexco_carga_masiva_contingentes where periodo_origen = ?");
        deleteInfo2.setParameter(1,period);
        deleteInfo2.executeUpdate();

        Query deleteInfo3 = entityManager.createNativeQuery("DELETE FROM nexco_reclasificacion_contingentes where periodo_origen = ?");
        deleteInfo3.setParameter(1,period);
        deleteInfo3.executeUpdate();

        Query deleteInfo4 = entityManager.createNativeQuery("DELETE FROM nexco_reclasificacion_contingentes_com where periodo_origen = ?");
        deleteInfo4.setParameter(1,period);
        deleteInfo4.executeUpdate();

        Query insertRecla = entityManager.createNativeQuery("insert into nexco_reclasificacion_contingentes (cuenta_contable, divisa, vr_div, saldo_pesos, fecha_alta, fecha_vencimiento, fecha_cierre,\n" +
                "nit, nombre_cliente, contrato, nombre_banco, pais_banco, intergrupo, tipo_aval, nombre_aval, cta_contable_60, tipo_moneda, periodo_origen, yntp, codicons,saldo_divisa,nit_banco)\n" +
                "select a.cuenta_contable, a.id_divisa, a.tasa, a.saldo_pesos, a.fecha_alta, a.fecha_vencimiento,\n" +
                "a.fecha_cierre, a.nit, a.nombre_cliente, a.contrato, f.nombre_banco_real, ISNULL(b.id_pais,f.pais),\n" +
                "a.intergrupo, b.tipo_aval, b.tipo_aval_origen, isnull(c.cuenta_contable_60, a.cuenta_contable), a.tipo_moneda, ?, e.yntp, d.CODICONS46, a.saldo_divisa,a.nit_banco \n" +
                "from nexco_inventario_contingentes a\n" +
                "left join (select * from nexco_contratos where isnull(tipo_proceso, '') <> 'MIS' AND isnull(tipo_proceso, '') <> 'OTROS') b\n" +
                "on a.contrato = b.id_contrato\n" +
                "left join (select * from nexco_banco_garante) f\n" +
                "on f.nit = a.nit_banco\n" +
                "left join (select * from nexco_tipo_aval where cuenta_contable_13 <> cuenta_contable_60) c\n" +
                "on a.cuenta_contable = c.cuenta_contable_13 and b.tipo_aval = c.id_tipo_aval \n" +
                "left join (select nucta, CODICONS46 from cuentas_puc where EMPRESA = '0060') d \n" +
                "on c.cuenta_contable_60 = d.NUCTA\n" +
                "left join nexco_terceros e\n" +
                "on left(right(a.nit, 10),9) = e.nit_contraparte\n" +
                "where a.periodo = ?;");
        insertRecla.setParameter(1,period);
        insertRecla.setParameter(2,period);
        insertRecla.executeUpdate();

        Query insertRecla2 = entityManager.createNativeQuery("insert into nexco_reclasificacion_contingentes_com (cuenta_contable, divisa, vr_div, saldo_pesos, fecha_alta, fecha_vencimiento, fecha_cierre,\n" +
                "nit, nombre_cliente, contrato, nombre_banco, pais_banco, intergrupo, tipo_aval, nombre_aval, cta_contable_60, tipo_moneda, periodo_origen, yntp, codicons,saldo_divisa,nit_banco,centro,td,dv,estado,nombre_pais)\n" +
                "select a.cuenta_contable, a.id_divisa, a.tasa, a.saldo_pesos, a.fecha_alta, a.fecha_vencimiento,\n" +
                "a.fecha_cierre, a.nit, a.nombre_cliente, a.contrato, ISNULL(h.sociedad_larga,f.nombre_banco_real), ISNULL(ISNULL(h.id_pais,f.pais),'CO'),\n" +
                "a.intergrupo, b.tipo_aval, b.tipo_aval_origen, isnull(c.cuenta_contable_60, a.cuenta_contable), a.tipo_moneda, ?, case when a.tipo_moneda = 'LOCAL' THEN Z.yntp ELSE e.yntp END, d.CODICONS46, a.saldo_divisa,CASE WHEN (LEN(a.nit_banco)<=3) THEN a.nit_banco ELSE SUBSTRING(a.nit_banco,2,(LEN(a.nit_banco)-2)) END,a.centro,CASE WHEN (LEN(a.nit_banco)>2) THEN SUBSTRING(a.nit_banco,1,1) ELSE SUBSTRING(b.banco,1,1) END,CASE WHEN (LEN(a.nit_banco)>2) THEN SUBSTRING(a.nit_banco,(LEN(a.nit_banco)),1) ELSE SUBSTRING(b.banco,(LEN(b.banco)),1) END,\n" +
                "case when c.cuenta_contable_13 is null or b.id_contrato is null or c.cuenta_contable_13 = c.cuenta_contable_60 then 'SIN RECLASIFICAR' else 'RECLASIFICADO' end estado, ISNULL(ISNULL(pa.id_pais,f.nombre_pais),'COLOMBIA') \n" +
                "from nexco_inventario_contingentes a\n" +
                "inner join (select * from nexco_contratos where isnull(tipo_proceso, '') <> 'MIS' AND isnull(tipo_proceso, '') <> 'OTROS') b\n" +
                "on a.contrato = b.id_contrato\n" +
                "left join (select * from nexco_banco_garante AS nbg,nexco_paises AS np where np.id_pais = nbg.pais) f\n" +
                "on f.nit = CASE WHEN (LEN(a.nit_banco)<=3) THEN a.nit_banco ELSE SUBSTRING(a.nit_banco,2,(LEN(a.nit_banco)-2)) END \n" +
                "left join (select * from nexco_sociedades_yntp) h\n" +
                "on h.yntp = b.banco\n" +
                "left join (select * from nexco_tipo_aval) c\n" +
                "on a.cuenta_contable = c.cuenta_contable_13 and b.tipo_aval = c.id_tipo_aval\n" +
                "left join (select nucta, CODICONS46 from cuentas_puc where EMPRESA = '0060') d \n" +
                "on c.cuenta_contable_60 = d.NUCTA \n" +
                "left join nexco_terceros e \n" +
                "on CASE WHEN (LEN(a.nit_banco)<=3) THEN a.nit_banco ELSE SUBSTRING(a.nit_banco,2,(LEN(a.nit_banco)-2)) END = e.nit_contraparte \n" +
                "left join nexco_terceros z \n" +
                "on CASE WHEN (LEN(a.nit)<=3) THEN a.nit ELSE SUBSTRING(a.nit,2,(LEN(a.nit)-2)) END = z.nit_contraparte \n" +
                "left join nexco_paises pa \n" +
                "on h.id_pais = pa.id_pais where a.periodo = ?;");
        insertRecla2.setParameter(1,period);
        insertRecla2.setParameter(2,period);
        insertRecla2.executeUpdate();

        Query insertReclaInter = entityManager.createNativeQuery("UPDATE nexco_reclasificacion_contingentes_com SET intergrupo = 'Y' WHERE tipo_moneda='LOCAL' AND periodo_origen = ? AND id_reclasificacion_contingentes_com IN \n" +
                "(SELECT id_reclasificacion_contingentes_com from nexco_reclasificacion_contingentes_com AS nrc,nexco_terceros AS nt where nt.nit_contraparte = nrc.nit_banco AND tipo_moneda = 'LOCAL' AND periodo_origen = ?)");
        insertReclaInter.setParameter(1,period);
        insertReclaInter.setParameter(2,period);
        insertReclaInter.executeUpdate();

        Query insertReclaInter2 = entityManager.createNativeQuery("UPDATE nexco_reclasificacion_contingentes_com SET intergrupo = 'N' WHERE tipo_moneda='LOCAL' AND periodo_origen = ? AND id_reclasificacion_contingentes_com NOT IN \n" +
                "(SELECT id_reclasificacion_contingentes_com from nexco_reclasificacion_contingentes_com AS nrc,nexco_terceros AS nt where nt.nit_contraparte = nrc.nit_banco AND tipo_moneda = 'LOCAL' AND periodo_origen = ?)");
        insertReclaInter2.setParameter(1,period);
        insertReclaInter2.setParameter(2,period);
        insertReclaInter2.executeUpdate();

        Query insertPlano = entityManager.createNativeQuery("insert into nexco_plano_reclasificacion (contrato, cta_anterior, cta_nueva, empresa, periodo)\n" +
                "select a.contrato, c.cuenta_contable_13 cta_anterior, c.cuenta_contable_60 cta_nueva, substring(a.contrato, 1, 4) empresa, ? \n" +
                "from nexco_inventario_contingentes a\n" +
                "left join (select * from nexco_contratos where isnull(tipo_proceso, '') <> 'MIS' AND isnull(tipo_proceso, '') <> 'OTROS') b\n" +
                "on a.contrato = b.id_contrato\n" +
                "inner join (select * from nexco_tipo_aval where cuenta_contable_13 <> cuenta_contable_60) c\n" +
                "on a.cuenta_contable = c.cuenta_contable_13 and b.tipo_aval = c.id_tipo_aval\n" +
                "where a.periodo = ?;");
        insertPlano.setParameter(1,period);
        insertPlano.setParameter(2,period);
        insertPlano.executeUpdate();

        Query insertCM1 = entityManager.createNativeQuery("insert into nexco_carga_masiva_contingentes (centro_costos, cuenta, divisa, contrato, referencia_cruce, importe, descripcion, fecha, td, numero_documento, dv, tipo_perdida, clase_riesgo, tipo_movimiento, producto, proceso, linea_operativa, periodo_origen, tipo_info) \n" +
                "select isnull(z.centro, substring(a.contrato, 5, 4)) centro_costos, a.cuenta_contable, a.id_divisa, a.contrato, convert(varchar, a.fecha_cierre, 112) referencia_de_cruce, \n" +
                "isnull(a.saldo_divisa, 0)*(-1) as importe, case when a.origen = 'PLANTILLA' then 'REC_AJT_TIPO_AVAL_ME' else 'REC_AJT_TIPO_AVAL_ML' end descripcion, \n" +
                "? fecha, left(a.nit, 1) tipo_documento, SUBSTRING(a.nit,2,LEN(a.nit)-2) nit, right(a.nit, 1) dv,\n" +
                "'' tipo_perdida, '' clase_riesgo, '' tipo_movimiento, '' producto, '' proceso, '' linea_operativa, ?, 'REC' \n" +
                "from nexco_inventario_contingentes a \n" +
                "left join (select * from nexco_contratos where isnull(tipo_proceso, '') <> 'MIS' AND isnull(tipo_proceso, '') <> 'OTROS') b \n" +
                "on a.contrato = b.id_contrato\n" +
                "inner join (select * from nexco_tipo_aval where cuenta_contable_13 <> cuenta_contable_60) c \n" +
                "on a.cuenta_contable = c.cuenta_contable_13 and b.tipo_aval = c.id_tipo_aval \n" +
                "left join nexco_contratos_borrar z \n" +
                "on a.contrato = z.contrato \n" +
                "where a.periodo = ?;");
        insertCM1.setParameter(1,day1);
        insertCM1.setParameter(2,period);
        insertCM1.setParameter(3,period);
        insertCM1.executeUpdate();

        Query insertCM2 = entityManager.createNativeQuery("insert into nexco_carga_masiva_contingentes (centro_costos, cuenta, divisa, contrato, referencia_cruce, importe, descripcion, fecha, td, numero_documento, dv, tipo_perdida, clase_riesgo, tipo_movimiento, producto, proceso, linea_operativa, periodo_origen, tipo_info)\n" +
                "select isnull(z.centro, substring(a.contrato, 5, 4)), c.contrapartida_generica, a.id_divisa, a.contrato, convert(varchar, a.fecha_cierre, 112) referencia_de_cruce,\n" +
                "isnull(a.saldo_divisa, 0) as importe, case when a.origen = 'PLANTILLA' then 'AJT_TIPO_AVAL_ME' else 'AJT_TIPO_AVAL_ML' end descripcion,\n" +
                "? fecha, left(a.nit, 1) tipo_documento, SUBSTRING(a.nit,2,LEN(a.nit)-2) nit, right(a.nit, 1) dv,\n" +
                "'' tipo_perdida, '' clase_riesgo, '' tipo_movimiento, '' producto, '' proceso, '' linea_operativa, ?, 'REC'\n" +
                "from nexco_inventario_contingentes a\n" +
                "left join (select * from nexco_contratos where isnull(tipo_proceso, '') <> 'MIS' AND isnull(tipo_proceso, '') <> 'OTROS') b\n" +
                "on a.contrato = b.id_contrato\n" +
                "inner join (select * from nexco_tipo_aval where cuenta_contable_13 <> cuenta_contable_60) c\n" +
                "on a.cuenta_contable = c.cuenta_contable_13 and b.tipo_aval = c.id_tipo_aval\n" +
                "left join nexco_contratos_borrar z \n" +
                "on a.contrato = z.contrato \n" +
                "where a.periodo = ?;");
        insertCM2.setParameter(1,day1);
        insertCM2.setParameter(2,period);
        insertCM2.setParameter(3,period);
        insertCM2.executeUpdate();

        Query insertCM3 = entityManager.createNativeQuery("insert into nexco_carga_masiva_contingentes (centro_costos, cuenta, divisa, contrato, referencia_cruce, importe, descripcion, fecha, td, numero_documento, dv, tipo_perdida, clase_riesgo, tipo_movimiento, producto, proceso, linea_operativa, periodo_origen, tipo_info)\n" +
                "select isnull(z.centro, substring(a.contrato, 5, 4)), c.cuenta_contable_60, a.id_divisa, a.contrato, convert(varchar, a.fecha_cierre, 112) referencia_de_cruce,\n" +
                "isnull(a.saldo_divisa, 0) as importe, case when a.origen = 'PLANTILLA' then 'AJT_TIPO_AVAL_ME' else 'AJT_TIPO_AVAL_ML' end descripcion,\n" +
                "? fecha, left(a.nit, 1) tipo_documento, SUBSTRING(a.nit,2,LEN(a.nit)-2) nit, right(a.nit, 1) dv,\n" +
                "'' tipo_perdida, '' clase_riesgo, '' tipo_movimiento, '' producto, '' proceso, '' linea_operativa, ?, 'REC' \n" +
                "from nexco_inventario_contingentes a\n" +
                "left join (select * from nexco_contratos where isnull(tipo_proceso, '') <> 'MIS' AND isnull(tipo_proceso, '') <> 'OTROS') b\n" +
                "on a.contrato = b.id_contrato\n" +
                "inner join (select * from nexco_tipo_aval where cuenta_contable_13 <> cuenta_contable_60) c\n" +
                "on a.cuenta_contable = c.cuenta_contable_13 and b.tipo_aval = c.id_tipo_aval\n" +
                "left join nexco_contratos_borrar z \n" +
                "on a.contrato = z.contrato \n" +
                "where a.periodo = ?;");
        insertCM3.setParameter(1,day1);
        insertCM3.setParameter(2,period);
        insertCM3.setParameter(3,period);
        insertCM3.executeUpdate();

        Query insertCM4 = entityManager.createNativeQuery("insert into nexco_carga_masiva_contingentes (centro_costos, cuenta, divisa, contrato, referencia_cruce, importe, descripcion, fecha, td, numero_documento, " +
                "dv, tipo_perdida, clase_riesgo, tipo_movimiento, producto, proceso, linea_operativa, periodo_origen, tipo_info)\n" +
                "select isnull(z.centro, substring(a.contrato, 5, 4)), c.contrapartida_generica, a.id_divisa, a.contrato, convert(varchar, a.fecha_cierre, 112) referencia_de_cruce,\n" +
                "isnull(a.saldo_divisa, 0)*(-1) as importe, case when a.origen = 'PLANTILLA' then 'REC_AJT_TIPO_AVAL_ME' else 'REC_AJT_TIPO_AVAL_ML' end descripcion,\n" +
                "? fecha, left(a.nit, 1) tipo_documento, SUBSTRING(a.nit,2,LEN(a.nit)-2) nit, right(a.nit, 1) dv,\n" +
                "'' tipo_perdida, '' clase_riesgo, '' tipo_movimiento, '' producto, '' proceso, '' linea_operativa, ?, 'REC' \n" +
                "from nexco_inventario_contingentes a\n" +
                "left join (select * from nexco_contratos where isnull(tipo_proceso, '') <> 'MIS' AND isnull(tipo_proceso, '') <> 'OTROS') b\n" +
                "on a.contrato = b.id_contrato\n" +
                "inner join (select * from nexco_tipo_aval where cuenta_contable_13 <> cuenta_contable_60) c\n" +
                "on a.cuenta_contable = c.cuenta_contable_13 and b.tipo_aval = c.id_tipo_aval\n" +
                "left join nexco_contratos_borrar z \n" +
                "on a.contrato = z.contrato \n" +
                "where a.periodo = ?;");
        insertCM4.setParameter(1,day1);
        insertCM4.setParameter(2,period);
        insertCM4.setParameter(3,period);
        insertCM4.executeUpdate();

        Query insertCMRev = entityManager.createNativeQuery("insert into nexco_carga_masiva_contingentes (centro_costos, cuenta, divisa, contrato, referencia_cruce, importe, descripcion, fecha, td,\n" +
                "numero_documento, dv, tipo_perdida, clase_riesgo, tipo_movimiento, producto, proceso, linea_operativa, periodo_origen, tipo_info)\n" +
                "SELECT centro_costos, cuenta, divisa, contrato, referencia_cruce, importe*(-1) importe, case when right(descripcion, 2) = 'ME' then 'DEV_REV_TIPO_AVAL_ME' else 'DEV_REV_TIPO_AVAL_ML' end, ?, td,\n" +
                "numero_documento, dv, tipo_perdida, clase_riesgo, tipo_movimiento, producto, proceso, linea_operativa, ?, 'REV'\n" +
                "FROM nexco_carga_masiva_contingentes where periodo_origen = ? \n" +
                ";");
        insertCMRev.setParameter(1,day2);
        insertCMRev.setParameter(2,period);
        insertCMRev.setParameter(3,period);
        insertCMRev.executeUpdate();

    }

    public List<ControlPanel> validateControlPanel(String periodo, String responsable, String input){
        Query query = entityManager.createNativeQuery("SELECT * FROM nexco_cuadro_mando WHERE fecha_reporte = ? " +
                "AND responsable = ? AND componente = ? AND input = ?;", ControlPanel.class);
        query.setParameter(1,periodo);
        query.setParameter(2,responsable);
        query.setParameter(3,"CONTINGENTES");
        query.setParameter(4,"PLANTILLA CONTINGENTES - NEXCO");
        List<ControlPanel> listControlPanel = query.getResultList();
        return listControlPanel;
    }

    public List<Third> validateBanco(String nit){
        Query query = entityManager.createNativeQuery("SELECT * FROM nexco_terceros WHERE nit_contraparte = SUBSTRING(?,2,len(?)-2) ", Third.class);
        query.setParameter(1,nit);
        List<Third> listControlPanel = query.getResultList();
        return listControlPanel;
    }

    public List<Object[]> validateQueryGlobal(String empresa, String period,String tipo)
    {
        String tableName = "";
        String divisaName = "";
        String saldoName = "";
        String fechaName = "";
        String cuentaName = "";
        if(tipo.equals("Reclasificado"))
        {
            tableName = " nexco_reclasificacion_contingentes_com ";
            divisaName = "divisa";
            saldoName = "saldo_divisa";
            fechaName = "periodo_origen";
            cuentaName = "cta_contable_60";
        }
        else if(tipo.equals("Sin Reclasificación"))
        {
            tableName = " nexco_inventario_contingentes ";
            divisaName = "id_divisa";
            saldoName = "saldo_divisa";
            fechaName = "periodo";
            cuentaName = "cuenta_contable";
        }

        String nameTable ="";
        String tempValue ="";
        Query verify = entityManager.createNativeQuery("SELECT TOP 1 * FROM nexco_query_marcados WHERE origen = 'LOCAL' AND fecont LIKE ? ");
        verify.setParameter(1,period+"%");

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

        Query query= entityManager.createNativeQuery("SELECT a.cuenta_local, ISNULL(ISNULL(b.id_divisa, C.coddiv),'-') divisa, ABS(CAST(ISNULL(b.saldo_divisa, 0) AS FLOAT)) saldo_divisa,ABS(CAST(ISNULL(b.saldo_pesos, 0) AS FLOAT)) saldo_pesos,\n" +
                "abs(CAST(ISNULL(c.salmesd, 0) AS FLOAT)) salmesd, abs(CAST(ISNULL(c.salmes, 0) AS FLOAT)) salmes, \n" +
                "CAST(ABS(ISNULL(b.saldo_divisa, 0))-abs(isnull(c.salmesd, 0)) AS FLOAT) diferencia_divisa, \n" +
                "CAST(ABS(CAST(ISNULL(b.saldo_pesos, 0) AS FLOAT))-abs(isnull(c.salmes, 0)) AS FLOAT) diferencia_pesos,c.fechproce \n" +
                "FROM nexco_cuentas_responsables a\n" +
                "LEFT JOIN (SELECT "+cuentaName+" cuenta_contable, "+divisaName+" id_divisa, sum("+saldoName+") saldo_divisa,sum(saldo_pesos) saldo_pesos\n" +
                "FROM "+tableName+" \n" +
                "WHERE "+cuentaName+" != '' \n" +
                "AND "+fechaName+" = ? \n" +
                "group by "+cuentaName+", "+divisaName+") b \n" +
                "on a.cuenta_local = b.cuenta_contable\n" +
                "LEFT JOIN (SELECT fecont,fechproce, nucta, coddiv, sum(salmesd) salmesd,sum(salmes) salmes\n" +
                "FROM "+nameTable+" \n" +
                "where "+tempValue+" \n" +
                "substring(fecont, 1, 7) = ? and empresa = ? \n" +
                "group by fecont,fechproce, nucta, coddiv) c\n" +
                "on a.cuenta_local = c.nucta AND b.id_divisa = c.coddiv \n" +
                "WHERE a.componente='CONTINGENTES' --AND\n" +
                "--CAST(ISNULL(b.saldo_divisa, 0)-isnull(c.salmesd, 0) AS FLOAT) NOT BETWEEN 0 and 1000 AND \n" +
                "--CAST(ISNULL(b.saldo_pesos, 0)-isnull(c.salmes, 0) AS FLOAT) NOT BETWEEN 0 and 1000\n" +
                "ORDER BY 1 asc \n" +
                ";");

        query.setParameter(1,period);
        query.setParameter(2,period);
        query.setParameter(3,empresa);
        return query.getResultList();

    }

    public List<Object[]> validateQueryGlobalIfrs(String empresa, String period)
    {
        String nameTable ="";
        String tempValue ="";
        Query verify = entityManager.createNativeQuery("SELECT TOP 1 * FROM nexco_query_marcados WHERE origen = 'IFRS9' AND fecont LIKE ? ");
        verify.setParameter(1,period+"%");

        if(verify.getResultList().isEmpty())
        {
            nameTable ="nexco_query";
            tempValue ="";
        }
        else
        {
            nameTable ="nexco_query_marcados";
            tempValue ="origen = 'IFRS9' AND";
        }
        Query query= entityManager.createNativeQuery("SELECT b.cuenta_contable,d.CODICONS46, ISNULL(ISNULL(b.id_divisa, C.coddiv),'-') divisa, ABS(CAST(ISNULL(b.saldo_divisa, 0) AS FLOAT)) saldo_divisa,ABS(CAST(ISNULL(b.saldo_pesos, 0) AS FLOAT)) saldo_pesos,\n" +
                "abs(CAST(ISNULL(c.salmesd, 0) AS FLOAT)) salmesd, abs(CAST(ISNULL(c.salmes, 0) AS FLOAT)) salmes, \n" +
                "CAST(ABS(ISNULL(b.saldo_divisa, 0))-abs(isnull(c.salmesd, 0)) AS FLOAT) diferencia_divisa, \n" +
                "CAST(ABS(CAST(ISNULL(b.saldo_pesos, 0) AS FLOAT))-abs(isnull(c.salmes, 0)) AS FLOAT) diferencia_pesos,c.fechproce \n" +
                "FROM (SELECT SUBSTRING(cta_conta_moneda,1,LEN(cta_conta_moneda)-4) cuenta_contable, divisa id_divisa, sum(saldodivisa) saldo_divisa,sum(saldopesos) saldo_pesos\n" +
                "FROM nexco_corep_obligatorios\n" +
                "WHERE cta_conta_moneda != ''\n" +
                "group by SUBSTRING(cta_conta_moneda,1,LEN(cta_conta_moneda)-4), divisa) b \n" +
                "LEFT JOIN (SELECT fecont,fechproce, nucta, coddiv, sum(salmesd) salmesd,sum(salmes) salmes\n" +
                "FROM "+nameTable+" \n" +
                "where "+tempValue+" \n" +
                "substring(fecont, 1, 7) = ? and empresa = ? \n" +
                "group by fecont,fechproce, nucta, coddiv) c\n" +
                "on b.cuenta_contable = c.nucta AND b.id_divisa = c.coddiv \n" +
                "LEFT JOIN (select CODICONS46,NUCTA from CUENTAS_PUC where empresa = ?) d\n" +
                "on b.cuenta_contable = d.NUCTA \n"+
                "\n" +
                "UNION ALL\n" +
                "\n" +
                "SELECT b.cuenta_contable,d.CODICONS46, 'COP' divisa, '0' saldo_divisa,ABS(CAST(ISNULL(b.saldo_pesos, 0) AS FLOAT)) saldo_pesos,\n" +
                "'0' salmesd, abs(CAST(ISNULL(c.salmes, 0) AS FLOAT)) salmes, \n" +
                "'0' diferencia_divisa, \n" +
                "CAST(ABS(CAST(ISNULL(b.saldo_pesos, 0) AS FLOAT))-abs(isnull(c.salmes, 0)) AS FLOAT) diferencia_pesos,c.fechproce \n" +
                "FROM (SELECT cuentaprovision cuenta_contable, sum(saldodivisa) saldo_divisa,sum(provision) saldo_pesos\n" +
                "FROM nexco_corep_obligatorios\n" +
                "WHERE cuentaprovision != ''\n" +
                "group by cuentaprovision) b \n" +
                "LEFT JOIN (SELECT fecont,fechproce, nucta, coddiv, sum(salmesd) salmesd,sum(salmes) salmes\n" +
                "FROM "+nameTable+" \n" +
                "where "+tempValue+" coddiv='COP' \n" +
                "AND substring(fecont, 1, 7) = ? and empresa = ? \n" +
                "group by fecont,fechproce, nucta, coddiv) c\n" +
                "on b.cuenta_contable = c.nucta \n" +
                "LEFT JOIN (select CODICONS46,NUCTA from CUENTAS_PUC where empresa = ?) d\n" +
                "on b.cuenta_contable = d.NUCTA ");

        query.setParameter(1,period);
        query.setParameter(2,empresa);
        query.setParameter(3,empresa);
        query.setParameter(4,period);
        query.setParameter(5,empresa);
        query.setParameter(6,empresa);
        return query.getResultList();

    }

    public void updateState(String perido, String responsable,String empresa, String input){
        Query query = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando SET semaforo_input = ? , semaforo_componente = ?" +
                " WHERE responsable = ? AND input = ? AND componente = ? AND fecha_reporte = ? AND empresa = ?", ControlPanel.class);
        query.setParameter(1, "FULL");
        query.setParameter(2, "FULL");
        query.setParameter(3, responsable);
        query.setParameter(4, input);
        query.setParameter(5, "CONTINGENTES");
        query.setParameter(6, perido);
        query.setParameter(7, empresa);
        query.executeUpdate();
    }

    public String ShowButton(String periodo, String responsable) {
        int validatePanel13 = 1;
        int validatePanel60 = 1;
        int button = 1;

        List<ControlPanel> controlPanelList = validateControlPanel(periodo, responsable,"CONTINGENTES");
        for (ControlPanel panel : controlPanelList) {
            if (panel.getSemaforoComponente().equals("FULL"))
                validatePanel13 = validatePanel13 & 1;
            else
                validatePanel13 = validatePanel13 & 0;
        }
        if (validatePanel13 == 1){

            if (getReclasficacion().size() > 0){

                for (ControlPanel panel:controlPanelList) {
                    if(panel.getSemaforoComponente().equals("PENDING"))
                        validatePanel60 = validatePanel60 & 1;
                    else
                        validatePanel60 = validatePanel60 & 0;
                }
                if(validatePanel60 == 1){
                    return "Intergrupo";
                } else
                    return "Query60";
            }  else
                return "Armado";
        }


        return "Query13";
    }

    public boolean updateSicc(String period,User user)
    {
        try
        {


                Query query2 = entityManager.createNativeQuery("DELETE FROM nexco_inventario_contingentes WHERE periodo = ? AND origen='SICC';");
                query2.setParameter(1, period);
                query2.executeUpdate();

                String[] partsDate = period.split("-");
                String fechaCierreCast = (String.valueOf("'30/") + partsDate[1] + String.valueOf("/") + partsDate[0] + String.valueOf("'"));

                Query query = entityManager.createNativeQuery("INSERT INTO nexco_inventario_contingentes ([contrato],[cuenta_contable],[fecha_cierre],[id_divisa],[nit],[nombre_cliente],[saldo_divisa],[origen],[periodo],[tipo_moneda],[saldo_pesos],[tasa],[nombre_banco],[pais_banco],[fecha_alta],[intergrupo],[centro],[nit_banco],[fecha_vencimiento])\n" +
                        "SELECT cas.CONTRATO, cas.CUENTA,CONVERT(date, " + fechaCierreCast + ", 103),cas.DIVISA,CONCAT(cas.TP,cas.IDENTIFICACION,cas.DV),cas.NOMBRE,CONVERT(NUMERIC,REPLACE(cas.IMPORTE,',','.')),'SICC',?, 'LOCAL', CONVERT(NUMERIC,REPLACE(cas.IMPORTE,',','.')),1,ISNULL(D.nombre_banco_real,P.nombre_banco_real),ISNULL(D.pais,P.pais),CONVERT(date, cas.FECHA_ORIGEN, 103),CASE WHEN T.nit_contraparte IS NULL THEN 'N' ELSE 'Y' END, cas.CENTRO, CASE WHEN D.nit IS NULL THEN (CASE WHEN P.nit IS NOT NULL THEN (CASE WHEN LEN(P.nit) > 3 THEN CONCAT(cas.TP,P.nit,cas.DV) ELSE P.nit END) END) ELSE (CASE WHEN LEN(D.nit) > 3 THEN CONCAT('0',D.nit,'0') ELSE D.nit END) END ,CONVERT(date, cas.FECHA_CIERRE, 103) FROM Cargas_Anexos_SICC_" + period.replace("-", "") + " AS cas \n" +
                        "LEFT JOIN (SELECT nc.id_contrato,nc.banco,nc.id_pais FROM nexco_contratos AS nc) C ON cas.CONTRATO = C.id_contrato \n" +
                        "LEFT JOIN (SELECT * FROM nexco_terceros AS nt) T ON cas.IDENTIFICACION = T.nit_contraparte \n" +
                        "LEFT JOIN (SELECT * FROM nexco_banco_garante AS nbg) D ON C.banco = D.nit \n" +
                        "LEFT JOIN (SELECT * FROM nexco_sociedades_yntp AS nsy) S ON C.banco = S.yntp \n" +
                        "LEFT JOIN (SELECT * FROM nexco_banco_garante AS nbg2) P ON cas.IDENTIFICACION = P.nit \n" +
                        "WHERE cas.cuenta in (SELECT cuenta_contable_13 FROM nexco_tipo_aval where tipo_archivo='SICC');");
                query.setParameter(1, period);
                query.executeUpdate();

                Query queryContract = entityManager.createNativeQuery("SELECT A.id_contrato, nic.contrato, CASE WHEN LEN(nic.nit_banco) > 3 THEN SUBSTRING(nic.nit_banco,2,(LEN(nic.nit_banco)-2)) ELSE nic.nit_banco END, nic.pais_banco, A.banco, A.id_pais  FROM nexco_inventario_contingentes AS nic \n" +
                        "LEFT JOIN (SELECT * FROM nexco_contratos) A ON nic.contrato = A.id_contrato " +
                        "WHERE nic.origen = ? AND nic.periodo = ?");
                queryContract.setParameter(1, "SICC");
                queryContract.setParameter(2, period);
                List<Object[]> contractNew = queryContract.getResultList();

                List<Contract> listaContratos = new ArrayList<Contract>();

                for (Object[] part : contractNew) {
                    if (part[0] == null || part[0].toString() == null) {
                        String contractSicc = part[1].toString();
                        Contract insert = new Contract();
                        insert.setContrato(contractSicc);
                        insert.setTipoAval(String.valueOf(validateTipoAvalExist("FINANCIERO").get(0)));
                        if (part[2] != null && part[2].toString() != null)
                            insert.setBanco(part[2].toString());
                        if (part[3] != null && part[3].toString() != null) {
                            Country c = new Country();
                            c.setId(part[3].toString());
                            insert.setPaisContrato(c);
                        }
                        insert.setTipoAvalOrigen("FINANCIERO");
                        insert.setArchivoEntrada("LOCAL");
                        insert.setTipoProceso("CONTINGENTES");
                        listaContratos.add(insert);
                    } else if (part[2] != null && part[3] != null && part[4] == null && part[5] == null) {
                        Contract insert = contractRepository.findAllByContrato(part[0].toString());
                        insert.setBanco(part[2].toString());
                        Country c = new Country();
                        c.setId(part[3].toString());
                        insert.setPaisContrato(c);
                        listaContratos.add(insert);
                    }


                }
                contractRepository.saveAll(listaContratos);

                Date today = new Date();
                String input = "CONTINGENTES-SICC";

                StatusInfo validateStatus = StatusInfoRepository.findByInputAndPeriodo(input, period);

                if (validateStatus == null) {
                    StatusInfo status = new StatusInfo();
                    status.setInput(input);
                    status.setPeriodo(period);
                    status.setFecha(today);
                    StatusInfoRepository.save(status);
                } else {
                    validateStatus.setFecha(today);
                    StatusInfoRepository.save(validateStatus);
                }

                Query query7 = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando SET semaforo_input = ? , usuario_carga = ? " +
                        "WHERE responsable = ? AND input = ? AND componente = ? AND fecha_reporte = ?", ControlPanel.class);
                query7.setParameter(1, "PENDING");
                query7.setParameter(2, user.getPrimerNombre());
                query7.setParameter(3, user.getCentro());
                query7.setParameter(4, "SICC");
                query7.setParameter(5, "CONTINGENTES");
                query7.setParameter(6, period);
                query7.executeUpdate();

                validateContingentesComponent(user, period,"SICC");


        }
        catch(Exception e)
        {
            return false;
        }
        return true;
    }

    public List validateSicc(String period){
        Query queryValidate = entityManager.createNativeQuery("SELECT contrato FROM nexco_inventario_contingentes WHERE periodo = ? AND origen='SICC';");
        queryValidate.setParameter(1, period);

        return queryValidate.getResultList();
    }

    public void updatePlantillaBase(String perido)
    {
        try{
            Query query = entityManager.createNativeQuery("BEGIN\n" +
                    "DELETE FROM nexco_base_contingentes WHERE periodo = ? AND origen='PLANTILLA';\n" +
                    "INSERT INTO nexco_base_contingentes (contrato,cuenta,fecha_cierre,divisa,nit,nombre,importe,tp,dv,origen,periodo)\n" +
                    "          (SELECT nic.contrato,nic.cuenta_contable,nic.fecha_cierre,nic.id_divisa,nic.nit,nic.nombre_banco,nic.saldo_divisa,SUBSTRING(nic.nit, 1, 1),SUBSTRING(nic.nit, LEN(nic.nit), 1),'PLANTILLA',nic.periodo from nexco_inventario_contingentes AS nic WHERE nic.periodo=?);\n" +
                    "END");
            query.setParameter(1, perido);
            query.setParameter(2, perido);
            query.executeUpdate();
        }
        catch(Exception e){

        }

    }

    public List<Object[]> getCompany(String periodo){

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
        Query companies = entityManager.createNativeQuery(
                "select empresa from "+nameTable+" where "+tempValue+" substring(fecont, 1, 7) = ? GROUP BY empresa" );

        companies.setParameter(1,periodo);

        return companies.getResultList();

    }

    public List<Object[]> getCompanyIfrs(String periodo){

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
            tempValue ="origen = 'IFRS9' AND";
        }
        Query companies = entityManager.createNativeQuery(
                "select empresa from "+nameTable+" where "+tempValue+" substring(fecont, 1, 7) = ? GROUP BY empresa" );

        companies.setParameter(1,periodo);

        return companies.getResultList();

    }

    public List<Object[]> contractsOffBalance(){

        Query result = entityManager.createNativeQuery("SELECT id_contrato, banco \n" +
                "FROM nexco_contratos WHERE tipo_proceso = 'CONTINGENTES' AND banco is null AND (archivo_entrada IN ('EXTRANJERO', NULL))");


        return result.getResultList();

    }

    public ArrayList<String[]> saveFileBDCon(InputStream file, User user) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<String[]>();
        if (file!=null)
        {
            Iterator<Row> rows = null;
            Iterator<Row> rows1 = null;

            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            rows = sheet.iterator();
            rows1 = sheet.iterator();
            list=validarPlantillaCon(rows);
            if(list.size()==0)
            {
                list=getRowsCon(rows1);
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Inserción archivo Contratos");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Contratos");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }else{
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Fallo en inserción archivo Contratos");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Contratos");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);

            }
        }
        return list;
    }

    public ArrayList<String[]> validarPlantillaCon(Iterator<Row> rows) {
        ArrayList lista= new ArrayList();
        XSSFRow row;
        int firstRow=1;

        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(firstRow!=1)
            {
                DataFormatter formatter = new DataFormatter();

                row.getCell(0).setCellType(CellType.STRING);
                String cellContrato = formatter.formatCellValue(row.getCell(0)).replace(" ", "");
                String cellBanco = formatter.formatCellValue(row.getCell(1));



                if(cellBanco.isBlank() || cellBanco.isEmpty())
                {
                    String[] log=new String[3];
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="2";
                    log[2]="El campo YNTP/NIT no puede estar vacío";
                    lista.add(log);
                }else{
                    if(!isNumeric(cellBanco)) {
                        try {
                            String bancoNumber = String.valueOf(Integer.parseInt(cellBanco));
                        } catch (Exception e) {
                            String[] log=new String[3];
                            log[0]=String.valueOf(row.getRowNum());
                            log[1] = "2";
                            log[2] = "El campo YNTP o NIT debe ser numérico";
                            lista.add(log);
                        }
                    }
                    if(findYntpByFilter(cellBanco, "yntp").size()==0)
                    {
                        if(garantBankRepository.findAllByNit(cellBanco)==null) {
                            String[] log=new String[3];
                            log[0]=String.valueOf(row.getRowNum());
                            log[1]="2";
                            log[2]="El campo YNTP no cruza con Sociedades YNTP ni Bancos Garante";
                            lista.add(log);
                        }
                    }
                }


                if(cellContrato.isBlank() || cellContrato.isEmpty())
                {
                    String[] log=new String[3];
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="1";
                    log[2]="El campo Contrato no puede estar vacío";
                    lista.add(log);
                }
                else if(cellContrato.trim().length()!=18)
                {
                    String[] log=new String[3];
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="1";
                    log[2]="El campo Contrato debe tener una longitud de 18 posiciones";
                    lista.add(log);
                }

            }
            else
            {
                firstRow=0;
            }
        }
        //lista.add(log);
        return lista;
    }

    public ArrayList getRowsCon(Iterator<Row> rows) {
        XSSFRow row;
        Date today=new Date();
        ArrayList lista= new ArrayList();
        int firstRow=1;
        while (rows.hasNext())
        {
            String[] log=new String[3];
            log[2]="true";
            row = (XSSFRow) rows.next();

            if(firstRow!=1 && row.getCell(0)!=null)
            {
                DataFormatter formatter = new DataFormatter();
                String cellContrato = formatter.formatCellValue(row.getCell(0));
                String cellBanco = formatter.formatCellValue(row.getCell(1));
                String bancoNumber;
                if(!isNumeric(cellBanco)){
                    bancoNumber = String.valueOf(Integer.parseInt(cellBanco));
                }else{
                    bancoNumber = cellBanco;
                }

                log[0]=cellContrato;
                if((cellContrato.isEmpty() || cellContrato.isBlank()) && (cellBanco.isEmpty() || cellBanco.isBlank()))
                {
                    break;
                }
                if(findYntpByFilter(cellBanco, "yntp").size()!=0)
                {
                    YntpSociety sociedad=yntpSocietyRepository.findByYntp(bancoNumber);
                    if(sociedad!=null && sociedad.getPais()!=null)
                    {
                        if(contractRepository.findAllByContrato(cellContrato)!=null){
                            Contract contract = contractRepository.findAllByContrato(cellContrato);
                            contract.setContrato(cellContrato);
                            contract.setBanco(bancoNumber);
                            contract.setPaisContrato(sociedad.getPais());
                            contractRepository.save(contract);
                            log[1] = "Registro actualizado exitosamente";
                        }else {
                            Contract contract = new Contract();
                            contract.setContrato(cellContrato);
                            contract.setBanco(bancoNumber);
                            contract.setTipoProceso("CONTINGENTES");
                            contract.setPaisContrato(sociedad.getPais());
                            contractRepository.save(contract);
                            log[1] = "Registro actualizado exitosamente";
                        }

                        List<RysConcil> ajuste = contingentTemplateRepository.findAllByContrato(cellContrato);
                        for (RysConcil template:ajuste) {
                            template.setPaisBanco(sociedad.getPais().getNombre());
                            template.setNombreBanco(cellBanco);
                            contingentTemplateRepository.save(template);
                        }

                    }
                    else
                    {
                        log[1] = "Registro no Insertado. El banco "+cellBanco+" en paramétrica de Sociedades Yntp no tiene un país asignado";
                    }
                }
                else if(garantBankRepository.findByNombreSimilar(cellBanco)!=null)
                {
                    GarantBank bancoGrante=garantBankRepository.findByNombreSimilar(cellBanco);
                    if(bancoGrante!=null && bancoGrante.getPais()!=null)
                    {

                        if(contractRepository.findAllByContrato(cellContrato)!=null){
                            Contract contract = contractRepository.findAllByContrato(cellContrato);
                            contract.setContrato(cellContrato);
                            contract.setBanco(bancoNumber);
                            contract.setTipoProceso("CONTINGENTES");
                            contract.setPaisContrato(countryRepository.findAllByNombre(bancoGrante.getPais()));
                            contractRepository.save(contract);
                            log[1] = "Registro actualizado exitosamente";
                        }else {
                            Contract contract = new Contract();
                            contract.setContrato(cellContrato);
                            contract.setBanco(bancoNumber);
                            contract.setPaisContrato(countryRepository.findAllByNombre(bancoGrante.getPais()));
                            contractRepository.save(contract);
                            log[1] = "Registro actualizado exitosamente";
                        }

                        List<RysConcil> ajuste = contingentTemplateRepository.findAllByContrato(cellContrato);
                        for (RysConcil template:ajuste) {
                            template.setPaisBanco(countryRepository.findAllByNombre(bancoGrante.getPais()).getNombre());
                            template.setNombreBanco(cellBanco);
                            contingentTemplateRepository.save(template);
                        }

                    }
                    else
                    {
                        log[1] = "Registro no Insertado. El banco "+cellBanco+" en paramétrica de Sociedades Yntp no tiene un país asignado";
                    }
                }
                lista.add(log);
            }
            else{
                firstRow=0;
            }
        }
        return lista;
    }

    public List<YntpSociety> findYntpByFilter(String value, String filter) {
        List<YntpSociety> list=new ArrayList<YntpSociety>();
        switch (filter)
        {
            case "yntp":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_sociedades_yntp as em " +
                        "WHERE RIGHT('00000' + Ltrim(Rtrim(em.yntp)),5) LIKE ?", YntpSociety.class);
                query.setParameter(1, value);

                list= query.getResultList();

                break;
            default:
                break;
        }
        return list;
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public List<Object[]> getPlanoReclasficacion(String periodo){
        Query query = entityManager.createNativeQuery("SELECT contrato+';'+cta_anterior+';'+cta_nueva FROM nexco_plano_reclasificacion \n" +
                "WHERE periodo = ?");
        query.setParameter(1,periodo);
        return query.getResultList();
    }

    public List<ContingentIntergroup> getArmadoIntergrupo(String periodo){
        Query query = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_contingentes WHERE periodo = ? ", ContingentIntergroup.class);
        query.setParameter(1,periodo);
        return query.getResultList();
    }

    public List<ReclassificationContingentCom> getReclasification(String periodo){
        Query query = entityManager.createNativeQuery("SELECT * FROM nexco_reclasificacion_contingentes_com WHERE periodo_origen = ? ", ReclassificationContingentCom.class);
        query.setParameter(1,periodo);
        return query.getResultList();
    }

    public List<Object> getSaldoTasa(String periodo,String divisa){
        Query query = entityManager.createNativeQuery("SELECT valor FROM nexco_divisas_valor WHERE MONTH(fecha) = ? AND YEAR(fecha) = ? AND divisa = ? ORDER BY fecha desc");
        String[] parts= periodo.split("-");
        query.setParameter(1,parts[1]);
        query.setParameter(2,parts[0]);
        query.setParameter(3,divisa);
        return query.getResultList();
    }

    public void updateArmadoIntergrupo(String periodo)
    {
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_intergrupo_contingentes WHERE periodo = ?", ContingentIntergroup.class);
        query.setParameter(1,periodo);
        query.executeUpdate();

        Query queryList= entityManager.createNativeQuery("INSERT INTO nexco_intergrupo_contingentes (cod_neocon,divisa,yntp,sociedad,contrato,nit,valor,cod_pais,pais,cuenta_local,periodo)\n" +
                "SELECT b.CODICONS46,nrc.divisa,nrc.yntp,d.sociedad_larga,nrc.contrato,nrc.nit,nrc.saldo_pesos,d.id_pais,e.nombre_pais,nrc.cta_contable_60,? FROM nexco_reclasificacion_contingentes_com AS nrc\n" +
                "LEFT JOIN (SELECT NUCTA, CODICONS46 FROM CUENTAS_PUC WHERE EMPRESA = '0060') b ON nrc.cta_contable_60 = b.NUCTA\n" +
                "LEFT JOIN (SELECT yntp, nit_contraparte FROM nexco_terceros) c ON c.nit_contraparte = nrc.nit \n" +
                "LEFT JOIN (SELECT yntp, sociedad_larga,id_pais FROM nexco_sociedades_yntp) d ON d.yntp = nrc.yntp\n" +
                "LEFT JOIN (SELECT id_pais, nombre_pais FROM nexco_paises) e ON e.id_pais = d.id_pais " +
                "WHERE nrc.intergrupo = 'Y' AND periodo_origen = ?");

        queryList.setParameter(1,periodo);
        queryList.setParameter(2,periodo);

        queryList.executeUpdate();
    }

    public void sendIntergrupo(String periodo)
    {
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_intergrupo_v1 WHERE periodo = ? AND fuente = ?");
        query.setParameter(1,periodo);
        query.setParameter(2,"CONTINGENTES");
        query.executeUpdate();

        Query queryList= entityManager.createNativeQuery("INSERT INTO nexco_intergrupo_v1 (cod_neocon,divisa,yntp,sociedad_yntp,contrato,nit,valor,cod_pais,pais,cuenta_local,periodo,fuente,yntp_empresa_reportante)\n" +
                "SELECT cod_neocon,divisa,yntp,sociedad,contrato,nit,valor,cod_pais,pais,cuenta_local,?,'CONTINGENTES','00548' FROM nexco_intergrupo_contingentes where periodo = ?");
        queryList.setParameter(1,periodo);
        queryList.setParameter(2,periodo);
        queryList.executeUpdate();

        Query updateAll = entityManager.createNativeQuery("update nexco_cuadro_mando \n" +
                "set semaforo_componente = 'FULL', estado = 0 \n" +
                "where componente = 'CONTINGENTES' \n" +
                "and fecha_reporte = ?");
        updateAll.setParameter(1,periodo);
        updateAll.executeUpdate();

        Query updateAll1 = entityManager.createNativeQuery("update nexco_cuadro_mando \n" +
                "set semaforo_input = 'FULL', estado = 0 \n" +
                "where componente = 'CONTINGENTES' \n" +
                "and fecha_reporte = ? AND semaforo_input ='PENDING'");
        updateAll1.setParameter(1,periodo);
        updateAll1.executeUpdate();

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

    public List<Object> getHabilDay(String period) {

        Query result = entityManager.createNativeQuery("select * from\n" +
                "(select top 1 FechaHabil\n" +
                "from fechas_habiles\n" +
                "where FechaHabil like ?+'%'\n" +
                "order by NumColumn desc) a\n" +
                "union all\n" +
                "select * from\n" +
                "(select top 1 FechaHabil\n" +
                "from fechas_habiles\n" +
                "where FechaHabil like substring(convert(varchar, DATEADD(d, 1, EOMONTH(convert(date, ?+'-01'))), 23), 1, 7)+'%'\n" +
                "order by NumColumn asc) b");
        result.setParameter(1,period);
        result.setParameter(2,period);

        return result.getResultList();
    }*/

}
